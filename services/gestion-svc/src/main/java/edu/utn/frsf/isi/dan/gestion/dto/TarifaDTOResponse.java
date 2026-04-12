package edu.utn.frsf.isi.dan.gestion.dto;

import java.time.LocalDate;

public record TarifaDTOResponse(
        Integer id,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        TipoHabitacionDTOResponse tipoHabitacion,
        Double precioNoche
) {
}
