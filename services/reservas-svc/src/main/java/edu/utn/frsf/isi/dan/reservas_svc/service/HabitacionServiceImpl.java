package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDisponibleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.exception.EntityNotFoundException;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.HabitacionMapper;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final MongoTemplate mongoTemplate;
    private final HabitacionMapper habitacionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitacionDTOResponse> findAll() {
        log.info("Listando todas las habitaciones sincronizadas");
        return habitacionRepository.findAll().stream()
                .map(habitacionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitacionDTOResponse buscarPorId(String id) {
        log.info("Buscando habitación con ID: {}", id);
        return habitacionMapper.toResponse(buscarHabitacionOExcepcion(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(HabitacionEvent event) {
        switch (event.getTipoEvento()) {
            case CREAR:
                habitacionRepository.save(habitacionMapper.toEntity(event.getHabitacion()));
                break;
            case ACTUALIZAR_DATOS:
                updateByHabitacionId(event.getHabitacion().getHabitacionId(),
                        habitacionMapper.toEntity(event.getHabitacion()));
                break;
            case ACTUALIZAR_PRECIO:
                actualizarPrecioPorTipo(event.getTarifa());
                break;
            case ELIMINAR:
                deleteByHabitacionId(event.getHabitacion().getHabitacionId());
                break;
            default:
                throw new IllegalArgumentException("Tipo de evento desconocido: " + event.getTipoEvento());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<HabitacionDisponibleDTO> buscarDisponibles(
            Instant checkIn, Instant checkOut,
            Integer capacidad, Double precioMin, Double precioMax,
            Integer categoriaHotel, List<String> amenities,
            Double latitud, Double longitud, Double radioKm,
            Pageable pageable) {

        log.info("Buscando habitaciones disponibles del {} al {}", checkIn, checkOut);

        Query habitacionQuery = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (capacidad != null && capacidad > 0) {
            criteriaList.add(Criteria.where("capacidad").gte(capacidad));
        }

        if (precioMin != null) {
            criteriaList.add(Criteria.where("precioNoche").gte(precioMin));
        }

        if (precioMax != null) {
            criteriaList.add(Criteria.where("precioNoche").lte(precioMax));
        }

        if (categoriaHotel != null) {
            criteriaList.add(Criteria.where("hotel.categoria").is(categoriaHotel));
        }

        if (amenities != null && !amenities.isEmpty()) {
            criteriaList.add(Criteria.where("amenities").all(amenities));
        }

        if (latitud != null && longitud != null && radioKm != null) {
            GeoJsonPoint geoPoint = new GeoJsonPoint(longitud, latitud);
            double maxDistanceMeters = radioKm * 1000.0;

            criteriaList.add(Criteria.where("hotel.ubicacion").nearSphere(geoPoint).maxDistance(maxDistanceMeters));
        }

        if (!criteriaList.isEmpty()) {
            habitacionQuery.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<Habitacion> todasHabitaciones = mongoTemplate.find(habitacionQuery, Habitacion.class);

        List<Habitacion> disponibles = todasHabitaciones.stream()
                .filter(hab -> esHabitacionDisponible(hab, checkIn, checkOut))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        if (start >= disponibles.size()) {
            return new PageImpl<>(List.of(), pageable, disponibles.size());
        }
        int end = Math.min((start + pageable.getPageSize()), disponibles.size());
        List<Habitacion> paginadas = disponibles.subList(start, end);

        List<HabitacionDisponibleDTO> dtos = paginadas.stream()
                .map(habitacionMapper::toDisponible)
                .collect(Collectors.toList());

        log.info("Encontradas {} habitaciones disponibles", disponibles.size());
        return new PageImpl<>(dtos, pageable, disponibles.size());
    }

    // ===== MÉTODOS AUXILIARES PRIVADOS =====

    private Habitacion buscarHabitacionOExcepcion(String id) {
        return habitacionRepository.findById(id)
                .orElseThrow(() -> {
                    String msg = "Habitación no encontrada con ID: " + id;
                    log.error(msg);
                    return new EntityNotFoundException(msg);
                });
    }

    private Habitacion updateByHabitacionId(Integer habitacionId, Habitacion nuevaHabitacion) {
        Query query = new Query(Criteria.where("habitacionId").is(habitacionId));
        Update update = new Update()
                .set("precioNoche", nuevaHabitacion.getPrecioNoche())
                .set("capacidad", nuevaHabitacion.getCapacidad())
                .set("amenities", nuevaHabitacion.getAmenities())
                .set("idTipoHabitacion", nuevaHabitacion.getIdTipoHabitacion())
                .set("tipoHabitacion", nuevaHabitacion.getTipoHabitacion());
        Habitacion actualizada = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Habitacion.class
        );
        if (actualizada == null) {
            throw new IllegalArgumentException("No se encontró la habitación con habitacionId: " + habitacionId);
        }
        return actualizada;
    }

    private void deleteByHabitacionId(Integer habitacionId) {
        Query query = new Query(Criteria.where("habitacionId").is(habitacionId));
        mongoTemplate.remove(query, Habitacion.class);
    }

    private void actualizarPrecioPorTipo(edu.utn.frsf.isi.dan.shared.TarifaDTO tarifaDTO) {
        Query query = new Query(Criteria.where("idTipoHabitacion").is(tarifaDTO.getTipoHabitacionId()));
        Update update = new Update().set("precioNoche", tarifaDTO.getNuevoPrecio());
        mongoTemplate.updateMulti(query, update, Habitacion.class);
        log.info("Precio actualizado para tipo habitación ID: {}", tarifaDTO.getTipoHabitacionId());
    }

    private boolean esHabitacionDisponible(Habitacion habitacion, Instant checkIn, Instant checkOut) {
        Query reservaQuery = new Query();
        reservaQuery.addCriteria(
                Criteria.where("idHabitacion").in(habitacion.getId(), String.valueOf(habitacion.getHabitacionId()))
                        .and("estadoReserva").in(
                                EstadoReserva.RESERVADA,
                                EstadoReserva.CONFIRMADA,
                                EstadoReserva.EFECTUADA,
                                EstadoReserva.BLOQUEADA,
                                EstadoReserva.CERRADA)
                        .andOperator(
                                new Criteria().orOperator(
                                        Criteria.where("checkOut").is(null),
                                        Criteria.where("checkIn").lt(checkOut)
                                                .and("checkOut").gt(checkIn)
                                )
                        )
        );

        return !mongoTemplate.exists(reservaQuery, Reserva.class);
    }
}
