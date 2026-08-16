package edu.utn.frsf.isi.dan.reservas_svc.dto;

import java.util.List;

public record HabitacionDisponibleDTO(
        String id,
        Integer habitacionId,
        Integer capacidad,
        Double precioNoche,
        String tipoHabitacion,
        List<String> amenities,
        HotelSimpleDTO hotel
) {
}
