package edu.utn.frsf.isi.dan.reservas_svc.dto;

public record PagoDTOResponse(
        String method,
        String transactionId,
        TarifaDTOResponse amount,
        String status,
        String nroTarjeta
) {
}
