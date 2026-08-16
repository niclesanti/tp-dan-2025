package edu.utn.frsf.isi.dan.gestion.controller;

import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.service.TipoHabitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "TipoHabitacion Controller", description = "Operaciones para la gestión de tipos de habitación")
@RestController
@RequestMapping("/tipos-habitacion")
@RequiredArgsConstructor
public class TipoHabitacionController {

    private final TipoHabitacionService tipoHabitacionService;

    @Operation(summary = "Crear tipo de habitación",
               description = "Crea un nuevo tipo de habitación",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Tipo de habitación creado exitosamente"),
                       @ApiResponse(responseCode = "400", description = "Error de validación"),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping
    public ResponseEntity<TipoHabitacionDTOResponse> crearTipoHabitacion(
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody TipoHabitacionDTORequest request) {

        return new ResponseEntity<>(tipoHabitacionService.crearTipoHabitacion(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar tipo de habitación por ID",
               description = "Retorna un tipo de habitación por su identificador",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Tipo de habitación encontrado"),
                       @ApiResponse(responseCode = "404", description = "Tipo de habitación no encontrado"),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/{id}")
    public ResponseEntity<TipoHabitacionDTOResponse> buscarTipoHabitacionPorId(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id) {

        return ResponseEntity.ok(tipoHabitacionService.buscarTipoHabitacionPorId(id));
    }

    @Operation(summary = "Buscar tipos de habitación",
               description = "Retorna todos los tipos de habitación",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Tipos de habitación encontrados"),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping
    public ResponseEntity<List<TipoHabitacionDTOResponse>> buscarTiposHabitacion() {

        return ResponseEntity.ok(tipoHabitacionService.buscarTiposHabitacion());
    }

    @Operation(summary = "Actualizar tipo de habitación",
               description = "Actualiza los datos de un tipo de habitación existente",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Tipo de habitación actualizado exitosamente"),
                       @ApiResponse(responseCode = "400", description = "Error de validación"),
                       @ApiResponse(responseCode = "404", description = "Tipo de habitación no encontrado"),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PutMapping("/{id}")
    public ResponseEntity<TipoHabitacionDTOResponse> actualizarTipoHabitacion(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id,
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody TipoHabitacionDTOUpdate request) {

        return ResponseEntity.ok(tipoHabitacionService.actualizarTipoHabitacion(id, request));
    }

    @Operation(summary = "Eliminar tipo de habitación",
               description = "Elimina un tipo de habitación existente",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Tipo de habitación eliminado exitosamente"),
                       @ApiResponse(responseCode = "404", description = "Tipo de habitación no encontrado"),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTipoHabitacion(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id) {

        tipoHabitacionService.eliminarTipoHabitacion(id);
        return ResponseEntity.noContent().build();
    }
}