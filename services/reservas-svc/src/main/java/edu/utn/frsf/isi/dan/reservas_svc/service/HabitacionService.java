package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDisponibleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDTOResponse;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface HabitacionService {

    /**
     * Retorna todas las habitaciones sincronizadas desde el servicio de gestión.
     *
     * @return lista de DTOs de respuesta con los datos de las habitaciones
     */
    List<HabitacionDTOResponse> findAll();

    /**
     * Busca una habitación por su ID de MongoDB.
     *
     * @param id ID de la habitación a buscar
     * @return DTO de respuesta con los datos de la habitación
     * @throws edu.utn.frsf.isi.dan.reservas_svc.exception.EntityNotFoundException si no existe una habitación con el ID especificado
     */
    HabitacionDTOResponse buscarPorId(String id);

    /**
     * Procesa un evento de habitación publicado por el servicio de gestión.
     * Según el tipo de evento: crea, actualiza, actualiza precio o elimina habitaciones.
     *
     * @param event evento de habitación recibido por RabbitMQ
     */
    void handleEvent(HabitacionEvent event);

    /**
     * Busca habitaciones disponibles para un rango de fechas aplicando filtros opcionales.
     * Todos los filtros se combinan en AND y se aplica paginación.
     *
     * @param checkIn      fecha de entrada
     * @param checkOut     fecha de salida
     * @param capacidad    capacidad mínima de la habitación (opcional)
     * @param precioMin    precio mínimo por noche (opcional)
     * @param precioMax    precio máximo por noche (opcional)
     * @param categoriaHotel categoría del hotel en estrellas (opcional)
     * @param amenities    amenities requeridas, operador AND (opcional)
     * @param latitud      latitud de referencia para búsqueda geoespacial (opcional)
     * @param longitud     longitud de referencia para búsqueda geoespacial (opcional)
     * @param radioKm      radio en kilómetros para búsqueda geoespacial (opcional)
     * @param pageable     parámetros de paginación y orden
     * @return página de {@link HabitacionDisponibleDTO} que coinciden con los criterios
     */
    Page<HabitacionDisponibleDTO> buscarDisponibles(
            Instant checkIn,
            Instant checkOut,
            Integer capacidad,
            Double precioMin,
            Double precioMax,
            Integer categoriaHotel,
            List<String> amenities,
            Double latitud,
            Double longitud,
            Double radioKm,
            Pageable pageable);
}
