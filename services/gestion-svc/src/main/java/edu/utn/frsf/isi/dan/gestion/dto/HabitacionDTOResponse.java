package edu.utn.frsf.isi.dan.gestion.dto;

public record HabitacionDTOResponse(
        Integer id,
        Integer numero,
        Integer piso,
        TipoHabitacionDTOResponse tipoHabitacion,
        Integer idHotel,
        String nombreHotel
) {
}
