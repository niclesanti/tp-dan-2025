package edu.utn.frsf.isi.dan.user.controller;

import edu.utn.frsf.isi.dan.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.UsuarioDTOResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "User Controller", description = "Operaciones para la gestión de usuarios")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class UserController {

    private final UserService userService;

    // Gestión de huespedes
    @Operation(summary = "Crear usuario huesped", 
                description = "Crea un nuevo usuario de tipo huesped",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario huesped creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PostMapping("/huesped")
    public ResponseEntity<HuespedDTOResponse> crearUsuarioHuesped(
        @Valid
        @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
        @RequestBody HuespedDTORequest request) {
        
        HuespedDTOResponse response = userService.createUsuarioHuesped(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar usuario huesped", 
                description = "Actualiza los datos de un usuario huesped existente (nombre, email, telefono, dni, fechaNacimiento)",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario huesped actualizado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "404", description = "Usuario huesped no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PutMapping("/huesped/{id}")
    public ResponseEntity<HuespedDTOResponse> actualizarUsuarioHuesped(
        @PathVariable 
        @NotNull(message = "El ID no puede ser nulo") Integer id,
        @Valid
        @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
        @RequestBody HuespedDTOUpdate request) {
        
        HuespedDTOResponse response = userService.updateUsuarioHuesped(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar usuario huesped", 
                description = "Elimina un usuario huesped del sistema. Las tarjetas de crédito asociadas se eliminan automáticamente.",
                responses = {
                    @ApiResponse(responseCode = "204", description = "Usuario huesped eliminado exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Usuario huesped no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @DeleteMapping("/huesped/{id}")
    public ResponseEntity<Void> eliminarUsuarioHuesped(
        @PathVariable 
        @NotNull(message = "El ID no puede ser nulo") Integer id) {
        
        userService.deleteUsuarioHuesped(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    // Gestión de propietarios
    @Operation(summary = "Crear usuario propietario",
                description = "Crea un nuevo usuario de tipo propietario. La cuenta bancaria e idHotel son opcionales.",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario propietario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "404", description = "Banco no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PostMapping("/propietario")
    public ResponseEntity<PropietarioDTOResponse> crearUsuarioPropietario(
        @Valid
        @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
        @RequestBody PropietarioDTORequest request) {

        PropietarioDTOResponse response = userService.createUsuarioPropietario(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Actualizar usuario propietario",
                description = "Actualiza los datos de un usuario propietario existente (nombre, email, telefono, dni, idHotel). La cuenta bancaria no se modifica.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario propietario actualizado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "404", description = "Usuario propietario no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PutMapping("/propietario/{id}")
    public ResponseEntity<PropietarioDTOResponse> actualizarUsuarioPropietario(
        @PathVariable
        @NotNull(message = "El ID no puede ser nulo") Integer id,
        @Valid
        @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
        @RequestBody PropietarioDTOUpdate request) {

        PropietarioDTOResponse response = userService.updateUsuarioPropietario(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar usuario propietario",
                description = "Elimina un usuario propietario del sistema.",
                responses = {
                    @ApiResponse(responseCode = "204", description = "Usuario propietario eliminado exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Usuario propietario no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @DeleteMapping("/propietario/{id}")
    public ResponseEntity<Void> eliminarUsuarioPropietario(
        @PathVariable
        @NotNull(message = "El ID no puede ser nulo") Integer id) {

        userService.deleteUsuarioPropietario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



    //Busquedas
    @Operation(summary = "Buscar usuarios por nombre", 
                description = "Busca usuarios cuyo nombre contenga el texto proporcionado. Si no se indica nombre, devuelve todos los usuarios.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Usuarios encontrados"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping("/buscar-nombre")
    public ResponseEntity<Page<UsuarioDTOResponse>> buscarUsuariosPorNombre(
        @RequestParam(required = false, defaultValue = "") String nombre,
        @ParameterObject Pageable pageable) {

        Page<UsuarioDTOResponse> usuarios = userService.buscarPorNombre(nombre, pageable);

        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Buscar usuario por DNI exacto", 
                description = "Busca un usuario cuyo DNI coincida exactamente con el valor proporcionado",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping("/dni/{dni}")
    public ResponseEntity<UsuarioDTOResponse> buscarUsuarioPorDni(
        @PathVariable
        @NotNull(message = "El DNI no puede ser nulo") String dni) {
        
        UsuarioDTOResponse usuario = userService.buscarPorDniExacto(dni);

        return ResponseEntity.ok(usuario);
    }

    @Operation(summary = "Buscar usuarios por DNI (parcial)", 
                description = "Busca usuarios cuyo DNI contenga el texto proporcionado. Si no se indica dni, devuelve todos los usuarios.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Usuarios encontrados"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping("/buscar-dni")
    public ResponseEntity<Page<UsuarioDTOResponse>> buscarUsuariosPorDni(
        @RequestParam(required = false, defaultValue = "") String dni,
        @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(userService.buscarPorDni(dni, pageable));
    }

    // ======================================================
    // ENDPOINTS DE TARJETAS DE CRÉDITO → /huespedes/{id}/tarjetas
    // ======================================================

    @Operation(summary = "Agregar tarjeta de crédito",
                description = "Agrega una nueva tarjeta de crédito a un huésped. Si es principal, desmarca la anterior.",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Tarjeta agregada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "404", description = "Huésped o banco no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PostMapping("/huespedes/{huespedId}/tarjetas")
    public ResponseEntity<TarjetaCreditoDTOResponse> agregarTarjeta(
        @PathVariable @NotNull(message = "El ID del huésped no puede ser nulo") Integer huespedId,
        @Valid @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
        @RequestBody TarjetaCreditoDTORequest request) {

        return new ResponseEntity<>(userService.agregarTarjeta(huespedId, request), HttpStatus.CREATED);
    }


    @Operation(summary = "Eliminar tarjeta de crédito",
                description = "Elimina una tarjeta de crédito si no es la principal.",
                responses = {
                    @ApiResponse(responseCode = "204", description = "Tarjeta eliminada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "No se puede eliminar la tarjeta principal"),
                    @ApiResponse(responseCode = "404", description = "Huésped o tarjeta no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @DeleteMapping("/huespedes/{huespedId}/tarjetas/{tarjetaId}")
    public ResponseEntity<Void> eliminarTarjeta(
        @PathVariable @NotNull(message = "El ID del huésped no puede ser nulo") Integer huespedId,
        @PathVariable @NotNull(message = "El ID de la tarjeta no puede ser nulo") Integer tarjetaId) {

        userService.eliminarTarjeta(huespedId, tarjetaId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Cambiar tarjeta principal",
                description = "Cambia la tarjeta de crédito principal de un huésped. Desmarca la anterior.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Tarjeta principal cambiada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "La tarjeta ya es principal o no pertenece al huésped"),
                    @ApiResponse(responseCode = "404", description = "Huésped o tarjeta no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PatchMapping("/huespedes/{huespedId}/tarjetas/{tarjetaId}/principal")
    public ResponseEntity<TarjetaCreditoDTOResponse> cambiarTarjetaPrincipal(
        @PathVariable @NotNull(message = "El ID del huésped no puede ser nulo") Integer huespedId,
        @PathVariable @NotNull(message = "El ID de la tarjeta no puede ser nulo") Integer tarjetaId) {

        return ResponseEntity.ok(userService.cambiarTarjetaPrincipal(huespedId, tarjetaId));
    }

    @Operation(summary = "Listar tarjetas de crédito",
                description = "Retorna la lista de todas las tarjetas de crédito de un huésped.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de tarjetas"),
                    @ApiResponse(responseCode = "404", description = "Huésped no encontrado"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping("/huespedes/{huespedId}/tarjetas")
    public ResponseEntity<Page<TarjetaCreditoDTOResponse>> listarTarjetas(
        @PathVariable @NotNull(message = "El ID del huésped no puede ser nulo") Integer huespedId,
        @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return ResponseEntity.ok(userService.listarTarjetas(huespedId, pageable));
    }
}