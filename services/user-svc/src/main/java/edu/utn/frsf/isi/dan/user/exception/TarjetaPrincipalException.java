package edu.utn.frsf.isi.dan.user.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación
 * no permitida sobre la tarjeta principal de un huésped.
 * Ej: eliminar la tarjeta principal.
 */
public class TarjetaPrincipalException extends RuntimeException {

    public TarjetaPrincipalException(String mensaje) {
        super(mensaje);
    }
}