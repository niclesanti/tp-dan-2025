package edu.utn.frsf.isi.dan.user.dto;

public record CuentaBancariaDTOResponse(
        Integer id,
        String numeroCuenta,
        String cbu,
        String alias,
        String nombreBanco
) {

}
