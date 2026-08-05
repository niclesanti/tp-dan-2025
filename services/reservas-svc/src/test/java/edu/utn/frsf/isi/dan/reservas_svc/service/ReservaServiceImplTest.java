package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.TestDataFactory;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.exception.EntityNotFoundException;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.ReservaMapper;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.reservas_svc.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private HabitacionRepository habitacionRepository;
    @Mock
    private ReservaMapper reservaMapper;
    @Mock
    private MongoTemplate mongoTemplate;
    @InjectMocks
    private ReservaServiceImpl reservaService;

    @Test
    void crearReservaShouldFailWhenCheckoutIsBeforeCheckin() {
        var req = new ReservaDTORequest("hab-1", Instant.now().plusSeconds(1000), Instant.now().plusSeconds(900), TestDataFactory.huespedDTORequest());
        assertThatThrownBy(() -> reservaService.crearReserva(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crearReservaShouldFailWhenRoomNotFound() {
        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reservaService.crearReserva(TestDataFactory.reservaDTORequest()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void crearReservaShouldFailWhenRoomIsNotAvailable() {
        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.of(TestDataFactory.habitacion()));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(true);
        assertThatThrownBy(() -> reservaService.crearReserva(TestDataFactory.reservaDTORequest()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void crearReservaShouldCreateInReservadaAndAddToRoomList() {
        var req = TestDataFactory.reservaDTORequest();
        var habitacion = TestDataFactory.habitacion();
        var entity = Reserva.builder().idHabitacion("hab-1").checkIn(req.checkIn()).checkOut(req.checkOut()).huesped(TestDataFactory.huesped()).build();
        var saved = Reserva.builder()
                ._id("r1")
                .idHabitacion("hab-1")
                .checkIn(req.checkIn())
                .checkOut(req.checkOut())
                .huesped(TestDataFactory.huesped())
                .estadoReserva(EstadoReserva.RESERVADA)
                .precioTotal(100.0)
                .build();

        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.of(habitacion), Optional.of(habitacion));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);
        when(reservaMapper.toEntity(req)).thenReturn(entity);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(saved);
        when(reservaMapper.toResponse(saved)).thenReturn(TestDataFactory.reservaDTOResponse());

        var response = reservaService.crearReserva(req);

        assertThat(response.getId()).isEqualTo("r1");
        verify(habitacionRepository).save(any(Habitacion.class));
    }

    @Test
    void crearReservaShouldInitializeRoomReservationListWhenNull() {
        var req = TestDataFactory.reservaDTORequest();
        var habitacion = TestDataFactory.habitacion();
        habitacion.setReservas(null);
        var entity = Reserva.builder().idHabitacion("hab-1").checkIn(req.checkIn()).checkOut(req.checkOut()).huesped(TestDataFactory.huesped()).build();
        var saved = Reserva.builder()._id("r2").idHabitacion("hab-1").checkIn(req.checkIn()).checkOut(req.checkOut()).huesped(TestDataFactory.huesped())
                .estadoReserva(EstadoReserva.RESERVADA).precioTotal(100.0).build();

        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.of(habitacion), Optional.of(habitacion));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);
        when(reservaMapper.toEntity(req)).thenReturn(entity);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(saved);
        when(reservaMapper.toResponse(saved)).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.crearReserva(req);

        assertThat(habitacion.getReservas()).isNotNull();
        verify(habitacionRepository).save(any(Habitacion.class));
    }

    @Test
    void actualizarEstadoFinalizadaShouldTurnAdeudadaWhenMissingRequirements() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        reserva.setHostReview(null);
        reserva.setPagos(new ArrayList<>());
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.actualizarEstadoReserva("r1", EstadoReserva.FINALIZADA);

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.ADEUDADA);
    }

    @Test
    void actualizarEstadoFinalizadaShouldTurnAdeudadaWhenOnlyPaymentIsMissing() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        reserva.setHostReview(TestDataFactory.review());
        reserva.setPagos(new ArrayList<>());
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.actualizarEstadoReserva("r1", EstadoReserva.FINALIZADA);

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.ADEUDADA);
    }

    @Test
    void actualizarEstadoShouldThrowWhenCurrentIsCancelada() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.actualizarEstadoReserva("r1", EstadoReserva.CONFIRMADA))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void actualizarEstadoShouldThrowWhenCurrentIsFinalizadaAndNewIsDifferent() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.actualizarEstadoReserva("r1", EstadoReserva.CONFIRMADA))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void actualizarEstadoShouldAllowFinalizadaToFinalizada() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
        reserva.setHostReview(TestDataFactory.review());
        reserva.setPagos(List.of(TestDataFactory.pago(), TestDataFactory.pago()));
        reserva.setPrecioTotal(100.0);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.actualizarEstadoReserva("r1", EstadoReserva.FINALIZADA);

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.FINALIZADA);
    }

    @Test
    void actualizarEstadoFinalizadaShouldRemainFinalizadaWhenRequirementsMet() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        reserva.setHostReview(TestDataFactory.review());
        reserva.setPagos(List.of(TestDataFactory.pago(), TestDataFactory.pago()));
        reserva.setPrecioTotal(100.0);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.actualizarEstadoReserva("r1", EstadoReserva.FINALIZADA);

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.FINALIZADA);
    }

    @Test
    void realizarCheckInShouldFailWhenNotConfirmed() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.RESERVADA);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        assertThatThrownBy(() -> reservaService.realizarCheckIn("r1")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void realizarCheckInShouldFailWhenDateIsBeforeCheckIn() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        reserva.setCheckIn(Instant.now().plusSeconds(7200));
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.realizarCheckIn("r1")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void realizarCheckInShouldSetEfectuada() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        reserva.setCheckIn(Instant.now().minusSeconds(3600));
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.realizarCheckIn("r1");

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.EFECTUADA);
    }

    @Test
    void agregarPagoShouldConfirmReservaWhenWasReservada() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.RESERVADA);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.agregarPago("r1", TestDataFactory.pagoDTORequest());

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(reserva.getPagos()).hasSize(1);
        assertThat(reserva.getPagos().get(0).getNroTarjeta()).isEqualTo("1234567812345678");
    }

    @Test
    void agregarPagoShouldFailWhenCancelled() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.agregarPago("r1", TestDataFactory.pagoDTORequest()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void agregarPagoShouldInitializeListWhenNull() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        reserva.setPagos(null);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.agregarPago("r1", TestDataFactory.pagoDTORequest());

        assertThat(reserva.getPagos()).hasSize(1);
        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.CONFIRMADA);
    }

    @Test
    void agregarReviewShouldFailWhenReservationIsNotFinalizada() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.EFECTUADA);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        assertThatThrownBy(() -> reservaService.agregarReview("r1", TestDataFactory.reviewDTORequest(), true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void agregarReviewShouldFailWhenCheckoutNotReached() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
        reserva.setCheckOut(Instant.now().plusSeconds(3600));
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.agregarReview("r1", TestDataFactory.reviewDTORequest(), true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void agregarReviewShouldSetHostReviewWhenEsClienteFalse() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
        reserva.setCheckOut(Instant.now().minusSeconds(3600));
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.agregarReview("r1", TestDataFactory.reviewDTORequest(), false);

        assertThat(reserva.getHostReview()).isNotNull();
    }

    @Test
    void agregarReviewShouldSetClientReviewWhenEsClienteTrue() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
        reserva.setCheckOut(Instant.now().minusSeconds(3600));
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());

        reservaService.agregarReview("r1", TestDataFactory.reviewDTORequest(), true);

        assertThat(reserva.getClientReview()).isNotNull();
    }

    @Test
    void cancelarReservaShouldFailWhenHasPayments() {
        var reserva = TestDataFactory.reserva();
        reserva.setPagos(List.of(TestDataFactory.pago()));
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        assertThatThrownBy(() -> reservaService.cancelarReserva("r1")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelarReservaShouldFailWhenFinalizada() {
        var reserva = TestDataFactory.reserva();
        reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        assertThatThrownBy(() -> reservaService.cancelarReserva("r1")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelarReservaShouldSetCancelledAndRemoveFromRoom() {
        var reserva = TestDataFactory.reserva();
        reserva.setPagos(new ArrayList<>());
        var habitacion = TestDataFactory.habitacion();
        habitacion.getReservas().add(Habitacion.ReservaSimple.builder()._id("r1").estadoReserva(EstadoReserva.RESERVADA).build());
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.of(habitacion));

        reservaService.cancelarReserva("r1");

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.CANCELADA);
        verify(habitacionRepository).save(any(Habitacion.class));
    }

    @Test
    void cancelarReservaShouldWorkWhenPaymentsAreNull() {
        var reserva = TestDataFactory.reserva();
        reserva.setPagos(null);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.empty());

        reservaService.cancelarReserva("r1");

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void cancelarReservaShouldWorkWhenRoomDoesNotExist() {
        var reserva = TestDataFactory.reserva();
        reserva.setPagos(new ArrayList<>());
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.empty());

        reservaService.cancelarReserva("r1");

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void cancelarReservaShouldWorkWhenRoomHasNullReservations() {
        var reserva = TestDataFactory.reserva();
        reserva.setPagos(new ArrayList<>());
        var habitacion = TestDataFactory.habitacion();
        habitacion.setReservas(null);
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.of(habitacion));

        reservaService.cancelarReserva("r1");

        assertThat(reserva.getEstadoReserva()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void buscarReservasPorHuespedShouldReturnPage() {
        when(mongoTemplate.count(any(), eq(Reserva.class))).thenReturn(1L);
        when(mongoTemplate.find(any(), any())).thenReturn(List.of(TestDataFactory.reserva()));
        when(reservaMapper.toResponse(any())).thenReturn(TestDataFactory.reservaDTOResponse());
        var page = reservaService.buscarReservasPorHuesped("12345678", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);

        var queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(Reserva.class));
        assertThat(queryCaptor.getValue().getQueryObject().get("huesped.dni")).isEqualTo("12345678");
    }

    @Test
    void buscarReservaPorIdShouldThrowWhenMissing() {
        when(reservaRepository.findById("no")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reservaService.buscarReservaPorId("no")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void buscarReservaPorIdShouldReturnMapped() {
        var reserva = TestDataFactory.reserva();
        when(reservaRepository.findById("r1")).thenReturn(Optional.of(reserva));
        when(reservaMapper.toResponse(reserva)).thenReturn(TestDataFactory.reservaDTOResponse());
        assertThat(reservaService.buscarReservaPorId("r1").getId()).isEqualTo("r1");
    }
}

