package edu.utn.frsf.isi.dan.reservas_svc.dto;

public record ReviewDTOResponse(
        double rating,
        String comment,
        String createdAt
) {
}
