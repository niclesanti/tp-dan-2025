package edu.utn.frsf.isi.dan.gestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.service.TarifaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarifaController.class)
class TarifaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TarifaService tarifaService;

    @Test
    void createShouldReturn201() throws Exception {
        when(tarifaService.crearTarifa(any())).thenReturn(TestDataFactory.tarifaDTOResponse());
        mockMvc.perform(post("/tarifas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.tarifaDTORequestNormal())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(post("/tarifas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idTipoHabitacion\":1,\"precioNoche\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdShouldReturn404() throws Exception {
        when(tarifaService.buscarTarifaPorId(99)).thenThrow(new EntityNotFoundException("x"));
        mockMvc.perform(get("/tarifas/99")).andExpect(status().isNotFound());
    }

    @Test
    void getAllShouldReturn200() throws Exception {
        when(tarifaService.buscarTarifas(any())).thenReturn(new PageImpl<>(List.of(TestDataFactory.tarifaDTOResponse())));
        mockMvc.perform(get("/tarifas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        mockMvc.perform(delete("/tarifas/1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteShouldReturn400WhenBusinessRuleFails() throws Exception {
        doThrow(new IllegalArgumentException("x")).when(tarifaService).eliminarTarifa(1);
        mockMvc.perform(delete("/tarifas/1")).andExpect(status().isBadRequest());
    }
}

