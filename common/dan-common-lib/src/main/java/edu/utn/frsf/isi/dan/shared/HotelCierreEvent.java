package edu.utn.frsf.isi.dan.shared;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelCierreEvent {
    private HotelDTO hotel;
    private List<HabitacionDTO> habitaciones;
}
