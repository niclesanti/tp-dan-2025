package edu.utn.frsf.isi.dan.gestion;

import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.model.AmenityHotel;
import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static TipoHabitacion tipoHabitacion() {
        return TipoHabitacion.builder().id(1).nombre("Suite").descripcion("Suite premium").capacidad(2).build();
    }

    public static Hotel hotel() {
        return Hotel.builder()
                .id(1)
                .nombre("Hotel Dan")
                .cuit("20-12345678-9")
                .domicilio("San Martin 123")
                .latitud(-32.95)
                .longitud(-60.66)
                .telefono("+543414444444")
                .correoContacto("hotel@dan.com")
                .categoria(4)
                .amenities(new ArrayList<>())
                .habitaciones(new ArrayList<>())
                .build();
    }

    public static Habitacion habitacion() {
        return Habitacion.builder()
                .id(1)
                .numero(101)
                .piso(1)
                .tipoHabitacion(tipoHabitacion())
                .hotel(hotel())
                .build();
    }

    public static AmenityHotel amenityHotel(Hotel hotel, Amenity amenity) {
        return AmenityHotel.builder().id(10L).hotel(hotel).amenity(amenity).build();
    }

    public static Tarifa tarifaVigente() {
        return Tarifa.builder()
                .id(1)
                .tipoHabitacion(tipoHabitacion())
                .fechaInicio(LocalDate.now().minusDays(5))
                .fechaFin(null)
                .precioNoche(50000.0)
                .build();
    }

    public static HabitacionDTORequest habitacionDTORequest() {
        return new HabitacionDTORequest(101, 1, 1, 1);
    }

    public static HabitacionDTOUpdate habitacionDTOUpdate() {
        return new HabitacionDTOUpdate(102, 2, 1);
    }

    public static HabitacionDTOResponse habitacionDTOResponse() {
        return new HabitacionDTOResponse(1, 101, 1, tipoHabitacionDTOResponse(), 1, "Hotel Dan");
    }

    public static HotelDTORequest hotelDTORequest() {
        return new HotelDTORequest("Hotel Dan", "20-12345678-9", "San Martin 123", -32.95, -60.66,
                "+543414444444", "hotel@dan.com", 4);
    }

    public static HotelDTOUpdate hotelDTOUpdate() {
        return new HotelDTOUpdate(5, "+543415555555", "nuevo@dan.com");
    }

    public static HotelDTOResponse hotelDTOResponse() {
        return new HotelDTOResponse(1, "Hotel Dan", "20-12345678-9", "San Martin 123", -32.95, -60.66,
                "+543414444444", "hotel@dan.com", 4, null, List.of());
    }

    public static TarifaDTORequest tarifaDTORequestNormal() {
        return new TarifaDTORequest(null, null, 1, 50000.0);
    }

    public static TarifaDTORequest tarifaDTORequestPromo() {
        return new TarifaDTORequest(LocalDate.now().plusDays(2), LocalDate.now().plusDays(5), 1, 40000.0);
    }

    public static TarifaDTOResponse tarifaDTOResponse() {
        return new TarifaDTOResponse(1, LocalDate.now(), null, tipoHabitacionDTOResponse(), 50000.0);
    }

    public static TipoHabitacionDTOResponse tipoHabitacionDTOResponse() {
        return new TipoHabitacionDTOResponse(1, "Suite", "Suite premium", 2);
    }

    public static TipoHabitacionDTORequest tipoHabitacionDTORequest() {
        return new TipoHabitacionDTORequest("Suite", "Suite premium", 2);
    }

    public static TipoHabitacionDTOUpdate tipoHabitacionDTOUpdate() {
        return new TipoHabitacionDTOUpdate("Suite Premium", "Suite de lujo", 3);
    }
}

