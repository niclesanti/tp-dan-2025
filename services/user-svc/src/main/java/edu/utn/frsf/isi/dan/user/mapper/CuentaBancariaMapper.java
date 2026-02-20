package edu.utn.frsf.isi.dan.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import edu.utn.frsf.isi.dan.user.dto.CuentaBancariaDTORequest;
import edu.utn.frsf.isi.dan.user.dto.CuentaBancariaDTOResponse;
import edu.utn.frsf.isi.dan.user.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.user.model.CuentaBancaria;

/**
 * Mapper para conversión entre CuentaBancaria Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface CuentaBancariaMapper {
    
    /**
     * Convierte CuentaBancariaDTORequest a CuentaBancaria Entity.
     * El banco se setea manualmente en el service.
     * 
     * @param dto CuentaBancariaDTORequest
     * @return CuentaBancaria entity
     */
    @Mapping(target = "propietario", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "banco", ignore = true) // Ignoramos el mapeo automático del banco para manejarlo manualmente
    CuentaBancaria toEntity(CuentaBancariaDTORequest dto);
    
    /**
     * Convierte CuentaBancaria Entity a CuentaBancariaDTOResponse.
     * El nombre del banco se extrae de la entidad Banco relacionada.
     * 
     * @param entity CuentaBancaria entity
     * @return CuentaBancariaDTOResponse
     */
    @Mapping(target = "nombreBanco", expression = "java(entity.getBanco() != null ? entity.getBanco().getNombre() : null)")
    CuentaBancariaDTOResponse toResponse(CuentaBancaria entity);
    
}
