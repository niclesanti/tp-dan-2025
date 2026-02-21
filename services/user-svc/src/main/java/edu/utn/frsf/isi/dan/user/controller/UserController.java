package edu.utn.frsf.isi.dan.user.controller;

import edu.utn.frsf.isi.dan.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.model.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Tag(name = "User Controller", description = "Operaciones para la gestión de usuarios")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class UserController {

    private final UserService userService;

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

    @Operation(summary = "Crear usuario propietario", 
                description = "Crea un nuevo usuario de tipo propietario",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario propietario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PostMapping("/propietario")
    public ResponseEntity<Void> crearUsuarioPropietario(
        @Valid
        @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
        @RequestBody PropietarioDTORequest request) {
        
        userService.createUsuarioPropietario(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar usuarios por nombre", 
                description = "Busca usuarios cuyo nombre contenga el texto proporcionado",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Usuarios encontrados"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping("/buscar-nombre")
    public Page<Usuario> buscarUsuariosPorNombre(
        @RequestParam(required = false) String nombre, 
        @NotNull(message = "El parámetro pageable no puede ser nulo")
        Pageable pageable) {

        if (nombre == null || nombre.isEmpty()) {
            return userService.buscarPorNombre("", pageable);
        }
        return userService.buscarPorNombre(nombre, pageable);
    }

    @Operation(summary = "Buscar usuario por dni", 
                description = "Busca un usuario cuyo dni coincida exactamente con el texto proporcionado",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping("/dni/{dni}")
    public ResponseEntity<Usuario> buscarUsuarioPorDni(
        @PathVariable 
        @NotNull(message = "El dni no puede ser nulo") String dni) {
        
        Usuario usuario = userService.buscarPorDniExacto(dni);
        if (usuario == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(usuario);
    }

    
    @Operation(summary = "Buscar usuarios por dni", 
                description = "Busca usuarios cuyo dni contenga el texto proporcionado",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Usuarios encontrados"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping("/buscar-dni")
    public Page<Usuario> buscarUsuariosPorDni(
        @RequestParam
        @NotNull(message = "El dni no puede ser nulo") String dni,
        @NotNull(message = "El parámetro pageable no puede ser nulo") Pageable pageable) {

        return userService.buscarPorDni(dni, pageable);
    }
}