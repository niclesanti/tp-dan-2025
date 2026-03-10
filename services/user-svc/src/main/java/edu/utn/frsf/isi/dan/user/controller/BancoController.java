package edu.utn.frsf.isi.dan.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.utn.frsf.isi.dan.user.dto.BancoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOUpdate;
import edu.utn.frsf.isi.dan.user.service.BancoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Tag(name = "Banco Controller", description = "Operaciones para la gestión de bancos y tarjetas de crédito")
@RestController
@RequiredArgsConstructor
public class BancoController {

    private final BancoService bancoService;

    // ==============================
    // ENDPOINTS DE BANCOS → /bancos
    // ==============================

    @Operation(summary = "Crear banco",
                description = "Crea un nuevo banco en el sistema",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Banco creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PostMapping("/bancos")
    public ResponseEntity<BancoDTOResponse> crearBanco(
        @Valid
        @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
        @RequestBody BancoDTORequest request) {

        return new ResponseEntity<>(bancoService.crearBanco(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar banco",
                description = "Actualiza los datos de un banco existente",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Banco actualizado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "404", description = "Banco no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PutMapping("/bancos/{id}")
    public ResponseEntity<BancoDTOResponse> actualizarBanco(
        @PathVariable
        @NotNull(message = "El ID no puede ser nulo") Integer id,
        @Valid
        @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
        @RequestBody BancoDTOUpdate request) {

        return ResponseEntity.ok(bancoService.actualizarBanco(id, request));
    }

    @Operation(summary = "Eliminar banco",
                description = "Elimina un banco del sistema",
                responses = {
                    @ApiResponse(responseCode = "204", description = "Banco eliminado exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Banco no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @DeleteMapping("/bancos/{id}")
    public ResponseEntity<Void> eliminarBanco(
        @PathVariable
        @NotNull(message = "El ID no puede ser nulo") Integer id) {

        bancoService.eliminarBanco(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Buscar banco por ID",
                description = "Retorna los datos de un banco específico",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Banco encontrado"),
                    @ApiResponse(responseCode = "404", description = "Banco no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping("/bancos/{id}")
    public ResponseEntity<BancoDTOResponse> buscarBancoPorId(
        @PathVariable
        @NotNull(message = "El ID no puede ser nulo") Integer id) {

        return ResponseEntity.ok(bancoService.buscarBancoPorId(id));
    }

    @Operation(summary = "Listar bancos",
                description = "Retorna la lista de todos los bancos del sistema",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de bancos"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping("/bancos")
    public ResponseEntity<List<BancoDTOResponse>> listarBancos() {

        return ResponseEntity.ok(bancoService.listarBancos());
    }
}