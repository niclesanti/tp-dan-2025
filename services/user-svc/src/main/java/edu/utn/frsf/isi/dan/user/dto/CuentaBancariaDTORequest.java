package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CuentaBancariaDTORequest(
        @NotBlank(message = "El número de cuenta es obligatorio")
        @Size(min = 6, max = 255, message = "El número de cuenta debe tener entre 6 y 255 caracteres")
        String numeroCuenta,

        @NotBlank(message = "El CBU es obligatorio")
        @Pattern(regexp = "^\\d{22}$", message = "El CBU debe tener exactamente 22 dígitos")
        String cbu,

        @NotBlank(message = "El alias es obligatorio")
        @Size(min = 6, max = 255, message = "El alias debe tener entre 6 y 255 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9.]+$", message = "El alias solo puede contener letras, números y puntos")
        String alias,

        @NotNull(message = "El ID del banco es obligatorio")
        @Positive(message = "El ID del banco debe ser un número positivo")
        Integer bancoId
) {

}
