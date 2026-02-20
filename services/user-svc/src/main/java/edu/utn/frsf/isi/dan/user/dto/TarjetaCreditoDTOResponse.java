package edu.utn.frsf.isi.dan.user.dto;

public record TarjetaCreditoDTOResponse(
        Integer id,
        String numero,
        String nombreTitular,
        String fechaVencimiento,
        String cvc,
        Boolean esPrincipal,
        String nombreBanco
) {

}
