package edu.utn.frsf.isi.dan.gestion.mapper;

import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import edu.utn.frsf.isi.dan.shared.TarifaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SharedDTOMapper {

    @Mapping(source = "id", target = "habitacionId")
    @Mapping(source = "tipoHabitacion.id", target = "tipoHabitacionId")
    @Mapping(source = "tipoHabitacion.nombre", target = "tipoHabitacion")
    @Mapping(source = "tipoHabitacion.descripcion", target = "tipoHabitacionDescripcion")
    @Mapping(source = "tipoHabitacion.capacidad", target = "capacidad")
    @Mapping(target = "precioNoche", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    HabitacionDTO toHabitacionDTO(Habitacion habitacion);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nombre", target = "nombre")
    @Mapping(source = "cuit", target = "cuit")
    @Mapping(source = "domicilio", target = "domicilio")
    @Mapping(source = "latitud", target = "latitud")
    @Mapping(source = "longitud", target = "longitud")
    @Mapping(source = "telefono", target = "telefono")
    @Mapping(source = "correoContacto", target = "correoContacto")
    @Mapping(source = "categoria", target = "categoria")
    HotelDTO toHotelDTO(Hotel hotel);

    @Mapping(source = "tipoHabitacion.id", target = "tipoHabitacionId")
    @Mapping(source = "precioNoche", target = "nuevoPrecio")
    TarifaDTO toTarifaDTO(Tarifa tarifa);
}
