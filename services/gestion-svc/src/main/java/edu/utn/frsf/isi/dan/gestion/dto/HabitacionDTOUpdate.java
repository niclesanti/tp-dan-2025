package edu.utn.frsf.isi.dan.gestion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record HabitacionDTOUpdate(

        @NotNull(message = "El número de habitación es obligatorio")
        @Positive(message = "El número de habitación debe ser un número positivo")
        Integer numero,

        @NotNull(message = "El piso es obligatorio")
        @PositiveOrZero(message = "El piso debe ser mayor o igual a 0")
        Integer piso,

        @NotNull(message = "El ID del tipo de habitación es obligatorio")
        @Positive(message = "El ID del tipo de habitación debe ser un número positivo")
        Integer idTipoHabitacion,

        @NotNull(message = "El ID del hotel es obligatorio")
        @Positive(message = "El ID del hotel debe ser un número positivo")
        Integer idHotel
) {
}
