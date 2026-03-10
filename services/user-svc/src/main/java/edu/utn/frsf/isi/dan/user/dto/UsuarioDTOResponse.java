package edu.utn.frsf.isi.dan.user.dto;

/**
 * DTO de respuesta genérico para resultados de búsqueda de usuarios.
 * Expone los campos comunes de {@code Usuario} más el discriminador {@code tipo}
 * ("HUESPED" o "PROPIETARIO"), permitiendo al cliente identificar el tipo sin
 * recibir datos específicos de cada subtipo.
 * Para obtener los detalles completos (tarjetas de crédito, cuenta bancaria, etc.)
 * se deben consultar los endpoints específicos de cada tipo.
 */
public record UsuarioDTOResponse(
        Integer id,
        String nombre,
        String email,
        String telefono,
        String dni,
        String tipo
) {
}
