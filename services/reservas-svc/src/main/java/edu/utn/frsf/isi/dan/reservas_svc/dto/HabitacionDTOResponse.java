package edu.utn.frsf.isi.dan.reservas_svc.dto;

import java.util.List;

public record HabitacionDTOResponse(
        String id,
        Integer habitacionId,
        Integer capacidad,
        Double precioNoche,
        List<String> amenities,
        List<ReservaSimpleDTOResponse> reservas,
        HotelDTOResponse hotel,
        Integer idTipoHabitacion,
        String tipoHabitacion
) {
}
