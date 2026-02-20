package edu.utn.frsf.isi.dan.user.controller;

import edu.utn.frsf.isi.dan.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.model.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@Tag(name = "User Controller", description = "Operaciones para la gestión de usuarios")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Crear usuario huesped", 
                description = "Crea un nuevo usuario de tipo huesped",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario huesped creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PostMapping("/huesped")
    public ResponseEntity<Void> crearUsuarioHuesped(@RequestBody HuespedDTORequest request) {
        userService.crearUsuarioHuesped(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Crear usuario propietario", 
                description = "Crea un nuevo usuario de tipo propietario",
                responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario propietario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PostMapping("/propietario")
    public ResponseEntity<Void> crearUsuarioPropietario(@RequestBody @Valid PropietarioDTORequest request) {
        userService.crearUsuarioPropietario(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar usuarios por nombre", 
                description = "Busca usuarios cuyo nombre contenga el texto proporcionado",
                responses = {
                    @ApiResponse(responseCode = "200", description = "Usuarios encontrados"),
                    @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @GetMapping
    public Page<Usuario> buscarUsuariosPorNombre(@RequestParam(required = false) String nombre, Pageable pageable) {
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
    public ResponseEntity<Usuario> buscarUsuarioPorDni(@PathVariable String dni) {
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
    public Page<Usuario> buscarUsuariosPorDni(@RequestParam String dni, Pageable pageable) {
        return userService.buscarPorDni(dni, pageable);
    }
}