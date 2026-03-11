package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TarjetaCreditoDTORequest(
        @NotBlank(message = "El número de tarjeta es obligatorio")
        @Pattern(regexp = "^\\d{13,22}$", message = "El número de tarjeta debe tener entre 13 y 22 dígitos")
        String numero,

        @NotBlank(message = "El nombre del titular es obligatorio")
        @Size(min = 3, max = 255, message = "El nombre del titular debe tener entre 3 y 255 caracteres")
        String nombreTitular,

        @NotBlank(message = "La fecha de vencimiento es obligatoria")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "La fecha de vencimiento debe tener formato MM/YY")
        String fechaVencimiento,

        @NotBlank(message = "El código de seguridad es obligatorio")
        @Pattern(regexp = "^\\d{3,4}$", message = "El código de seguridad debe tener 3 o 4 dígitos")
        String cvc,

        @NotNull(message = "Debe indicar si es tarjeta principal")
        Boolean esPrincipal,

        @NotNull(message = "El ID del banco es obligatorio")
        @Positive(message = "El ID del banco debe ser un número positivo")
        Integer bancoId
) {

}
