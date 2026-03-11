package edu.utn.frsf.isi.dan.user;

import edu.utn.frsf.isi.dan.user.dto.*;
import edu.utn.frsf.isi.dan.user.model.*;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Fábrica de datos de prueba reutilizables para todos los tests del microservicio user-svc.
 * Proporciona métodos estáticos que crean objetos con valores válidos para los casos de prueba.
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    // ──────────────────────────────────────────
    // ENTIDADES
    // ──────────────────────────────────────────

    public static Banco banco() {
        return Banco.builder()
                .id(1)
                .nombre("Banco Nación")
                .build();
    }

    public static Banco banco(Integer id, String nombre) {
        return Banco.builder()
                .id(id)
                .nombre(nombre)
                .build();
    }

    public static Huesped huesped() {
        Huesped h = new Huesped();
        h.setId(1);
        h.setNombre("Juan Pérez");
        h.setEmail("juan.perez@email.com");
        h.setTelefono("3412345678");
        h.setDni("12345678");
        h.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        h.setTarjetaCredito(new ArrayList<>());
        return h;
    }

    public static Propietario propietario() {
        Propietario p = new Propietario();
        p.setId(1);
        p.setNombre("María García");
        p.setEmail("maria.garcia@email.com");
        p.setTelefono("3419876543");
        p.setDni("87654321");
        p.setIdHotel(10L);
        p.setCuentaBancaria(cuentaBancaria());
        return p;
    }

    public static TarjetaCredito tarjetaCredito(Huesped huesped, Banco banco, boolean esPrincipal) {
        return TarjetaCredito.builder()
                .id(1)
                .numero("4111111111111111")
                .nombreTitular("Juan Pérez")
                .fechaVencimiento("12/27")
                .cvc("123")
                .esPrincipal(esPrincipal)
                .banco(banco)
                .huesped(huesped)
                .build();
    }

    public static TarjetaCredito tarjetaCredito(Integer id, Huesped huesped, Banco banco, boolean esPrincipal) {
        return TarjetaCredito.builder()
                .id(id)
                .numero("5500005555555559")
                .nombreTitular("Juan Pérez")
                .fechaVencimiento("06/26")
                .cvc("456")
                .esPrincipal(esPrincipal)
                .banco(banco)
                .huesped(huesped)
                .build();
    }

    public static CuentaBancaria cuentaBancaria() {
        CuentaBancaria cb = new CuentaBancaria();
        cb.setId(1);
        cb.setNumeroCuenta("000-123456/7");
        cb.setCbu("1234567890123456789012");
        cb.setAlias("juan.perez.cuenta");
        cb.setBanco(banco());
        return cb;
    }

    // ──────────────────────────────────────────
    // DTOs REQUEST
    // ──────────────────────────────────────────

    public static BancoDTORequest bancoDTORequest() {
        return new BancoDTORequest("Banco Nación");
    }

    public static BancoDTOUpdate bancoDTOUpdate() {
        return new BancoDTOUpdate("Banco Provincia");
    }

    public static TarjetaCreditoDTORequest tarjetaCreditoDTORequest() {
        return new TarjetaCreditoDTORequest(
                "4111111111111111",
                "Juan Pérez",
                "12/27",
                "123",
                true,
                1
        );
    }

    public static TarjetaCreditoDTORequest tarjetaCreditoDTORequest(boolean esPrincipal, Integer bancoId) {
        return new TarjetaCreditoDTORequest(
                "5500005555555559",
                "Juan Pérez",
                "06/26",
                "456",
                esPrincipal,
                bancoId
        );
    }

    public static CuentaBancariaDTORequest cuentaBancariaDTORequest() {
        return new CuentaBancariaDTORequest(
                "000-123456/7",
                "1234567890123456789012",
                "juan.perez.cuenta",
                1
        );
    }

    public static HuespedDTORequest huespedDTORequest() {
        return new HuespedDTORequest(
                "Juan Pérez",
                "juan.perez@email.com",
                "3412345678",
                "12345678",
                LocalDate.of(1990, 5, 15),
                tarjetaCreditoDTORequest()
        );
    }

    public static HuespedDTOUpdate huespedDTOUpdate() {
        return new HuespedDTOUpdate(
                "Juan Pérez Actualizado",
                "juan.actualizado@email.com",
                "3419999999",
                "12345678",
                LocalDate.of(1990, 5, 15)
        );
    }

    public static PropietarioDTORequest propietarioDTORequest() {
        return new PropietarioDTORequest(
                "María García",
                "maria.garcia@email.com",
                "3419876543",
                "87654321",
                cuentaBancariaDTORequest(),
                10L
        );
    }

    public static PropietarioDTOUpdate propietarioDTOUpdate() {
        return new PropietarioDTOUpdate(
                "María García Actualizada",
                "maria.actualizada@email.com",
                "3411111111",
                "87654321",
                20L
        );
    }

    // ──────────────────────────────────────────
    // DTOs RESPONSE
    // ──────────────────────────────────────────

    public static BancoDTOResponse bancoDTOResponse() {
        return new BancoDTOResponse(1, "Banco Nación");
    }

    public static TarjetaCreditoDTOResponse tarjetaCreditoDTOResponse() {
        return new TarjetaCreditoDTOResponse(
                1,
                "4111111111111111",
                "Juan Pérez",
                "12/27",
                true,
                "Banco Nación"
        );
    }

    public static CuentaBancariaDTOResponse cuentaBancariaDTOResponse() {
        return new CuentaBancariaDTOResponse(
                1,
                "000-123456/7",
                "1234567890123456789012",
                "juan.perez.cuenta",
                "Banco Nación"
        );
    }

    public static HuespedDTOResponse huespedDTOResponse() {
        return new HuespedDTOResponse(
                1,
                "Juan Pérez",
                "juan.perez@email.com",
                "3412345678",
                "12345678",
                LocalDate.of(1990, 5, 15),
                java.util.List.of(tarjetaCreditoDTOResponse())
        );
    }

    public static PropietarioDTOResponse propietarioDTOResponse() {
        return new PropietarioDTOResponse(
                1,
                "María García",
                "maria.garcia@email.com",
                "3419876543",
                "87654321",
                cuentaBancariaDTOResponse(),
                10L
        );
    }

    public static UsuarioDTOResponse usuarioDTOResponseHuesped() {
        return new UsuarioDTOResponse(1, "Juan Pérez", "juan.perez@email.com", "3412345678", "12345678", "HUESPED");
    }

    public static UsuarioDTOResponse usuarioDTOResponsePropietario() {
        return new UsuarioDTOResponse(1, "María García", "maria.garcia@email.com", "3419876543", "87654321", "PROPIETARIO");
    }
}
