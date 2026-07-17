package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.TestDataFactory;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDisponibleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import edu.utn.frsf.isi.dan.shared.TarifaDTO;
import edu.utn.frsf.isi.dan.shared.TipoEvento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitacionServiceImplTest {

    @Mock
    private HabitacionRepository habitacionRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @InjectMocks
    private HabitacionServiceImpl habitacionService;

    @Test
    void handleEventCrearShouldSave() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.CREAR)
                .habitacion(HabitacionDTO.builder()
                        .habitacionId(101)
                        .capacidad(2)
                        .precioNoche(100.0)
                        .hotel(HotelDTO.builder().id(1).latitud(-32.95).longitud(-60.66).build())
                        .build())
                .build();

        when(habitacionRepository.save(any(Habitacion.class))).thenAnswer(i -> i.getArgument(0));
        habitacionService.handleEvent(event);
        verify(habitacionRepository).save(any(Habitacion.class));
    }

    @Test
    void handleEventActualizarDatosShouldThrowWhenNotFound() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ACTUALIZAR_DATOS)
                .habitacion(HabitacionDTO.builder().habitacionId(999).capacidad(2).precioNoche(100.0).build())
                .build();
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(Habitacion.class))).thenReturn(null);
        assertThatThrownBy(() -> habitacionService.handleEvent(event)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handleEventActualizarDatosShouldUpdateWhenFound() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ACTUALIZAR_DATOS)
                .habitacion(HabitacionDTO.builder().habitacionId(101).capacidad(3).precioNoche(150.0).build())
                .build();
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(Habitacion.class))).thenReturn(TestDataFactory.habitacion());
        habitacionService.handleEvent(event);
        verify(mongoTemplate).findAndModify(any(), any(), any(), eq(Habitacion.class));
    }

    @Test
    void handleEventActualizarPrecioShouldUpdateMany() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ACTUALIZAR_PRECIO)
                .tarifa(TarifaDTO.builder().tipoHabitacionId(1).nuevoPrecio(200.0).build())
                .build();
        when(mongoTemplate.updateMulti(any(), any(), eq(Habitacion.class)))
                .thenReturn(null);
        habitacionService.handleEvent(event);
        verify(mongoTemplate).updateMulti(any(), any(), eq(Habitacion.class));
    }

    @Test
    void handleEventEliminarShouldRemove() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ELIMINAR)
                .habitacion(HabitacionDTO.builder().habitacionId(101).build())
                .build();
        habitacionService.handleEvent(event);
        verify(mongoTemplate).remove(any(), eq(Habitacion.class));
    }

    @Test
    void handleEventCerrarHotelShouldThrowIllegalArgument() {
        var event = HabitacionEvent.builder().tipoEvento(TipoEvento.CERRAR_HOTEL).build();
        assertThatThrownBy(() -> habitacionService.handleEvent(event)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handleEventCrearShouldAllowNullHotelInPayload() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.CREAR)
                .habitacion(HabitacionDTO.builder().habitacionId(202).capacidad(2).precioNoche(90.0).hotel(null).build())
                .build();
        when(habitacionRepository.save(any(Habitacion.class))).thenAnswer(i -> i.getArgument(0));
        habitacionService.handleEvent(event);
        verify(habitacionRepository).save(any(Habitacion.class));
    }

    @Test
    void buscarDisponiblesShouldReturnPage() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                1, 50.0, 200.0, 4, List.of("WIFI"), null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1).extracting(HabitacionDisponibleDTO::getId).contains("hab-1");
    }

    @Test
    void buscarDisponiblesShouldSupportGeoFilterAndConflict() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(true);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                null, null, null, null, null, -32.95, -60.66, 3.0, PageRequest.of(0, 10));
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void buscarDisponiblesShouldWorkWithoutAnyOptionalFilter() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                null, null, null, null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void buscarDisponiblesShouldHandleIndividualPriceFiltersAndEmptyAmenities() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var in = Instant.now().plusSeconds(86400);
        var out = Instant.now().plusSeconds(172800);
        assertThat(habitacionService.buscarDisponibles(in, out, null, 80.0, null, null, List.of(), null, null, null, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
        assertThat(habitacionService.buscarDisponibles(in, out, null, null, 120.0, null, List.of(), null, null, null, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }

    @Test
    void buscarDisponiblesShouldIgnoreCapacityWhenZero() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                0, null, null, null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void buscarDisponiblesShouldIgnoreIncompleteGeoFilter() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);
        var in = Instant.now().plusSeconds(86400);
        var out = Instant.now().plusSeconds(172800);

        assertThat(habitacionService.buscarDisponibles(in, out, null, null, null, null, null, -32.95, null, 3.0, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
        assertThat(habitacionService.buscarDisponibles(in, out, null, null, null, null, null, -32.95, -60.66, null, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }

    @Test
    void simpleCrudMethodsShouldDelegate() {
        var hab = TestDataFactory.habitacion();
        when(habitacionRepository.findAll()).thenReturn(List.of(hab));
        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.of(hab));
        when(habitacionRepository.save(hab)).thenReturn(hab);

        assertThat(habitacionService.findAll()).hasSize(1);
        assertThat(habitacionService.findById("hab-1")).contains(hab);
        assertThat(habitacionService.save(hab)).isEqualTo(hab);
        habitacionService.deleteById("hab-1");
        verify(habitacionRepository).deleteById("hab-1");
    }

}

