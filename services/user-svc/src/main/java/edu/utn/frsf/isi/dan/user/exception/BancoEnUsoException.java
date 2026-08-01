package edu.utn.frsf.isi.dan.user.exception;

/**
 * Excepción lanzada cuando se intenta eliminar un banco
 * que está siendo utilizado por tarjetas de crédito o cuentas bancarias.
 */
public class BancoEnUsoException extends RuntimeException {

    public BancoEnUsoException(String mensaje) {
        super(mensaje);
    }
}
