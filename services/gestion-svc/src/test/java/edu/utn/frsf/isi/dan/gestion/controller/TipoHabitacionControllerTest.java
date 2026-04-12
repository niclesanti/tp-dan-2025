package edu.utn.frsf.isi.dan.gestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;
import edu.utn.frsf.isi.dan.gestion.service.TipoHabitacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TipoHabitacionController.class)
class TipoHabitacionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TipoHabitacionService tipoHabitacionService;

    @Test
    void createShouldReturn200() throws Exception {
        var entity = TestDataFactory.tipoHabitacion();
        when(tipoHabitacionService.save(any())).thenReturn(entity);
        mockMvc.perform(post("/tipos-habitacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByIdShouldReturn404() throws Exception {
        when(tipoHabitacionService.findById(9)).thenReturn(Optional.empty());
        mockMvc.perform(get("/tipos-habitacion/9")).andExpect(status().isNotFound());
    }

    @Test
    void getAllShouldReturn200() throws Exception {
        when(tipoHabitacionService.findAll()).thenReturn(List.of(TestDataFactory.tipoHabitacion()));
        mockMvc.perform(get("/tipos-habitacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void updateShouldReturn404WhenMissing() throws Exception {
        when(tipoHabitacionService.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(put("/tipos-habitacion/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TipoHabitacion())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateShouldReturn200WhenExists() throws Exception {
        var entity = TestDataFactory.tipoHabitacion();
        when(tipoHabitacionService.findById(1)).thenReturn(Optional.of(entity));
        when(tipoHabitacionService.save(any(TipoHabitacion.class))).thenReturn(entity);

        mockMvc.perform(put("/tipos-habitacion/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        when(tipoHabitacionService.findById(1)).thenReturn(Optional.of(TestDataFactory.tipoHabitacion()));
        doNothing().when(tipoHabitacionService).deleteById(1);
        mockMvc.perform(delete("/tipos-habitacion/1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteShouldReturn404WhenMissing() throws Exception {
        when(tipoHabitacionService.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/tipos-habitacion/99")).andExpect(status().isNotFound());
    }
}

