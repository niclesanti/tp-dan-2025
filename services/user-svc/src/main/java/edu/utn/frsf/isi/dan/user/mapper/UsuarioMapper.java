package edu.utn.frsf.isi.dan.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import edu.utn.frsf.isi.dan.user.dto.UsuarioDTOResponse;
import edu.utn.frsf.isi.dan.user.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.user.model.Usuario;

/**
 * Mapper para conversión de {@code Usuario} (y subtipos) al DTO de búsqueda.
 * Mapea los campos comunes y resuelve el discriminador de tipo en tiempo de
 * ejecución para no exponer la entidad directamente en la capa de API.
 */
@Mapper(config = MapstructConfig.class)
public interface UsuarioMapper {

    @Mapping(
        target = "tipo",
        expression = "java(entity instanceof edu.utn.frsf.isi.dan.user.model.Huesped ? \"HUESPED\" : \"PROPIETARIO\")"
    )
    UsuarioDTOResponse toResponse(Usuario entity);
}
