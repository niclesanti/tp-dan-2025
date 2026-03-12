package edu.utn.frsf.isi.dan.gestion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar un Hotel.
 * Solo permite modificar categoria, telefono y correoContacto.
 * El resto de los datos (nombre, cuit, domicilio, coordenadas) permanecen sin modificar.
 */
public record HotelDTOUpdate(

        @NotNull(message = "La categoría es obligatoria")
        @Min(value = 1, message = "La categoría debe ser al menos 1 estrella")
        @Max(value = 5, message = "La categoría no puede superar las 5 estrellas")
        Integer categoria,

        @Pattern(regexp = "^[0-9+\\-\\s()]{7,30}$", message = "El teléfono debe tener un formato válido")
        String telefono,

        @Email(message = "El correo de contacto debe ser un email válido")
        @Size(max = 100, message = "El correo de contacto no puede superar los 100 caracteres")
        String correoContacto
) {
}
