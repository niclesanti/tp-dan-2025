package edu.utn.frsf.isi.dan.reservas_svc.dto;

import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Pago;
import edu.utn.frsf.isi.dan.reservas_svc.model.Review;

public record ReservaDTOUpdate(
        EstadoReserva estadoReserva,
        Pago nuevoPago,
        Review review
) {
}
