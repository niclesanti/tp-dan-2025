package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PropietarioDTORequest(
        // Atributos de Usuario
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$", message = "El teléfono debe tener un formato válido")
        @Size(max = 255, message = "El teléfono no puede superar los 255 caracteres")
        String telefono,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener 7 u 8 dígitos")
        String dni,

        // Atributos específicos de Propietario
        @NotNull(message = "La cuenta bancaria es obligatoria")
        @Valid
        CuentaBancariaDTORequest cuentaBancaria,

        @NotNull(message = "El ID del hotel es obligatorio")
        @Positive(message = "El ID del hotel debe ser un número positivo")
        Integer idHotel
) {

}
