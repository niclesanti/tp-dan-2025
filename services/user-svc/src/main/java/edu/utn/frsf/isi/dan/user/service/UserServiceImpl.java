package edu.utn.frsf.isi.dan.user.service;

import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dao.CuentaBancariaRepository;
import edu.utn.frsf.isi.dan.user.dao.HuespedRepository;
import edu.utn.frsf.isi.dan.user.dao.PropietarioRepository;
import edu.utn.frsf.isi.dan.user.dao.UsuarioRepository;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.UsuarioDTOResponse;
import edu.utn.frsf.isi.dan.user.mapper.CuentaBancariaMapper;
import edu.utn.frsf.isi.dan.user.mapper.HuespedMapper;
import edu.utn.frsf.isi.dan.user.mapper.PropietarioMapper;
import edu.utn.frsf.isi.dan.user.mapper.TarjetaCreditoMapper;
import edu.utn.frsf.isi.dan.user.mapper.UsuarioMapper;
import edu.utn.frsf.isi.dan.user.model.Banco;
import edu.utn.frsf.isi.dan.user.model.CuentaBancaria;
import edu.utn.frsf.isi.dan.user.model.Huesped;
import edu.utn.frsf.isi.dan.user.model.Propietario;
import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class UserServiceImpl implements UserService {

    private final BancoRepository bancoRepository;
    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HuespedRepository huespedRepository;
    private final PropietarioRepository propietarioRepository;

    private final HuespedMapper huespedMapper;
    private final PropietarioMapper propietarioMapper;
    private final CuentaBancariaMapper cuentaBancariaMapper;
    private final TarjetaCreditoMapper tarjetaCreditoMapper;
    private final UsuarioMapper usuarioMapper;


    /*
    ----------------------------
    GESTIÓN DE USUARIOS HUESPED
    ----------------------------
    */

    /**
     * Crea un nuevo usuario de tipo huesped con su tarjeta de crédito asociada.
     * Valida que el banco exista antes de crear la tarjeta de crédito.
     * 
     * @param huespedRequest DTO con los datos del nuevo huésped y su tarjeta de crédito
     * @return DTO de respuesta con los datos del huésped creado
     * @throws EntityNotFoundException si el banco especificado en la tarjeta de crédito no existe
     */
    @Transactional
    @Override
    public HuespedDTOResponse createUsuarioHuesped(HuespedDTORequest huespedRequest) {

        log.info("Creando usuario huesped con datos: {}", huespedRequest);

        // Buscar el banco por ID
        Banco banco = bancoRepository.findById(huespedRequest.tarjetaCredito().bancoId())
                .orElseThrow(() -> {
                    String errorMessage = "Banco no encontrado con ID: " + huespedRequest.tarjetaCredito().bancoId();
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });

        // Crear el huésped
        Huesped huesped = huespedMapper.toEntity(huespedRequest);

        // Crear la tarjeta de crédito y establecer relaciones bidireccionales
        TarjetaCredito tarjetaCredito = tarjetaCreditoMapper.toEntity(huespedRequest.tarjetaCredito());
        tarjetaCredito.setHuesped(huesped);
        tarjetaCredito.setBanco(banco);
        
        // Inicializar la lista de tarjetas si es null y agregar la tarjeta
        if (huesped.getTarjetaCredito() == null) {
            huesped.setTarjetaCredito(new ArrayList<>());
        }
        huesped.getTarjetaCredito().add(tarjetaCredito);

        // Guardar el huésped (la tarjeta se guarda automáticamente por cascada)
        Huesped huespedGuardado = huespedRepository.save(huesped);
        
        log.info("Usuario huesped creado exitosamente con ID: {}", huespedGuardado.getId());

        return huespedMapper.toResponse(huespedGuardado);
    }

    /**
     * Actualiza los datos de un usuario huesped existente. Solo se pueden actualizar los campos de Usuario y fechaNacimiento.
     * Las tarjetas de crédito no se actualizan mediante este método.
     * 
     * @param id ID del huésped a actualizar
     * @param huespedUpdate DTO con los nuevos datos del huésped (sin tarjetas de crédito)
     * @return DTO de respuesta con los datos del huésped actualizado
     * @throws EntityNotFoundException si no se encuentra un huésped con el ID especificado
     */
    @Transactional
    @Override
    public HuespedDTOResponse updateUsuarioHuesped(Integer id, HuespedDTOUpdate huespedUpdate) {
        
        log.info("Actualizando usuario huesped con ID: {}", id);
        
        // Buscar el huésped existente
        Huesped huesped = huespedRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Usuario no encontrado con ID: " + id;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });
        
        // Actualizar solo los campos permitidos usando el mapper
        huespedMapper.updateEntity(huespedUpdate, huesped);
        
        // Guardar los cambios
        Huesped huespedActualizado = huespedRepository.save(huesped);
        
        log.info("Usuario huesped actualizado exitosamente con ID: {}", huespedActualizado.getId());
        
        return huespedMapper.toResponse(huespedActualizado);
    }

    /**
     * Elimina un usuario huesped del sistema.
     * Las tarjetas de crédito asociadas se eliminan automáticamente por la configuración de cascada en la entidad Huesped.
     * 
     * @param id ID del huésped a eliminar
     * @throws EntityNotFoundException si no se encuentra un huésped con el ID especificado
     */
    @Transactional
    @Override
    public void deleteUsuarioHuesped(Integer id) {
        
        log.info("Eliminando usuario huesped con ID: {}", id);
        
        // Buscar el huésped existente
        Huesped huesped = huespedRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Usuario no encontrado con ID: " + id;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });
        
        // Eliminar el huésped (las tarjetas se eliminan automáticamente por cascada)
        huespedRepository.delete(huesped);
        
        log.info("Usuario huesped eliminado exitosamente con ID: {}", id);
    }

    /*
    --------------------------------
    GESTIÓN DE USUARIOS PROPIETARIO
    --------------------------------
    */

    /**
    * Crea un nuevo usuario de tipo propietario.
    * La cuenta bancaria e idHotel son opcionales al momento de la creación.
    * Si se proporciona cuenta bancaria, se valida que el banco exista.
    *
    * @param propietarioRequest DTO con los datos del nuevo propietario
    * @return DTO de respuesta con los datos del propietario creado
    * @throws EntityNotFoundException si se especifica un banco que no existe
    */
    @Transactional
    @Override
    public PropietarioDTOResponse createUsuarioPropietario(PropietarioDTORequest propietarioRequest) {

        log.info("Creando usuario propietario con datos: {}", propietarioRequest);

        Propietario propietario = propietarioMapper.toEntity(propietarioRequest);
        
        Banco banco = bancoRepository.findById(propietarioRequest.cuentaBancaria().bancoId())
                    .orElseThrow(() -> {
                        String errorMessage = "Banco no encontrado con ID: " + propietarioRequest.cuentaBancaria().bancoId();
                        log.error(errorMessage);
                        return new EntityNotFoundException(errorMessage);
                    });

        CuentaBancaria cuentaBancaria = cuentaBancariaMapper.toEntity(propietarioRequest.cuentaBancaria());
        cuentaBancaria.setBanco(banco);
        cuentaBancaria.setPropietario(propietario);
        propietario.setCuentaBancaria(cuentaBancariaRepository.save(cuentaBancaria));
        

        Propietario propietarioGuardado = propietarioRepository.save(propietario);

        log.info("Usuario propietario creado exitosamente con ID: {}", propietarioGuardado.getId());

        return propietarioMapper.toResponse(propietarioGuardado);
    }

        
    /**
     * Actualiza los datos de un usuario propietario existente.
     * Solo se pueden actualizar los campos de Usuario e idHotel.
    * La cuenta bancaria no se actualiza mediante este método.
    *
    * @param id ID del propietario a actualizar
    * @param propietarioUpdate DTO con los nuevos datos del propietario
    * @return DTO de respuesta con los datos del propietario actualizado
    * @throws EntityNotFoundException si no se encuentra un propietario con el ID especificado
    */
    @Transactional
    @Override
    public PropietarioDTOResponse updateUsuarioPropietario(Integer id, PropietarioDTOUpdate propietarioUpdate) {
        log.info("Actualizando usuario propietario con ID: {}", id);

        // Buscar el propietario existente
        Propietario propietario = propietarioRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Propietario no encontrado con ID: " + id;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });

        propietarioMapper.updateEntity(propietarioUpdate, propietario);

        Propietario propietarioActualizado = propietarioRepository.save(propietario);

        log.info("Usuario propietario actualizado exitosamente con ID: {}", propietarioActualizado.getId());

        return propietarioMapper.toResponse(propietarioActualizado);

    }
    
    /**
    * Elimina un usuario propietario del sistema.
    *
    * @param id ID del propietario a eliminar
    * @throws EntityNotFoundException si no se encuentra un propietario con el ID especificado
    */
    @Transactional
    @Override
    public void deleteUsuarioPropietario(Integer id) {

        log.info("Eliminando usuario propietario con ID: {}", id);

        Propietario propietario = propietarioRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Propietario no encontrado con ID: " + id;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });

        propietarioRepository.delete(propietario);

        log.info("Usuario propietario eliminado exitosamente con ID: {}", id);
    }


     * Busca usuarios cuyo nombre contenga el texto proporcionado (búsqueda parcial, insensible a mayúsculas).
     * Si el nombre está vacío, devuelve todos los usuarios paginados.
     *
     * @param nombre texto parcial a buscar en el campo nombre
     * @param pageable parámetros de paginación y orden
     * @return página de {@link UsuarioDTOResponse} que coinciden con el criterio
     */
    @Transactional(readOnly = true)
    @Override
    public Page<UsuarioDTOResponse> buscarPorNombre(String nombre, Pageable pageable) {
        log.info("Buscando usuarios por nombre con criterio: '{}'", nombre);
        // Delega al repositorio la búsqueda parcial case-insensitive; mapea cada entidad al DTO de respuesta
        var resultado = usuarioRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(usuarioMapper::toResponse);
        log.info("Búsqueda por nombre '{}' retornó {} resultados en página {}/{}",
                nombre, resultado.getNumberOfElements(), resultado.getNumber() + 1, resultado.getTotalPages());
        return resultado;
    }

    /**
     * Busca usuarios cuyo DNI contenga el texto proporcionado (búsqueda parcial).
     * Si el DNI está vacío, devuelve todos los usuarios paginados.
     *
     * @param dni texto parcial a buscar en el campo dni
     * @param pageable parámetros de paginación y orden
     * @return página de {@link UsuarioDTOResponse} que coinciden con el criterio
     */
    @Transactional(readOnly = true)
    @Override
    public Page<UsuarioDTOResponse> buscarPorDni(String dni, Pageable pageable) {
        log.info("Buscando usuarios por DNI con criterio: '{}'", dni);
        // Delega al repositorio la búsqueda parcial; mapea cada entidad al DTO de respuesta
        var resultado = usuarioRepository.findByDniContaining(dni, pageable)
                .map(usuarioMapper::toResponse);
        log.info("Búsqueda por DNI '{}' retornó {} resultados en página {}/{}",
                dni, resultado.getNumberOfElements(), resultado.getNumber() + 1, resultado.getTotalPages());
        return resultado;
    }

    /**
     * Busca un usuario cuyo DNI coincida exactamente con el valor proporcionado.
     *
     * @param dni DNI exacto a buscar
     * @return {@link UsuarioDTOResponse} del usuario encontrado
     * @throws EntityNotFoundException si no existe ningún usuario con ese DNI
     */
    @Transactional(readOnly = true)
    @Override
    public UsuarioDTOResponse buscarPorDniExacto(String dni) {
        log.info("Buscando usuario con DNI exacto: '{}'", dni);
        // Lanza EntityNotFoundException directamente si el DNI no corresponde a ningún usuario registrado
        var usuario = usuarioRepository.findByDni(dni)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado con DNI: '{}'", dni);
                    return new EntityNotFoundException("Usuario no encontrado con DNI: " + dni);
                });
        log.info("Usuario encontrado con DNI '{}': id={}, tipo={}", dni, usuario.getId(), usuario.getClass().getSimpleName());
        return usuarioMapper.toResponse(usuario);
    }
}