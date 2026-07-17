package edu.utn.frsf.isi.dan.reservas_svc.dto;

import edu.utn.frsf.isi.dan.reservas_svc.model.Huesped;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReservaDTORequest(
        @NotBlank(message = "El ID de la habitación es obligatorio")
        String idHabitacion,

        @NotNull(message = "La fecha de check-in es obligatoria")
        Instant checkIn,

        @NotNull(message = "La fecha de check-out es obligatoria")
        @Future(message = "La fecha de check-out debe ser futura")
        Instant checkOut,

        @NotNull(message = "Los datos del huésped son obligatorios")
        Huesped huesped
) {
}
