package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDisponibleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HabitacionService {
    
    List<Habitacion> findAll();
    
    Optional<Habitacion> findById(String id);
    
    Habitacion save(Habitacion habitacion);
    
    void deleteById(String id);
    
    void handleEvent(HabitacionEvent event);
    
    Page<HabitacionDisponibleDTO> buscarDisponibles(
            Instant checkIn, 
            Instant checkOut, 
            Integer capacidad, 
            Double precioMin, 
            Double precioMax,
            Integer categoriaHotel,
            List<String> amenities,
            Double latitud,
            Double longitud,
            Double radioKm,
            Pageable pageable);
}
