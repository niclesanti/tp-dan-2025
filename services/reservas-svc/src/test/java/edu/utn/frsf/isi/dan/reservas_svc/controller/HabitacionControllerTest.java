package edu.utn.frsf.isi.dan.reservas_svc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.reservas_svc.TestDataFactory;
import edu.utn.frsf.isi.dan.reservas_svc.service.HabitacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HabitacionController.class)
class HabitacionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private HabitacionService habitacionService;

    @Test
    void getAllShouldReturn200() throws Exception {
        when(habitacionService.findAll()).thenReturn(List.of(TestDataFactory.habitacion()));
        mockMvc.perform(get("/habitaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("hab-1"));
    }

    @Test
    void getByIdShouldReturn404WhenMissing() throws Exception {
        when(habitacionService.findById("x")).thenReturn(Optional.empty());
        mockMvc.perform(get("/habitaciones/x")).andExpect(status().isNotFound());
    }

    @Test
    void buscarDisponiblesShouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(TestDataFactory.disponibleDTO()));
        when(habitacionService.buscarDisponibles(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        var now = java.time.Instant.now().plusSeconds(86400).toString();
        var out = java.time.Instant.now().plusSeconds(172800).toString();

        mockMvc.perform(get("/habitaciones/disponibles")
                        .param("checkIn", now)
                        .param("checkOut", out))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("hab-1"));
    }
}

