package edu.utn.frsf.isi.dan.reservas_svc.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
