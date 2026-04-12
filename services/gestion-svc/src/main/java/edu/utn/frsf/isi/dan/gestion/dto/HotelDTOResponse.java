package edu.utn.frsf.isi.dan.gestion.dto;

import java.time.LocalDate;
import java.util.List;

public record HotelDTOResponse(
        Integer id,
        String nombre,
        String cuit,
        String domicilio,
        Double latitud,
        Double longitud,
        String telefono,
        String correoContacto,
        Integer categoria,
        LocalDate fechaCierre,
        List<AmenityHotelDTOResponse> amenities
) {
}
