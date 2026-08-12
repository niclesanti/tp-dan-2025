package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.mapper.TipoHabitacionMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoHabitacionServiceImplTest {

    @Mock
    private TipoHabitacionRepository tipoHabitacionRepository;
    @Mock
    private TipoHabitacionMapper tipoHabitacionMapper;

    @InjectMocks
    private TipoHabitacionServiceImpl tipoHabitacionService;

    @Test
    void crearTipoHabitacion_RequestValido_RetornaResponse() {
        var request = TestDataFactory.tipoHabitacionDTORequest();
        var entity = TestDataFactory.tipoHabitacion();
        var response = TestDataFactory.tipoHabitacionDTOResponse();

        when(tipoHabitacionMapper.toEntity(request)).thenReturn(entity);
        when(tipoHabitacionRepository.save(entity)).thenReturn(entity);
        when(tipoHabitacionMapper.toResponse(entity)).thenReturn(response);

        var result = tipoHabitacionService.crearTipoHabitacion(request);

        assertThat(result).isEqualTo(response);
        verify(tipoHabitacionRepository).save(entity);
    }

    @Test
    void buscarTipoHabitacionPorId_TipoExistente_RetornaResponse() {
        var entity = TestDataFactory.tipoHabitacion();
        var response = TestDataFactory.tipoHabitacionDTOResponse();

        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(entity));
        when(tipoHabitacionMapper.toResponse(entity)).thenReturn(response);

        assertThat(tipoHabitacionService.buscarTipoHabitacionPorId(1)).isEqualTo(response);
    }

    @Test
    void buscarTipoHabitacionPorId_TipoInexistente_LanzaEntityNotFoundException() {
        when(tipoHabitacionRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipoHabitacionService.buscarTipoHabitacionPorId(9))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TipoHabitacion no encontrada con ID: 9");
    }

    @Test
    void buscarTiposHabitacion_ConDatos_RetornaListaMapeada() {
        var entity = TestDataFactory.tipoHabitacion();
        var response = TestDataFactory.tipoHabitacionDTOResponse();

        when(tipoHabitacionRepository.findAll()).thenReturn(List.of(entity));
        when(tipoHabitacionMapper.toResponse(entity)).thenReturn(response);

        assertThat(tipoHabitacionService.buscarTiposHabitacion()).containsExactly(response);
    }

    @Test
    void buscarTiposHabitacion_SinDatos_RetornaListaVacia() {
        when(tipoHabitacionRepository.findAll()).thenReturn(List.of());

        assertThat(tipoHabitacionService.buscarTiposHabitacion()).isEmpty();
    }

    @Test
    void actualizarTipoHabitacion_TipoExistente_ActualizaYRetornaResponse() {
        var update = TestDataFactory.tipoHabitacionDTOUpdate();
        var entity = TestDataFactory.tipoHabitacion();
        var response = TestDataFactory.tipoHabitacionDTOResponse();

        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(entity));
        when(tipoHabitacionRepository.save(entity)).thenReturn(entity);
        when(tipoHabitacionMapper.toResponse(entity)).thenReturn(response);

        var result = tipoHabitacionService.actualizarTipoHabitacion(1, update);

        assertThat(result).isEqualTo(response);
        verify(tipoHabitacionMapper).updateEntity(update, entity);
        verify(tipoHabitacionRepository).save(entity);
    }

    @Test
    void actualizarTipoHabitacion_TipoInexistente_LanzaEntityNotFoundException() {
        when(tipoHabitacionRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipoHabitacionService.actualizarTipoHabitacion(9, TestDataFactory.tipoHabitacionDTOUpdate()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void eliminarTipoHabitacion_TipoExistente_Elimina() {
        var entity = TestDataFactory.tipoHabitacion();
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(entity));

        tipoHabitacionService.eliminarTipoHabitacion(1);

        verify(tipoHabitacionRepository).deleteById(1);
    }

    @Test
    void eliminarTipoHabitacion_TipoInexistente_LanzaEntityNotFoundException() {
        when(tipoHabitacionRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipoHabitacionService.eliminarTipoHabitacion(9))
                .isInstanceOf(EntityNotFoundException.class);
    }
}