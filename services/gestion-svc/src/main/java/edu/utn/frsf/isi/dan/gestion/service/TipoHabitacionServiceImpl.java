package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTORequest;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTOResponse;
import edu.utn.frsf.isi.dan.gestion.dto.TipoHabitacionDTOUpdate;
import edu.utn.frsf.isi.dan.gestion.mapper.TipoHabitacionMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TipoHabitacionServiceImpl implements TipoHabitacionService {

    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final TipoHabitacionMapper tipoHabitacionMapper;

    @Transactional
    @Override
    public TipoHabitacionDTOResponse crearTipoHabitacion(TipoHabitacionDTORequest request) {
        log.info("Creando tipo de habitación con nombre {}", request.nombre());
        var entity = tipoHabitacionMapper.toEntity(request);
        entity = tipoHabitacionRepository.save(entity);
        log.info("Tipo de habitación creado exitosamente con ID: {}", entity.getId());
        return tipoHabitacionMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public TipoHabitacionDTOResponse buscarTipoHabitacionPorId(Integer id) {
        log.info("Buscando tipo de habitación con ID: {}", id);
        return tipoHabitacionMapper.toResponse(tipoHabitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TipoHabitacion no encontrada con ID: " + id)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<TipoHabitacionDTOResponse> buscarTiposHabitacion() {
        log.info("Buscando todos los tipos de habitación");
        return tipoHabitacionRepository.findAll().stream()
                .map(tipoHabitacionMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public TipoHabitacionDTOResponse actualizarTipoHabitacion(Integer id, TipoHabitacionDTOUpdate request) {
        log.info("Actualizando tipo de habitación con ID: {}", id);
        var entity = tipoHabitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TipoHabitacion no encontrada con ID: " + id));
        tipoHabitacionMapper.updateEntity(request, entity);
        var actualizado = tipoHabitacionRepository.save(entity);
        log.info("Tipo de habitación actualizado exitosamente con ID: {}", id);
        return tipoHabitacionMapper.toResponse(actualizado);
    }

    @Transactional
    @Override
    public void eliminarTipoHabitacion(Integer id) {
        log.info("Eliminando tipo de habitación con ID: {}", id);
        tipoHabitacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TipoHabitacion no encontrada con ID: " + id));
        tipoHabitacionRepository.deleteById(id);
        log.info("Tipo de habitación eliminado exitosamente con ID: {}", id);
    }
}
