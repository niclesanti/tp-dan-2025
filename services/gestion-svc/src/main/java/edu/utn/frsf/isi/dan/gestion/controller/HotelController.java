package edu.utn.frsf.isi.dan.gestion.controller;

import edu.utn.frsf.isi.dan.gestion.dto.HotelDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.service.HotelService;
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

import java.util.List;

@Tag(name = "Hotel Controller", description = "Operaciones para la gestión de hoteles")
@RestController
@RequestMapping("/hoteles")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @Operation(summary = "Crear hotel",
               description = "Crea un nuevo hotel con todos sus datos",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Hotel creado exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PostMapping
    public ResponseEntity<HotelDTOResponse> crearHotel(
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody HotelDTORequest request) {

        return new ResponseEntity<>(hotelService.crearHotel(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar hotel por ID",
               description = "Retorna los datos de un hotel específico incluyendo sus amenities",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Hotel encontrado"),
                   @ApiResponse(responseCode = "404", description = "Hotel no encontrado"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping("/{id}")
    public ResponseEntity<HotelDTOResponse> buscarHotelPorId(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id) {

        return ResponseEntity.ok(hotelService.buscarHotelPorId(id));
    }

    @Operation(summary = "Buscar hoteles",
               description = "Busca hoteles con filtros opcionales de nombre, categoría, domicilio y amenity. Devuelve resultados paginados.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Hoteles encontrados"),
                   @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @GetMapping
    public ResponseEntity<Page<HotelDTOResponse>> buscarHoteles(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) String domicilio,
            @RequestParam(required = false) Amenity amenity,
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(hotelService.buscarHoteles(nombre, categoria, domicilio, amenity, pageable));
    }

    @Operation(summary = "Actualizar hotel",
               description = "Actualiza los datos editables de un hotel existente: categoría, teléfono y correo de contacto. El nombre, CUIT, domicilio y coordenadas no se pueden modificar.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Hotel actualizado exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                   @ApiResponse(responseCode = "404", description = "Hotel no encontrado"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PutMapping("/{id}")
    public ResponseEntity<HotelDTOResponse> actualizarHotel(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id,
            @Valid
            @NotNull(message = "El cuerpo de la solicitud no puede ser nulo")
            @RequestBody HotelDTOUpdate request) {

        return ResponseEntity.ok(hotelService.actualizarHotel(id, request));
    }

    @Operation(summary = "Cerrar hotel",
               description = "Marca el hotel como cerrado estableciendo la fecha de cierre. Todas las habitaciones quedan no disponibles. No se puede deshacer.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Hotel cerrado exitosamente"),
                   @ApiResponse(responseCode = "400", description = "El hotel ya está cerrado"),
                   @ApiResponse(responseCode = "404", description = "Hotel no encontrado"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<HotelDTOResponse> cerrarHotel(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id) {

        return ResponseEntity.ok(hotelService.cerrarHotel(id));
    }

    @Operation(summary = "Agregar amenities",
               description = "Agrega una o varias amenities a un hotel existente",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Amenities agregadas exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                   @ApiResponse(responseCode = "404", description = "Hotel no encontrado"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @PutMapping("/{id}/amenities")
    public ResponseEntity<HotelDTOResponse> agregarAmenities(
            @PathVariable
            @NotNull(message = "El ID no puede ser nulo") Integer id,
            @NotNull(message = "La lista de amenities no puede ser nula")
            @RequestBody List<Amenity> amenities) {

        return ResponseEntity.ok(hotelService.agregarAmenities(id, amenities));
    }

    @Operation(summary = "Eliminar amenity",
               description = "Elimina una amenity específica de un hotel",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Amenity eliminada exitosamente"),
                   @ApiResponse(responseCode = "404", description = "Hotel o amenity no encontrada"),
                   @ApiResponse(responseCode = "500", description = "Error interno del servidor")})
    @DeleteMapping("/{id}/amenities/{amenityId}")
    public ResponseEntity<Void> eliminarAmenity(
            @PathVariable
            @NotNull(message = "El ID del hotel no puede ser nulo") Integer id,
            @PathVariable
            @NotNull(message = "El ID de la amenity no puede ser nulo") Long amenityId) {

        hotelService.eliminarAmenity(id, amenityId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
