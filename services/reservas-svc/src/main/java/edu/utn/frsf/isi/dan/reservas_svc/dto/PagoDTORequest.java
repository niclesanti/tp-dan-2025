package edu.utn.frsf.isi.dan.reservas_svc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PagoDTORequest(
        @NotBlank(message = "El método de pago es obligatorio")
        String method,
        
        @NotBlank(message = "El ID de transacción es obligatorio")
        String transactionId,
        
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser positivo")
        Double amount,
        
        @NotBlank(message = "La moneda es obligatoria")
        String currency
) {
}
