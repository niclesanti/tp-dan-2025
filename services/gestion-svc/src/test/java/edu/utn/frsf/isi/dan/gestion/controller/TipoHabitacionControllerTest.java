package edu.utn.frsf.isi.dan.gestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.service.TipoHabitacionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
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
    void crearTipoHabitacion_RequestValido_Retorna201() throws Exception {
        when(tipoHabitacionService.crearTipoHabitacion(any())).thenReturn(TestDataFactory.tipoHabitacionDTOResponse());

        mockMvc.perform(post("/tipos-habitacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Suite\",\"descripcion\":\"Suite premium\",\"capacidad\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crearTipoHabitacion_RequestInvalido_Retorna400() throws Exception {
        mockMvc.perform(post("/tipos-habitacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"descripcion\":\"\",\"capacidad\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarTipoHabitacionPorId_TipoExistente_Retorna200() throws Exception {
        when(tipoHabitacionService.buscarTipoHabitacionPorId(1)).thenReturn(TestDataFactory.tipoHabitacionDTOResponse());

        mockMvc.perform(get("/tipos-habitacion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Suite"));
    }

    @Test
    void buscarTipoHabitacionPorId_TipoInexistente_Retorna404() throws Exception {
        when(tipoHabitacionService.buscarTipoHabitacionPorId(9))
                .thenThrow(new EntityNotFoundException("TipoHabitacion no encontrada con ID: 9"));

        mockMvc.perform(get("/tipos-habitacion/9")).andExpect(status().isNotFound());
    }

    @Test
    void buscarTiposHabitacion_ConDatos_Retorna200() throws Exception {
        when(tipoHabitacionService.buscarTiposHabitacion())
                .thenReturn(List.of(TestDataFactory.tipoHabitacionDTOResponse()));

        mockMvc.perform(get("/tipos-habitacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void actualizarTipoHabitacion_TipoExistente_Retorna200() throws Exception {
        when(tipoHabitacionService.actualizarTipoHabitacion(anyInt(), any()))
                .thenReturn(TestDataFactory.tipoHabitacionDTOResponse());

        mockMvc.perform(put("/tipos-habitacion/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Suite Premium\",\"descripcion\":\"Suite de lujo\",\"capacidad\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizarTipoHabitacion_TipoInexistente_Retorna404() throws Exception {
        when(tipoHabitacionService.actualizarTipoHabitacion(anyInt(), any()))
                .thenThrow(new EntityNotFoundException("TipoHabitacion no encontrada con ID: 9"));

        mockMvc.perform(put("/tipos-habitacion/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Suite Premium\",\"descripcion\":\"Suite de lujo\",\"capacidad\":3}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarTipoHabitacion_TipoExistente_Retorna204() throws Exception {
        mockMvc.perform(delete("/tipos-habitacion/1")).andExpect(status().isNoContent());
    }

    @Test
    void eliminarTipoHabitacion_TipoInexistente_Retorna404() throws Exception {
        doThrow(new EntityNotFoundException("TipoHabitacion no encontrada con ID: 9"))
                .when(tipoHabitacionService).eliminarTipoHabitacion(9);

        mockMvc.perform(delete("/tipos-habitacion/9")).andExpect(status().isNotFound());
    }
}