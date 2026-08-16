package edu.utn.frsf.isi.dan.reservas_svc.controller;

import edu.utn.frsf.isi.dan.reservas_svc.dto.PagoDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReviewDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ReservaDTOResponse> crear(
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody ReservaDTORequest request) {
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
    public ResponseEntity<ReservaDTOResponse> buscarPorId(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") String id) {
        var reserva = reservaService.buscarReservaPorId(id);
        return ResponseEntity.ok(reserva);
    }

    @Operation(summary = "Buscar reservas por DNI de huésped",
               description = "Retorna todas las reservas de un huésped específico según su DNI, con paginación. Incluye reservas en todos los estados.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error en los parámetros"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/huesped/dni/{dni}")
    public ResponseEntity<Page<ReservaDTOResponse>> buscarPorHuesped(
            @PathVariable
            @NotNull(message = "El DNI no puede ser nulo") String dni,
            @ParameterObject Pageable pageable) {
        var reservas = reservaService.buscarReservasPorHuesped(dni, pageable);
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
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") String id,
            @RequestParam EstadoReserva estado) {
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
    public ResponseEntity<ReservaDTOResponse> realizarCheckIn(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") String id) {
        var reserva = reservaService.realizarCheckIn(id);
        return ResponseEntity.ok(reserva);
    }

    @Operation(summary = "Realizar check-out",
               description = "Registra la salida del cliente del hotel. La reserva debe estar en estado EFECTUADA. " +
                             "Si el huésped dejó review del host y el pago está completo, la reserva pasa a FINALIZADA; " +
                             "de lo contrario pasa a ADEUDADA. Se valida que el check-out se realice en la fecha programada.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Check-out realizado exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Reserva no está en estado válido para check-out"),
                   @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
                   @ApiResponse(responseCode = "409", description = "Conflicto de estado o fecha (check-out antes de la fecha programada)"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping("/{id}/check-out")
    public ResponseEntity<ReservaDTOResponse> realizarCheckOut(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") String id) {
        var reserva = reservaService.realizarCheckOut(id);
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
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") String id,
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody PagoDTORequest pagoRequest) {
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
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") String id,
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody ReviewDTORequest reviewRequest) {
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
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") String id,
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody ReviewDTORequest reviewRequest) {
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
    public ResponseEntity<Void> cancelar(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") String id) {
        reservaService.cancelarReserva(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
