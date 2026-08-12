package edu.utn.frsf.isi.dan.reservas_svc.mapper;

import edu.utn.frsf.isi.dan.reservas_svc.dto.PagoDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.PagoDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.reservas_svc.model.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructConfig.class, uses = TarifaMapper.class)
public interface PagoMapper {

    @Mapping(source = "amount", target = "amount.precio")
    @Mapping(source = "currency", target = "amount.moneda")
    @Mapping(target = "status", constant = "APPROVED")
    Pago toEntity(PagoDTORequest dto);

    PagoDTOResponse toResponse(Pago pago);
}
