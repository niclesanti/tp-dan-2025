package edu.utn.frsf.isi.dan.user.dto;

import java.time.LocalDate;
import java.util.List;

public record HuespedDTOResponse(
        Integer id,
        String nombre,
        String email,
        String telefono,
        String dni,
        LocalDate fechaNacimiento,
        List<TarjetaCreditoDTOResponse> tarjetaCredito
) {

}
