package edu.utn.frsf.isi.dan.gestion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.TarifaDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;

@Mapper(config = MapstructConfig.class, uses = {TipoHabitacionMapper.class})
public interface TarifaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoHabitacion", ignore = true)
    Tarifa toEntity(TarifaDTORequest dto);

    TarifaDTOResponse toResponse(Tarifa entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoHabitacion", ignore = true)
    void updateEntity(TarifaDTOUpdate dto, @MappingTarget Tarifa tarifa);
}
