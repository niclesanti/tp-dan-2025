package edu.utn.frsf.isi.dan.reservas_svc.controller;

import edu.utn.frsf.isi.dan.reservas_svc.dto.*;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reserva Controller", description = "Operaciones para la gestión del ciclo de vida de reservas")
@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
@Slf4j
public class ReservaController {
    
    private final ReservaService reservaService;

    @Operation(summary = "Crear reserva",
               description = "Crea una nueva reserva para una habitación en fechas específicas. La reserva se crea en estado REALIZADA. " +
                             "Se valida que la habitación esté disponible en las fechas solicitadas y se agrega a la lista de reservas de la habitación.",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error de validación (fechas inválidas, habitación no disponible)"),
                   @ApiResponse(responseCode = "404", description = "Habitación no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping
    public ResponseEntity<ReservaDTOResponse> crear(@Valid @RequestBody ReservaDTORequest request) {
        log.info("POST /reservas - Crear nueva reserva");
        var reserva = reservaService.crearReserva(request);
        return new ResponseEntity<>(reserva, HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar reserva por ID",
               description = "Retorna los datos completos de una reserva específica incluyendo habitación, huésped, pagos y reviews",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
                   @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTOResponse> buscarPorId(@PathVariable String id) {
        log.info("GET /reservas/{} - Buscar por ID", id);
        var reserva = reservaService.buscarReservaPorId(id);
        return ResponseEntity.ok(reserva);
    }

    @Operation(summary = "Buscar reservas por huésped",
               description = "Retorna todas las reservas de un huésped específico con paginación. Incluye reservas en todos los estados.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error en los parámetros"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/huesped/{huespedId}")
    public ResponseEntity<Page<ReservaDTOResponse>> buscarPorHuesped(
            @PathVariable String huespedId,
            @ParameterObject Pageable pageable) {
        log.info("GET /reservas/huesped/{} - Buscar por huésped", huespedId);
        var reservas = reservaService.buscarReservasPorHuesped(huespedId, pageable);
        return ResponseEntity.ok(reservas);
    }

    @Operation(summary = "Actualizar estado de reserva",
               description = "Cambia el estado de una reserva. Estados válidos: REALIZADA, CONFIRMADA (requiere pago >= 50%), " +
                             "EFECTUADA (cliente ya ingresó al hotel), FINALIZADA (requiere review y pago completo), " +
                             "ADEUDADA (finalizada sin pago completo), CANCELADA (solo si no tiene pagos), BLOQUEADA, CERRADA. " +
                             "Las transiciones de estado se validan según las reglas de negocio.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Transición de estado inválida o condiciones no cumplidas"),
                   @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ReservaDTOResponse> actualizarEstado(
            @PathVariable String id,
            @RequestParam EstadoReserva estado) {
        log.info("PATCH /reservas/{}/estado - Actualizar estado a {}", id, estado);
        var reserva = reservaService.actualizarEstadoReserva(id, estado);
        return ResponseEntity.ok(reserva);
    }

    @Operation(summary = "Realizar check-in",
               description = "Registra el ingreso del cliente al hotel. La reserva debe estar en estado CONFIRMADA y se actualiza a EFECTUADA. " +
                             "Se valida que el check-in se realice en la fecha programada.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Check-in realizado exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Reserva no está en estado válido para check-in o fecha incorrecta"),
                   @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping("/{id}/check-in")
    public ResponseEntity<ReservaDTOResponse> realizarCheckIn(@PathVariable String id) {
        log.info("POST /reservas/{}/check-in - Realizar check-in (cliente ingresa al hotel)", id);
        var reserva = reservaService.realizarCheckIn(id);
        return ResponseEntity.ok(reserva);
    }

    @Operation(summary = "Agregar pago a reserva",
               description = "Registra un pago para la reserva. Si el total pagado alcanza el 50% o más del precio total, " +
                             "la reserva pasa automáticamente a estado CONFIRMADA. Si el pago completa el 100%, queda lista para finalizar.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Pago registrado exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error de validación (monto inválido, pago excede el total)"),
                   @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping("/{id}/pagos")
    public ResponseEntity<ReservaDTOResponse> agregarPago(
            @PathVariable String id,
            @Valid @RequestBody PagoDTORequest pagoRequest) {
        log.info("POST /reservas/{}/pagos - Agregar pago", id);
        var reserva = reservaService.agregarPago(id, pagoRequest);
        return ResponseEntity.ok(reserva);
    }

    @Operation(summary = "Agregar review del cliente",
               description = "Permite al cliente (huésped) dejar una calificación y comentario sobre la habitación y el hotel. " +
                             "Solo se puede hacer después de la fecha de check-out. El review es obligatorio para finalizar la reserva.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Review agregado exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error de validación (calificación fuera de rango, fecha de checkout no alcanzada)"),
                   @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping("/{id}/reviews/cliente")
    public ResponseEntity<ReservaDTOResponse> agregarReviewCliente(
            @PathVariable String id,
            @Valid @RequestBody ReviewDTORequest reviewRequest) {
        log.info("POST /reservas/{}/reviews/cliente - Agregar review de cliente", id);
        var reserva = reservaService.agregarReview(id, reviewRequest, true);
        return ResponseEntity.ok(reserva);
    }

    @Operation(summary = "Agregar review del host",
               description = "Permite al dueño del hotel dejar una calificación y comentario sobre el huésped. " +
                             "Solo se puede hacer después de la fecha de check-out.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Review agregado exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error de validación (calificación fuera de rango, fecha de checkout no alcanzada)"),
                   @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping("/{id}/reviews/host")
    public ResponseEntity<ReservaDTOResponse> agregarReviewHost(
            @PathVariable String id,
            @Valid @RequestBody ReviewDTORequest reviewRequest) {
        log.info("POST /reservas/{}/reviews/host - Agregar review de host", id);
        var reserva = reservaService.agregarReview(id, reviewRequest, false);
        return ResponseEntity.ok(reserva);
    }

    @Operation(summary = "Cancelar reserva",
               description = "Cancela una reserva existente. Solo se puede cancelar si la reserva no tiene pagos registrados. " +
                             "Al cancelarse, la reserva se elimina de la lista de reservas de la habitación, liberándola para otras reservas.",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Reserva cancelada exitosamente"),
                   @ApiResponse(responseCode = "400", description = "No se puede cancelar (ya tiene pagos o está en estado no cancelable)"),
                   @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable String id) {
        log.info("DELETE /reservas/{} - Cancelar reserva", id);
        reservaService.cancelarReserva(id);
        return ResponseEntity.noContent().build();
    }
}
