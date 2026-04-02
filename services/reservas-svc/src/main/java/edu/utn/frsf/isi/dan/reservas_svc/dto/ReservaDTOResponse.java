package edu.utn.frsf.isi.dan.reservas_svc.dto;

import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Huesped;
import edu.utn.frsf.isi.dan.reservas_svc.model.Pago;
import edu.utn.frsf.isi.dan.reservas_svc.model.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTOResponse {
    private String id;
    private String idHabitacion;
    private Integer hotelId;
    private Instant createdAt;
    private Instant checkIn;
    private Instant checkOut;
    private Double precioNoche;
    private Double precioTotal;
    private Huesped huesped;
    private List<Pago> pagos;
    private Review clientReview;
    private Review hostReview;
    private EstadoReserva estadoReserva;
}
