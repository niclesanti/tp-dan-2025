package edu.utn.frsf.isi.dan.reservas_svc.controller;

import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDisponibleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.service.HabitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Tag(name = "Habitación Controller (Reservas)", description = "Operaciones de consulta de habitaciones sincronizadas desde gestion-svc")
@RestController
@RequestMapping("/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {

    private final HabitacionService habitacionService;

    @Operation(summary = "Listar todas las habitaciones",
               description = "Retorna todas las habitaciones sincronizadas desde el servicio de gestión. Incluye información del hotel, tipo y amenities.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Lista de habitaciones obtenida exitosamente"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping
    public ResponseEntity<List<HabitacionDTOResponse>> getAll() {
        return ResponseEntity.ok(habitacionService.findAll());
    }

    @Operation(summary = "Buscar habitación por ID",
               description = "Retorna los datos completos de una habitación específica incluyendo hotel, tipo, tarifa y lista de reservas",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Habitación encontrada"),
                   @ApiResponse(responseCode = "404", description = "Habitación no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/{id}")
    public ResponseEntity<HabitacionDTOResponse> getById(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") String id) {
        return ResponseEntity.ok(habitacionService.buscarPorId(id));
    }

    @Operation(summary = "Buscar habitaciones disponibles",
               description = "Busca habitaciones disponibles para un rango de fechas con filtros avanzados. " +
                             "Filtros disponibles: capacidad, rango de precio, categoría del hotel (1-5 estrellas), " +
                             "amenities (wifi, tv, piscina, etc. - operador AND), y búsqueda geoespacial por proximidad. " +
                             "Una habitación está disponible si no tiene reservas que se solapen con las fechas solicitadas. " +
                             "Todos los filtros son opcionales excepto las fechas de entrada y salida.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error en los parámetros (fechas inválidas, coordenadas fuera de rango)"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/disponibles")
    public ResponseEntity<Page<HabitacionDisponibleDTO>> buscarDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant checkOut,
            @RequestParam(required = false) Integer capacidad,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Integer categoriaHotel,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            @RequestParam(required = false) Double radioKm,
            @ParameterObject Pageable pageable) {
        var habitaciones = habitacionService.buscarDisponibles(
                checkIn, checkOut, capacidad, precioMin, precioMax,
                categoriaHotel, amenities, latitud, longitud, radioKm, pageable);
        return ResponseEntity.ok(habitaciones);
    }
}
