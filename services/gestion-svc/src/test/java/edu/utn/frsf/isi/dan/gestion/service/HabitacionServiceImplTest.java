package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.dao.HabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.dao.TarifaRepository;
import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.mapper.HabitacionMapper;
import edu.utn.frsf.isi.dan.gestion.mapper.SharedDTOMapper;
import edu.utn.frsf.isi.dan.gestion.mapper.TarifaMapper;
import edu.utn.frsf.isi.dan.gestion.messaging.GestionMessagePublisher;
import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitacionServiceImplTest {

    @Mock
    private HabitacionRepository habitacionRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private TipoHabitacionRepository tipoHabitacionRepository;
    @Mock
    private TarifaRepository tarifaRepository;
    @Mock
    private HabitacionMapper habitacionMapper;
    @Mock
    private TarifaMapper tarifaMapper;
    @Mock
    private SharedDTOMapper sharedDTOMapper;
    @Mock
    private GestionMessagePublisher messagePublisher;
    @InjectMocks
    private HabitacionServiceImpl habitacionService;

    @Test
    void crearHabitacionShouldThrowWhenHotelMissing() {
        when(hotelRepository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> habitacionService.crearHabitacion(TestDataFactory.habitacionDTORequest()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void crearHabitacionShouldThrowWhenTipoMissing() {
        when(hotelRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.hotel()));
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> habitacionService.crearHabitacion(TestDataFactory.habitacionDTORequest()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void crearHabitacionShouldPublishEvent() {
        var req = TestDataFactory.habitacionDTORequest();
        var entity = TestDataFactory.habitacion();
        var res = TestDataFactory.habitacionDTOResponse();
        when(hotelRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.hotel()));
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.tipoHabitacion()));
        when(habitacionMapper.toEntity(req)).thenReturn(entity);
        when(habitacionRepository.save(any(Habitacion.class))).thenReturn(entity);
        when(habitacionMapper.toResponse(entity)).thenReturn(res);
        when(sharedDTOMapper.toHabitacionDTO(any())).thenReturn(HabitacionDTO.builder().habitacionId(1).build());
        when(sharedDTOMapper.toHotelDTO(any())).thenReturn(HotelDTO.builder().id(1).build());
        when(tarifaRepository.buscarTarifasVigentesEnFecha(any(), any())).thenReturn(List.of());

        var result = habitacionService.crearHabitacion(req);
        assertThat(result).isEqualTo(res);
        verify(messagePublisher).publishHabitacionEvent(any());
    }

    @Test
    void crearHabitacionShouldIgnorePublisherError() {
        var req = TestDataFactory.habitacionDTORequest();
        var entity = TestDataFactory.habitacion();
        when(hotelRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.hotel()));
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.tipoHabitacion()));
        when(habitacionMapper.toEntity(req)).thenReturn(entity);
        when(habitacionRepository.save(any(Habitacion.class))).thenReturn(entity);
        when(habitacionMapper.toResponse(entity)).thenReturn(TestDataFactory.habitacionDTOResponse());
        when(sharedDTOMapper.toHabitacionDTO(any())).thenReturn(HabitacionDTO.builder().habitacionId(1).build());
        when(sharedDTOMapper.toHotelDTO(any())).thenReturn(HotelDTO.builder().id(1).build());
        when(tarifaRepository.buscarTarifasVigentesEnFecha(any(), any())).thenReturn(List.of());
        doThrow(new RuntimeException("mq")).when(messagePublisher).publishHabitacionEvent(any());

        assertThat(habitacionService.crearHabitacion(req).id()).isEqualTo(1);
    }

    @Test
    void crearHabitacionShouldPublishWhenHotelAmenitiesIsNull() {
        var req = TestDataFactory.habitacionDTORequest();
        var hotel = TestDataFactory.hotel();
        hotel.setAmenities(null);
        var entity = TestDataFactory.habitacion();
        entity.setHotel(hotel);
        when(hotelRepository.findById(1)).thenReturn(Optional.of(hotel));
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.tipoHabitacion()));
        when(habitacionMapper.toEntity(req)).thenReturn(entity);
        when(habitacionRepository.save(any(Habitacion.class))).thenReturn(entity);
        when(habitacionMapper.toResponse(entity)).thenReturn(TestDataFactory.habitacionDTOResponse());
        when(sharedDTOMapper.toHabitacionDTO(any())).thenReturn(HabitacionDTO.builder().habitacionId(1).build());
        when(sharedDTOMapper.toHotelDTO(any())).thenReturn(HotelDTO.builder().id(1).build());
        when(tarifaRepository.buscarTarifasVigentesEnFecha(any(), any())).thenReturn(List.of());

        habitacionService.crearHabitacion(req);

        verify(messagePublisher).publishHabitacionEvent(any());
    }

    @Test
    void buscarHabitacionesShouldReturnPage() {
        var entity = TestDataFactory.habitacion();
        when(habitacionRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(habitacionMapper.toResponse(entity)).thenReturn(TestDataFactory.habitacionDTOResponse());

        var page = habitacionService.buscarHabitaciones(2, 1, 100.0, 500.0, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void buscarHabitacionesShouldReturnPageWithoutFilters() {
        var entity = TestDataFactory.habitacion();
        when(habitacionRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(habitacionMapper.toResponse(entity)).thenReturn(TestDataFactory.habitacionDTOResponse());

        var page = habitacionService.buscarHabitaciones(null, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void buscarHabitacionesShouldIgnoreCapacidadWhenZero() {
        var entity = TestDataFactory.habitacion();
        when(habitacionRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(habitacionMapper.toResponse(entity)).thenReturn(TestDataFactory.habitacionDTOResponse());

        var page = habitacionService.buscarHabitaciones(0, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void obtenerTarifaVigenteShouldThrowWhenEmpty() {
        when(habitacionRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.habitacion()));
        when(tarifaRepository.buscarTarifasVigentesEnFecha(eq(1), any())).thenReturn(List.of());
        assertThatThrownBy(() -> habitacionService.obtenerTarifaVigente(1)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void obtenerTarifaVigenteShouldReturnMapped() {
        var tarifa = Tarifa.builder().id(1).build();
        when(habitacionRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.habitacion()));
        when(tarifaRepository.buscarTarifasVigentesEnFecha(eq(1), any())).thenReturn(List.of(tarifa));
        when(tarifaMapper.toResponse(tarifa)).thenReturn(TestDataFactory.tarifaDTOResponse());
        assertThat(habitacionService.obtenerTarifaVigente(1).id()).isEqualTo(1);
    }

    @Test
    void eliminarHabitacionShouldDeleteAndPublish() {
        when(habitacionRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.habitacion()));
        when(sharedDTOMapper.toHabitacionDTO(any())).thenReturn(HabitacionDTO.builder().habitacionId(1).build());
        when(sharedDTOMapper.toHotelDTO(any())).thenReturn(HotelDTO.builder().id(1).build());
        habitacionService.eliminarHabitacion(1);
        verify(habitacionRepository).deleteById(1);
        verify(messagePublisher).publishHabitacionEvent(any());
    }

    @Test
    void actualizarHabitacionShouldThrowWhenHabitacionMissing() {
        when(habitacionRepository.findById(9)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> habitacionService.actualizarHabitacion(9, TestDataFactory.habitacionDTOUpdate()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void actualizarHabitacionShouldThrowWhenTipoMissing() {
        when(habitacionRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.habitacion()));
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> habitacionService.actualizarHabitacion(1, TestDataFactory.habitacionDTOUpdate()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void buscarHabitacionPorIdShouldThrowWhenMissing() {
        when(habitacionRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> habitacionService.buscarHabitacionPorId(99)).isInstanceOf(EntityNotFoundException.class);
    }
}

