package edu.utn.frsf.isi.dan.reservas_svc.mapper;

import edu.utn.frsf.isi.dan.reservas_svc.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HuespedDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.reservas_svc.model.Huesped;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfig.class)
public interface HuespedMapper {

    Huesped toEntity(HuespedDTORequest dto);

    HuespedDTOResponse toResponse(Huesped entity);
}
