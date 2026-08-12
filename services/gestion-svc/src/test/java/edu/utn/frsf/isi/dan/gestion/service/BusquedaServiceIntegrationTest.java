package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.HabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.dao.TarifaRepository;
import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.messaging.GestionMessagePublisher;
import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.model.AmenityHotel;
import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:busqueda_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS tp_dan",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=tp_dan",
        "spring.flyway.enabled=false",
        "spring.liquibase.enabled=false"
})
@Transactional
class BusquedaServiceIntegrationTest {

    @Autowired
    private HotelService hotelService;
    @Autowired
    private HabitacionService habitacionService;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private HabitacionRepository habitacionRepository;
    @Autowired
    private TipoHabitacionRepository tipoHabitacionRepository;
    @Autowired
    private TarifaRepository tarifaRepository;

    @MockBean
    private GestionMessagePublisher messagePublisher;

    private Integer hotelId;
    private Integer tipoId;
    private Integer habitacionId;

    @BeforeEach
    void setup() {
        tarifaRepository.deleteAll();
        habitacionRepository.deleteAll();
        hotelRepository.deleteAll();
        tipoHabitacionRepository.deleteAll();

        var tipo = TipoHabitacion.builder()
                .nombre("Suite")
                .descripcion("Suite premium")
                .capacidad(3)
                .build();
        tipoHabitacionRepository.save(tipo);
        tipoId = tipo.getId();

        var hotel = Hotel.builder()
                .nombre("Hotel Dan Centro")
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
        hotel = hotelRepository.save(hotel);
        hotelId = hotel.getId();

        hotel.getAmenities().add(AmenityHotel.builder().hotel(hotel).amenity(Amenity.WIFI).build());
        hotelRepository.save(hotel);

        var habitacion = Habitacion.builder()
                .numero(101)
                .piso(1)
                .hotel(hotel)
                .tipoHabitacion(tipo)
                .build();
        habitacion = habitacionRepository.save(habitacion);
        habitacionId = habitacion.getId();

        tarifaRepository.save(Tarifa.builder()
                .tipoHabitacion(tipo)
                .fechaInicio(LocalDate.now().minusDays(5))
                .fechaFin(null)
                .precioNoche(200.0)
                .build());
    }

    @Test
    void buscarHotelesDebeAplicarTodasLasRamasDeFiltros() {
        var pageable = PageRequest.of(0, 10);
        assertThat(hotelService.buscarHoteles(null, null, null, null, pageable).getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(hotelService.buscarHoteles("dan", null, null, null, pageable).getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(hotelService.buscarHoteles("dan", 4, "martin", Amenity.WIFI, pageable).getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(hotelService.buscarHoteles("zzz", null, null, null, pageable).getTotalElements()).isEqualTo(0);
    }

    @Test
    void buscarHabitacionesDebeAplicarRamasCapacidadTipoYPrecio() {
        var pageable = PageRequest.of(0, 10);
        assertThat(habitacionService.buscarHabitaciones(null, null, null, null, pageable).getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(habitacionService.buscarHabitaciones(2, null, null, null, pageable).getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(habitacionService.buscarHabitaciones(0, null, null, null, pageable).getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(habitacionService.buscarHabitaciones(null, tipoId, 100.0, null, pageable).getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(habitacionService.buscarHabitaciones(null, tipoId, null, 300.0, pageable).getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(habitacionService.buscarHabitaciones(null, tipoId, 100.0, 300.0, pageable).getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(habitacionService.buscarHabitaciones(null, tipoId, 1000.0, 1200.0, pageable).getTotalElements()).isEqualTo(0);
    }

    @Test
    void crearYActualizarHabitacionDebeEjecutarPublicacionConRamasDePrecioYAmenities() {
        var createReq = new edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTORequest(202, 2, tipoId, hotelId);
        var created = habitacionService.crearHabitacion(createReq);
        assertThat(created.id()).isNotNull();

        var updateReq = new edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOUpdate(203, 2, tipoId);
        var updated = habitacionService.actualizarHabitacion(created.id(), updateReq);
        assertThat(updated.numero()).isEqualTo(203);

        assertThat(habitacionService.buscarHabitacionPorId(habitacionId).id()).isEqualTo(habitacionId);
    }
}

