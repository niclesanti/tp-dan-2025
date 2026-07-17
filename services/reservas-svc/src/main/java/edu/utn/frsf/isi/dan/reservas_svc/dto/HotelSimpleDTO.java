package edu.utn.frsf.isi.dan.reservas_svc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelSimpleDTO {
    private Integer id;
    private String nombre;
    private Integer categoria;
    private String domicilio;
}
