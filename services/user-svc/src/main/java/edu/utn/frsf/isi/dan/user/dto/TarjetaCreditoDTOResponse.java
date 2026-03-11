package edu.utn.frsf.isi.dan.user.dto;

public record TarjetaCreditoDTOResponse(
        Integer id,
        String numero,
        String nombreTitular,
        String fechaVencimiento,
        Boolean esPrincipal,
        String nombreBanco
) {

}
