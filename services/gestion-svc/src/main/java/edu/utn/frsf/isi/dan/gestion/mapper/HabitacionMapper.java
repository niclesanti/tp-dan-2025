package edu.utn.frsf.isi.dan.gestion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.HabitacionDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.gestion.model.Habitacion;

@Mapper(config = MapstructConfig.class, uses = {TipoHabitacionMapper.class})
public interface HabitacionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoHabitacion", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    Habitacion toEntity(HabitacionDTORequest dto);

    @Mapping(target = "idHotel", expression = "java(entity.getHotel() != null ? entity.getHotel().getId() : null)")
    @Mapping(target = "nombreHotel", expression = "java(entity.getHotel() != null ? entity.getHotel().getNombre() : null)")
    HabitacionDTOResponse toResponse(Habitacion entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoHabitacion", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    void updateEntity(HabitacionDTOUpdate dto, @MappingTarget Habitacion habitacion);
}
