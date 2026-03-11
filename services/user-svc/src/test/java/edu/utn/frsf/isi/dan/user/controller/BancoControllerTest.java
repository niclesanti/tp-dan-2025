package edu.utn.frsf.isi.dan.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.user.TestDataFactory;
import edu.utn.frsf.isi.dan.user.dto.BancoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOUpdate;
import edu.utn.frsf.isi.dan.user.service.BancoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para {@link BancoController} usando {@code @WebMvcTest}.
 * Los tests verifican rutas, HTTP status codes y manejo de errores.
 * El {@link BancoService} es mockeado con {@code @MockBean}.
 */
@WebMvcTest(BancoController.class)
@DisplayName("BancoController — Tests @WebMvcTest")
class BancoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BancoService bancoService;

    // ──────────────────────────────────────────────────────────────────────
    // POST /bancos — crearBanco
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /bancos")
    class PostBancos {

        @Test
        @DisplayName("Debe retornar 201 y el banco creado con datos válidos")
        void debeCrearBancoConDatosValidos() throws Exception {
            BancoDTORequest request = TestDataFactory.bancoDTORequest();
            BancoDTOResponse response = TestDataFactory.bancoDTOResponse();

            when(bancoService.crearBanco(any())).thenReturn(response);

            mockMvc.perform(post("/bancos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Banco Nación"));

            verify(bancoService).crearBanco(any());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el nombre está en blanco")
        void debeRetornar400ConNombreEnBlanco() throws Exception {
            BancoDTORequest request = new BancoDTORequest("");

            mockMvc.perform(post("/bancos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(bancoService, never()).crearBanco(any());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el nombre es nulo")
        void debeRetornar400ConNombreNulo() throws Exception {
            mockMvc.perform(post("/bancos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nombre\": null}"))
                    .andExpect(status().isBadRequest());

            verify(bancoService, never()).crearBanco(any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // PUT /bancos/{id} — actualizarBanco
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /bancos/{id}")
    class PutBancosId {

        @Test
        @DisplayName("Debe retornar 200 y el banco actualizado")
        void debeActualizarBancoExistente() throws Exception {
            BancoDTOUpdate update = TestDataFactory.bancoDTOUpdate();
            BancoDTOResponse response = new BancoDTOResponse(1, "Banco Provincia");

            when(bancoService.actualizarBanco(eq(1), any())).thenReturn(response);

            mockMvc.perform(put("/bancos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Banco Provincia"));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el banco no existe")
        void debeRetornar404CuandoBancoNoExiste() throws Exception {
            when(bancoService.actualizarBanco(eq(99), any()))
                    .thenThrow(new EntityNotFoundException("Banco no encontrado con ID: 99"));

            mockMvc.perform(put("/bancos/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TestDataFactory.bancoDTOUpdate())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Banco no encontrado con ID: 99"));
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el nombre está en blanco")
        void debeRetornar400ConNombreEnBlanco() throws Exception {
            mockMvc.perform(put("/bancos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nombre\": \"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // DELETE /bancos/{id} — eliminarBanco
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /bancos/{id}")
    class DeleteBancosId {

        @Test
        @DisplayName("Debe retornar 204 al eliminar un banco existente")
        void debeEliminarBancoExistente() throws Exception {
            doNothing().when(bancoService).eliminarBanco(1);

            mockMvc.perform(delete("/bancos/1"))
                    .andExpect(status().isNoContent());

            verify(bancoService).eliminarBanco(1);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el banco no existe")
        void debeRetornar404CuandoBancoNoExiste() throws Exception {
            doThrow(new EntityNotFoundException("Banco no encontrado con ID: 99"))
                    .when(bancoService).eliminarBanco(99);

            mockMvc.perform(delete("/bancos/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /bancos/{id} — buscarBancoPorId
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /bancos/{id}")
    class GetBancosId {

        @Test
        @DisplayName("Debe retornar 200 con el banco cuando existe")
        void debeRetornarBancoExistente() throws Exception {
            BancoDTOResponse response = TestDataFactory.bancoDTOResponse();

            when(bancoService.buscarBancoPorId(1)).thenReturn(response);

            mockMvc.perform(get("/bancos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Banco Nación"));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el banco no existe")
        void debeRetornar404CuandoBancoNoExiste() throws Exception {
            when(bancoService.buscarBancoPorId(99))
                    .thenThrow(new EntityNotFoundException("Banco no encontrado con ID: 99"));

            mockMvc.perform(get("/bancos/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /bancos — listarBancos
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /bancos")
    class GetBancos {

        @Test
        @DisplayName("Debe retornar 200 con la lista de bancos")
        void debeRetornarListaDeBancos() throws Exception {
            List<BancoDTOResponse> bancos = List.of(
                    new BancoDTOResponse(1, "Banco Nación"),
                    new BancoDTOResponse(2, "Banco Provincia")
            );

            when(bancoService.listarBancos()).thenReturn(bancos);

            mockMvc.perform(get("/bancos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nombre").value("Banco Nación"))
                    .andExpect(jsonPath("$[1].nombre").value("Banco Provincia"));
        }

        @Test
        @DisplayName("Debe retornar 200 con lista vacía cuando no hay bancos")
        void debeRetornarListaVacia() throws Exception {
            when(bancoService.listarBancos()).thenReturn(List.of());

            mockMvc.perform(get("/bancos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
