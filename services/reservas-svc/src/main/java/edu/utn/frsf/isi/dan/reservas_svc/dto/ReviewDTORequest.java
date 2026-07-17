package edu.utn.frsf.isi.dan.reservas_svc.dto;

import jakarta.validation.constraints.*;

public record ReviewDTORequest(
        @NotNull(message = "La calificación es obligatoria")
        @Min(value = 1, message = "La calificación mínima es 1")
        @Max(value = 5, message = "La calificación máxima es 5")
        Double rating,
        
        @NotBlank(message = "El comentario es obligatorio")
        @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
        String comment
) {
}
