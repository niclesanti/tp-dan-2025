package edu.utn.frsf.isi.dan.gestion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TipoHabitacionDTORequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 1, max = 50, message = "El nombre debe tener entre 1 y 50 caracteres")
        String nombre,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 1, max = 255, message = "La descripción debe tener entre 1 y 255 caracteres")
        String descripcion,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser un número positivo")
        @Max(value = 10, message = "La capacidad no puede superar las 10 personas")
        Integer capacidad
) {
}
