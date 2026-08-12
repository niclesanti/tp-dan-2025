package edu.utn.frsf.isi.dan.reservas_svc.mapper;

import edu.utn.frsf.isi.dan.reservas_svc.dto.HotelDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HotelSimpleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.reservas_svc.model.Hotel;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructConfig.class)
public interface HotelMapper {

    @Mapping(target = "ubicacion",
            expression = "java(new org.springframework.data.mongodb.core.geo.GeoJsonPoint(dto.getLongitud(), dto.getLatitud()))")
    Hotel toEntity(HotelDTO dto);

    @Mapping(target = "latitud",
            expression = "java(hotel.getUbicacion() != null ? hotel.getUbicacion().getY() : null)")
    @Mapping(target = "longitud",
            expression = "java(hotel.getUbicacion() != null ? hotel.getUbicacion().getX() : null)")
    HotelSimpleDTO toSimple(Hotel hotel);

    HotelDTOResponse toResponse(Hotel hotel);
}
