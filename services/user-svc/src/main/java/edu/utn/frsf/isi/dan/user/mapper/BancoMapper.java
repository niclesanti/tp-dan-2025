package edu.utn.frsf.isi.dan.user.mapper;

import org.mapstruct.Mapper;

import edu.utn.frsf.isi.dan.user.dto.BancoRecord;
import edu.utn.frsf.isi.dan.user.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.user.model.Banco;

/**
 * Mapper para conversión entre Banco Entity y BancoRecord DTO.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface BancoMapper {
    
    /**
     * Convierte BancoRecord DTO a Banco Entity.
     * El ID puede ser null para nuevas entidades (INSERT).
     * 
     * @param dto BancoRecord
     * @return Banco entity
     */
    Banco toEntity(BancoRecord dto);
    
    /**
     * Convierte Banco Entity a BancoRecord DTO.
     * 
     * @param entity Banco entity
     * @return BancoRecord DTO
     */
    BancoRecord toDto(Banco entity);
}
