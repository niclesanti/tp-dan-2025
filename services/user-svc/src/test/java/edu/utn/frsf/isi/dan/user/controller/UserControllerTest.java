package edu.utn.frsf.isi.dan.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.user.TestDataFactory;
import edu.utn.frsf.isi.dan.user.dto.*;
import edu.utn.frsf.isi.dan.user.exception.TarjetaPrincipalException;
import edu.utn.frsf.isi.dan.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para {@link UserController} usando {@code @WebMvcTest}.
 * Verifica los HTTP status codes, routing y validaciones de bean para cada endpoint.
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController — Tests @WebMvcTest")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // ──────────────────────────────────────────────────────────────────────
    // HUESPED
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /users/huesped")
    class PostHuesped {

        @Test
        @DisplayName("Debe retornar 201 con datos válidos")
        void debeCrearHuespedConDatosValidos() throws Exception {
            HuespedDTORequest request = TestDataFactory.huespedDTORequest();
            HuespedDTOResponse response = TestDataFactory.huespedDTOResponse();

            when(userService.createUsuarioHuesped(any())).thenReturn(response);

            mockMvc.perform(post("/users/huesped")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                    .andExpect(jsonPath("$.dni").value("12345678"));

            verify(userService).createUsuarioHuesped(any());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el DNI tiene formato inválido")
        void debeRetornar400ConDniInvalido() throws Exception {
            String body = buildHuespedJson("Juan Pérez", "juan@email.com", "3412345678", "1234", "1990-05-15");

            mockMvc.perform(post("/users/huesped")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUsuarioHuesped(any());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el email es inválido")
        void debeRetornar400ConEmailInvalido() throws Exception {
            String body = buildHuespedJson("Juan Pérez", "no-es-un-email", "3412345678", "12345678", "1990-05-15");

            mockMvc.perform(post("/users/huesped")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el nombre está vacío")
        void debeRetornar400ConNombreVacio() throws Exception {
            String body = buildHuespedJson("", "juan@email.com", "3412345678", "12345678", "1990-05-15");

            mockMvc.perform(post("/users/huesped")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el banco de la tarjeta no existe")
        void debeRetornar404CuandoBancoNoExiste() throws Exception {
            HuespedDTORequest request = TestDataFactory.huespedDTORequest();

            when(userService.createUsuarioHuesped(any()))
                    .thenThrow(new EntityNotFoundException("Banco no encontrado con ID: 1"));

            mockMvc.perform(post("/users/huesped")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /users/huesped/{id}")
    class PutHuespedId {

        @Test
        @DisplayName("Debe retornar 200 con datos válidos")
        void debeActualizarHuespedConDatosValidos() throws Exception {
            HuespedDTOUpdate update = TestDataFactory.huespedDTOUpdate();
            HuespedDTOResponse response = TestDataFactory.huespedDTOResponse();

            when(userService.updateUsuarioHuesped(eq(1), any())).thenReturn(response);

            mockMvc.perform(put("/users/huesped/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el huesped no existe")
        void debeRetornar404CuandoHuespedNoExiste() throws Exception {
            when(userService.updateUsuarioHuesped(eq(99), any()))
                    .thenThrow(new EntityNotFoundException("Usuario no encontrado con ID: 99"));

            mockMvc.perform(put("/users/huesped/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TestDataFactory.huespedDTOUpdate())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /users/huesped/{id}")
    class DeleteHuespedId {

        @Test
        @DisplayName("Debe retornar 204 al eliminar un huesped existente")
        void debeEliminarHuespedExistente() throws Exception {
            doNothing().when(userService).deleteUsuarioHuesped(1);

            mockMvc.perform(delete("/users/huesped/1"))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUsuarioHuesped(1);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el huesped no existe")
        void debeRetornar404CuandoHuespedNoExiste() throws Exception {
            doThrow(new EntityNotFoundException("Usuario no encontrado con ID: 99"))
                    .when(userService).deleteUsuarioHuesped(99);

            mockMvc.perform(delete("/users/huesped/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // PROPIETARIO
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /users/propietario")
    class PostPropietario {

        @Test
        @DisplayName("Debe retornar 201 con datos válidos")
        void debeCrearPropietarioConDatosValidos() throws Exception {
            PropietarioDTORequest request = TestDataFactory.propietarioDTORequest();
            PropietarioDTOResponse response = TestDataFactory.propietarioDTOResponse();

            when(userService.createUsuarioPropietario(any())).thenReturn(response);

            mockMvc.perform(post("/users/propietario")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nombre").value("María García"));
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el CBU no tiene 22 dígitos")
        void debeRetornar400ConCbuInvalido() throws Exception {
            String body = buildPropietarioJson("María García", "maria@email.com", "3419876543", "87654321",
                    "000-123", "12345", "juan.alias", 1, 10L);

            mockMvc.perform(post("/users/propietario")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUsuarioPropietario(any());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el alias tiene caracteres inválidos")
        void debeRetornar400ConAliasInvalido() throws Exception {
            // alias con caracteres especiales no permitidos
            String body = buildPropietarioJson("María García", "maria@email.com", "3419876543", "87654321",
                    "000-123456/7", "1234567890123456789012", "alias con espacios!", 1, 10L);

            mockMvc.perform(post("/users/propietario")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el banco de la cuenta no existe")
        void debeRetornar404CuandoBancoNoExiste() throws Exception {
            when(userService.createUsuarioPropietario(any()))
                    .thenThrow(new EntityNotFoundException("Banco no encontrado con ID: 1"));

            mockMvc.perform(post("/users/propietario")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TestDataFactory.propietarioDTORequest())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /users/propietario/{id}")
    class PutPropietarioId {

        @Test
        @DisplayName("Debe retornar 200 con datos válidos")
        void debeActualizarPropietarioConDatosValidos() throws Exception {
            PropietarioDTOUpdate update = TestDataFactory.propietarioDTOUpdate();
            PropietarioDTOResponse response = TestDataFactory.propietarioDTOResponse();

            when(userService.updateUsuarioPropietario(eq(1), any())).thenReturn(response);

            mockMvc.perform(put("/users/propietario/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el propietario no existe")
        void debeRetornar404CuandoPropietarioNoExiste() throws Exception {
            when(userService.updateUsuarioPropietario(eq(99), any()))
                    .thenThrow(new EntityNotFoundException("Propietario no encontrado con ID: 99"));

            mockMvc.perform(put("/users/propietario/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TestDataFactory.propietarioDTOUpdate())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /users/propietario/{id}")
    class DeletePropietarioId {

        @Test
        @DisplayName("Debe retornar 204 al eliminar un propietario existente")
        void debeEliminarPropietarioExistente() throws Exception {
            doNothing().when(userService).deleteUsuarioPropietario(1);

            mockMvc.perform(delete("/users/propietario/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el propietario no existe")
        void debeRetornar404CuandoPropietarioNoExiste() throws Exception {
            doThrow(new EntityNotFoundException("Propietario no encontrado con ID: 99"))
                    .when(userService).deleteUsuarioPropietario(99);

            mockMvc.perform(delete("/users/propietario/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // BÚSQUEDAS
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /users/buscar-nombre")
    class GetBuscarNombre {

        @Test
        @DisplayName("Debe retornar 200 con la página de resultados")
        void debeRetornarResultadosPorNombre() throws Exception {
            UsuarioDTOResponse userResponse = TestDataFactory.usuarioDTOResponseHuesped();
            var page = new PageImpl<>(List.of(userResponse));

            when(userService.buscarPorNombre(eq("Juan"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/users/buscar-nombre").param("nombre", "Juan"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].nombre").value("Juan Pérez"));
        }

        @Test
        @DisplayName("Debe retornar 200 con página vacía cuando no hay coincidencias")
        void debeRetornarPaginaVaciaConParametroPorDefecto() throws Exception {
            when(userService.buscarPorNombre(eq(""), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/users/buscar-nombre"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /users/buscar-dni")
    class GetBuscarDni {

        @Test
        @DisplayName("Debe retornar 200 con resultados por DNI parcial")
        void debeRetornarResultadosPorDni() throws Exception {
            UsuarioDTOResponse userResponse = TestDataFactory.usuarioDTOResponseHuesped();
            var page = new PageImpl<>(List.of(userResponse));

            when(userService.buscarPorDni(eq("1234"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/users/buscar-dni").param("dni", "1234"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].dni").value("12345678"));
        }
    }

    @Nested
    @DisplayName("GET /users/dni/{dni}")
    class GetDniExacto {

        @Test
        @DisplayName("Debe retornar 200 cuando el DNI existe exactamente")
        void debeRetornarUsuarioPorDniExacto() throws Exception {
            UsuarioDTOResponse response = TestDataFactory.usuarioDTOResponseHuesped();

            when(userService.buscarPorDniExacto("12345678")).thenReturn(response);

            mockMvc.perform(get("/users/dni/12345678"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dni").value("12345678"))
                    .andExpect(jsonPath("$.tipo").value("HUESPED"));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el DNI no existe")
        void debeRetornar404CuandoDniNoExiste() throws Exception {
            when(userService.buscarPorDniExacto("00000000"))
                    .thenThrow(new EntityNotFoundException("Usuario no encontrado con DNI: 00000000"));

            mockMvc.perform(get("/users/dni/00000000"))
                    .andExpect(status().isNotFound());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // TARJETAS DE CRÉDITO
    // ──────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /users/huespedes/{huespedId}/tarjetas")
    class PostTarjeta {

        @Test
        @DisplayName("Debe retornar 201 al agregar tarjeta con datos válidos")
        void debeAgregarTarjetaConDatosValidos() throws Exception {
            TarjetaCreditoDTORequest request = TestDataFactory.tarjetaCreditoDTORequest();
            TarjetaCreditoDTOResponse response = TestDataFactory.tarjetaCreditoDTOResponse();

            when(userService.agregarTarjeta(eq(1), any())).thenReturn(response);

            mockMvc.perform(post("/users/huespedes/1/tarjetas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.esPrincipal").value(true));
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el número de tarjeta es inválido (< 13 dígitos)")
        void debeRetornar400ConNumeroTarjetaInvalido() throws Exception {
            String body = buildTarjetaJson("12345", "Juan Pérez", "12/27", "123", true, 1);

            mockMvc.perform(post("/users/huespedes/1/tarjetas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).agregarTarjeta(any(), any());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando el CVC tiene formato inválido (< 3 dígitos)")
        void debeRetornar400ConCvcInvalido() throws Exception {
            String body = buildTarjetaJson("4111111111111111", "Juan Pérez", "12/27", "12", true, 1);

            mockMvc.perform(post("/users/huespedes/1/tarjetas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando la fecha de vencimiento tiene formato inválido")
        void debeRetornar400ConFechaVencimientoInvalida() throws Exception {
            String body = buildTarjetaJson("4111111111111111", "Juan Pérez", "13/27", "123", true, 1);

            mockMvc.perform(post("/users/huespedes/1/tarjetas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el huesped no existe")
        void debeRetornar404CuandoHuespedNoExiste() throws Exception {
            when(userService.agregarTarjeta(eq(99), any()))
                    .thenThrow(new EntityNotFoundException("Huésped no encontrado con ID: 99"));

            mockMvc.perform(post("/users/huespedes/99/tarjetas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TestDataFactory.tarjetaCreditoDTORequest())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /users/huespedes/{huespedId}/tarjetas/{tarjetaId}")
    class DeleteTarjeta {

        @Test
        @DisplayName("Debe retornar 204 al eliminar tarjeta no principal")
        void debeEliminarTarjetaNoPrincipal() throws Exception {
            doNothing().when(userService).eliminarTarjeta(1, 2);

            mockMvc.perform(delete("/users/huespedes/1/tarjetas/2"))
                    .andExpect(status().isNoContent());

            verify(userService).eliminarTarjeta(1, 2);
        }

        @Test
        @DisplayName("Debe retornar 422 al intentar eliminar la tarjeta principal")
        void debeRetornar422AlEliminarTarjetaPrincipal() throws Exception {
            doThrow(new TarjetaPrincipalException("No se puede eliminar la tarjeta principal"))
                    .when(userService).eliminarTarjeta(1, 1);

            mockMvc.perform(delete("/users/huespedes/1/tarjetas/1"))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando la tarjeta no existe")
        void debeRetornar404CuandoTarjetaNoExiste() throws Exception {
            doThrow(new EntityNotFoundException("Tarjeta no encontrada con ID: 99"))
                    .when(userService).eliminarTarjeta(1, 99);

            mockMvc.perform(delete("/users/huespedes/1/tarjetas/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /users/huespedes/{huespedId}/tarjetas/{tarjetaId}/principal")
    class PatchTarjetaPrincipal {

        @Test
        @DisplayName("Debe retornar 200 al cambiar tarjeta principal exitosamente")
        void debeCambiarTarjetaPrincipalExitosamente() throws Exception {
            TarjetaCreditoDTOResponse response = TestDataFactory.tarjetaCreditoDTOResponse();

            when(userService.cambiarTarjetaPrincipal(1, 2)).thenReturn(response);

            mockMvc.perform(patch("/users/huespedes/1/tarjetas/2/principal"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.esPrincipal").value(true));
        }

        @Test
        @DisplayName("Debe retornar 400 cuando la tarjeta ya es principal")
        void debeRetornar400CuandoTarjetaYaEsPrincipal() throws Exception {
            when(userService.cambiarTarjetaPrincipal(1, 1))
                    .thenThrow(new IllegalArgumentException("La tarjeta con ID: 1 ya es la tarjeta principal"));

            mockMvc.perform(patch("/users/huespedes/1/tarjetas/1/principal"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando la tarjeta no existe")
        void debeRetornar404CuandoTarjetaNoExiste() throws Exception {
            when(userService.cambiarTarjetaPrincipal(1, 99))
                    .thenThrow(new EntityNotFoundException("Tarjeta no encontrada con ID: 99"));

            mockMvc.perform(patch("/users/huespedes/1/tarjetas/99/principal"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /users/huespedes/{huespedId}/tarjetas")
    class GetTarjetas {

        @Test
        @DisplayName("Debe retornar 200 con la lista de tarjetas del huesped")
        void debeRetornarTarjetasDelHuesped() throws Exception {
            TarjetaCreditoDTOResponse tarjeta = TestDataFactory.tarjetaCreditoDTOResponse();
            var page = new PageImpl<>(List.of(tarjeta));

            when(userService.listarTarjetas(eq(1), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/users/huespedes/1/tarjetas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].esPrincipal").value(true));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el huesped no existe")
        void debeRetornar404CuandoHuespedNoExiste() throws Exception {
            when(userService.listarTarjetas(eq(99), any(Pageable.class)))
                    .thenThrow(new EntityNotFoundException("Huésped no encontrado con ID: 99"));

            mockMvc.perform(get("/users/huespedes/99/tarjetas"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /users/huesped/tarjeta-principal")
    class GetTarjetaPrincipal {

        @Test
        @DisplayName("Debe retornar 200 con el número de la tarjeta principal")
        void debeRetornarNumeroTarjetaPrincipal() throws Exception {
            when(userService.obtenerTarjetaPrincipalPorDni("12345678"))
                    .thenReturn(new TarjetaPrincipalDTO("4111111111111111"));

            mockMvc.perform(get("/users/huesped/tarjeta-principal").param("dni", "12345678"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.numero").value("4111111111111111"));

            verify(userService).obtenerTarjetaPrincipalPorDni("12345678");
        }

        @Test
        @DisplayName("Debe retornar 404 cuando el servicio lanza EntityNotFoundException")
        void debeRetornar404CuandoNoHayTarjetaPrincipal() throws Exception {
            when(userService.obtenerTarjetaPrincipalPorDni("00000000"))
                    .thenThrow(new EntityNotFoundException("No se encontró usuario con DNI: 00000000"));

            mockMvc.perform(get("/users/huesped/tarjeta-principal").param("dni", "00000000"))
                    .andExpect(status().isNotFound());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // HELPERS DE CONSTRUCCIÓN DE JSON
    // ──────────────────────────────────────────────────────────────────────

    private String buildHuespedJson(String nombre, String email, String telefono, String dni, String fechaNacimiento) {
        return """
                {
                    "nombre": "%s",
                    "email": "%s",
                    "telefono": "%s",
                    "dni": "%s",
                    "fechaNacimiento": "%s",
                    "tarjetaCredito": {
                        "numero": "4111111111111111",
                        "nombreTitular": "Juan Pérez",
                        "fechaVencimiento": "12/27",
                        "cvc": "123",
                        "esPrincipal": true,
                        "bancoId": 1
                    }
                }
                """.formatted(nombre, email, telefono, dni, fechaNacimiento);
    }

    private String buildPropietarioJson(String nombre, String email, String telefono, String dni,
                                         String numeroCuenta, String cbu, String alias, int bancoId, Long idHotel) {
        return """
                {
                    "nombre": "%s",
                    "email": "%s",
                    "telefono": "%s",
                    "dni": "%s",
                    "cuentaBancaria": {
                        "numeroCuenta": "%s",
                        "cbu": "%s",
                        "alias": "%s",
                        "bancoId": %d
                    },
                    "idHotel": %d
                }
                """.formatted(nombre, email, telefono, dni, numeroCuenta, cbu, alias, bancoId, idHotel);
    }

    private String buildTarjetaJson(String numero, String titular, String fechaVenc, String cvc,
                                     boolean esPrincipal, int bancoId) {
        return """
                {
                    "numero": "%s",
                    "nombreTitular": "%s",
                    "fechaVencimiento": "%s",
                    "cvc": "%s",
                    "esPrincipal": %b,
                    "bancoId": %d
                }
                """.formatted(numero, titular, fechaVenc, cvc, esPrincipal, bancoId);
    }
}
