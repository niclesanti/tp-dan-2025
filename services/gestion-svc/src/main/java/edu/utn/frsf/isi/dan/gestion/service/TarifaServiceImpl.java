package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.TarifaRepository;
import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTOResponse;
import edu.utn.frsf.isi.dan.gestion.mapper.SharedDTOMapper;
import edu.utn.frsf.isi.dan.gestion.mapper.TarifaMapper;
import edu.utn.frsf.isi.dan.gestion.messaging.GestionMessagePublisher;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.TipoEvento;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TarifaServiceImpl implements TarifaService {

    private final TarifaRepository tarifaRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final TarifaMapper tarifaMapper;
    private final SharedDTOMapper sharedDTOMapper;
    private final GestionMessagePublisher messagePublisher;

    @Transactional
    @Override
    public TarifaDTOResponse crearTarifa(TarifaDTORequest request) {
        log.info("Creando tarifa para tipoHabitacion {}", request.idTipoHabitacion());
        validarModoDeAlta(request);
        var tipoHabitacion = buscarTipoHabitacionOExcepcion(request.idTipoHabitacion());

        if (request.fechaInicio() == null && request.fechaFin() == null) {
            return crearTarifaNormal(request, tipoHabitacion);
        }
        return crearTarifaPromocional(request, tipoHabitacion);
    }

    @Transactional(readOnly = true)
    @Override
    public TarifaDTOResponse buscarTarifaPorId(Integer id) {
        log.info("Buscando tarifa con ID: {}", id);
        return tarifaMapper.toResponse(buscarTarifaOExcepcion(id));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TarifaDTOResponse> buscarTarifas(Pageable pageable) {
        log.info("Buscando tarifas paginadas. page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return tarifaRepository.findAll(pageable).map(tarifaMapper::toResponse);
    }

    @Transactional
    @Override
    public void eliminarTarifa(Integer id) {
        log.info("Eliminando tarifa con ID: {}", id);
        var tarifa = buscarTarifaOExcepcion(id);
        var tipoHabitacionId = tarifa.getTipoHabitacion().getId();
        var totalTarifasMismoTipo = tarifaRepository.countByTipoHabitacionId(tipoHabitacionId);

        if (totalTarifasMismoTipo <= 1) {
            throw new IllegalArgumentException(
                    "No se puede eliminar la única tarifa del tipo de habitación " + tipoHabitacionId);
        }

        var hoy = LocalDate.now();
        if (estaVigenteEnFecha(tarifa, hoy)) {
            tarifaRepository.delete(tarifa);
            buscarTarifaAnterior(tipoHabitacionId, tarifa.getFechaInicio())
                    .ifPresent(tarifaAnterior -> {
                        tarifaAnterior.setFechaFin(null);
                        tarifaRepository.save(tarifaAnterior);
                    });
            log.info("Tarifa vigente eliminada y tarifa anterior promovida a vigente para tipoHabitacion {}",
                    tipoHabitacionId);
            return;
        }

        tarifaRepository.delete(tarifa);
        log.info("Tarifa no vigente eliminada con ID: {}", id);
    }

    /*-------------------------------- MÉTODOS PRIVADOS -----------------------------*/

    private TarifaDTOResponse crearTarifaNormal(TarifaDTORequest request, TipoHabitacion tipoHabitacion) {
        var hoy = LocalDate.now();
        var vigente = buscarTarifaVigenteEnFecha(tipoHabitacion.getId(), hoy);
        if (vigente.isPresent()) {
            var tarifaVigente = vigente.get();
            var ayer = hoy.minusDays(1);
            if (tarifaVigente.getFechaInicio().isAfter(ayer)) {
                throw new IllegalArgumentException("No se puede cerrar la tarifa vigente con una fecha inválida");
            }
            tarifaVigente.setFechaFin(ayer);
            tarifaRepository.save(tarifaVigente);
        }

        var nuevaTarifa = tarifaMapper.toEntity(request);
        nuevaTarifa.setTipoHabitacion(tipoHabitacion);
        nuevaTarifa.setFechaInicio(hoy);
        nuevaTarifa.setFechaFin(null);
        var tarifaGuardada = tarifaRepository.save(nuevaTarifa);
        log.info("Tarifa normal creada con ID: {}", tarifaGuardada.getId());
        
        // Publicar evento de cambio de precio
        publicarEventoCambioPrecio(tarifaGuardada);
        
        return tarifaMapper.toResponse(tarifaGuardada);
    }

    private TarifaDTOResponse crearTarifaPromocional(TarifaDTORequest request, TipoHabitacion tipoHabitacion) {
        var fechaInicioPromo = request.fechaInicio();
        var fechaFinPromo = request.fechaFin();
        if (fechaInicioPromo.isAfter(fechaFinPromo)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        var tarifaBase = buscarTarifaVigenteEnFecha(tipoHabitacion.getId(), fechaInicioPromo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una tarifa base vigente para programar una promoción"));

        var precioBase = tarifaBase.getPrecioNoche();
        var cierreBase = fechaInicioPromo.minusDays(1);
        if (tarifaBase.getFechaInicio().isAfter(cierreBase)) {
            throw new IllegalArgumentException("La tarifa base no puede cerrarse antes de su inicio");
        }

        tarifaBase.setFechaFin(cierreBase);
        tarifaRepository.save(tarifaBase);

        var tarifaPromocional = tarifaMapper.toEntity(request);
        tarifaPromocional.setTipoHabitacion(tipoHabitacion);
        tarifaPromocional.setFechaInicio(fechaInicioPromo);
        tarifaPromocional.setFechaFin(fechaFinPromo);
        var promoGuardada = tarifaRepository.save(tarifaPromocional);

        var tarifaPosterior = Tarifa.builder()
                .fechaInicio(fechaFinPromo.plusDays(1))
                .fechaFin(null)
                .tipoHabitacion(tipoHabitacion)
                .precioNoche(precioBase)
                .build();
        tarifaRepository.save(tarifaPosterior);

        log.info("Tarifa promocional creada con ID: {} para tipoHabitacion {}",
                promoGuardada.getId(), tipoHabitacion.getId());
        
        // Publicar evento de cambio de precio (la promocional)
        publicarEventoCambioPrecio(promoGuardada);
        
        return tarifaMapper.toResponse(promoGuardada);
    }

    private void validarModoDeAlta(TarifaDTORequest request) {
        var fechaInicio = request.fechaInicio();
        var fechaFin = request.fechaFin();
        var ambasFechasNulas = fechaInicio == null && fechaFin == null;
        var ambasFechasInformadas = fechaInicio != null && fechaFin != null;

        if (!ambasFechasNulas && !ambasFechasInformadas) {
            throw new IllegalArgumentException(
                    "Debe informar ambas fechas (inicio y fin) o ninguna para una tarifa normal vigente");
        }
    }

    private boolean estaVigenteEnFecha(Tarifa tarifa, LocalDate fecha) {
        var empiezaAntesOEnFecha = !tarifa.getFechaInicio().isAfter(fecha);
        var terminaDespuesOEnFecha = tarifa.getFechaFin() == null || !tarifa.getFechaFin().isBefore(fecha);
        return empiezaAntesOEnFecha && terminaDespuesOEnFecha;
    }

    private Tarifa buscarTarifaOExcepcion(Integer id) {
        return tarifaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarifa no encontrada con ID: " + id));
    }

    private Optional<Tarifa> buscarTarifaVigenteEnFecha(Integer tipoHabitacionId, LocalDate fecha) {
        List<Tarifa> tarifas = tarifaRepository.buscarTarifasVigentesEnFecha(tipoHabitacionId, fecha);
        if (tarifas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tarifas.get(0));
    }

    private Optional<Tarifa> buscarTarifaAnterior(Integer tipoHabitacionId, LocalDate fechaReferencia) {
        List<Tarifa> tarifas = tarifaRepository.buscarTarifasAnteriores(tipoHabitacionId, fechaReferencia);
        if (tarifas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tarifas.get(0));
    }

    private TipoHabitacion buscarTipoHabitacionOExcepcion(Integer id) {
        return tipoHabitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TipoHabitacion no encontrada con ID: " + id));
    }
    
    private void publicarEventoCambioPrecio(Tarifa tarifa) {
        try {
            var tarifaDTO = sharedDTOMapper.toTarifaDTO(tarifa);
            var event = HabitacionEvent.builder()
                    .tarifa(tarifaDTO)
                    .tipoEvento(TipoEvento.ACTUALIZAR_PRECIO)
                    .build();
            messagePublisher.publishHabitacionEvent(event);
            log.info("Evento ACTUALIZAR_PRECIO publicado para tipoHabitacion ID: {}", 
                    tarifa.getTipoHabitacion().getId());
        } catch (Exception e) {
            log.error("Error al publicar evento de cambio de precio: {}", e.getMessage(), e);
        }
    }
}
