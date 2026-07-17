package edu.utn.frsf.isi.dan.gestion.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record HotelDTORequest(

        @NotBlank(message = "El nombre del hotel es obligatorio")
        @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
        String nombre,

        @NotBlank(message = "El CUIT es obligatorio")
        @Pattern(regexp = "^(\\d{11}|\\d{2}-\\d{8}-\\d{1})$", message = "El CUIT debe tener el formato XX-XXXXXXXX-X o 11 dígitos")
        @Size(max = 20, message = "El CUIT no puede superar los 20 caracteres")
        String cuit,

        @NotBlank(message = "El domicilio es obligatorio")
        @Size(min = 5, max = 255, message = "El domicilio debe tener entre 5 y 255 caracteres")
        String domicilio,

        @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
        Double latitud,

        @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
        @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
        Double longitud,

        @Pattern(regexp = "^[0-9+\\-\\s()]{7,30}$", message = "El teléfono debe tener un formato válido")
        String telefono,

        @Email(message = "El correo de contacto debe ser un email válido")
        @Size(max = 100, message = "El correo de contacto no puede superar los 100 caracteres")
        String correoContacto,

        @NotNull(message = "La categoría es obligatoria")
        @Min(value = 1, message = "La categoría debe ser al menos 1 estrella")
        @Max(value = 5, message = "La categoría no puede superar las 5 estrellas")
        Integer categoria
) {
}
