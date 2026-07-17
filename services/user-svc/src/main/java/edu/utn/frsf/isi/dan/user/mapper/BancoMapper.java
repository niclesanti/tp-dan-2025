package edu.utn.frsf.isi.dan.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import edu.utn.frsf.isi.dan.user.dto.BancoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOUpdate;
import edu.utn.frsf.isi.dan.user.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.user.model.Banco;

/**
 * Mapper para conversión entre Banco Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface BancoMapper {
    
    /**
     * Convierte BancoDTORequest a Banco Entity.
     * El ID será null para nuevas entidades (INSERT).
     * 
     * @param dto BancoDTORequest
     * @return Banco entity
     */
    @Mapping(target = "id", ignore = true)
    Banco toEntity(BancoDTORequest dto);
    
    /**
     * Convierte Banco Entity a BancoDTOResponse.
     * 
     * @param entity Banco entity
     * @return BancoDTOResponse
     */
    BancoDTOResponse toResponse(Banco entity);

    /**
     * Actualiza una entidad Banco existente con los datos del DTO de actualización.
     *
     * @param dto    BancoDTOUpdate con los nuevos datos
     * @param banco  Entidad existente a actualizar
     */
    @Mapping(target = "id", ignore = true)
    void updateEntity(BancoDTOUpdate dto, @MappingTarget Banco banco);

}
