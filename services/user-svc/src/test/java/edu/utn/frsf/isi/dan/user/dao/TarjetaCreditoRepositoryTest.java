package edu.utn.frsf.isi.dan.user.dao;

import edu.utn.frsf.isi.dan.user.model.Banco;
import edu.utn.frsf.isi.dan.user.model.Huesped;
import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para {@link TarjetaCreditoRepository} usando {@code @DataJpaTest} con H2.
 * Cubre: findByHuespedIdAndEsPrincipalTrue, findByHuespedId(pageable).
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TarjetaCreditoRepository — Queries personalizadas")
class TarjetaCreditoRepositoryTest {

    @Autowired
    private TarjetaCreditoRepository tarjetaCreditoRepository;

    @Autowired
    private HuespedRepository huespedRepository;

    @Autowired
    private BancoRepository bancoRepository;

    private Huesped huesped;
    private Banco banco;
    private TarjetaCredito tarjetaPrincipal;
    private TarjetaCredito tarjetaSecundaria;

    @BeforeEach
    void setUp() {
        tarjetaCreditoRepository.deleteAll();
        huespedRepository.deleteAll();
        bancoRepository.deleteAll();

        banco = new Banco();
        banco.setNombre("Banco Nación");
        banco = bancoRepository.save(banco);

        huesped = new Huesped();
        huesped.setNombre("Juan Pérez");
        huesped.setEmail("juan@email.com");
        huesped.setTelefono("3412345678");
        huesped.setDni("12345678");
        huesped.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        huesped = huespedRepository.save(huesped);

        tarjetaPrincipal = TarjetaCredito.builder()
                .numero("4111111111111111")
                .nombreTitular("Juan Pérez")
                .fechaVencimiento("12/27")
                .cvc("123")
                .esPrincipal(true)
                .banco(banco)
                .huesped(huesped)
                .build();

        tarjetaSecundaria = TarjetaCredito.builder()
                .numero("5500005555555559")
                .nombreTitular("Juan Pérez")
                .fechaVencimiento("06/26")
                .cvc("456")
                .esPrincipal(false)
                .banco(banco)
                .huesped(huesped)
                .build();

        tarjetaPrincipal = tarjetaCreditoRepository.save(tarjetaPrincipal);
        tarjetaSecundaria = tarjetaCreditoRepository.save(tarjetaSecundaria);
    }

    // ──────────────────────────────────────────────────────────────────────
    // findByHuespedIdAndEsPrincipalTrue
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByHuespedIdAndEsPrincipalTrue")
    class FindByHuespedIdAndEsPrincipalTrue {

        @Test
        @DisplayName("Debe retornar la tarjeta principal del huésped")
        void debeRetornarTarjetaPrincipal() {
            Optional<TarjetaCredito> result = tarjetaCreditoRepository
                    .findByHuespedIdAndEsPrincipalTrue(huesped.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getEsPrincipal()).isTrue();
            assertThat(result.get().getNumero()).isEqualTo("4111111111111111");
        }

        @Test
        @DisplayName("Debe retornar Optional vacío si el huésped no tiene tarjeta principal")
        void debeRetornarVacioSinTarjetaPrincipal() {
            // Desmarcar la tarjeta principal
            tarjetaPrincipal.setEsPrincipal(false);
            tarjetaCreditoRepository.save(tarjetaPrincipal);

            Optional<TarjetaCredito> result = tarjetaCreditoRepository
                    .findByHuespedIdAndEsPrincipalTrue(huesped.getId());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar Optional vacío para un huésped inexistente")
        void debeRetornarVacioConHuespedInexistente() {
            Optional<TarjetaCredito> result = tarjetaCreditoRepository
                    .findByHuespedIdAndEsPrincipalTrue(999);

            assertThat(result).isEmpty();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // findByHuespedId (paginado)
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByHuespedId (paginado)")
    class FindByHuespedId {

        @Test
        @DisplayName("Debe retornar todas las tarjetas del huésped")
        void debeRetornarTodasLasTarjetas() {
            Page<TarjetaCredito> result = tarjetaCreditoRepository
                    .findByHuespedId(huesped.getId(), PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(TarjetaCredito::getNumero)
                    .containsExactlyInAnyOrder("4111111111111111", "5500005555555559");
        }

        @Test
        @DisplayName("Debe retornar página vacía para un huésped sin tarjetas")
        void debeRetornarVacioParaHuespedSinTarjetas() {
            Huesped otroHuesped = new Huesped();
            otroHuesped.setNombre("Sin Tarjetas");
            otroHuesped.setEmail("sintarjetas@email.com");
            otroHuesped.setTelefono("3410000000");
            otroHuesped.setDni("00000001");
            otroHuesped.setFechaNacimiento(LocalDate.of(1995, 1, 1));
            otroHuesped = huespedRepository.save(otroHuesped);

            Page<TarjetaCredito> result = tarjetaCreditoRepository
                    .findByHuespedId(otroHuesped.getId(), PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Debe respetar la paginación")
        void debeRespetarPaginacion() {
            Page<TarjetaCredito> page0 = tarjetaCreditoRepository
                    .findByHuespedId(huesped.getId(), PageRequest.of(0, 1));
            Page<TarjetaCredito> page1 = tarjetaCreditoRepository
                    .findByHuespedId(huesped.getId(), PageRequest.of(1, 1));

            assertThat(page0.getContent()).hasSize(1);
            assertThat(page1.getContent()).hasSize(1);
            assertThat(page0.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Debe retornar página vacía para huésped inexistente")
        void debeRetornarVacioParaHuespedInexistente() {
            Page<TarjetaCredito> result = tarjetaCreditoRepository
                    .findByHuespedId(999, PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
        }
    }
}
