package edu.utn.frsf.isi.dan.reservas_svc.mapper;

import edu.utn.frsf.isi.dan.reservas_svc.dto.TarifaDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.reservas_svc.model.Tarifa;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfig.class)
public interface TarifaMapper {

    TarifaDTOResponse toResponse(Tarifa tarifa);
}
