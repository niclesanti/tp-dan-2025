package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDisponibleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HotelSimpleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Hotel;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Habitacion> findAll() {
        return habitacionRepository.findAll();
    }

    @Override
    public Optional<Habitacion> findById(String id) {
        return habitacionRepository.findById(id);
    }

    @Override
    public Habitacion save(Habitacion habitacion) {
        return habitacionRepository.save(habitacion);
    }

    @Override
    public void deleteById(String id) {
        habitacionRepository.deleteById(id);
    }

    @Override
    public void handleEvent(HabitacionEvent event) {
        switch (event.getTipoEvento()) {
            case CREAR:
                save(mapFromHabitacion(event.getHabitacion()));
                break;
            case ACTUALIZAR_DATOS:
                updateByHabitacionId(event.getHabitacion().getHabitacionId(),mapFromHabitacion(event.getHabitacion()));
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

    @Override
    public Page<HabitacionDisponibleDTO> buscarDisponibles(
            Instant checkIn, Instant checkOut, 
            Integer capacidad, Double precioMin, Double precioMax,
            Integer categoriaHotel, List<String> amenities,
            Double latitud, Double longitud, Double radioKm,
            Pageable pageable) {
        
        log.info("Buscando habitaciones disponibles del {} al {}", checkIn, checkOut);
        if (categoriaHotel != null) {
            log.info("  - Filtro categoría hotel: {} estrellas", categoriaHotel);
        }
        if (amenities != null && !amenities.isEmpty()) {
            log.info("  - Filtro amenities: {}", amenities);
        }
        if (latitud != null && longitud != null && radioKm != null) {
            log.info("  - Filtro geoespacial: lat={}, lon={}, radio={}km", latitud, longitud, radioKm);
        }
        
        // Construir query para habitaciones según filtros
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
        
        // Filtro por categoría de hotel (cantidad de estrellas)
        if (categoriaHotel != null) {
            criteriaList.add(Criteria.where("hotel.categoria").is(categoriaHotel));
        }
        
        // Filtro por amenities (la habitación debe tener TODAS las amenities solicitadas)
        if (amenities != null && !amenities.isEmpty()) {
            criteriaList.add(Criteria.where("amenities").all(amenities));
        }
        
        // Filtro geoespacial (búsqueda por proximidad)
        if (latitud != null && longitud != null && radioKm != null) {
            // MongoDB GeoJSON usa [longitude, latitude] (x, y)
            Point point = new Point(longitud, latitud);
            Distance distance = new Distance(radioKm, Metrics.KILOMETERS);
            
            // Usar near con Point y Distance
            criteriaList.add(Criteria.where("hotel.ubicacion").near(point).maxDistance(distance.getValue()));
        }
        
        if (!criteriaList.isEmpty()) {
            habitacionQuery.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        // Obtener todas las habitaciones que cumplen criterios básicos
        List<Habitacion> todasHabitaciones = mongoTemplate.find(habitacionQuery, Habitacion.class);
        
        // Filtrar las que NO tienen reservas conflictivas
        List<Habitacion> disponibles = todasHabitaciones.stream()
                .filter(hab -> esHabitacionDisponible(hab.getId(), checkIn, checkOut))
                .collect(Collectors.toList());
        
        // Aplicar paginación
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), disponibles.size());
        List<Habitacion> paginadas = disponibles.subList(start, end);
        
        // Mapear a DTO
        List<HabitacionDisponibleDTO> dtos = paginadas.stream()
                .map(this::toDisponibleDTO)
                .collect(Collectors.toList());
        
        log.info("Encontradas {} habitaciones disponibles", disponibles.size());
        return new PageImpl<>(dtos, pageable, disponibles.size());
    }

    // ===== MÉTODOS PRIVADOS =====

    private Habitacion mapFromHabitacion(HabitacionDTO dto) {
        return Habitacion.builder()
                .habitacionId(dto.getHabitacionId())
                .precioNoche(dto.getPrecioNoche())
                .capacidad(dto.getCapacidad())
                .amenities(dto.getAmenities())
                .idTipoHabitacion(dto.getTipoHabitacionId())
                .tipoHabitacion(dto.getTipoHabitacion())
                .hotel(mapFromDto(dto.getHotel()))
                .build();
    }

    private Hotel mapFromDto(HotelDTO dto){
        if(dto == null) {
            return null;
        }
        return Hotel.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .domicilio(dto.getDomicilio())
                .categoria(dto.getCategoria())
                .ubicacion(new GeoJsonPoint(dto.getLongitud(), dto.getLatitud()))  // GeoJSON: (longitude, latitude)
                .build();
    }

    private Optional<Habitacion> findByHabitacionId(Integer habitacionId) {
        Query query = new Query(Criteria.where("habitacionId").is(habitacionId));
        Habitacion habitacion = mongoTemplate.findOne(query, Habitacion.class);
        return Optional.ofNullable(habitacion);
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
    
    private boolean esHabitacionDisponible(String idHabitacion, Instant checkIn, Instant checkOut) {
        Query reservaQuery = new Query();
        reservaQuery.addCriteria(
                Criteria.where("idHabitacion").is(idHabitacion)
                        .and("estadoReserva").in(
                                EstadoReserva.RESERVADA,
                                EstadoReserva.CONFIRMADA, 
                                EstadoReserva.EFECTUADA,
                                EstadoReserva.BLOQUEADA,
                                EstadoReserva.CERRADA)
                        .orOperator(
                                Criteria.where("checkIn").lt(checkOut).and("checkOut").gt(checkIn)
                        )
        );
        
        return !mongoTemplate.exists(reservaQuery, Reserva.class);
    }
    
    private HabitacionDisponibleDTO toDisponibleDTO(Habitacion habitacion) {
        var hotelDTO = HotelSimpleDTO.builder()
                .id(habitacion.getHotel().getId())
                .nombre(habitacion.getHotel().getNombre())
                .categoria(habitacion.getHotel().getCategoria())
                .domicilio(habitacion.getHotel().getDomicilio())
                .build();
        
        return HabitacionDisponibleDTO.builder()
                .id(habitacion.getId())
                .habitacionId(habitacion.getHabitacionId())
                .capacidad(habitacion.getCapacidad())
                .precioNoche(habitacion.getPrecioNoche())
                .tipoHabitacion(habitacion.getTipoHabitacion())
                .hotel(hotelDTO)
                .build();
    }
}
