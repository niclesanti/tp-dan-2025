package edu.utn.frsf.isi.dan.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTOUpdate;
import edu.utn.frsf.isi.dan.user.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;

/**
 * Mapper para conversión entre TarjetaCredito Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 */
@Mapper(config = MapstructConfig.class)
public interface TarjetaCreditoMapper {
    
    /**
     * Convierte TarjetaCreditoDTORequest a TarjetaCredito Entity.
     * El banco y el huesped se setean manualmente en el service.
     *
     * @param dto TarjetaCreditoDTORequest
     * @return TarjetaCredito entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "huesped", ignore = true)
    @Mapping(target = "banco", ignore = true)
    TarjetaCredito toEntity(TarjetaCreditoDTORequest dto);

    /**
     * Convierte TarjetaCredito Entity a TarjetaCreditoDTOResponse.
     * El nombre del banco se extrae de la entidad Banco relacionada.
     *
     * @param entity TarjetaCredito entity
     * @return TarjetaCreditoDTOResponse
     */
    @Mapping(target = "nombreBanco", expression = "java(entity.getBanco() != null ? entity.getBanco().getNombre() : null)")
    TarjetaCreditoDTOResponse toResponse(TarjetaCredito entity); 

    /**
     * Actualiza una entidad TarjetaCredito existente con los datos del DTO de actualización.
     * El banco y el huesped se actualizan manualmente en el service.
     *
     * @param dto     TarjetaCreditoDTOUpdate con los nuevos datos
     * @param tarjeta Entidad existente a actualizar
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "huesped", ignore = true)
    @Mapping(target = "banco", ignore = true)
    void updateEntity(TarjetaCreditoDTOUpdate dto, @MappingTarget TarjetaCredito tarjeta);

    
}
