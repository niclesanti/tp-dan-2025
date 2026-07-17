package edu.utn.frsf.isi.dan.user.dao;

import edu.utn.frsf.isi.dan.user.model.Huesped;
import edu.utn.frsf.isi.dan.user.model.Propietario;
import edu.utn.frsf.isi.dan.user.model.Usuario;
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
 * Tests de integración para {@link UsuarioRepository} usando {@code @DataJpaTest} con H2.
 * Cubre las queries custom: findByNombreContainingIgnoreCase, findByDniContaining, findByDni.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UsuarioRepository — Queries personalizadas")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HuespedRepository huespedRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    private Huesped huesped1;
    private Huesped huesped2;
    private Propietario propietario1;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        huesped1 = new Huesped();
        huesped1.setNombre("Juan Pérez");
        huesped1.setEmail("juan@email.com");
        huesped1.setTelefono("3412345678");
        huesped1.setDni("12345678");
        huesped1.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        huesped2 = new Huesped();
        huesped2.setNombre("Ana González");
        huesped2.setEmail("ana@email.com");
        huesped2.setTelefono("3419876543");
        huesped2.setDni("87654321");
        huesped2.setFechaNacimiento(LocalDate.of(1985, 3, 20));

        propietario1 = new Propietario();
        propietario1.setNombre("Juan Carlos López");
        propietario1.setEmail("jclopez@email.com");
        propietario1.setTelefono("3411111111");
        propietario1.setDni("11223344");
        propietario1.setIdHotel(1);

        huespedRepository.save(huesped1);
        huespedRepository.save(huesped2);
        propietarioRepository.save(propietario1);
    }

    // ──────────────────────────────────────────────────────────────────────
    // findByNombreContainingIgnoreCase
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByNombreContainingIgnoreCase")
    class FindByNombreContainingIgnoreCase {

        @Test
        @DisplayName("Debe retornar usuarios cuyo nombre contenga el texto (case insensitive)")
        void debeRetornarUsuariosConNombreParcial() {
            Page<Usuario> result = usuarioRepository.findByNombreContainingIgnoreCase("juan", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Usuario::getNombre)
                    .containsExactlyInAnyOrder("Juan Pérez", "Juan Carlos López");
        }

        @Test
        @DisplayName("Debe ser insensible a mayúsculas/minúsculas")
        void debeSerCaseInsensitive() {
            Page<Usuario> upper = usuarioRepository.findByNombreContainingIgnoreCase("JUAN", PageRequest.of(0, 10));
            Page<Usuario> lower = usuarioRepository.findByNombreContainingIgnoreCase("juan", PageRequest.of(0, 10));

            assertThat(upper.getTotalElements()).isEqualTo(lower.getTotalElements());
        }

        @Test
        @DisplayName("Debe retornar página vacía cuando no hay coincidencias")
        void debeRetornarVacioSinCoincidencias() {
            Page<Usuario> result = usuarioRepository.findByNombreContainingIgnoreCase("ZZZNOMBREX", PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Debe retornar todos los usuarios con cadena vacía")
        void debeRetornarTodosConCadenaVacia() {
            Page<Usuario> result = usuarioRepository.findByNombreContainingIgnoreCase("", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Debe respetar la paginación")
        void debeRespetarPaginacion() {
            Page<Usuario> page0 = usuarioRepository.findByNombreContainingIgnoreCase("", PageRequest.of(0, 2));
            Page<Usuario> page1 = usuarioRepository.findByNombreContainingIgnoreCase("", PageRequest.of(1, 2));

            assertThat(page0.getContent()).hasSize(2);
            assertThat(page1.getContent()).hasSize(1);
            assertThat(page0.getTotalElements()).isEqualTo(3);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // findByDniContaining
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByDniContaining")
    class FindByDniContaining {

        @Test
        @DisplayName("Debe retornar usuarios cuyo DNI contenga el texto")
        void debeRetornarUsuariosConDniParcial() {
            Page<Usuario> result = usuarioRepository.findByDniContaining("1234", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDni()).isEqualTo("12345678");
        }

        @Test
        @DisplayName("Debe retornar todos cuando se busca con cadena vacía")
        void debeRetornarTodosConCadenaVacia() {
            Page<Usuario> result = usuarioRepository.findByDniContaining("", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Debe retornar vacío si no hay match")
        void debeRetornarVacioSinMatch() {
            Page<Usuario> result = usuarioRepository.findByDniContaining("99999999", PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar múltiples usuarios cuando el fragmento coincide con varios")
        void debeRetornarMultiplesUsuariosConFragmentoComun() {
            // "12" está en "12345678" y "11223344"
            Page<Usuario> result = usuarioRepository.findByDniContaining("12", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // findByDni (exacto)
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByDni (exacto)")
    class FindByDniExacto {

        @Test
        @DisplayName("Debe retornar el usuario cuando el DNI coincide exactamente")
        void debeRetornarUsuarioConDniExacto() {
            Optional<Usuario> result = usuarioRepository.findByDni("12345678");

            assertThat(result).isPresent();
            assertThat(result.get().getDni()).isEqualTo("12345678");
            assertThat(result.get().getNombre()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("Debe retornar Optional vacío cuando el DNI no existe")
        void debeRetornarVacioConDniInexistente() {
            Optional<Usuario> result = usuarioRepository.findByDni("00000000");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("No debe retornar un usuario con DNI parcialmente coincidente")
        void noDebeRetornarUsuarioConDniParcial() {
            Optional<Usuario> result = usuarioRepository.findByDni("1234");

            assertThat(result).isEmpty();
        }
    }
}
