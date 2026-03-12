package edu.utn.frsf.isi.dan.gestion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import edu.utn.frsf.isi.dan.gestion.dto.AmenityHotelDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.AmenityHotelDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.AmenityHotelDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.gestion.model.AmenityHotel;

@Mapper(config = MapstructConfig.class)
public interface AmenityHotelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    AmenityHotel toEntity(AmenityHotelDTORequest dto);

    AmenityHotelDTOResponse toResponse(AmenityHotel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    void updateEntity(AmenityHotelDTOUpdate dto, @MappingTarget AmenityHotel amenityHotel);
}
