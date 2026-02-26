package edu.utn.frsf.isi.dan.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOUpdate;
import edu.utn.frsf.isi.dan.user.mapper.config.MapstructConfig;
import edu.utn.frsf.isi.dan.user.model.Propietario;

/**
 * Mapper para conversión entre Propietario Entity y DTOs.
 * 
 * MapStruct genera automáticamente la implementación de este mapper.
 * Utiliza la configuración definida en MapstructConfig.
 * 
 * Este mapper usa CuentaBancariaMapper para mapear la cuenta bancaria del propietario.
 */
@Mapper(config = MapstructConfig.class, uses = {CuentaBancariaMapper.class})
public interface PropietarioMapper {
    
    /**
     * Convierte PropietarioDTORequest a Propietario Entity.
     * Los atributos heredados de Usuario se mapean automáticamente.
     * La cuenta bancaria se mapea usando CuentaBancariaMapper.
     * 
     * @param dto PropietarioDTORequest
     * @return Propietario entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cuentaBancaria", ignore = true) // Ignoramos el mapeo automático de la cuenta bancaria para manejarlo manualmente en el service
    Propietario toEntity(PropietarioDTORequest dto);
    
    /**
     * Convierte Propietario Entity a PropietarioDTOResponse.
     * Incluye la cuenta bancaria usando CuentaBancariaMapper.
     * 
     * @param entity Propietario entity
     * @return PropietarioDTOResponse
     */
    PropietarioDTOResponse toResponse(Propietario entity);

    /**
     * Actualiza una entidad Propietario existente con los datos del DTO de actualización.
     * Solo actualiza los campos permitidos: nombre, email, telefono, dni e idHotel.
     * La cuenta bancaria no se modifica.
     * 
     * @param dto PropietarioDTOUpdate con los nuevos datos
     * @param propietario Entidad existente a actualizar
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cuentaBancaria", ignore = true)
    void updateEntity(PropietarioDTOUpdate dto, @MappingTarget Propietario propietario);

}
