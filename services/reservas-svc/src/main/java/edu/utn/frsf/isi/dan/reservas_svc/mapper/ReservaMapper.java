package edu.utn.frsf.isi.dan.reservas_svc.mapper;

import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTORequest;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = HuespedMapper.class)
public interface ReservaMapper {

    @Mapping(source = "_id", target = "id")
    ReservaDTOResponse toResponse(Reserva reserva);

    @Mapping(target = "_id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "precioNoche", ignore = true)
    @Mapping(target = "precioTotal", ignore = true)
    @Mapping(target = "hotelId", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    @Mapping(target = "clientReview", ignore = true)
    @Mapping(target = "hostReview", ignore = true)
    @Mapping(target = "estadoReserva", ignore = true)
    Reserva toEntity(ReservaDTORequest request);
}
