package edu.utn.frsf.isi.dan.gestion.dto;

import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AmenityHotelDTORequest(

        @NotNull(message = "El ID del hotel es obligatorio")
        @Positive(message = "El ID del hotel debe ser un número positivo")
        Integer idHotel,

        @NotNull(message = "El amenity es obligatorio")
        Amenity amenity
) {
}
