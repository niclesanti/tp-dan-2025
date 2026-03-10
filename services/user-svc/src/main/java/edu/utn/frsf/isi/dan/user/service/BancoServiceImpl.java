package edu.utn.frsf.isi.dan.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dao.HuespedRepository;
import edu.utn.frsf.isi.dan.user.dao.TarjetaCreditoRepository;
import edu.utn.frsf.isi.dan.user.dto.BancoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOUpdate;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTOUpdate;
import edu.utn.frsf.isi.dan.user.exception.TarjetaPrincipalException;
import edu.utn.frsf.isi.dan.user.mapper.BancoMapper;
import edu.utn.frsf.isi.dan.user.mapper.TarjetaCreditoMapper;
import edu.utn.frsf.isi.dan.user.model.Banco;
import edu.utn.frsf.isi.dan.user.model.Huesped;
import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BancoServiceImpl implements BancoService {

    private final BancoRepository bancoRepository;
    private final BancoMapper bancoMapper;

    private final TarjetaCreditoRepository tarjetaCreditoRepository;
    private final TarjetaCreditoMapper tarjetaCreditoMapper;

    private final HuespedRepository huespedRepository;
    
    /**
     * Crea un nuevo banco en el sistema.
     *
     * @param bancoRequest DTO con los datos del nuevo banco
     * @return DTO de respuesta con los datos del banco creado
     */
    @Transactional
    @Override
    public BancoDTOResponse crearBanco(BancoDTORequest bancoRequest) {

        log.info("Creando banco con datos: {}", bancoRequest);

        Banco banco = bancoMapper.toEntity(bancoRequest);
        Banco bancoGuardado = bancoRepository.save(banco);

        log.info("Banco creado exitosamente con ID: {}", bancoGuardado.getId());

        return bancoMapper.toResponse(bancoGuardado);
    }

    /**
     * Actualiza los datos de un banco existente.
     *
     * @param id ID del banco a actualizar
     * @param bancoUpdate DTO con los nuevos datos del banco
     * @return DTO de respuesta con los datos del banco actualizado
     * @throws EntityNotFoundException si no se encuentra un banco con el ID especificado
     */
    @Transactional
    @Override
    public BancoDTOResponse actualizarBanco(Integer id, BancoDTOUpdate bancoUpdate) {

        log.info("Actualizando banco con ID: {}", id);

        Banco banco = bancoRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Banco no encontrado con ID: " + id;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });

        // Usamos el mapper en lugar de setters manuales
        bancoMapper.updateEntity(bancoUpdate, banco);

        Banco bancoActualizado = bancoRepository.save(banco);

        log.info("Banco actualizado exitosamente con ID: {}", bancoActualizado.getId());

        return bancoMapper.toResponse(bancoActualizado);
    }

    /**
     * Elimina un banco del sistema.
     *
     * @param id ID del banco a eliminar
     * @throws EntityNotFoundException si no se encuentra un banco con el ID especificado
     */
    @Transactional
    @Override
    public void eliminarBanco(Integer id) {

        log.info("Eliminando banco con ID: {}", id);

        Banco banco = bancoRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Banco no encontrado con ID: " + id;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });

        bancoRepository.delete(banco);

        log.info("Banco eliminado exitosamente con ID: {}", id);
    }

    /**
     * Busca un banco por su ID.
     *
     * @param id ID del banco a buscar
     * @return DTO de respuesta con los datos del banco encontrado
     * @throws EntityNotFoundException si no se encuentra un banco con el ID especificado
     */
    @Override
    public BancoDTOResponse buscarBancoPorId(Integer id) {

        log.info("Buscando banco con ID: {}", id);

        Banco banco = bancoRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = "Banco no encontrado con ID: " + id;
                    log.error(errorMessage);
                    return new EntityNotFoundException(errorMessage);
                });

        return bancoMapper.toResponse(banco);
    }

    /**
     * Lista todos los bancos del sistema.
     *
     * @return Lista de DTOs de respuesta con los datos de todos los bancos
     */
    @Override
    public List<BancoDTOResponse> listarBancos() {

        log.info("Listando todos los bancos");

        return bancoRepository.findAll()
                .stream()
                .map(bancoMapper::toResponse)
                .toList();
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
     * Lista todas las tarjetas de crédito de un huésped.
     *
     * @param huespedId ID del huésped
     * @return Lista de DTOs de respuesta con los datos de las tarjetas
     * @throws EntityNotFoundException si no se encuentra el huésped
     */
    @Override
    public List<TarjetaCreditoDTOResponse> listarTarjetas(Integer huespedId) {
        log.info("Listando tarjetas de crédito del huésped con ID: {}", huespedId);
        buscarHuespedOExcepcion(huespedId);
        // Filtramos en memoria desde findAll usando el huespedId
        return tarjetaCreditoRepository.findAll()
                .stream()
                .filter(t -> t.getHuesped().getId().equals(huespedId))
                .map(tarjetaCreditoMapper::toResponse)
                .toList();
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
        // Filtramos en memoria desde findAll usando el huespedId
        tarjetaCreditoRepository.findAll()
                .stream()
                .filter(t -> t.getHuesped().getId().equals(huespedId) && Boolean.TRUE.equals(t.getEsPrincipal()))
                .findFirst()
                .ifPresent(tarjetaAnterior -> {
                    tarjetaAnterior.setEsPrincipal(false);
                    tarjetaCreditoRepository.save(tarjetaAnterior);
                    log.info("Tarjeta principal anterior desmarcada con ID: {}", tarjetaAnterior.getId());
                });
    }


}