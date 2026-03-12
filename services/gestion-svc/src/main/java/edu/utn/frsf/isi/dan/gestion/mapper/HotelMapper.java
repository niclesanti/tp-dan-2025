package edu.utn.frsf.isi.dan.gestion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import edu.utn.frsf.isi.dan.gestion.dto.HotelDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HotelDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;

@Mapper(config = MapstructConfig.class, uses = {AmenityHotelMapper.class})
public interface HotelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "habitaciones", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    Hotel toEntity(HotelDTORequest dto);

    HotelDTOResponse toResponse(Hotel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "habitaciones", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    void updateEntity(HotelDTOUpdate dto, @MappingTarget Hotel hotel);
}
