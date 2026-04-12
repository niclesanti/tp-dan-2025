package edu.utn.frsf.isi.dan.gestion.dto;

import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import jakarta.validation.constraints.NotNull;

public record AmenityHotelDTOUpdate(

        @NotNull(message = "El amenity es obligatorio")
        Amenity amenity
) {
}
