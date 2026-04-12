package edu.utn.frsf.isi.dan.reservas_svc.model;

public enum EstadoReserva {
RESERVADA,    // Estado inicial al crear la reserva
CONFIRMADA,   // Cuando tiene al menos un pago
EFECTUADA,    // Cuando el cliente ingresa al hotel (check-in)
FINALIZADA,   // Después del check-out (requiere review host + pago completo)
CANCELADA,    // Reserva cancelada (solo si no tiene pagos)
BLOQUEADA,    // Habitación bloqueada (no se puede reservar)
CERRADA,      // Reserva cerrada
ADEUDADA      // Marcada cuando se finaliza sin review host o sin pago completo
}
