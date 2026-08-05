package edu.utn.frsf.isi.dan.user.service;

import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dao.CuentaBancariaRepository;
import edu.utn.frsf.isi.dan.user.dao.HuespedRepository;
import edu.utn.frsf.isi.dan.user.dao.PropietarioRepository;
import edu.utn.frsf.isi.dan.user.dao.TarjetaCreditoRepository;
import edu.utn.frsf.isi.dan.user.dao.UsuarioRepository;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.TarjetaPrincipalDTO;
import edu.utn.frsf.isi.dan.user.dto.UsuarioDTOResponse;
import edu.utn.frsf.isi.dan.user.exception.TarjetaPrincipalException;
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
    private final TarjetaCreditoRepository tarjetaCreditoRepository;

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

        // Romper la asociación bidireccional @OneToOne antes de eliminar.
        // Si no se hace, Hibernate lanza TransientObjectException en el flush porque
        // CuentaBancaria (persistente en sesión) todavía referencia al Propietario
        // que está siendo marcado como eliminado.
        CuentaBancaria cuenta = propietario.getCuentaBancaria();
        if (cuenta != null) {
            cuenta.setPropietario(null);        // rompe la referencia inversa en sesión
            propietario.setCuentaBancaria(null); // actualiza cuenta_bancaria_id = NULL en BD
            propietarioRepository.saveAndFlush(propietario);
            cuentaBancariaRepository.delete(cuenta);
        }

        propietarioRepository.delete(propietario);

        log.info("Usuario propietario eliminado exitosamente con ID: {}", id);
    }

    /**
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

    // ==============================
    // GESTIÓN DE TARJETAS DE CRÉDITO
    // ==============================

    /**
     * Agrega una tarjeta de crédito a un huésped.
     * Si se la agrega como principal, desmarca la tarjeta principal anterior.
     *
     * @param huespedId ID del huésped
     * @param request   DTO con los datos de la nueva tarjeta
     * @return DTO de respuesta con los datos de la tarjeta creada
     * @throws EntityNotFoundException si no se encuentra el huésped o el banco
     */
    @Transactional
    @Override
    public TarjetaCreditoDTOResponse agregarTarjeta(Integer huespedId, TarjetaCreditoDTORequest request) {
        log.info("Agregando tarjeta de crédito al huésped con ID: {}", huespedId);

        Huesped huesped = buscarHuespedOExcepcion(huespedId);
        Banco banco = buscarBancoOExcepcion(request.bancoId());

        if (Boolean.TRUE.equals(request.esPrincipal())) {
            desmarcarTarjetaPrincipalAnterior(huespedId);
        }

        TarjetaCredito tarjeta = tarjetaCreditoMapper.toEntity(request);
        tarjeta.setBanco(banco);
        tarjeta.setHuesped(huesped);

        TarjetaCredito tarjetaGuardada = tarjetaCreditoRepository.save(tarjeta);
        log.info("Tarjeta de crédito agregada exitosamente con ID: {}", tarjetaGuardada.getId());

        return tarjetaCreditoMapper.toResponse(tarjetaGuardada);
    }

    /**
     * Elimina una tarjeta de crédito si no es la principal.
     *
     * @param huespedId ID del huésped propietario de la tarjeta
     * @param tarjetaId ID de la tarjeta a eliminar
     * @throws EntityNotFoundException  si no se encuentra la tarjeta o el huésped
     * @throws IllegalArgumentException si la tarjeta es la principal o no pertenece al huésped
     */
    @Transactional
    @Override
    public void eliminarTarjeta(Integer huespedId, Integer tarjetaId) {
        log.info("Eliminando tarjeta con ID: {} del huésped con ID: {}", tarjetaId, huespedId);

        TarjetaCredito tarjeta = buscarTarjetaOExcepcion(tarjetaId);
        validarTarjetaPertenecealHuesped(tarjeta, huespedId);

        if (Boolean.TRUE.equals(tarjeta.getEsPrincipal())) {
            String errorMessage = "No se puede eliminar la tarjeta principal del huésped con ID: " + huespedId;
            log.error(errorMessage);
            throw new TarjetaPrincipalException(errorMessage);
        }

        tarjetaCreditoRepository.delete(tarjeta);
        log.info("Tarjeta de crédito eliminada exitosamente con ID: {}", tarjetaId);
    }

    /**
     * Cambia la tarjeta de crédito principal de un huésped.
     *
     * @param huespedId ID del huésped
     * @param tarjetaId ID de la nueva tarjeta principal
     * @return DTO de respuesta con los datos de la nueva tarjeta principal
     * @throws EntityNotFoundException  si no se encuentra la tarjeta o el huésped
     * @throws IllegalArgumentException si la tarjeta ya es principal o no pertenece al huésped
     */
    @Transactional
    @Override
    public TarjetaCreditoDTOResponse cambiarTarjetaPrincipal(Integer huespedId, Integer tarjetaId) {
        log.info("Cambiando tarjeta principal del huésped con ID: {} a tarjeta con ID: {}", huespedId, tarjetaId);

        TarjetaCredito nuevaPrincipal = buscarTarjetaOExcepcion(tarjetaId);
        validarTarjetaPertenecealHuesped(nuevaPrincipal, huespedId);

        if (Boolean.TRUE.equals(nuevaPrincipal.getEsPrincipal())) {
            String errorMessage = "La tarjeta con ID: " + tarjetaId + " ya es la tarjeta principal";
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        desmarcarTarjetaPrincipalAnterior(huespedId);
        nuevaPrincipal.setEsPrincipal(true);

        TarjetaCredito tarjetaActualizada = tarjetaCreditoRepository.save(nuevaPrincipal);
        log.info("Tarjeta principal cambiada exitosamente a ID: {}", tarjetaActualizada.getId());

        return tarjetaCreditoMapper.toResponse(tarjetaActualizada);
    }

    /**
     * Lista las tarjetas de crédito de un huésped de forma paginada.
     *
     * @param huespedId ID del huésped
     * @param pageable  parámetros de paginación y orden
     * @return página de DTOs de respuesta con los datos de las tarjetas
     * @throws EntityNotFoundException si no se encuentra el huésped
     */
    @Transactional(readOnly = true)
    @Override
    public Page<TarjetaCreditoDTOResponse> listarTarjetas(Integer huespedId, Pageable pageable) {
        log.info("Listando tarjetas de crédito del huésped con ID: {}", huespedId);
        buscarHuespedOExcepcion(huespedId);
        var resultado = tarjetaCreditoRepository.findByHuespedId(huespedId, pageable)
                .map(tarjetaCreditoMapper::toResponse);
        log.info("Listado de tarjetas del huésped {} retornó {} resultados en página {}/{}",
                huespedId, resultado.getNumberOfElements(), resultado.getNumber() + 1, resultado.getTotalPages());
        return resultado;
    }

    /**
     * Obtiene el número de la tarjeta de crédito principal de un huésped buscándolo por DNI.
     *
     * @param dni DNI exacto del huésped
     * @return DTO con el número de la tarjeta de crédito principal del huésped
     * @throws EntityNotFoundException si el DNI no existe, el usuario no es huésped o no tiene tarjeta principal
     */
    @Transactional(readOnly = true)
    @Override
    public TarjetaPrincipalDTO obtenerTarjetaPrincipalPorDni(String dni) {
        log.info("Obteniendo tarjeta principal para huésped con DNI: {}", dni);
        var usuario = usuarioRepository.findByDni(dni)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró usuario con DNI: " + dni));
        if (!(usuario instanceof Huesped huesped)) {
            throw new EntityNotFoundException("El usuario con DNI " + dni + " no es un huésped");
        }
        var tarjeta = tarjetaCreditoRepository
                .findByHuespedIdAndEsPrincipalTrue(huesped.getId())
                .orElseThrow(() -> new EntityNotFoundException("El huésped con DNI " + dni + " no tiene tarjeta principal"));
        return new TarjetaPrincipalDTO(tarjeta.getNumero());
    }

    // ==============================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==============================

    private Banco buscarBancoOExcepcion(Integer bancoId) {
        return bancoRepository.findById(bancoId)
                .orElseThrow(() -> {
                    String errorMessage = "Banco no encontrado con ID: " + bancoId;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });
    }

    private Huesped buscarHuespedOExcepcion(Integer huespedId) {
        return huespedRepository.findById(huespedId)
                .orElseThrow(() -> {
                    String errorMessage = "Huésped no encontrado con ID: " + huespedId;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });
    }

    private TarjetaCredito buscarTarjetaOExcepcion(Integer tarjetaId) {
        return tarjetaCreditoRepository.findById(tarjetaId)
                .orElseThrow(() -> {
                    String errorMessage = "Tarjeta de crédito no encontrada con ID: " + tarjetaId;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });
    }

    private void validarTarjetaPertenecealHuesped(TarjetaCredito tarjeta, Integer huespedId) {
        if (!tarjeta.getHuesped().getId().equals(huespedId)) {
            String errorMessage = "La tarjeta con ID: " + tarjeta.getId() + " no pertenece al huésped con ID: " + huespedId;
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void desmarcarTarjetaPrincipalAnterior(Integer huespedId) {
        tarjetaCreditoRepository.findByHuespedIdAndEsPrincipalTrue(huespedId)
                .ifPresent(tarjetaAnterior -> {
                    tarjetaAnterior.setEsPrincipal(false);
                    tarjetaCreditoRepository.save(tarjetaAnterior);
                    log.info("Tarjeta principal anterior desmarcada con ID: {}", tarjetaAnterior.getId());
                });
    }
}