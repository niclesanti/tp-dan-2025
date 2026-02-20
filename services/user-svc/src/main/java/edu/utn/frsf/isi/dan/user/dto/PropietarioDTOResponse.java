package edu.utn.frsf.isi.dan.user.dto;

public record PropietarioDTOResponse(
        Integer id,
        String nombre,
        String email,
        String telefono,
        String dni,
        CuentaBancariaDTOResponse cuentaBancaria,
        Long idHotel
) {

}
