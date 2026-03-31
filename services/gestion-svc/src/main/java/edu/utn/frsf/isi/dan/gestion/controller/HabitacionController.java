package edu.utn.frsf.isi.dan.gestion.controller;

import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTOResponse;
import edu.utn.frsf.isi.dan.gestion.service.HabitacionService;
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

@Tag(name = "Habitación Controller", description = "Operaciones para la gestión de habitaciones")
@RestController
@RequestMapping("/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    @Operation(summary = "Crear habitación",
               description = "Crea una nueva habitación y notifica al servicio de reservas",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Habitación creada exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error de validación"),
                   @ApiResponse(responseCode = "404", description = "Hotel o tipo de habitación no encontrado"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping
    public ResponseEntity<HabitacionDTOResponse> crearHabitacion(
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody HabitacionDTORequest request) {

        return new ResponseEntity<>(habitacionService.crearHabitacion(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar habitación por ID",
               description = "Retorna los datos de una habitación específica",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Habitación encontrada"),
                   @ApiResponse(responseCode = "404", description = "Habitación no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/{id}")
    public ResponseEntity<HabitacionDTOResponse> buscarHabitacionPorId(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id) {

        return ResponseEntity.ok(habitacionService.buscarHabitacionPorId(id));
    }

    @Operation(summary = "Buscar habitaciones",
               description = "Busca habitaciones con filtros opcionales (cantidad de huéspedes, tipo, rango de precio). Los filtros se aplican en AND.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Búsqueda completada"),
                   @ApiResponse(responseCode = "400", description = "Error en los parámetros"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping
    public ResponseEntity<Page<HabitacionDTOResponse>> buscarHabitaciones(
            @RequestParam(required = false) Integer cantidadHuespedes,
            @RequestParam(required = false) Integer idTipoHabitacion,
            @RequestParam(required = false) Double precioMinimo,
            @RequestParam(required = false) Double precioMaximo,
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(
                habitacionService.buscarHabitaciones(cantidadHuespedes, idTipoHabitacion, 
                                                      precioMinimo, precioMaximo, pageable));
    }

    @Operation(summary = "Actualizar habitación",
               description = "Actualiza los datos de una habitación existente y notifica al servicio de reservas",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Habitación actualizada exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error de validación"),
                   @ApiResponse(responseCode = "404", description = "Habitación, hotel o tipo no encontrado"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PutMapping("/{id}")
    public ResponseEntity<HabitacionDTOResponse> actualizarHabitacion(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id,
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody HabitacionDTOUpdate request) {

        return ResponseEntity.ok(habitacionService.actualizarHabitacion(id, request));
    }

    @Operation(summary = "Eliminar habitación",
               description = "Elimina una habitación y notifica al servicio de reservas",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Habitación eliminada exitosamente"),
                   @ApiResponse(responseCode = "404", description = "Habitación no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHabitacion(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id) {

        habitacionService.eliminarHabitacion(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Obtener tarifa vigente de habitación",
               description = "Retorna la tarifa vigente (hoy) para una habitación específica",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Tarifa encontrada"),
                   @ApiResponse(responseCode = "404", description = "Habitación o tarifa no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/{id}/tarifa-vigente")
    public ResponseEntity<TarifaDTOResponse> obtenerTarifaVigente(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id) {

        return ResponseEntity.ok(habitacionService.obtenerTarifaVigente(id));
    }
}
