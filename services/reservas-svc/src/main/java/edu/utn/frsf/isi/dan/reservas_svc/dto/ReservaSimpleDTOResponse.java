package edu.utn.frsf.isi.dan.reservas_svc.dto;

import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;

import java.time.Instant;

public record ReservaSimpleDTOResponse(
        String _id,
        Instant checkIn,
        Instant checkOut,
        Double precioTotal,
        EstadoReserva estadoReserva
) {
}
