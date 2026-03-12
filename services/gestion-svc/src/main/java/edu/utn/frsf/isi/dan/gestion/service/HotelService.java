package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dto.HotelDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelService {

    HotelDTOResponse crearHotel(HotelDTORequest request);

    HotelDTOResponse actualizarHotel(Integer id, HotelDTOUpdate request);

    HotelDTOResponse cerrarHotel(Integer id);

    HotelDTOResponse buscarHotelPorId(Integer id);

    Page<HotelDTOResponse> buscarHoteles(String nombre, Integer categoria,
                                         String domicilio, Amenity amenity,
                                         Pageable pageable);

    HotelDTOResponse agregarAmenities(Integer hotelId, List<Amenity> amenities);

    void eliminarAmenity(Integer hotelId, Long amenityId);
}
