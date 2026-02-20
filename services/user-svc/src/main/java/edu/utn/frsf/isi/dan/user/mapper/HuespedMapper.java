package edu.utn.frsf.isi.dan.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOResponse;
import edu.utn.frsf.isi.dan.user.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.user.model.Huesped;

/**
 * Mapper para conversión entre Huesped Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 * 
 * Este mapper usa TarjetaCreditoMapper para mapear la lista de tarjetas de crédito.
 */
@Mapper(config = MapstructConfig.class, uses = {TarjetaCreditoMapper.class})
public interface HuespedMapper {
    
    /**
     * Convierte HuespedDTORequest a Huesped Entity.
     * Los atributos heredados de Usuario se mapean automáticamente.
     * La lista de tarjetas de crédito se mapea usando TarjetaCreditoMapper, pero se ignora en el mapeo automático para manejarla manualmente en el service.
     * 
     * 
     * @param dto HuespedDTORequest
     * @return Huesped entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tarjetaCredito", ignore = true)
    Huesped toEntity(HuespedDTORequest dto);
    
    /**
     * Convierte Huesped Entity a HuespedDTOResponse.
     * Incluye la lista de tarjetas de crédito usando TarjetaCreditoMapper.
     * 
     * @param entity Huesped entity
     * @return HuespedDTOResponse
     */
    HuespedDTOResponse toResponse(Huesped entity);
}
