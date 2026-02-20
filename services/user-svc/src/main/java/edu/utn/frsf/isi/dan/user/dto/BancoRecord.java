package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para transferencia de datos de Banco.
 * 
 * @param id Identificador del banco (opcional para creación)
 * @param nombre Nombre del banco (requerido, mínimo 3 caracteres)
 */
public record BancoRecord(
    Integer id,
    @NotBlank(message = "El nombre del banco no puede estar vacío")
    String nombre
) {
}
