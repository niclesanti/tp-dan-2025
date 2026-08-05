package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.dto.*;
import edu.utn.frsf.isi.dan.reservas_svc.exception.EntityNotFoundException;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.ReservaMapper;
import edu.utn.frsf.isi.dan.reservas_svc.model.*;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.reservas_svc.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final ReservaMapper reservaMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public ReservaDTOResponse crearReserva(ReservaDTORequest request) {
        log.info("Creando reserva para habitación: {}", request.idHabitacion());

        // Validar que checkIn no sea anterior a hoy
        Instant inicioDeHoy = Instant.now().truncatedTo(ChronoUnit.DAYS);
        if (request.checkIn().isBefore(inicioDeHoy)) {
            throw new IllegalArgumentException("La fecha de check-in no puede ser anterior a hoy");
        }

        // Validar que checkOut > checkIn
        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new IllegalArgumentException("La fecha de check-out debe ser posterior a la fecha de check-in");
        }
        
        // Buscar la habitación en MongoDB
        var habitacion = habitacionRepository.findById(request.idHabitacion())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Habitación no encontrada con ID: " + request.idHabitacion()));
        
        // Validar disponibilidad
        validarDisponibilidad(habitacion, request.checkIn(), request.checkOut());
        
        // Calcular precio total
        long cantidadNoches = Duration.between(request.checkIn(), request.checkOut()).toDays();
        double precioTotal = habitacion.getPrecioNoche() * cantidadNoches;
        
        // Crear la reserva en estado RESERVADA según consigna
        var reserva = reservaMapper.toEntity(request);
        reserva.setCreatedAt(Instant.now());
        reserva.setPrecioNoche(habitacion.getPrecioNoche());
        reserva.setPrecioTotal(precioTotal);
        reserva.setHotelId(habitacion.getHotel().getId());
        reserva.setEstadoReserva(EstadoReserva.RESERVADA);
        reserva.setPagos(new ArrayList<>());
        
        var reservaGuardada = reservaRepository.save(reserva);
        log.info("Reserva creada exitosamente con ID: {} en estado RESERVADA", reservaGuardada.get_id());
        
        // Agregar a la lista de reservas de la habitación según consigna
        agregarReservaAHabitacion(habitacion, reservaGuardada);
        
        return reservaMapper.toResponse(reservaGuardada);
    }

    @Override
    public ReservaDTOResponse buscarReservaPorId(String id) {
        log.info("Buscando reserva con ID: {}", id);
        var reserva = buscarReservaOExcepcion(id);
        return reservaMapper.toResponse(reserva);
    }

    @Override
    public Page<ReservaDTOResponse> buscarReservasPorHuesped(String dni, Pageable pageable) {
        log.info("Buscando reservas para huésped con DNI: {}", dni);
        Query query = new Query(Criteria.where("huesped.dni").is(dni));
        query.with(pageable);
        
        long total = mongoTemplate.count(query, Reserva.class);
        List<Reserva> reservas = mongoTemplate.find(query, Reserva.class);
        
        return new org.springframework.data.domain.PageImpl<>(
                reservas.stream().map(reservaMapper::toResponse).toList(),
                pageable,
                total
        );
    }

    @Override
    public ReservaDTOResponse actualizarEstadoReserva(String id, EstadoReserva nuevoEstado) {
        log.info("Actualizando estado de reserva {} a {}", id, nuevoEstado);
        var reserva = buscarReservaOExcepcion(id);
        
        // Validar transición de estado
        validarTransicionEstado(reserva.getEstadoReserva(), nuevoEstado);
        
        // Validación especial para FINALIZADA según consigna
        if (nuevoEstado == EstadoReserva.FINALIZADA) {
            boolean tieneReviewHost = reserva.getHostReview() != null;
            double totalPagado = calcularTotalPagado(reserva);
            boolean pagoCompleto = totalPagado >= reserva.getPrecioTotal();
            
            // Si no tiene review del host O no tiene pago completo → ADEUDADA
            if (!tieneReviewHost || !pagoCompleto) {
                log.warn("Reserva {} no cumple requisitos para FINALIZADA. Review host: {}, Pago completo: {}",
                        id, tieneReviewHost, pagoCompleto);
                reserva.setEstadoReserva(EstadoReserva.ADEUDADA);
                var reservaActualizada = reservaRepository.save(reserva);
                log.info("Reserva marcada como ADEUDADA por incumplimiento de requisitos");
                return reservaMapper.toResponse(reservaActualizada);
            }
        }
        
        reserva.setEstadoReserva(nuevoEstado);
        var reservaActualizada = reservaRepository.save(reserva);
        
        log.info("Estado de reserva actualizado exitosamente a {}", nuevoEstado);
        return reservaMapper.toResponse(reservaActualizada);
    }

    @Override
    public ReservaDTOResponse realizarCheckIn(String id) {
        log.info("Realizando check-in para reserva: {}", id);
        var reserva = buscarReservaOExcepcion(id);
        
        // Validar que la reserva esté confirmada
        if (reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "Solo se puede hacer check-in de reservas CONFIRMADAS. Estado actual: " 
                    + reserva.getEstadoReserva());
        }
        
        // Validar que la fecha de check-in sea hoy o anterior
        Instant ahora = Instant.now();
        if (ahora.isBefore(reserva.getCheckIn())) {
            throw new IllegalStateException(
                    "No se puede hacer check-in antes de la fecha programada");
        }
        
        // Cambiar estado a EFECTUADA según consigna
        reserva.setEstadoReserva(EstadoReserva.EFECTUADA);
        var reservaActualizada = reservaRepository.save(reserva);
        
        log.info("Check-in realizado exitosamente. Reserva en estado EFECTUADA");
        return reservaMapper.toResponse(reservaActualizada);
    }

    @Override
    public ReservaDTOResponse agregarPago(String id, PagoDTORequest pagoRequest) {
        log.info("Agregando pago a reserva: {}", id);
        var reserva = buscarReservaOExcepcion(id);
        
        if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("No se puede agregar un pago a una reserva cancelada");
        }
        
        var pago = Pago.builder()
                .method(pagoRequest.method())
                .transactionId(pagoRequest.transactionId())
                .amount(Tarifa.builder()
                        .precio(pagoRequest.amount())
                        .moneda(pagoRequest.currency())
                        .build())
                .status("APPROVED")
                .nroTarjeta(pagoRequest.nroTarjeta())
                .build();
        
        if (reserva.getPagos() == null) {
            reserva.setPagos(new ArrayList<>());
        }
        reserva.getPagos().add(pago);
        
        // Cambiar a CONFIRMADA con al menos un pago según consigna
        if (reserva.getEstadoReserva() == EstadoReserva.RESERVADA && !reserva.getPagos().isEmpty()) {
            reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
            log.info("Reserva confirmada al tener al menos un pago");
        }
        
        var reservaActualizada = reservaRepository.save(reserva);
        log.info("Pago agregado exitosamente");
        
        return reservaMapper.toResponse(reservaActualizada);
    }

    @Override
    public ReservaDTOResponse agregarReview(String id, ReviewDTORequest reviewRequest, boolean esCliente) {
        log.info("Agregando review de {} a reserva: {}", esCliente ? "cliente" : "host", id);
        var reserva = buscarReservaOExcepcion(id);
        
        // Validar que la reserva esté finalizada
        if (reserva.getEstadoReserva() != EstadoReserva.FINALIZADA) {
            throw new IllegalStateException("Solo se pueden agregar reviews a reservas finalizadas");
        }
        
        // Validar que sea después de la fecha de checkout según consigna
        Instant ahora = Instant.now();
        if (ahora.isBefore(reserva.getCheckOut())) {
            throw new IllegalStateException(
                    "Solo se pueden agregar reviews después de la fecha de check-out");
        }
        
        var review = Review.builder()
                .rating(reviewRequest.rating())
                .comment(reviewRequest.comment())
                .createdAt(Instant.now().toString())
                .build();
        
        if (esCliente) {
            reserva.setClientReview(review);
        } else {
            reserva.setHostReview(review);
        }
        
        var reservaActualizada = reservaRepository.save(reserva);
        log.info("Review agregada exitosamente");
        
        return reservaMapper.toResponse(reservaActualizada);
    }

    @Override
    public void cancelarReserva(String id) {
        log.info("Cancelando reserva: {}", id);
        var reserva = buscarReservaOExcepcion(id);
        
        if (reserva.getEstadoReserva() == EstadoReserva.FINALIZADA) {
            throw new IllegalStateException("No se puede cancelar una reserva finalizada");
        }
        
        // Validar que no tenga pagos según consigna
        if (reserva.getPagos() != null && !reserva.getPagos().isEmpty()) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva que ya tiene pagos. Estado actual: " 
                    + reserva.getEstadoReserva());
        }
        
        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);
        
        // Eliminar de la lista de reservas de la habitación según consigna
        eliminarReservaDeHabitacion(reserva.getIdHabitacion(), id);
        
        log.info("Reserva cancelada y eliminada de la habitación exitosamente");
    }

    // ===== MÉTODOS PRIVADOS =====

    private Reserva buscarReservaOExcepcion(String id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> {
                    String msg = "Reserva no encontrada con ID: " + id;
                    log.error(msg);
                    return new EntityNotFoundException(msg);
                });
    }

    private void validarDisponibilidad(Habitacion habitacion, Instant checkIn, Instant checkOut) {
        // La reserva puede referenciar la habitación por su _id de MongoDB (flujo REST)
        // o por su habitacionId numérico como String (eventos de cierre de hotel).
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
        // Lógica simple de transiciones válidas
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
    
    private double calcularTotalPagado(Reserva reserva) {
        if (reserva.getPagos() == null || reserva.getPagos().isEmpty()) {
            return 0.0;
        }
        return reserva.getPagos().stream()
                .mapToDouble(p -> p.getAmount().getPrecio())
                .sum();
    }
}
