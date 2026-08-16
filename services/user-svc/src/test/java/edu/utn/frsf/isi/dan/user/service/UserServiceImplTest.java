package edu.utn.frsf.isi.dan.user.service;

import edu.utn.frsf.isi.dan.user.TestDataFactory;
import edu.utn.frsf.isi.dan.user.dao.*;
import edu.utn.frsf.isi.dan.user.dto.*;
import edu.utn.frsf.isi.dan.user.exception.TarjetaPrincipalException;
import edu.utn.frsf.isi.dan.user.mapper.*;
import edu.utn.frsf.isi.dan.user.model.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para {@link UserServiceImpl}.
 * Usa Mockito para aislar dependencias. Cubre los paths felices y los caminos de error
 * para cada operación de dominio: Huesped, Propietario, búsquedas y gestión de tarjetas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl — Tests unitarios")
class UserServiceImplTest {

    @Mock private BancoRepository bancoRepository;
    @Mock private CuentaBancariaRepository cuentaBancariaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HuespedRepository huespedRepository;
    @Mock private PropietarioRepository propietarioRepository;
    @Mock private TarjetaCreditoRepository tarjetaCreditoRepository;
    @Mock private HuespedMapper huespedMapper;
    @Mock private PropietarioMapper propietarioMapper;
    @Mock private CuentaBancariaMapper cuentaBancariaMapper;
    @Mock private TarjetaCreditoMapper tarjetaCreditoMapper;
    @Mock private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // ──────────────────────────────────────────────────────────────────────
    // HUESPED — CRUD
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createUsuarioHuesped")
    class CreateUsuarioHuesped {

        @Test
        @DisplayName("Debe crear un huesped con tarjeta de crédito válida")
        void debeCrearHuespedConTarjetaValida() {
            HuespedDTORequest request = TestDataFactory.huespedDTORequest();
            Banco banco = TestDataFactory.banco();
            Huesped huesped = TestDataFactory.huesped();
            huesped.setId(null);
            Huesped huespedGuardado = TestDataFactory.huesped();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(huespedGuardado, banco, true);
            HuespedDTOResponse response = TestDataFactory.huespedDTOResponse();

            when(bancoRepository.findById(request.tarjetaCredito().bancoId())).thenReturn(Optional.of(banco));
            when(huespedMapper.toEntity(request)).thenReturn(huesped);
            when(tarjetaCreditoMapper.toEntity(request.tarjetaCredito())).thenReturn(tarjeta);
            when(huespedRepository.save(huesped)).thenReturn(huespedGuardado);
            when(huespedMapper.toResponse(huespedGuardado)).thenReturn(response);

            HuespedDTOResponse result = userService.createUsuarioHuesped(request);

            assertThat(result).isEqualTo(response);
            verify(huespedRepository).save(huesped);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el banco de la tarjeta no existe")
        void debeLanzarExcepcionSiBancoNoExiste() {
            HuespedDTORequest request = TestDataFactory.huespedDTORequest();

            when(bancoRepository.findById(request.tarjetaCredito().bancoId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUsuarioHuesped(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(String.valueOf(request.tarjetaCredito().bancoId()));

            verify(huespedRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe inicializar lista de tarjetas cuando mapper retorna null")
        void debeInicializarListaTarjetasSiMapperRetornaNull() {
            HuespedDTORequest request = TestDataFactory.huespedDTORequest();
            Banco banco = TestDataFactory.banco();
            Huesped huesped = TestDataFactory.huesped();
            huesped.setTarjetaCredito(null);
            Huesped guardado = TestDataFactory.huesped();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(guardado, banco, true);

            when(bancoRepository.findById(request.tarjetaCredito().bancoId())).thenReturn(Optional.of(banco));
            when(huespedMapper.toEntity(request)).thenReturn(huesped);
            when(tarjetaCreditoMapper.toEntity(request.tarjetaCredito())).thenReturn(tarjeta);
            when(huespedRepository.save(huesped)).thenReturn(guardado);
            when(huespedMapper.toResponse(guardado)).thenReturn(TestDataFactory.huespedDTOResponse());

            HuespedDTOResponse result = userService.createUsuarioHuesped(request);

            assertThat(result.id()).isEqualTo(1);
            assertThat(huesped.getTarjetaCredito()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateUsuarioHuesped")
    class UpdateUsuarioHuesped {

        @Test
        @DisplayName("Debe actualizar el huesped existente")
        void debeActualizarHuespedExistente() {
            HuespedDTOUpdate update = TestDataFactory.huespedDTOUpdate();
            Huesped huesped = TestDataFactory.huesped();
            Huesped huespedActualizado = TestDataFactory.huesped();
            HuespedDTOResponse response = TestDataFactory.huespedDTOResponse();

            when(huespedRepository.findById(1)).thenReturn(Optional.of(huesped));
            doNothing().when(huespedMapper).updateEntity(update, huesped);
            when(huespedRepository.save(huesped)).thenReturn(huespedActualizado);
            when(huespedMapper.toResponse(huespedActualizado)).thenReturn(response);

            HuespedDTOResponse result = userService.updateUsuarioHuesped(1, update);

            assertThat(result).isEqualTo(response);
            verify(huespedMapper).updateEntity(update, huesped);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el huesped no existe")
        void debeLanzarExcepcionSiHuespedNoExiste() {
            when(huespedRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUsuarioHuesped(99, TestDataFactory.huespedDTOUpdate()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");

            verify(huespedRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteUsuarioHuesped")
    class DeleteUsuarioHuesped {

        @Test
        @DisplayName("Debe eliminar un huesped existente (cascade elimina las tarjetas)")
        void debeEliminarHuespedExistente() {
            Huesped huesped = TestDataFactory.huesped();

            when(huespedRepository.findById(1)).thenReturn(Optional.of(huesped));
            doNothing().when(huespedRepository).delete(huesped);

            userService.deleteUsuarioHuesped(1);

            verify(huespedRepository).delete(huesped);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el huesped no existe")
        void debeLanzarExcepcionSiHuespedNoExiste() {
            when(huespedRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUsuarioHuesped(99))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");

            verify(huespedRepository, never()).delete(any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // PROPIETARIO — CRUD
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createUsuarioPropietario")
    class CreateUsuarioPropietario {

        @Test
        @DisplayName("Debe crear un propietario con cuenta bancaria válida")
        void debeCrearPropietarioConCuentaValida() {
            PropietarioDTORequest request = TestDataFactory.propietarioDTORequest();
            Banco banco = TestDataFactory.banco();
            Propietario propietario = TestDataFactory.propietario();
            propietario.setId(null);
            CuentaBancaria cuenta = TestDataFactory.cuentaBancaria();
            Propietario propietarioGuardado = TestDataFactory.propietario();
            PropietarioDTOResponse response = TestDataFactory.propietarioDTOResponse();

            when(propietarioMapper.toEntity(request)).thenReturn(propietario);
            when(bancoRepository.findById(request.cuentaBancaria().bancoId())).thenReturn(Optional.of(banco));
            when(cuentaBancariaMapper.toEntity(request.cuentaBancaria())).thenReturn(cuenta);
            when(cuentaBancariaRepository.save(cuenta)).thenReturn(cuenta);
            when(propietarioRepository.save(propietario)).thenReturn(propietarioGuardado);
            when(propietarioMapper.toResponse(propietarioGuardado)).thenReturn(response);

            PropietarioDTOResponse result = userService.createUsuarioPropietario(request);

            assertThat(result).isEqualTo(response);
            verify(propietarioRepository).save(propietario);
            verify(cuentaBancariaRepository).save(cuenta);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el banco de la cuenta no existe")
        void debeLanzarExcepcionSiBancoNoExiste() {
            PropietarioDTORequest request = TestDataFactory.propietarioDTORequest();

            when(bancoRepository.findById(request.cuentaBancaria().bancoId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUsuarioPropietario(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(String.valueOf(request.cuentaBancaria().bancoId()));

            verify(propietarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateUsuarioPropietario")
    class UpdateUsuarioPropietario {

        @Test
        @DisplayName("Debe actualizar los datos del propietario existente")
        void debeActualizarPropietarioExistente() {
            PropietarioDTOUpdate update = TestDataFactory.propietarioDTOUpdate();
            Propietario propietario = TestDataFactory.propietario();
            Propietario propietarioActualizado = TestDataFactory.propietario();
            PropietarioDTOResponse response = TestDataFactory.propietarioDTOResponse();

            when(propietarioRepository.findById(1)).thenReturn(Optional.of(propietario));
            doNothing().when(propietarioMapper).updateEntity(update, propietario);
            when(propietarioRepository.save(propietario)).thenReturn(propietarioActualizado);
            when(propietarioMapper.toResponse(propietarioActualizado)).thenReturn(response);

            PropietarioDTOResponse result = userService.updateUsuarioPropietario(1, update);

            assertThat(result).isEqualTo(response);
            verify(propietarioMapper).updateEntity(update, propietario);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el propietario no existe")
        void debeLanzarExcepcionSiPropietarioNoExiste() {
            when(propietarioRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUsuarioPropietario(99, TestDataFactory.propietarioDTOUpdate()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");

            verify(propietarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteUsuarioPropietario")
    class DeleteUsuarioPropietario {

        @Test
        @DisplayName("Debe eliminar propietario con cuenta bancaria (limpia relación bidireccional)")
        void debeEliminarPropietarioConCuenta() {
            Propietario propietario = TestDataFactory.propietario();
            CuentaBancaria cuenta = propietario.getCuentaBancaria();

            when(propietarioRepository.findById(1)).thenReturn(Optional.of(propietario));
            when(propietarioRepository.saveAndFlush(propietario)).thenReturn(propietario);
            doNothing().when(cuentaBancariaRepository).delete(cuenta);
            doNothing().when(propietarioRepository).delete(propietario);

            userService.deleteUsuarioPropietario(1);

            verify(propietarioRepository).saveAndFlush(propietario);
            verify(cuentaBancariaRepository).delete(cuenta);
            verify(propietarioRepository).delete(propietario);
        }

        @Test
        @DisplayName("Debe eliminar propietario sin cuenta bancaria directamente")
        void debeEliminarPropietarioSinCuenta() {
            Propietario propietario = TestDataFactory.propietario();
            propietario.setCuentaBancaria(null);

            when(propietarioRepository.findById(1)).thenReturn(Optional.of(propietario));
            doNothing().when(propietarioRepository).delete(propietario);

            userService.deleteUsuarioPropietario(1);

            verify(propietarioRepository).delete(propietario);
            verify(propietarioRepository, never()).saveAndFlush(any());
            verify(cuentaBancariaRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el propietario no existe")
        void debeLanzarExcepcionSiPropietarioNoExiste() {
            when(propietarioRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUsuarioPropietario(99))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");

            verify(propietarioRepository, never()).delete(any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // BÚSQUEDAS
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buscarPorNombre")
    class BuscarPorNombre {

        @Test
        @DisplayName("Debe retornar página con resultados cuando hay coincidencias")
        void debeRetornarResultadosPorNombre() {
            Pageable pageable = PageRequest.of(0, 10);
            Huesped huesped = TestDataFactory.huesped();
            Page<Usuario> page = new PageImpl<>(List.of(huesped));
            UsuarioDTOResponse response = TestDataFactory.usuarioDTOResponseHuesped();

            when(usuarioRepository.findByNombreContainingIgnoreCase("Juan", pageable)).thenReturn(page);
            when(usuarioMapper.toResponse(huesped)).thenReturn(response);

            Page<UsuarioDTOResponse> result = userService.buscarPorNombre("Juan", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(response);
        }

        @Test
        @DisplayName("Debe retornar página vacía cuando no hay resultados")
        void debeRetornarPaginaVacia() {
            Pageable pageable = PageRequest.of(0, 10);
            when(usuarioRepository.findByNombreContainingIgnoreCase("XYZNOTFOUND", pageable))
                    .thenReturn(Page.empty());

            Page<UsuarioDTOResponse> result = userService.buscarPorNombre("XYZNOTFOUND", pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorDni")
    class BuscarPorDni {

        @Test
        @DisplayName("Debe retornar página con resultados cuando hay coincidencias de DNI parcial")
        void debeRetornarResultadosPorDni() {
            Pageable pageable = PageRequest.of(0, 10);
            Huesped huesped = TestDataFactory.huesped();
            Page<Usuario> page = new PageImpl<>(List.of(huesped));
            UsuarioDTOResponse response = TestDataFactory.usuarioDTOResponseHuesped();

            when(usuarioRepository.findByDniContaining("1234", pageable)).thenReturn(page);
            when(usuarioMapper.toResponse(huesped)).thenReturn(response);

            Page<UsuarioDTOResponse> result = userService.buscarPorDni("1234", pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Debe retornar página vacía cuando no hay resultados")
        void debeRetornarPaginaVacia() {
            Pageable pageable = PageRequest.of(0, 10);
            when(usuarioRepository.findByDniContaining("99999999", pageable))
                    .thenReturn(Page.empty());

            Page<UsuarioDTOResponse> result = userService.buscarPorDni("99999999", pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorDniExacto")
    class BuscarPorDniExacto {

        @Test
        @DisplayName("Debe retornar el DTO del usuario cuando el DNI coincide exactamente")
        void debeRetornarUsuarioPorDniExacto() {
            Huesped huesped = TestDataFactory.huesped();
            UsuarioDTOResponse response = TestDataFactory.usuarioDTOResponseHuesped();

            when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(huesped));
            when(usuarioMapper.toResponse(huesped)).thenReturn(response);

            UsuarioDTOResponse result = userService.buscarPorDniExacto("12345678");

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el DNI no existe")
        void debeLanzarExcepcionSiDniNoExiste() {
            when(usuarioRepository.findByDni("00000000")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.buscarPorDniExacto("00000000"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("00000000");
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // GESTIÓN DE TARJETAS
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("agregarTarjeta")
    class AgregarTarjeta {

        @Test
        @DisplayName("Debe agregar tarjeta no principal sin desmarcar la anterior")
        void debeAgregarTarjetaNoPrincipal() {
            TarjetaCreditoDTORequest request = TestDataFactory.tarjetaCreditoDTORequest(false, 1);
            Huesped huesped = TestDataFactory.huesped();
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(huesped, banco, false);
            TarjetaCredito tarjetaGuardada = TestDataFactory.tarjetaCredito(2, huesped, banco, false);
            TarjetaCreditoDTOResponse response = new TarjetaCreditoDTOResponse(2, "5500005555555559", "Juan Pérez", "06/26", false, "Banco Nación");

            when(huespedRepository.findById(1)).thenReturn(Optional.of(huesped));
            when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
            when(tarjetaCreditoMapper.toEntity(request)).thenReturn(tarjeta);
            when(tarjetaCreditoRepository.save(tarjeta)).thenReturn(tarjetaGuardada);
            when(tarjetaCreditoMapper.toResponse(tarjetaGuardada)).thenReturn(response);

            TarjetaCreditoDTOResponse result = userService.agregarTarjeta(1, request);

            assertThat(result).isEqualTo(response);
            // No debe buscar la tarjeta principal anterior si esPrincipal=false
            verify(tarjetaCreditoRepository, never()).findByHuespedIdAndEsPrincipalTrue(any());
        }

        @Test
        @DisplayName("Debe desmarcar la tarjeta principal anterior al agregar nueva como principal")
        void debeDesmarcarTarjetaAnteriorAlAgregarPrincipal() {
            TarjetaCreditoDTORequest request = TestDataFactory.tarjetaCreditoDTORequest(true, 1);
            Huesped huesped = TestDataFactory.huesped();
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjetaAnterior = TestDataFactory.tarjetaCredito(10, huesped, banco, true);
            TarjetaCredito nuevaTarjeta = TestDataFactory.tarjetaCredito(huesped, banco, true);
            TarjetaCredito tarjetaGuardada = TestDataFactory.tarjetaCredito(11, huesped, banco, true);
            TarjetaCreditoDTOResponse response = TestDataFactory.tarjetaCreditoDTOResponse();

            when(huespedRepository.findById(1)).thenReturn(Optional.of(huesped));
            when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
            when(tarjetaCreditoRepository.findByHuespedIdAndEsPrincipalTrue(1))
                    .thenReturn(Optional.of(tarjetaAnterior));
            when(tarjetaCreditoRepository.save(tarjetaAnterior)).thenReturn(tarjetaAnterior);
            when(tarjetaCreditoMapper.toEntity(request)).thenReturn(nuevaTarjeta);
            when(tarjetaCreditoRepository.save(nuevaTarjeta)).thenReturn(tarjetaGuardada);
            when(tarjetaCreditoMapper.toResponse(tarjetaGuardada)).thenReturn(response);

            userService.agregarTarjeta(1, request);

            assertThat(tarjetaAnterior.getEsPrincipal()).isFalse();
            verify(tarjetaCreditoRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el huesped no existe")
        void debeLanzarExcepcionSiHuespedNoExiste() {
            when(huespedRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.agregarTarjeta(99, TestDataFactory.tarjetaCreditoDTORequest()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el banco de la tarjeta no existe")
        void debeLanzarExcepcionSiBancoNoExiste() {
            TarjetaCreditoDTORequest request = TestDataFactory.tarjetaCreditoDTORequest(false, 99);
            Huesped huesped = TestDataFactory.huesped();

            when(huespedRepository.findById(1)).thenReturn(Optional.of(huesped));
            when(bancoRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.agregarTarjeta(1, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("eliminarTarjeta")
    class EliminarTarjeta {

        @Test
        @DisplayName("Debe eliminar una tarjeta no principal correctamente")
        void debeEliminarTarjetaNoPrincipal() {
            Huesped huesped = TestDataFactory.huesped();
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(2, huesped, banco, false);

            when(tarjetaCreditoRepository.findById(2)).thenReturn(Optional.of(tarjeta));
            doNothing().when(tarjetaCreditoRepository).delete(tarjeta);

            userService.eliminarTarjeta(1, 2);

            verify(tarjetaCreditoRepository).delete(tarjeta);
        }

        @Test
        @DisplayName("Debe lanzar TarjetaPrincipalException al intentar eliminar la tarjeta principal")
        void debeLanzarExcepcionAlEliminarTarjetaPrincipal() {
            Huesped huesped = TestDataFactory.huesped();
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(1, huesped, banco, true);

            when(tarjetaCreditoRepository.findById(1)).thenReturn(Optional.of(tarjeta));

            assertThatThrownBy(() -> userService.eliminarTarjeta(1, 1))
                    .isInstanceOf(TarjetaPrincipalException.class);

            verify(tarjetaCreditoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando la tarjeta no pertenece al huesped")
        void debeLanzarExcepcionSiTarjetaNoPerteneceAlHuesped() {
            Huesped huespedDueño = TestDataFactory.huesped();
            huespedDueño.setId(2); // diferente ID
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(1, huespedDueño, banco, false);

            when(tarjetaCreditoRepository.findById(1)).thenReturn(Optional.of(tarjeta));

            // LA tarjeta pertenece al huesped 2, pero se intenta eliminar para el huesped 1
            assertThatThrownBy(() -> userService.eliminarTarjeta(1, 1))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(tarjetaCreditoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando la tarjeta no existe")
        void debeLanzarExcepcionSiTarjetaNoExiste() {
            when(tarjetaCreditoRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.eliminarTarjeta(1, 99))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Debe cambiar tarjeta principal aunque no exista principal previa")
        void debeCambiarPrincipalSinPrincipalPrevia() {
            Huesped huesped = TestDataFactory.huesped();
            Banco banco = TestDataFactory.banco();
            TarjetaCredito nuevaPrincipal = TestDataFactory.tarjetaCredito(2, huesped, banco, false);
            TarjetaCreditoDTOResponse response = new TarjetaCreditoDTOResponse(2, "5500005555555559", "Juan Pérez", "06/26", true, "Banco Nación");

            when(tarjetaCreditoRepository.findById(2)).thenReturn(Optional.of(nuevaPrincipal));
            when(tarjetaCreditoRepository.findByHuespedIdAndEsPrincipalTrue(1)).thenReturn(Optional.empty());
            when(tarjetaCreditoRepository.save(nuevaPrincipal)).thenReturn(nuevaPrincipal);
            when(tarjetaCreditoMapper.toResponse(nuevaPrincipal)).thenReturn(response);

            TarjetaCreditoDTOResponse result = userService.cambiarTarjetaPrincipal(1, 2);

            assertThat(result).isEqualTo(response);
            assertThat(nuevaPrincipal.getEsPrincipal()).isTrue();
        }
    }

    @Nested
    @DisplayName("cambiarTarjetaPrincipal")
    class CambiarTarjetaPrincipal {

        @Test
        @DisplayName("Debe cambiar la tarjeta principal y desmarcar la anterior")
        void debeCambiarTarjetaPrincipal() {
            Huesped huesped = TestDataFactory.huesped();
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjetaAnterior = TestDataFactory.tarjetaCredito(1, huesped, banco, true);
            TarjetaCredito nuevaPrincipal = TestDataFactory.tarjetaCredito(2, huesped, banco, false);
            TarjetaCreditoDTOResponse response = new TarjetaCreditoDTOResponse(2, "5500005555555559", "Juan Pérez", "06/26", true, "Banco Nación");

            when(tarjetaCreditoRepository.findById(2)).thenReturn(Optional.of(nuevaPrincipal));
            when(tarjetaCreditoRepository.findByHuespedIdAndEsPrincipalTrue(1))
                    .thenReturn(Optional.of(tarjetaAnterior));
            when(tarjetaCreditoRepository.save(tarjetaAnterior)).thenReturn(tarjetaAnterior);
            when(tarjetaCreditoRepository.save(nuevaPrincipal)).thenReturn(nuevaPrincipal);
            when(tarjetaCreditoMapper.toResponse(nuevaPrincipal)).thenReturn(response);

            TarjetaCreditoDTOResponse result = userService.cambiarTarjetaPrincipal(1, 2);

            assertThat(result).isEqualTo(response);
            assertThat(tarjetaAnterior.getEsPrincipal()).isFalse();
            assertThat(nuevaPrincipal.getEsPrincipal()).isTrue();
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si la tarjeta ya es principal")
        void debeLanzarExcepcionSiTarjetaYaEsPrincipal() {
            Huesped huesped = TestDataFactory.huesped();
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(1, huesped, banco, true);

            when(tarjetaCreditoRepository.findById(1)).thenReturn(Optional.of(tarjeta));

            assertThatThrownBy(() -> userService.cambiarTarjetaPrincipal(1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ya es la tarjeta principal");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si la tarjeta no pertenece al huesped")
        void debeLanzarExcepcionSiTarjetaNoPerteneceAlHuesped() {
            Huesped huespedDueño = TestDataFactory.huesped();
            huespedDueño.setId(2);
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(1, huespedDueño, banco, false);

            when(tarjetaCreditoRepository.findById(1)).thenReturn(Optional.of(tarjeta));

            assertThatThrownBy(() -> userService.cambiarTarjetaPrincipal(1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando la tarjeta no existe")
        void debeLanzarExcepcionSiTarjetaNoExiste() {
            when(tarjetaCreditoRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.cambiarTarjetaPrincipal(1, 99))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("listarTarjetas")
    class ListarTarjetas {

        @Test
        @DisplayName("Debe retornar página con tarjetas del huesped")
        void debeRetornarTarjetasDelHuesped() {
            Pageable pageable = PageRequest.of(0, 10);
            Huesped huesped = TestDataFactory.huesped();
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(huesped, banco, true);
            Page<TarjetaCredito> page = new PageImpl<>(List.of(tarjeta));
            TarjetaCreditoDTOResponse response = TestDataFactory.tarjetaCreditoDTOResponse();

            when(huespedRepository.findById(1)).thenReturn(Optional.of(huesped));
            when(tarjetaCreditoRepository.findByHuespedId(1, pageable)).thenReturn(page);
            when(tarjetaCreditoMapper.toResponse(tarjeta)).thenReturn(response);

            Page<TarjetaCreditoDTOResponse> result = userService.listarTarjetas(1, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(response);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el huesped no existe")
        void debeLanzarExcepcionSiHuespedNoExiste() {
            when(huespedRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.listarTarjetas(99, PageRequest.of(0, 10)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("obtenerTarjetaPrincipalPorDni")
    class ObtenerTarjetaPrincipalPorDni {

        @Test
        @DisplayName("Debe retornar el número de la tarjeta principal cuando el huésped existe")
        void debeRetornarNumeroTarjetaPrincipal() {
            Huesped huesped = TestDataFactory.huesped();
            Banco banco = TestDataFactory.banco();
            TarjetaCredito tarjeta = TestDataFactory.tarjetaCredito(huesped, banco, true);

            when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(huesped));
            when(tarjetaCreditoRepository.findByHuespedIdAndEsPrincipalTrue(1))
                    .thenReturn(Optional.of(tarjeta));

            TarjetaPrincipalDTO result = userService.obtenerTarjetaPrincipalPorDni("12345678");

            assertThat(result.numero()).isEqualTo("4111111111111111");
            verify(usuarioRepository).findByDni("12345678");
            verify(tarjetaCreditoRepository).findByHuespedIdAndEsPrincipalTrue(1);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el DNI no existe")
        void debeLanzarExcepcionSiDniNoExiste() {
            when(usuarioRepository.findByDni("00000000")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.obtenerTarjetaPrincipalPorDni("00000000"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("00000000");

            verify(tarjetaCreditoRepository, never()).findByHuespedIdAndEsPrincipalTrue(any());
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el usuario no es un huésped")
        void debeLanzarExcepcionSiUsuarioNoEsHuesped() {
            Propietario propietario = TestDataFactory.propietario();

            when(usuarioRepository.findByDni("87654321")).thenReturn(Optional.of(propietario));

            assertThatThrownBy(() -> userService.obtenerTarjetaPrincipalPorDni("87654321"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("no es un huésped");

            verify(tarjetaCreditoRepository, never()).findByHuespedIdAndEsPrincipalTrue(any());
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el huésped no tiene tarjeta principal")
        void debeLanzarExcepcionSiHuespedNoTieneTarjetaPrincipal() {
            Huesped huesped = TestDataFactory.huesped();

            when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(huesped));
            when(tarjetaCreditoRepository.findByHuespedIdAndEsPrincipalTrue(1))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.obtenerTarjetaPrincipalPorDni("12345678"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("no tiene tarjeta principal");
        }
    }
}
