package edu.utn.frsf.isi.dan.reservas_svc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitacionDisponibleDTO {
    private String id;
    private Integer habitacionId;
    private Integer capacidad;
    private Double precioNoche;
    private String tipoHabitacion;
    private List<String> amenities;
    private HotelSimpleDTO hotel;
}
