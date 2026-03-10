package edu.utn.frsf.isi.dan.user.service;

import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dao.CuentaBancariaRepository;
import edu.utn.frsf.isi.dan.user.dao.HuespedRepository;
import edu.utn.frsf.isi.dan.user.dao.PropietarioRepository;
import edu.utn.frsf.isi.dan.user.dao.UsuarioRepository;
import edu.utn.frsf.isi.dan.user.dto.CuentaBancariaDTORequest;
import edu.utn.frsf.isi.dan.user.dto.CuentaBancariaDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOUpdate;
import edu.utn.frsf.isi.dan.user.mapper.CuentaBancariaMapper;
import edu.utn.frsf.isi.dan.user.mapper.HuespedMapper;
import edu.utn.frsf.isi.dan.user.mapper.PropietarioMapper;
import edu.utn.frsf.isi.dan.user.mapper.TarjetaCreditoMapper;
import edu.utn.frsf.isi.dan.user.model.Banco;
import edu.utn.frsf.isi.dan.user.model.CuentaBancaria;
import edu.utn.frsf.isi.dan.user.model.Propietario;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests para los métodos de Propietario en UserServiceImpl.
 *
 * Usa Mockito para simular las dependencias (repositorios y mappers),
 * por lo que NO requiere conexión a base de datos ni levantar el contexto de Spring.
 *
 * Cada método del service tiene sus propios casos de éxito y de error.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl - Gestión de Propietario")
class PropietarioServiceTest {

    // ──────────────────────────────────────────────
    // Mocks de todas las dependencias del service
    // ──────────────────────────────────────────────
    @Mock private BancoRepository bancoRepository;
    @Mock private CuentaBancariaRepository cuentaBancariaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HuespedRepository huespedRepository;
    @Mock private PropietarioRepository propietarioRepository;

    @Mock private HuespedMapper huespedMapper;
    @Mock private PropietarioMapper propietarioMapper;
    @Mock private CuentaBancariaMapper cuentaBancariaMapper;
    @Mock private TarjetaCreditoMapper tarjetaCreditoMapper;

    // Instancia real del service con los mocks inyectados
    @InjectMocks
    private UserServiceImpl userService;


    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createUsuarioPropietario")
    class CreateUsuarioPropietario {

        /**
         * Caso: se crea un propietario SIN cuenta bancaria ni idHotel (valores null).
         *
         * Esperado: el service mapea el request, guarda el propietario
         * sin tocar bancoRepository ni cuentaBancariaRepository,
         * y retorna el PropietarioDTOResponse correcto.
         */
        @Test
        @DisplayName("sin cuenta bancaria → crea propietario y retorna response")
        void sinCuentaBancaria_creaYRetornaResponse() {
            // GIVEN
            PropietarioDTORequest request = new PropietarioDTORequest(
                    "Juan Perez", "juan@mail.com", "3415551234", "12345678",
                    null,  // cuentaBancaria null
                    null   // idHotel null
            );

            Propietario propietarioEntidad = new Propietario();
            propietarioEntidad.setNombre("Juan Perez");

            Propietario propietarioGuardado = new Propietario();
            propietarioGuardado.setId(1);
            propietarioGuardado.setNombre("Juan Perez");

            PropietarioDTOResponse responseEsperado = new PropietarioDTOResponse(
                    1, "Juan Perez", "juan@mail.com", "3415551234", "12345678",
                    null, null
            );

            when(propietarioMapper.toEntity(request)).thenReturn(propietarioEntidad);
            when(propietarioRepository.save(propietarioEntidad)).thenReturn(propietarioGuardado);
            when(propietarioMapper.toResponse(propietarioGuardado)).thenReturn(responseEsperado);

            // WHEN
            PropietarioDTOResponse resultado = userService.createUsuarioPropietario(request);

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(1);
            assertThat(resultado.nombre()).isEqualTo("Juan Perez");
            assertThat(resultado.cuentaBancaria()).isNull();
            assertThat(resultado.idHotel()).isNull();

            // Verifica que NO se buscó banco ni se guardó cuenta bancaria
            verifyNoInteractions(bancoRepository);
            verifyNoInteractions(cuentaBancariaRepository);
            verify(propietarioRepository).save(propietarioEntidad);
        }

        /**
         * Caso: se crea un propietario CON cuenta bancaria.
         *
         * Esperado: el service busca el banco, mapea la cuenta bancaria,
         * establece las relaciones bidireccionales y persiste correctamente.
         */
        @Test
        @DisplayName("con cuenta bancaria válida → crea propietario con cuenta y retorna response")
        void conCuentaBancariaValida_creaYRetornaResponse() {
            // GIVEN
            CuentaBancariaDTORequest cuentaRequest = new CuentaBancariaDTORequest(
                    "123456789", "1234567890123456789012", "mi.alias.banco", 1
            );

            PropietarioDTORequest request = new PropietarioDTORequest(
                    "Ana Lopez", "ana@mail.com", "3415559876", "87654321",
                    cuentaRequest, null
            );

            Banco banco = new Banco();
            banco.setId(1);
            banco.setNombre("Banco Nacion");

            Propietario propietarioEntidad = new Propietario();
            propietarioEntidad.setNombre("Ana Lopez");

            CuentaBancaria cuentaEntidad = new CuentaBancaria();
            cuentaEntidad.setNumeroCuenta("123456789");

            CuentaBancaria cuentaGuardada = new CuentaBancaria();
            cuentaGuardada.setId(10);
            cuentaGuardada.setNumeroCuenta("123456789");

            Propietario propietarioGuardado = new Propietario();
            propietarioGuardado.setId(2);
            propietarioGuardado.setNombre("Ana Lopez");
            propietarioGuardado.setCuentaBancaria(cuentaGuardada);

            CuentaBancariaDTOResponse cuentaResponse = new CuentaBancariaDTOResponse(
                    10, "123456789", "1234567890123456789012", "mi.alias.banco", "Banco Nacion"
            );

            PropietarioDTOResponse responseEsperado = new PropietarioDTOResponse(
                    2, "Ana Lopez", "ana@mail.com", "3415559876", "87654321",
                    cuentaResponse, null
            );

            when(propietarioMapper.toEntity(request)).thenReturn(propietarioEntidad);
            when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
            when(cuentaBancariaMapper.toEntity(cuentaRequest)).thenReturn(cuentaEntidad);
            when(cuentaBancariaRepository.save(cuentaEntidad)).thenReturn(cuentaGuardada);
            when(propietarioRepository.save(propietarioEntidad)).thenReturn(propietarioGuardado);
            when(propietarioMapper.toResponse(propietarioGuardado)).thenReturn(responseEsperado);

            // WHEN
            PropietarioDTOResponse resultado = userService.createUsuarioPropietario(request);

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(2);
            assertThat(resultado.cuentaBancaria()).isNotNull();
            assertThat(resultado.cuentaBancaria().numeroCuenta()).isEqualTo("123456789");

            // Verifica que se buscó el banco y se guardó la cuenta bancaria
            verify(bancoRepository).findById(1);
            verify(cuentaBancariaRepository).save(cuentaEntidad);
            verify(propietarioRepository).save(propietarioEntidad);
        }

        /**
         * Caso: se intenta crear un propietario con cuenta bancaria pero el banco NO existe.
         *
         * Esperado: se lanza EntityNotFoundException y NO se persiste nada.
         */
        @Test
        @DisplayName("con banco inexistente → lanza EntityNotFoundException")
        void conBancoInexistente_lanzaEntityNotFoundException() {
            // GIVEN
            CuentaBancariaDTORequest cuentaRequest = new CuentaBancariaDTORequest(
                    "123456789", "1234567890123456789012", "mi.alias.banco", 99
            );

            PropietarioDTORequest request = new PropietarioDTORequest(
                    "Carlos Ruiz", "carlos@mail.com", "3415550000", "11223344",
                    cuentaRequest, null
            );

            Propietario propietarioEntidad = new Propietario();
            when(propietarioMapper.toEntity(request)).thenReturn(propietarioEntidad);
            when(bancoRepository.findById(99)).thenReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> userService.createUsuarioPropietario(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");

            // Verifica que nunca se guardó nada
            verifyNoInteractions(cuentaBancariaRepository);
            verify(propietarioRepository, never()).save(any());
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUsuarioPropietario")
    class UpdateUsuarioPropietario {

        /**
         * Caso: se actualiza un propietario existente con datos válidos.
         *
         * Esperado: el service busca el propietario, aplica el mapper de update,
         * guarda y retorna el response actualizado.
         */
        @Test
        @DisplayName("propietario existente → actualiza y retorna response")
        void propietarioExistente_actualizaYRetornaResponse() {
            // GIVEN
            PropietarioDTOUpdate update = new PropietarioDTOUpdate(
                    "Juan Actualizado", "juannuevo@mail.com", "3415550001", "12345678", 5L
            );

            Propietario propietarioExistente = new Propietario();
            propietarioExistente.setId(1);
            propietarioExistente.setNombre("Juan Perez");

            Propietario propietarioActualizado = new Propietario();
            propietarioActualizado.setId(1);
            propietarioActualizado.setNombre("Juan Actualizado");
            propietarioActualizado.setIdHotel(5L);

            PropietarioDTOResponse responseEsperado = new PropietarioDTOResponse(
                    1, "Juan Actualizado", "juannuevo@mail.com", "3415550001", "12345678",
                    null, 5L
            );

            when(propietarioRepository.findById(1)).thenReturn(Optional.of(propietarioExistente));
            // updateEntity es void, Mockito no necesita configuración especial
            when(propietarioRepository.save(propietarioExistente)).thenReturn(propietarioActualizado);
            when(propietarioMapper.toResponse(propietarioActualizado)).thenReturn(responseEsperado);

            // WHEN
            PropietarioDTOResponse resultado = userService.updateUsuarioPropietario(1, update);

            // THEN
            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(1);
            assertThat(resultado.nombre()).isEqualTo("Juan Actualizado");
            assertThat(resultado.idHotel()).isEqualTo(5L);

            verify(propietarioMapper).updateEntity(update, propietarioExistente);
            verify(propietarioRepository).save(propietarioExistente);
        }

        /**
         * Caso: se intenta actualizar un propietario que NO existe en la base de datos.
         *
         * Esperado: se lanza EntityNotFoundException y no se guarda nada.
         */
        @Test
        @DisplayName("propietario no encontrado → lanza EntityNotFoundException")
        void propietarioNoEncontrado_lanzaEntityNotFoundException() {
            // GIVEN
            PropietarioDTOUpdate update = new PropietarioDTOUpdate(
                    "Nombre", "mail@mail.com", "3415550000", "12345678", 1L
            );

            when(propietarioRepository.findById(999)).thenReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> userService.updateUsuarioPropietario(999, update))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("999");

            verify(propietarioRepository, never()).save(any());
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUsuarioPropietario")
    class DeleteUsuarioPropietario {

        /**
         * Caso: se elimina un propietario existente.
         *
         * Esperado: el service busca el propietario y llama a delete.
         */
        @Test
        @DisplayName("propietario existente → elimina correctamente")
        void propietarioExistente_eliminaCorrectamente() {
            // GIVEN
            Propietario propietario = new Propietario();
            propietario.setId(1);

            when(propietarioRepository.findById(1)).thenReturn(Optional.of(propietario));

            // WHEN
            userService.deleteUsuarioPropietario(1);

            // THEN
            verify(propietarioRepository).findById(1);
            verify(propietarioRepository).delete(propietario);
        }

        /**
         * Caso: se intenta eliminar un propietario que NO existe.
         *
         * Esperado: se lanza EntityNotFoundException y no se llama a delete.
         */
        @Test
        @DisplayName("propietario no encontrado → lanza EntityNotFoundException")
        void propietarioNoEncontrado_lanzaEntityNotFoundException() {
            // GIVEN
            when(propietarioRepository.findById(999)).thenReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> userService.deleteUsuarioPropietario(999))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("999");

            verify(propietarioRepository, never()).delete(any());
        }
    }
}
