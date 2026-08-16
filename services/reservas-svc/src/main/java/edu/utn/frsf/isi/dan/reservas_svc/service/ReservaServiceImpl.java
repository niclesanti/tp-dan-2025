package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.dto.PagoDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReviewDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.exception.EntityNotFoundException;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.PagoMapper;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.ReservaMapper;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.ReviewMapper;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.reservas_svc.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final ReservaMapper reservaMapper;
    private final MongoTemplate mongoTemplate;
    private final PagoMapper pagoMapper;
    private final ReviewMapper reviewMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ReservaDTOResponse crearReserva(ReservaDTORequest request) {
        log.info("Creando reserva para habitación: {}", request.idHabitacion());

        Instant inicioDeHoy = Instant.now().truncatedTo(ChronoUnit.DAYS);
        if (request.checkIn().isBefore(inicioDeHoy)) {
            throw new IllegalArgumentException("La fecha de check-in no puede ser anterior a hoy");
        }

        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new IllegalArgumentException("La fecha de check-out debe ser posterior a la fecha de check-in");
        }

        var habitacion = habitacionRepository.findById(request.idHabitacion())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Habitación no encontrada con ID: " + request.idHabitacion()));

        validarDisponibilidad(habitacion, request.checkIn(), request.checkOut());

        long cantidadNoches = Duration.between(request.checkIn(), request.checkOut()).toDays();
        double precioTotal = habitacion.getPrecioNoche() * cantidadNoches;

        var reserva = reservaMapper.toEntity(request);
        reserva.setCreatedAt(Instant.now());
        reserva.setPrecioNoche(habitacion.getPrecioNoche());
        reserva.setPrecioTotal(precioTotal);
        reserva.setHotelId(habitacion.getHotel().getId());
        reserva.setEstadoReserva(EstadoReserva.RESERVADA);
        reserva.setPagos(new ArrayList<>());

        var reservaGuardada = reservaRepository.save(reserva);
        log.info("Reserva creada exitosamente con ID: {} en estado RESERVADA", reservaGuardada.get_id());

        agregarReservaAHabitacion(habitacion, reservaGuardada);

        return reservaMapper.toResponse(reservaGuardada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReservaDTOResponse buscarReservaPorId(String id) {
        log.info("Buscando reserva con ID: {}", id);
        var reserva = buscarReservaOExcepcion(id);
        return reservaMapper.toResponse(reserva);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ReservaDTOResponse> buscarReservasPorHuesped(String dni, Pageable pageable) {
        log.info("Buscando reservas para huésped con DNI: {}", dni);
        Query query = new Query(Criteria.where("huesped.dni").is(dni));
        query.with(pageable);

        long total = mongoTemplate.count(query, Reserva.class);
        List<Reserva> reservas = mongoTemplate.find(query, Reserva.class);

        return new PageImpl<>(
                reservas.stream().map(reservaMapper::toResponse).toList(),
                pageable,
                total
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReservaDTOResponse actualizarEstadoReserva(String id, EstadoReserva nuevoEstado) {
        log.info("Actualizando estado de reserva {} a {}", id, nuevoEstado);
        var reserva = buscarReservaOExcepcion(id);

        validarTransicionEstado(reserva.getEstadoReserva(), nuevoEstado);

        if (nuevoEstado == EstadoReserva.FINALIZADA) {
            var estadoEvaluado = evaluarEstadoFinal(reserva);
            if (estadoEvaluado != EstadoReserva.FINALIZADA) {
                reserva.setEstadoReserva(estadoEvaluado);
                var reservaActualizada = reservaRepository.save(reserva);
                log.info("Reserva marcada como {} por incumplimiento de requisitos para FINALIZADA", estadoEvaluado);
                return reservaMapper.toResponse(reservaActualizada);
            }
        }

        reserva.setEstadoReserva(nuevoEstado);
        var reservaActualizada = reservaRepository.save(reserva);

        log.info("Estado de reserva actualizado exitosamente a {}", nuevoEstado);
        return reservaMapper.toResponse(reservaActualizada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReservaDTOResponse realizarCheckIn(String id) {
        log.info("Realizando check-in para reserva: {}", id);
        var reserva = buscarReservaOExcepcion(id);

        if (reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "Solo se puede hacer check-in de reservas CONFIRMADAS. Estado actual: "
                    + reserva.getEstadoReserva());
        }

        Instant ahora = Instant.now();
        if (ahora.isBefore(reserva.getCheckIn())) {
            throw new IllegalStateException(
                    "No se puede hacer check-in antes de la fecha programada");
        }

        reserva.setEstadoReserva(EstadoReserva.EFECTUADA);
        var reservaActualizada = reservaRepository.save(reserva);

        log.info("Check-in realizado exitosamente. Reserva en estado EFECTUADA");
        return reservaMapper.toResponse(reservaActualizada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReservaDTOResponse realizarCheckOut(String id) {
        log.info("Realizando check-out para reserva: {}", id);
        var reserva = buscarReservaOExcepcion(id);

        if (reserva.getEstadoReserva() != EstadoReserva.EFECTUADA) {
            throw new IllegalStateException(
                    "Solo se puede hacer check-out de reservas EFECTUADAS. Estado actual: "
                    + reserva.getEstadoReserva());
        }

        reserva.setEstadoReserva(evaluarEstadoFinal(reserva));
        var reservaActualizada = reservaRepository.save(reserva);

        sincronizarEstadoReservaEnHabitacion(reserva);

        log.info("Check-out realizado exitosamente. Reserva en estado {}", reservaActualizada.getEstadoReserva());
        return reservaMapper.toResponse(reservaActualizada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReservaDTOResponse agregarPago(String id, PagoDTORequest pagoRequest) {
        log.info("Agregando pago a reserva: {}", id);
        var reserva = buscarReservaOExcepcion(id);

        if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("No se puede agregar un pago a una reserva cancelada");
        }

        String transactionId = pagoRequest.transactionId();
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "PAY-" + UUID.randomUUID();
            log.info("TransactionId no provisto, generado automáticamente: {}", transactionId);
        }

        var pago = pagoMapper.toEntity(pagoRequest);
        pago.setTransactionId(transactionId);

        if (reserva.getPagos() == null) {
            reserva.setPagos(new ArrayList<>());
        }
        reserva.getPagos().add(pago);

        if (reserva.getEstadoReserva() == EstadoReserva.RESERVADA && !reserva.getPagos().isEmpty()) {
            reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
            log.info("Reserva confirmada al tener al menos un pago");
        }

        if (reserva.getEstadoReserva() == EstadoReserva.ADEUDADA
                && evaluarEstadoFinal(reserva) == EstadoReserva.FINALIZADA) {
            reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
            log.info("Reserva {} pasó de ADEUDADA a FINALIZADA tras completarse el pago", id);
        }

        var reservaActualizada = reservaRepository.save(reserva);
        log.info("Pago agregado exitosamente");

        return reservaMapper.toResponse(reservaActualizada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReservaDTOResponse agregarReview(String id, ReviewDTORequest reviewRequest, boolean esCliente) {
        log.info("Agregando review de {} a reserva: {}", esCliente ? "cliente" : "host", id);
        var reserva = buscarReservaOExcepcion(id);

        var estadoActual = reserva.getEstadoReserva();
        if (estadoActual != EstadoReserva.EFECTUADA
                && estadoActual != EstadoReserva.ADEUDADA
                && estadoActual != EstadoReserva.FINALIZADA) {
            throw new IllegalStateException(
                    "Solo se pueden agregar reviews a reservas EFECTUADAS, ADEUDADAS o FINALIZADAS. Estado actual: "
                    + estadoActual);
        }

        var review = reviewMapper.toEntity(reviewRequest);

        if (esCliente) {
            reserva.setClientReview(review);
        } else {
            reserva.setHostReview(review);
        }

        if (reserva.getEstadoReserva() == EstadoReserva.ADEUDADA
                && evaluarEstadoFinal(reserva) == EstadoReserva.FINALIZADA) {
            reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
            log.info("Reserva {} pasó de ADEUDADA a FINALIZADA tras agregarse la review", id);
        }

        var reservaActualizada = reservaRepository.save(reserva);
        log.info("Review agregada exitosamente");

        return reservaMapper.toResponse(reservaActualizada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelarReserva(String id) {
        log.info("Cancelando reserva: {}", id);
        var reserva = buscarReservaOExcepcion(id);

        if (reserva.getEstadoReserva() == EstadoReserva.FINALIZADA) {
            throw new IllegalStateException("No se puede cancelar una reserva finalizada");
        }

        if (reserva.getPagos() != null && !reserva.getPagos().isEmpty()) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva que ya tiene pagos. Estado actual: "
                    + reserva.getEstadoReserva());
        }

        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);

        eliminarReservaDeHabitacion(reserva.getIdHabitacion(), id);

        log.info("Reserva cancelada y eliminada de la habitación exitosamente");
    }

    // ===== MÉTODOS AUXILIARES PRIVADOS =====

    private Reserva buscarReservaOExcepcion(String id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> {
                    String msg = "Reserva no encontrada con ID: " + id;
                    log.error(msg);
                    return new EntityNotFoundException(msg);
                });
    }

    private void validarDisponibilidad(Habitacion habitacion, Instant checkIn, Instant checkOut) {
        Query query = new Query();
        query.addCriteria(Criteria.where("idHabitacion").in(habitacion.getId(), String.valueOf(habitacion.getHabitacionId()))
                .and("estadoReserva").in(
                        EstadoReserva.RESERVADA,
                        EstadoReserva.CONFIRMADA,
                        EstadoReserva.EFECTUADA,
                        EstadoReserva.BLOQUEADA,
                        EstadoReserva.CERRADA)
                .andOperator(
                        new Criteria().orOperator(
                                Criteria.where("checkOut").is(null),
                                Criteria.where("checkIn").lt(checkOut)
                                        .and("checkOut").gt(checkIn)
                        )
                ));

        boolean hayConflicto = mongoTemplate.exists(query, Reserva.class);
        if (hayConflicto) {
            throw new IllegalStateException(
                    "La habitación no está disponible en las fechas seleccionadas");
        }
    }

    private void validarTransicionEstado(EstadoReserva estadoActual, EstadoReserva estadoNuevo) {
        if (estadoActual == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("No se puede cambiar el estado de una reserva cancelada");
        }

        if (estadoActual == EstadoReserva.FINALIZADA && estadoNuevo != EstadoReserva.FINALIZADA) {
            throw new IllegalStateException("No se puede cambiar el estado de una reserva finalizada");
        }
    }

    private void agregarReservaAHabitacion(Habitacion habitacion, Reserva reserva) {
        log.debug("Agregando reserva {} a habitación {}", reserva.get_id(), habitacion.getId());

        if (habitacion.getReservas() == null) {
            habitacion.setReservas(new ArrayList<>());
        }

        var reservaSimple = Habitacion.ReservaSimple.builder()
                ._id(reserva.get_id())
                .checkIn(reserva.getCheckIn())
                .checkOut(reserva.getCheckOut())
                .precioTotal(reserva.getPrecioTotal())
                .estadoReserva(reserva.getEstadoReserva())
                .build();

        habitacion.getReservas().add(reservaSimple);
        habitacionRepository.save(habitacion);

        log.info("Reserva agregada a la lista de reservas de la habitación");
    }

    private void eliminarReservaDeHabitacion(String idHabitacion, String idReserva) {
        log.debug("Eliminando reserva {} de habitación {}", idReserva, idHabitacion);

        var habitacion = habitacionRepository.findById(idHabitacion);
        if (habitacion.isPresent()) {
            var hab = habitacion.get();
            if (hab.getReservas() != null) {
                hab.getReservas().removeIf(r -> r.get_id().equals(idReserva));
                habitacionRepository.save(hab);
                log.info("Reserva eliminada de la lista de reservas de la habitación");
            }
        }
    }

    private EstadoReserva evaluarEstadoFinal(Reserva reserva) {
        boolean tieneReviewHost = reserva.getHostReview() != null;
        double totalPagado = calcularTotalPagado(reserva);
        boolean pagoCompleto = totalPagado >= reserva.getPrecioTotal();

        if (tieneReviewHost && pagoCompleto) {
            return EstadoReserva.FINALIZADA;
        }
        log.warn("Reserva {} no cumple requisitos para FINALIZADA. Review host: {}, Pago completo: {}",
                reserva.get_id(), tieneReviewHost, pagoCompleto);
        return EstadoReserva.ADEUDADA;
    }

    private void sincronizarEstadoReservaEnHabitacion(Reserva reserva) {
        log.debug("Sincronizando estado de reserva {} en habitación {}", reserva.get_id(), reserva.getIdHabitacion());

        var habitacionOpt = habitacionRepository.findById(reserva.getIdHabitacion());
        if (habitacionOpt.isPresent()) {
            var habitacion = habitacionOpt.get();
            if (habitacion.getReservas() != null) {
                habitacion.getReservas().forEach(r -> {
                    if (r.get_id().equals(reserva.get_id())) {
                        r.setEstadoReserva(reserva.getEstadoReserva());
                    }
                });
                habitacionRepository.save(habitacion);
                log.info("Estado de reserva sincronizado en la lista de reservas de la habitación");
            }
        }
    }

    private double calcularTotalPagado(Reserva reserva) {
        if (reserva.getPagos() == null || reserva.getPagos().isEmpty()) {
            return 0.0;
        }
        return reserva.getPagos().stream()
                .mapToDouble(p -> p.getAmount().getPrecio())
                .sum();
    }
}
