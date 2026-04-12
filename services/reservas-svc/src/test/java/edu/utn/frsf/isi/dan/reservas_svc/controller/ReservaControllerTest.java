package edu.utn.frsf.isi.dan.reservas_svc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.reservas_svc.TestDataFactory;
import edu.utn.frsf.isi.dan.reservas_svc.exception.EntityNotFoundException;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ReservaService reservaService;

    @Test
    void createShouldReturn201() throws Exception {
        when(reservaService.crearReserva(any())).thenReturn(TestDataFactory.reservaDTOResponse());
        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.reservaDTORequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("r1"));
    }

    @Test
    void createShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idHabitacion\":\"\",\"checkIn\":null,\"checkOut\":null,\"huesped\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdShouldReturn404() throws Exception {
        when(reservaService.buscarReservaPorId("x")).thenThrow(new EntityNotFoundException("x"));
        mockMvc.perform(get("/reservas/x")).andExpect(status().isNotFound());
    }

    @Test
    void getByHuespedShouldReturn200() throws Exception {
        when(reservaService.buscarReservasPorHuesped(eq("h1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(TestDataFactory.reservaDTOResponse())));
        mockMvc.perform(get("/reservas/huesped/h1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("r1"));
    }

    @Test
    void patchEstadoShouldReturn200() throws Exception {
        when(reservaService.actualizarEstadoReserva("r1", EstadoReserva.CONFIRMADA)).thenReturn(TestDataFactory.reservaDTOResponse());
        mockMvc.perform(patch("/reservas/r1/estado").param("estado", "CONFIRMADA"))
                .andExpect(status().isOk());
    }

    @Test
    void patchEstadoShouldReturn409OnIllegalState() throws Exception {
        when(reservaService.actualizarEstadoReserva("r1", EstadoReserva.CONFIRMADA)).thenThrow(new IllegalStateException("x"));
        mockMvc.perform(patch("/reservas/r1/estado").param("estado", "CONFIRMADA"))
                .andExpect(status().isConflict());
    }

    @Test
    void addPagoShouldReturn200() throws Exception {
        when(reservaService.agregarPago(eq("r1"), any())).thenReturn(TestDataFactory.reservaDTOResponse());
        mockMvc.perform(post("/reservas/r1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.pagoDTORequest())))
                .andExpect(status().isOk());
    }

    @Test
    void cancelarShouldReturn204() throws Exception {
        mockMvc.perform(delete("/reservas/r1")).andExpect(status().isNoContent());
    }

    @Test
    void cancelarShouldReturn404() throws Exception {
        doThrow(new EntityNotFoundException("x")).when(reservaService).cancelarReserva("r2");
        mockMvc.perform(delete("/reservas/r2")).andExpect(status().isNotFound());
    }
}

