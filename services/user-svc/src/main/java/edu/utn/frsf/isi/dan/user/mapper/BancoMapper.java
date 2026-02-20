package edu.utn.frsf.isi.dan.user.mapper;

import org.mapstruct.Mapper;

import edu.utn.frsf.isi.dan.user.dto.BancoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOResponse;
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
    Banco toEntity(BancoDTORequest dto);
    
    /**
     * Convierte Banco Entity a BancoDTOResponse.
     * 
     * @param entity Banco entity
     * @return BancoDTOResponse
     */
    BancoDTOResponse toResponse(Banco entity);
}
