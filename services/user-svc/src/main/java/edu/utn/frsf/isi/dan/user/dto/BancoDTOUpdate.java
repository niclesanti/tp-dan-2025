package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BancoDTOUpdate(
        @NotBlank(message = "El nombre del banco es obligatorio")
        @Size(min = 1, max = 255, message = "El nombre debe tener entre 1 y 255 caracteres")
        String nombre
) {

}