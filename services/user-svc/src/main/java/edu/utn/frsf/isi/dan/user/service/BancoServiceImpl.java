package edu.utn.frsf.isi.dan.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dao.CuentaBancariaRepository;
import edu.utn.frsf.isi.dan.user.dao.TarjetaCreditoRepository;
import edu.utn.frsf.isi.dan.user.dto.BancoDTORequest;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOResponse;
import edu.utn.frsf.isi.dan.user.dto.BancoDTOUpdate;
import edu.utn.frsf.isi.dan.user.exception.BancoEnUsoException;
import edu.utn.frsf.isi.dan.user.mapper.BancoMapper;
import edu.utn.frsf.isi.dan.user.model.Banco;
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
    private final CuentaBancariaRepository cuentaBancariaRepository;
    
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

        if (tarjetaCreditoRepository.existsByBancoId(id)
                || cuentaBancariaRepository.existsByBancoId(id)) {
            String errorMessage = "No se puede eliminar el banco con ID: " + id
                    + " porque está en uso por tarjetas de crédito o cuentas bancarias";
            log.warn(errorMessage);
            throw new BancoEnUsoException(errorMessage);
        }

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


}