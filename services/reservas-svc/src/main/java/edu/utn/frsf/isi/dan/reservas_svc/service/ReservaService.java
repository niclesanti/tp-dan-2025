package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.dto.*;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {
    
    ReservaDTOResponse crearReserva(ReservaDTORequest request);
    
    ReservaDTOResponse buscarReservaPorId(String id);
    
    Page<ReservaDTOResponse> buscarReservasPorHuesped(String huespedId, Pageable pageable);
    
    ReservaDTOResponse actualizarEstadoReserva(String id, EstadoReserva nuevoEstado);
    
    ReservaDTOResponse realizarCheckIn(String id);
    
    ReservaDTOResponse agregarPago(String id, PagoDTORequest pagoRequest);
    
    ReservaDTOResponse agregarReview(String id, ReviewDTORequest reviewRequest, boolean esCliente);
    
    void cancelarReserva(String id);
}
