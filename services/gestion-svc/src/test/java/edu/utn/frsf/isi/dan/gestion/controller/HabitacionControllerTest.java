package edu.utn.frsf.isi.dan.gestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.service.HabitacionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HabitacionController.class)
@DisplayName("HabitacionController WebMvcTest")
class HabitacionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private HabitacionService habitacionService;

    @Test
    void createShouldReturn201() throws Exception {
        var request = TestDataFactory.habitacionDTORequest();
        when(habitacionService.crearHabitacion(any())).thenReturn(TestDataFactory.habitacionDTOResponse());

        mockMvc.perform(post("/habitaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createShouldReturn400WhenInvalidBody() throws Exception {
        mockMvc.perform(post("/habitaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\":null,\"piso\":1,\"idTipoHabitacion\":1,\"idHotel\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdShouldReturn404WhenNotFound() throws Exception {
        when(habitacionService.buscarHabitacionPorId(99)).thenThrow(new EntityNotFoundException("no"));
        mockMvc.perform(get("/habitaciones/99")).andExpect(status().isNotFound());
    }

    @Test
    void searchShouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(TestDataFactory.habitacionDTOResponse()));
        when(habitacionService.buscarHabitaciones(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/habitaciones").param("cantidadHuespedes", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void updateShouldReturn200() throws Exception {
        when(habitacionService.actualizarHabitacion(eq(1), any())).thenReturn(TestDataFactory.habitacionDTOResponse());

        mockMvc.perform(put("/habitaciones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.habitacionDTOUpdate())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        mockMvc.perform(delete("/habitaciones/1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteShouldReturn404WhenMissing() throws Exception {
        doThrow(new EntityNotFoundException("no")).when(habitacionService).eliminarHabitacion(9);
        mockMvc.perform(delete("/habitaciones/9")).andExpect(status().isNotFound());
    }

    @Test
    void getTarifaVigenteShouldReturn200() throws Exception {
        when(habitacionService.obtenerTarifaVigente(1)).thenReturn(TestDataFactory.tarifaDTOResponse());
        mockMvc.perform(get("/habitaciones/1/tarifa-vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}

