package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.dto.PagoDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReviewDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {

    /**
     * Crea una nueva reserva validando fechas, disponibilidad de la habitación
     * y calculando el precio total por cantidad de noches.
     *
     * @param request DTO con los datos de la nueva reserva
     * @return DTO de respuesta con los datos de la reserva creada en estado RESERVADA
     */
    ReservaDTOResponse crearReserva(ReservaDTORequest request);

    /**
     * Busca una reserva por su ID.
     *
     * @param id ID de la reserva a buscar
     * @return DTO de respuesta con los datos de la reserva
     * @throws edu.utn.frsf.isi.dan.reservas_svc.exception.EntityNotFoundException si no existe la reserva
     */
    ReservaDTOResponse buscarReservaPorId(String id);

    /**
     * Busca reservas por DNI del huésped con paginación.
     *
     * @param dni      DNI del huésped
     * @param pageable parámetros de paginación
     * @return página de {@link ReservaDTOResponse} del huésped
     */
    Page<ReservaDTOResponse> buscarReservasPorHuesped(String dni, Pageable pageable);

    /**
     * Actualiza el estado de una reserva validando las transiciones permitidas.
     *
     * @param id         ID de la reserva
     * @param nuevoEstado estado nuevo a aplicar
     * @return DTO de respuesta con la reserva actualizada
     */
    ReservaDTOResponse actualizarEstadoReserva(String id, EstadoReserva nuevoEstado);

    /**
     * Realiza el check-in de una reserva confirmada, pasándola a EFECTUADA.
     *
     * @param id ID de la reserva
     * @return DTO de respuesta con la reserva actualizada
     */
    ReservaDTOResponse realizarCheckIn(String id);

    /**
     * Realiza el check-out de una reserva efectuada. Determina el estado final
     * (FINALIZADA si hay review del host y pago completo, ADEUDADA en caso contrario).
     *
     * @param id ID de la reserva
     * @return DTO de respuesta con la reserva actualizada
     */
    ReservaDTOResponse realizarCheckOut(String id);

    /**
     * Agrega un pago a una reserva. Con al menos un pago la reserva pasa a CONFIRMADA.
     *
     * @param id         ID de la reserva
     * @param pagoRequest DTO con los datos del pago
     * @return DTO de respuesta con la reserva actualizada
     */
    ReservaDTOResponse agregarPago(String id, PagoDTORequest pagoRequest);

    /**
     * Agrega una review de cliente o de host a una reserva.
     *
     * @param id            ID de la reserva
     * @param reviewRequest DTO con los datos de la review
     * @param esCliente     true si es review del cliente, false si es del host
     * @return DTO de respuesta con la reserva actualizada
     */
    ReservaDTOResponse agregarReview(String id, ReviewDTORequest reviewRequest, boolean esCliente);

    /**
     * Cancela una reserva (solo si no tiene pagos y no está finalizada),
     * eliminándola de la lista de reservas de la habitación.
     *
     * @param id ID de la reserva a cancelar
     */
    void cancelarReserva(String id);
}
