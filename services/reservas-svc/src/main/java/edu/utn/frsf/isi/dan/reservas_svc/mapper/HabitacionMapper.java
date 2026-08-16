package edu.utn.frsf.isi.dan.reservas_svc.mapper;

import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDisponibleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.dto.ReservaSimpleDTOResponse;
import edu.utn.frsf.isi.dan.reservas_svc.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructConfig.class, uses = HotelMapper.class)
public interface HabitacionMapper {

    @Mapping(source = "tipoHabitacionId", target = "idTipoHabitacion")
    @Mapping(source = "precioNoche", target = "precioNoche", defaultValue = "0.0")
    Habitacion toEntity(HabitacionDTO dto);

    @Mapping(source = "precioNoche", target = "precioNoche", defaultValue = "0.0")
    HabitacionDisponibleDTO toDisponible(Habitacion habitacion);

    HabitacionDTOResponse toResponse(Habitacion habitacion);

    ReservaSimpleDTOResponse toReservaSimpleResponse(Habitacion.ReservaSimple reservaSimple);
}
