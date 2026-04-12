package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTOResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HabitacionService {

    /**
     * Crea una nueva habitación y notifica al servicio de reservas.
     */
    HabitacionDTOResponse crearHabitacion(HabitacionDTORequest request);

    /**
     * Actualiza una habitación existente y notifica al servicio de reservas.
     */
    HabitacionDTOResponse actualizarHabitacion(Integer id, HabitacionDTOUpdate request);

    /**
     * Elimina una habitación y notifica al servicio de reservas.
     */
    void eliminarHabitacion(Integer id);

    /**
     * Busca una habitación por ID.
     */
    HabitacionDTOResponse buscarHabitacionPorId(Integer id);

    /**
     * Busca habitaciones con filtros opcionales:
     * - cantidadHuespedes: busca tipos con capacidad >= este valor
     * - idTipoHabitacion: filtra por tipo exacto
     * - precioMinimo y precioMaximo: rango de precio vigente hoy
     */
    Page<HabitacionDTOResponse> buscarHabitaciones(
            Integer cantidadHuespedes,
            Integer idTipoHabitacion,
            Double precioMinimo,
            Double precioMaximo,
            Pageable pageable
    );

    /**
     * Retorna la tarifa vigente (hoy) para una habitación específica.
     */
    TarifaDTOResponse obtenerTarifaVigente(Integer habitacionId);
}