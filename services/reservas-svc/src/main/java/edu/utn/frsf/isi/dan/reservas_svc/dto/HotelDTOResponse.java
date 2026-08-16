package edu.utn.frsf.isi.dan.reservas_svc.dto;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public record HotelDTOResponse(
        Integer id,
        String nombre,
        Integer categoria,
        String domicilio,
        GeoJsonPoint ubicacion
) {
}
