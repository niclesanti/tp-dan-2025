package edu.utn.frsf.isi.dan.reservas_svc;

import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDisponibleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HotelSimpleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.PagoDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReviewDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Hotel;
import edu.utn.frsf.isi.dan.reservas_svc.model.Huesped;
import edu.utn.frsf.isi.dan.reservas_svc.model.Pago;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Review;
import edu.utn.frsf.isi.dan.reservas_svc.model.Tarifa;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Huesped huesped() {
        return Huesped.builder().dni("12345678").nombreApellido("Juan Perez").email("juan@email.com").build();
    }

    public static HuespedDTORequest huespedDTORequest() {
        return new HuespedDTORequest("Juan Perez", "juan@email.com", "12345678");
    }

    public static Hotel hotel() {
        return Hotel.builder().id(1).nombre("Hotel Dan").categoria(4).domicilio("San Martin 123")
                .ubicacion(new GeoJsonPoint(-60.66, -32.95)).build();
    }

    public static Habitacion habitacion() {
        return Habitacion.builder()
                .id("hab-1")
                .habitacionId(101)
                .capacidad(2)
                .precioNoche(100.0)
                .tipoHabitacion("Suite")
                .idTipoHabitacion(1)
                .amenities(List.of("WIFI"))
                .reservas(new ArrayList<>())
                .hotel(hotel())
                .build();
    }

    public static Reserva reserva() {
        return Reserva.builder()
                ._id("r1")
                .idHabitacion("hab-1")
                .hotelId(1)
                .checkIn(Instant.now().plusSeconds(86400))
                .checkOut(Instant.now().plusSeconds(172800))
                .huesped(huesped())
                .precioNoche(100.0)
                .precioTotal(100.0)
                .estadoReserva(EstadoReserva.RESERVADA)
                .pagos(new ArrayList<>())
                .build();
    }

    public static ReservaDTORequest reservaDTORequest() {
        return new ReservaDTORequest("hab-1", Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800), huespedDTORequest());
    }

    public static PagoDTORequest pagoDTORequest() {
        return new PagoDTORequest("CARD", "tx-1", 50.0, "ARS", "1234567812345678");
    }

    public static PagoDTORequest pagoDTORequestSinTransactionId() {
        return new PagoDTORequest("CARD", null, 50.0, "ARS", "1234567812345678");
    }

    public static ReviewDTORequest reviewDTORequest() {
        return new ReviewDTORequest(5.0, "Excelente");
    }

    public static ReservaDTOResponse reservaDTOResponse() {
        return ReservaDTOResponse.builder()
                .id("r1")
                .idHabitacion("hab-1")
                .hotelId(1)
                .checkIn(Instant.now().plusSeconds(86400))
                .checkOut(Instant.now().plusSeconds(172800))
                .huesped(huesped())
                .estadoReserva(EstadoReserva.RESERVADA)
                .build();
    }

    public static Pago pago() {
        return Pago.builder()
                .method("CARD")
                .transactionId("tx-1")
                .amount(Tarifa.builder().precio(50.0).moneda("ARS").build())
                .status("APPROVED")
                .nroTarjeta("1234567812345678")
                .build();
    }

    public static Review review() {
        return Review.builder().rating(4.0).comment("Muy bien").createdAt(Instant.now().toString()).build();
    }

    public static HabitacionDisponibleDTO disponibleDTO() {
        return HabitacionDisponibleDTO.builder()
                .id("hab-1")
                .habitacionId(101)
                .capacidad(2)
                .precioNoche(100.0)
                .tipoHabitacion("Suite")
                .amenities(List.of("WIFI"))
                .hotel(HotelSimpleDTO.builder()
                        .id(1)
                        .nombre("Hotel Dan")
                        .categoria(4)
                        .domicilio("San Martin 123")
                        .latitud(-32.95)
                        .longitud(-60.66)
                        .build())
                .build();
    }
}

