package edu.utn.frsf.isi.dan.user.service;

import edu.utn.frsf.isi.dan.user.TestDataFactory;
import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dao.CuentaBancariaRepository;
import edu.utn.frsf.isi.dan.user.dao.TarjetaCreditoRepository;
import edu.utn.frsf.isi.dan.user.dto.BancoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOUpdate;
import edu.utn.frsf.isi.dan.user.exception.BancoEnUsoException;
import edu.utn.frsf.isi.dan.user.mapper.BancoMapper;
import edu.utn.frsf.isi.dan.user.model.Banco;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para {@link BancoServiceImpl}.
 * Cubre todos los casos de uso del servicio usando clases de equivalencia.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BancoServiceImpl — Tests unitarios")
class BancoServiceImplTest {

    @Mock
    private BancoRepository bancoRepository;

    @Mock
    private BancoMapper bancoMapper;

    @Mock
    private TarjetaCreditoRepository tarjetaCreditoRepository;

    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;

    @InjectMocks
    private BancoServiceImpl bancoService;

    // ──────────────────────────────────────────────────────────────────────
    // crearBanco
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("crearBanco")
    class CrearBanco {

        @Test
        @DisplayName("Debe crear un banco correctamente y retornar el DTO response")
        void debeCrearBancoExitosamente() {
            BancoDTORequest request = TestDataFactory.bancoDTORequest();
            Banco banco = TestDataFactory.banco();
            banco.setId(null); // antes de persistir
            Banco bancoGuardado = TestDataFactory.banco();
            BancoDTOResponse response = TestDataFactory.bancoDTOResponse();

            when(bancoMapper.toEntity(request)).thenReturn(banco);
            when(bancoRepository.save(banco)).thenReturn(bancoGuardado);
            when(bancoMapper.toResponse(bancoGuardado)).thenReturn(response);

            BancoDTOResponse result = bancoService.crearBanco(request);

            assertThat(result).isEqualTo(response);
            verify(bancoRepository).save(banco);
            verify(bancoMapper).toEntity(request);
            verify(bancoMapper).toResponse(bancoGuardado);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // actualizarBanco
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("actualizarBanco")
    class ActualizarBanco {

        @Test
        @DisplayName("Debe actualizar banco existente y retornar el DTO response")
        void debeActualizarBancoExistente() {
            BancoDTOUpdate update = TestDataFactory.bancoDTOUpdate();
            Banco banco = TestDataFactory.banco();
            Banco bancoActualizado = TestDataFactory.banco(1, "Banco Provincia");
            BancoDTOResponse response = new BancoDTOResponse(1, "Banco Provincia");

            when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
            doNothing().when(bancoMapper).updateEntity(update, banco);
            when(bancoRepository.save(banco)).thenReturn(bancoActualizado);
            when(bancoMapper.toResponse(bancoActualizado)).thenReturn(response);

            BancoDTOResponse result = bancoService.actualizarBanco(1, update);

            assertThat(result.nombre()).isEqualTo("Banco Provincia");
            verify(bancoMapper).updateEntity(update, banco);
            verify(bancoRepository).save(banco);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el banco no existe")
        void debeLanzarExcepcionCuandoBancoNoExiste() {
            when(bancoRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bancoService.actualizarBanco(99, TestDataFactory.bancoDTOUpdate()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");

            verify(bancoRepository, never()).save(any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // eliminarBanco
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("eliminarBanco")
    class EliminarBanco {

        @Test
        @DisplayName("Debe eliminar un banco existente sin retornar valor")
        void debeEliminarBancoExistente() {
            Banco banco = TestDataFactory.banco();

            when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
            when(tarjetaCreditoRepository.existsByBancoId(1)).thenReturn(false);
            when(cuentaBancariaRepository.existsByBancoId(1)).thenReturn(false);
            doNothing().when(bancoRepository).delete(banco);

            bancoService.eliminarBanco(1);

            verify(bancoRepository).delete(banco);
            verify(tarjetaCreditoRepository).existsByBancoId(1);
            verify(cuentaBancariaRepository).existsByBancoId(1);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el banco no existe")
        void debeLanzarExcepcionCuandoBancoNoExiste() {
            when(bancoRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bancoService.eliminarBanco(99))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");

            verify(bancoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Debe lanzar BancoEnUsoException cuando el banco está referenciado por tarjetas de crédito")
        void debeLanzarBancoEnUsoCuandoReferenciadoPorTarjeta() {
            Banco banco = TestDataFactory.banco();

            when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
            when(tarjetaCreditoRepository.existsByBancoId(1)).thenReturn(true);

            assertThatThrownBy(() -> bancoService.eliminarBanco(1))
                    .isInstanceOf(BancoEnUsoException.class)
                    .hasMessageContaining("1");

            verify(bancoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Debe lanzar BancoEnUsoException cuando el banco está referenciado por cuentas bancarias")
        void debeLanzarBancoEnUsoCuandoReferenciadoPorCuentaBancaria() {
            Banco banco = TestDataFactory.banco();

            when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
            when(tarjetaCreditoRepository.existsByBancoId(1)).thenReturn(false);
            when(cuentaBancariaRepository.existsByBancoId(1)).thenReturn(true);

            assertThatThrownBy(() -> bancoService.eliminarBanco(1))
                    .isInstanceOf(BancoEnUsoException.class)
                    .hasMessageContaining("1");

            verify(bancoRepository, never()).delete(any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // buscarBancoPorId
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buscarBancoPorId")
    class BuscarBancoPorId {

        @Test
        @DisplayName("Debe retornar el DTO response del banco encontrado")
        void debeRetornarBancoExistente() {
            Banco banco = TestDataFactory.banco();
            BancoDTOResponse response = TestDataFactory.bancoDTOResponse();

            when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
            when(bancoMapper.toResponse(banco)).thenReturn(response);

            BancoDTOResponse result = bancoService.buscarBancoPorId(1);

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando el banco no existe")
        void debeLanzarExcepcionCuandoBancoNoExiste() {
            when(bancoRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bancoService.buscarBancoPorId(99))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // listarBancos
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarBancos")
    class ListarBancos {

        @Test
        @DisplayName("Debe retornar lista con todos los bancos")
        void debeRetornarListaDeBancos() {
            List<Banco> bancos = List.of(
                    TestDataFactory.banco(1, "Banco Nación"),
                    TestDataFactory.banco(2, "Banco Provincia")
            );
            List<BancoDTOResponse> responses = List.of(
                    new BancoDTOResponse(1, "Banco Nación"),
                    new BancoDTOResponse(2, "Banco Provincia")
            );

            when(bancoRepository.findAll()).thenReturn(bancos);
            when(bancoMapper.toResponse(bancos.get(0))).thenReturn(responses.get(0));
            when(bancoMapper.toResponse(bancos.get(1))).thenReturn(responses.get(1));

            List<BancoDTOResponse> result = bancoService.listarBancos();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(BancoDTOResponse::nombre)
                    .containsExactlyInAnyOrder("Banco Nación", "Banco Provincia");
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay bancos")
        void debeRetornarListaVacia() {
            when(bancoRepository.findAll()).thenReturn(List.of());

            List<BancoDTOResponse> result = bancoService.listarBancos();

            assertThat(result).isEmpty();
        }
    }
}
