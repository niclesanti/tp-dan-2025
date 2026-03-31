package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.HabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dao.TarifaRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTOResponse;
import edu.utn.frsf.isi.dan.gestion.mapper.HabitacionMapper;
import edu.utn.frsf.isi.dan.gestion.mapper.TarifaMapper;
import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final HotelRepository hotelRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final TarifaRepository tarifaRepository;
    private final HabitacionMapper habitacionMapper;
    private final TarifaMapper tarifaMapper;
    

    

    @Transactional
    @Override
    public HabitacionDTOResponse crearHabitacion(HabitacionDTORequest request) {
        log.info("Creando habitación número {} en hotel {}", request.numero(), request.idHotel());
        
        var hotel = hotelRepository.findById(request.idHotel())
                .orElseThrow(() -> new EntityNotFoundException("Hotel no encontrado con ID: " + request.idHotel()));
        var tipoHabitacion = tipoHabitacionRepository.findById(request.idTipoHabitacion())
                .orElseThrow(() -> new EntityNotFoundException("TipoHabitacion no encontrada con ID: " + request.idTipoHabitacion()));

        var habitacion = habitacionMapper.toEntity(request);
        habitacion.setHotel(hotel);
        habitacion.setTipoHabitacion(tipoHabitacion);
        
        var habitacionGuardada = habitacionRepository.save(habitacion);
        log.info("Habitación creada exitosamente con ID: {}", habitacionGuardada.getId());
        
        return habitacionMapper.toResponse(habitacionGuardada);
    }

    @Transactional
    @Override
    public HabitacionDTOResponse actualizarHabitacion(Integer id, HabitacionDTOUpdate request) {
        log.info("Actualizando habitación con ID: {}", id);
        
        var habitacion = buscarHabitacionOExcepcion(id);
        var tipoHabitacion = tipoHabitacionRepository.findById(request.idTipoHabitacion())
                .orElseThrow(() -> new EntityNotFoundException("TipoHabitacion no encontrada con ID: " + request.idTipoHabitacion()));
        
        habitacionMapper.updateEntity(request, habitacion);
        habitacion.setTipoHabitacion(tipoHabitacion);
        
        var habitacionActualizada = habitacionRepository.save(habitacion);
        log.info("Habitación actualizada exitosamente con ID: {}", id);
        
        return habitacionMapper.toResponse(habitacionActualizada);
    }

    @Transactional
    @Override
    public void eliminarHabitacion(Integer id) {
        log.info("Eliminando habitación con ID: {}", id);
        
        habitacionRepository.deleteById(id);
        
        log.info("Habitación eliminada exitosamente con ID: {}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public HabitacionDTOResponse buscarHabitacionPorId(Integer id) {
        log.info("Buscando habitación con ID: {}", id);
        return habitacionMapper.toResponse(buscarHabitacionOExcepcion(id));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<HabitacionDTOResponse> buscarHabitaciones(
            Integer cantidadHuespedes,
            Integer idTipoHabitacion,
            Double precioMinimo,
            Double precioMaximo,
            Pageable pageable) {
        
        log.info("Buscando habitaciones con filtros — huespedes: {}, tipoHabitacion: {}, precioMin: {}, precioMax: {}",
                cantidadHuespedes, idTipoHabitacion, precioMinimo, precioMaximo);

        Specification<Habitacion> spec = Specification.where(null);

        // Filtro por capacidad: tipo debe tener capacidad >= cantidadHuespedes
        if (cantidadHuespedes != null && cantidadHuespedes > 0) {
            spec = spec.and((root, query, cb) -> {
                var tipoJoin = root.join("tipoHabitacion", JoinType.INNER);
                return cb.greaterThanOrEqualTo(tipoJoin.get("capacidad"), cantidadHuespedes);
            });
        }

        // Filtro por tipo exacto
        if (idTipoHabitacion != null) {
            spec = spec.and((root, query, cb) -> {
                var tipoJoin = root.join("tipoHabitacion", JoinType.INNER);
                return cb.equal(tipoJoin.get("id"), idTipoHabitacion);
            });
        }

        // Filtro por rango de precio vigente
        if (precioMinimo != null || precioMaximo != null) {
            spec = spec.and((root, query, cb) -> {
                var tipoJoin = root.join("tipoHabitacion", JoinType.INNER);
                LocalDate hoy = LocalDate.now();

                var subquery = query.subquery(Long.class);
                var tarifaRoot = subquery.from(Tarifa.class);
                var predicados = cb.conjunction();
                predicados = cb.and(predicados,
                        cb.equal(tarifaRoot.get("tipoHabitacion").get("id"), tipoJoin.get("id")));
                predicados = cb.and(predicados,
                        cb.lessThanOrEqualTo(tarifaRoot.get("fechaInicio"), hoy));
                predicados = cb.and(predicados,
                        cb.or(
                                cb.isNull(tarifaRoot.get("fechaFin")),
                                cb.greaterThanOrEqualTo(tarifaRoot.get("fechaFin"), hoy)
                        ));
                if (precioMinimo != null) {
                    predicados = cb.and(predicados,
                            cb.greaterThanOrEqualTo(tarifaRoot.get("precioNoche"), precioMinimo));
                }
                if (precioMaximo != null) {
                    predicados = cb.and(predicados,
                            cb.lessThanOrEqualTo(tarifaRoot.get("precioNoche"), precioMaximo));
                }

                subquery.select(cb.literal(1L)).where(predicados);
                return cb.exists(subquery);
            });
        }

        var resultado = habitacionRepository.findAll(spec, pageable)
                .map(habitacionMapper::toResponse);
        
        log.info("Búsqueda retornó {} habitaciones", resultado.getTotalElements());
        return resultado;
    }

    @Transactional(readOnly = true)
    @Override
    public TarifaDTOResponse obtenerTarifaVigente(Integer habitacionId) {
        log.info("Obteniendo tarifa vigente para habitación ID: {}", habitacionId);
        
        var habitacion = buscarHabitacionOExcepcion(habitacionId);
        Integer tipoHabitacionId = habitacion.getTipoHabitacion().getId();
        LocalDate hoy = LocalDate.now();

        List<Tarifa> tarifasVigentes = tarifaRepository.buscarTarifasVigentesEnFecha(tipoHabitacionId, hoy);
        
        if (tarifasVigentes.isEmpty()) {
            throw new EntityNotFoundException(
                    "No hay tarifa vigente para la habitación " + habitacionId + " en fecha " + hoy);
        }

        Tarifa tarifaVigente = tarifasVigentes.get(0);
        return tarifaMapper.toResponse(tarifaVigente);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Habitacion buscarHabitacionOExcepcion(Integer id) {
        return habitacionRepository.findById(id)
                .orElseThrow(() -> {
                    String msg = "Habitación no encontrada con ID: " + id;
                    log.error(msg);
                    return new EntityNotFoundException(msg);
                });
    }

}
