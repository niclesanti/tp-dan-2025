package edu.utn.frsf.isi.dan.gestion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;

@Mapper(config = MapstructConfig.class)
public interface TipoHabitacionMapper {

    @Mapping(target = "id", ignore = true)
    TipoHabitacion toEntity(TipoHabitacionDTORequest dto);

    TipoHabitacionDTOResponse toResponse(TipoHabitacion entity);

    @Mapping(target = "id", ignore = true)
    void updateEntity(TipoHabitacionDTOUpdate dto, @MappingTarget TipoHabitacion tipoHabitacion);
}
