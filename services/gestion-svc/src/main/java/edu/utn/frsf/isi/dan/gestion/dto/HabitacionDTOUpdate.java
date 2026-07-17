package edu.utn.frsf.isi.dan.gestion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HabitacionDTOUpdate(

        @NotNull(message = "El número de habitación es obligatorio")
        @Positive(message = "El número de habitación debe ser un número positivo")
        Integer numero,

        @NotNull(message = "El piso es obligatorio")
        @Positive(message = "El piso debe ser un número positivo")
        Integer piso,

        @NotNull(message = "El ID del tipo de habitación es obligatorio")
        @Positive(message = "El ID del tipo de habitación debe ser un número positivo")
        Integer idTipoHabitacion
) {
}
