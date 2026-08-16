package edu.utn.frsf.isi.dan.reservas_svc.dto;

public record HotelSimpleDTO(
        Integer id,
        String nombre,
        Integer categoria,
        String domicilio,
        Double latitud,
        Double longitud
) {
}
