package edu.utn.frsf.isi.dan.gestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.service.HotelService;
import jakarta.persistence.EntityNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private HotelService hotelService;

    @Test
    void createShouldReturn201() throws Exception {
        when(hotelService.crearHotel(any())).thenReturn(TestDataFactory.hotelDTOResponse());
        mockMvc.perform(post("/hoteles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.hotelDTORequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getShouldReturn404WhenMissing() throws Exception {
        when(hotelService.buscarHotelPorId(99)).thenThrow(new EntityNotFoundException("x"));
        mockMvc.perform(get("/hoteles/99")).andExpect(status().isNotFound());
    }

    @Test
    void searchShouldReturn200() throws Exception {
        var page = new PageImpl<>(List.of(TestDataFactory.hotelDTOResponse()));
        when(hotelService.buscarHoteles(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/hoteles").param("nombre", "Hotel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void updateShouldReturn200() throws Exception {
        when(hotelService.actualizarHotel(eq(1), any())).thenReturn(TestDataFactory.hotelDTOResponse());
        mockMvc.perform(put("/hoteles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.hotelDTOUpdate())))
                .andExpect(status().isOk());
    }

    @Test
    void closeShouldReturn400OnIllegalState() throws Exception {
        when(hotelService.cerrarHotel(1)).thenThrow(new IllegalStateException("cerrado"));
        mockMvc.perform(patch("/hoteles/1/cerrar")).andExpect(status().isInternalServerError());
    }

    @Test
    void amenitiesShouldReturn200() throws Exception {
        when(hotelService.agregarAmenities(eq(1), any())).thenReturn(TestDataFactory.hotelDTOResponse());
        mockMvc.perform(put("/hoteles/1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(Amenity.WIFI, Amenity.BAR))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAmenityShouldReturn204() throws Exception {
        mockMvc.perform(delete("/hoteles/1/amenities/2")).andExpect(status().isNoContent());
    }

    @Test
    void deleteAmenityShouldReturn404() throws Exception {
        doThrow(new EntityNotFoundException("x")).when(hotelService).eliminarAmenity(1, 2L);
        mockMvc.perform(delete("/hoteles/1/amenities/2")).andExpect(status().isNotFound());
    }
}

