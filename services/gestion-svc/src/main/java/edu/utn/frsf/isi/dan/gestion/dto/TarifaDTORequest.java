package edu.utn.frsf.isi.dan.gestion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record TarifaDTORequest(

        LocalDate fechaInicio,

        LocalDate fechaFin,

        @NotNull(message = "El ID del tipo de habitación es obligatorio")
        @Positive(message = "El ID del tipo de habitación debe ser un número positivo")
        Integer idTipoHabitacion,

        @NotNull(message = "El precio por noche es obligatorio")
        @Positive(message = "El precio por noche debe ser mayor a 0")
        @DecimalMin(value = "0.01", message = "El precio por noche debe ser al menos 0.01")
        Double precioNoche
) {
}
