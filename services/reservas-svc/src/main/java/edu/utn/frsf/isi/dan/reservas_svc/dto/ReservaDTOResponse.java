package edu.utn.frsf.isi.dan.reservas_svc.dto;

import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;

import java.time.Instant;
import java.util.List;

public record ReservaDTOResponse(
        String id,
        String idHabitacion,
        Integer hotelId,
        Instant createdAt,
        Instant checkIn,
        Instant checkOut,
        Double precioNoche,
        Double precioTotal,
        HuespedDTOResponse huesped,
        List<PagoDTOResponse> pagos,
        ReviewDTOResponse clientReview,
        ReviewDTOResponse hostReview,
        EstadoReserva estadoReserva
) {
}
