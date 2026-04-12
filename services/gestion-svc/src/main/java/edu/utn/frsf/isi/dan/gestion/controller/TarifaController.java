package edu.utn.frsf.isi.dan.gestion.controller;

import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTOResponse;
import edu.utn.frsf.isi.dan.gestion.service.TarifaService;
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

@Tag(name = "Tarifa Controller", description = "Operaciones para la gestión de tarifas")
@RestController
@RequestMapping("/tarifas")
@RequiredArgsConstructor
public class TarifaController {

    private final TarifaService tarifaService;

    @Operation(summary = "Crear tarifa",
               description = "Crea una tarifa normal vigente o una tarifa promocional según las fechas enviadas",
               responses = {
                       @ApiResponse(responseCode = "201", description = "Tarifa creada exitosamente"),
                       @ApiResponse(responseCode = "400", description = "Error de validación o de negocio"),
                       @ApiResponse(responseCode = "404", description = "Tipo de habitación no encontrado"),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping
    public ResponseEntity<TarifaDTOResponse> crearTarifa(
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody TarifaDTORequest request) {

        return new ResponseEntity<>(tarifaService.crearTarifa(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar tarifa por ID",
               description = "Retorna una tarifa por su identificador",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Tarifa encontrada"),
                       @ApiResponse(responseCode = "404", description = "Tarifa no encontrada"),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/{id}")
    public ResponseEntity<TarifaDTOResponse> buscarTarifaPorId(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id) {

        return ResponseEntity.ok(tarifaService.buscarTarifaPorId(id));
    }

    @Operation(summary = "Buscar tarifas",
               description = "Retorna tarifas paginadas",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Tarifas encontradas"),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping
    public ResponseEntity<Page<TarifaDTOResponse>> buscarTarifas(@ParameterObject Pageable pageable) {

        return ResponseEntity.ok(tarifaService.buscarTarifas(pageable));
    }

    @Operation(summary = "Eliminar tarifa",
               description = "Elimina una tarifa. Si la tarifa eliminada es la vigente, se promueve la tarifa anterior",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Tarifa eliminada exitosamente"),
                       @ApiResponse(responseCode = "400", description = "No se puede eliminar por regla de negocio"),
                       @ApiResponse(responseCode = "404", description = "Tarifa no encontrada"),
                       @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarifa(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id) {

        tarifaService.eliminarTarifa(id);
        return ResponseEntity.noContent().build();
    }
}
