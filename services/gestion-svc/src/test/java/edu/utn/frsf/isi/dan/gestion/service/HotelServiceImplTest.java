package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.dao.AmenityHotelRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.mapper.HotelMapper;
import edu.utn.frsf.isi.dan.gestion.mapper.SharedDTOMapper;
import edu.utn.frsf.isi.dan.gestion.messaging.GestionMessagePublisher;
import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private AmenityHotelRepository amenityHotelRepository;
    @Mock
    private HotelMapper hotelMapper;
    @Mock
    private GestionMessagePublisher messagePublisher;
    @Mock
    private SharedDTOMapper sharedDTOMapper;
    @InjectMocks
    private HotelServiceImpl hotelService;

    @Test
    void crearHotelShouldReturnMappedResponse() {
        var entity = TestDataFactory.hotel();
        var req = TestDataFactory.hotelDTORequest();
        var res = TestDataFactory.hotelDTOResponse();
        when(hotelMapper.toEntity(req)).thenReturn(entity);
        when(hotelRepository.save(entity)).thenReturn(entity);
        when(hotelMapper.toResponse(entity)).thenReturn(res);

        assertThat(hotelService.crearHotel(req)).isEqualTo(res);
    }

    @Test
    void cerrarHotelShouldThrowWhenAlreadyClosed() {
        var hotel = TestDataFactory.hotel();
        hotel.setFechaCierre(LocalDate.now().minusDays(1));
        when(hotelRepository.findById(1)).thenReturn(Optional.of(hotel));
        assertThatThrownBy(() -> hotelService.cerrarHotel(1)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cerrarHotelShouldPublishEvent() {
        var hotel = TestDataFactory.hotel();
        when(hotelRepository.findById(1)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(i -> i.getArgument(0));
        when(hotelMapper.toResponse(any())).thenReturn(TestDataFactory.hotelDTOResponse());
        when(sharedDTOMapper.toHotelDTO(any())).thenReturn(HotelDTO.builder().id(1).build());

        hotelService.cerrarHotel(1);

        verify(messagePublisher).publishHotelCierreEvent(any());
    }

    @Test
    void buscarHotelesShouldReturnPage() {
        var hotel = TestDataFactory.hotel();
        when(hotelRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(hotel)));
        when(hotelMapper.toResponse(hotel)).thenReturn(TestDataFactory.hotelDTOResponse());

        var page = hotelService.buscarHoteles("Dan", 4, "San", Amenity.WIFI, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void buscarHotelesShouldIgnoreBlankNombreAndDomicilio() {
        var hotel = TestDataFactory.hotel();
        when(hotelRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(hotel)));
        when(hotelMapper.toResponse(hotel)).thenReturn(TestDataFactory.hotelDTOResponse());

        var page = hotelService.buscarHoteles("   ", null, "   ", null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void agregarAmenitiesShouldAppendAndReturn() {
        var hotel = TestDataFactory.hotel();
        when(hotelRepository.findById(1)).thenReturn(Optional.of(hotel));
        when(amenityHotelRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(hotelMapper.toResponse(hotel)).thenReturn(TestDataFactory.hotelDTOResponse());

        var res = hotelService.agregarAmenities(1, List.of(Amenity.WIFI, Amenity.BAR));
        assertThat(res.id()).isEqualTo(1);
    }

    @Test
    void eliminarAmenityShouldThrowWhenMissing() {
        when(amenityHotelRepository.findByIdAndHotelId(10L, 1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> hotelService.eliminarAmenity(1, 10L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void buscarHotelPorIdShouldThrowWhenMissing() {
        when(hotelRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> hotelService.buscarHotelPorId(99)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void actualizarHotelShouldThrowWhenMissing() {
        when(hotelRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> hotelService.actualizarHotel(99, TestDataFactory.hotelDTOUpdate()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void eliminarAmenityShouldDeleteWhenExists() {
        var amenityHotel = TestDataFactory.amenityHotel(TestDataFactory.hotel(), Amenity.WIFI);
        when(amenityHotelRepository.findByIdAndHotelId(10L, 1)).thenReturn(Optional.of(amenityHotel));

        hotelService.eliminarAmenity(1, 10L);

        verify(amenityHotelRepository).delete(amenityHotel);
    }

    @Test
    void cerrarHotelShouldThrowWhenMissing() {
        when(hotelRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> hotelService.cerrarHotel(99)).isInstanceOf(EntityNotFoundException.class);
    }
}

