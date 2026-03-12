package edu.utn.frsf.isi.dan.gestion.dto;

import edu.utn.frsf.isi.dan.gestion.model.Amenity;

public record AmenityHotelDTOResponse(
        Long id,
        Amenity amenity
) {
}
