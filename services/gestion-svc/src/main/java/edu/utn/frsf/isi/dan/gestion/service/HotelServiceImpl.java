package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.AmenityHotelRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.mapper.HotelMapper;
import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.model.AmenityHotel;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
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
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final AmenityHotelRepository amenityHotelRepository;
    private final HotelMapper hotelMapper;

    /**
     * Crea un nuevo hotel con los datos proporcionados.
     *
     * @param request DTO con los datos del nuevo hotel
     * @return DTO de respuesta con los datos del hotel creado
     */
    @Transactional
    @Override
    public HotelDTOResponse crearHotel(HotelDTORequest request) {
        log.info("Creando hotel con datos: {}", request);
        var hotel = hotelMapper.toEntity(request);
        var hotelGuardado = hotelRepository.save(hotel);
        log.info("Hotel creado exitosamente con ID: {}", hotelGuardado.getId());
        return hotelMapper.toResponse(hotelGuardado);
    }

    /**
     * Actualiza los datos editables de un hotel existente.
     * Solo se pueden modificar: categoria, telefono y correoContacto.
     *
     * @param id      ID del hotel a actualizar
     * @param request DTO con los 3 campos editables
     * @return DTO de respuesta con los datos del hotel actualizado
     * @throws EntityNotFoundException si no existe un hotel con el ID especificado
     */
    @Transactional
    @Override
    public HotelDTOResponse actualizarHotel(Integer id, HotelDTOUpdate request) {
        log.info("Actualizando hotel con ID: {}", id);
        var hotel = buscarHotelOExcepcion(id);
        hotelMapper.updateEntity(request, hotel);
        var hotelActualizado = hotelRepository.save(hotel);
        log.info("Hotel actualizado exitosamente con ID: {}", hotelActualizado.getId());
        return hotelMapper.toResponse(hotelActualizado);
    }

    /**
     * Marca un hotel como cerrado estableciendo la fecha de cierre en el día actual.
     * Automáticamente todas sus habitaciones quedan no disponibles.
     * <p>
     * NOTA: El mensaje asíncrono al servicio de reservas se implementará
     * cuando reservas-svc esté desarrollado.
     * </p>
     *
     * @param id ID del hotel a cerrar
     * @return DTO de respuesta con los datos del hotel cerrado
     * @throws EntityNotFoundException  si no existe un hotel con el ID especificado
     * @throws IllegalStateException    si el hotel ya está cerrado
     */
    @Transactional
    @Override
    public HotelDTOResponse cerrarHotel(Integer id) {
        log.info("Cerrando hotel con ID: {}", id);
        var hotel = buscarHotelOExcepcion(id);
        if (hotel.getFechaCierre() != null) {
            String msg = "El hotel con ID " + id + " ya está cerrado desde: " + hotel.getFechaCierre();
            log.warn(msg);
            throw new IllegalStateException(msg);
        }
        hotel.setFechaCierre(LocalDate.now());
        var hotelCerrado = hotelRepository.save(hotel);
        log.info("Hotel cerrado exitosamente con ID: {} en fecha: {}", hotelCerrado.getId(), hotelCerrado.getFechaCierre());
        return hotelMapper.toResponse(hotelCerrado);
    }

    /**
     * Busca un hotel por su ID.
     *
     * @param id ID del hotel a buscar
     * @return DTO de respuesta con los datos del hotel encontrado
     * @throws EntityNotFoundException si no existe un hotel con el ID especificado
     */
    @Transactional(readOnly = true)
    @Override
    public HotelDTOResponse buscarHotelPorId(Integer id) {
        log.info("Buscando hotel con ID: {}", id);
        return hotelMapper.toResponse(buscarHotelOExcepcion(id));
    }

    /**
     * Busca hoteles por criterios opcionales de filtrado con paginación.
     *
     * @param nombre    texto parcial a buscar en el nombre (opcional)
     * @param categoria categoría exacta (opcional)
     * @param domicilio texto parcial a buscar en el domicilio (opcional)
     * @param amenity   amenity requerida (opcional)
     * @param pageable  parámetros de paginación y orden
     * @return página de {@link HotelDTOResponse} que coinciden con los criterios
     */
    @Transactional(readOnly = true)
    @Override
    public Page<HotelDTOResponse> buscarHoteles(String nombre, Integer categoria,
                                                 String domicilio, Amenity amenity,
                                                 Pageable pageable) {
        log.info("Buscando hoteles con filtros — nombre: '{}', categoria: {}, domicilio: '{}', amenity: {}",
                nombre, categoria, domicilio, amenity);

        Specification<Hotel> spec = Specification.where((Specification<Hotel>) null);
        if (nombre != null && !nombre.isBlank()) {
            String patron = "%" + nombre.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nombre")), patron));
        }
        if (categoria != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoria"), categoria));
        }
        if (domicilio != null && !domicilio.isBlank()) {
            String patron = "%" + domicilio.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("domicilio")), patron));
        }
        if (amenity != null) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                var join = root.join("amenities", JoinType.INNER);
                return cb.equal(join.get("amenity"), amenity);
            });
        }

        var resultado = hotelRepository.findAll(spec, pageable).map(hotelMapper::toResponse);
        log.info("Búsqueda retornó {} resultados en página {}/{}",
                resultado.getNumberOfElements(), resultado.getNumber() + 1, resultado.getTotalPages());
        return resultado;
    }

    /**
     * Agrega una o varias amenities a un hotel.
     *
     * @param hotelId  ID del hotel al que se agregan las amenities
     * @param amenities lista de amenities a agregar
     * @return DTO de respuesta con los datos del hotel actualizado
     * @throws EntityNotFoundException si no existe un hotel con el ID especificado
     */
    @Transactional
    @Override
    public HotelDTOResponse agregarAmenities(Integer hotelId, List<Amenity> amenities) {
        log.info("Agregando {} amenities al hotel con ID: {}", amenities.size(), hotelId);
        var hotel = buscarHotelOExcepcion(hotelId);
        for (var amenity : amenities) {
            var amenityHotel = AmenityHotel.builder()
                    .hotel(hotel)
                    .amenity(amenity)
                    .build();
            var savedAmenity = amenityHotelRepository.save(amenityHotel);
            hotel.getAmenities().add(savedAmenity);
        }
        log.info("Amenities agregadas exitosamente al hotel con ID: {}", hotelId);
        return hotelMapper.toResponse(hotel);
    }

    /**
     * Elimina una amenity específica de un hotel.
     *
     * @param hotelId   ID del hotel propietario de la amenity
     * @param amenityId ID de la amenity a eliminar
     * @throws EntityNotFoundException si la amenity no existe o no pertenece al hotel
     */
    @Transactional
    @Override
    public void eliminarAmenity(Integer hotelId, Long amenityId) {
        log.info("Eliminando amenity con ID: {} del hotel con ID: {}", amenityId, hotelId);
        var amenityHotel = amenityHotelRepository.findByIdAndHotelId(amenityId, hotelId)
                .orElseThrow(() -> {
                    String msg = "Amenity con ID " + amenityId + " no encontrada para el hotel con ID " + hotelId;
                    log.error(msg);
                    return new EntityNotFoundException(msg);
                });
        amenityHotelRepository.delete(amenityHotel);
        log.info("Amenity eliminada exitosamente con ID: {} del hotel con ID: {}", amenityId, hotelId);
    }

    // ==============================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==============================

    private Hotel buscarHotelOExcepcion(Integer id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> {
                    String msg = "Hotel no encontrado con ID: " + id;
                    log.error(msg);
                    return new EntityNotFoundException(msg);
                });
    }
}
