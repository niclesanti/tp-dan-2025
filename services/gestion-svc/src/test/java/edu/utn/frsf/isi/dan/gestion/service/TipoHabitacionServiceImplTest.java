package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoHabitacionServiceTest {

    @Mock
    private TipoHabitacionRepository tipoHabitacionRepository;
    @InjectMocks
    private TipoHabitacionService tipoHabitacionService;

    @Test
    void saveShouldDelegateToRepository() {
        var tipo = TestDataFactory.tipoHabitacion();
        when(tipoHabitacionRepository.save(tipo)).thenReturn(tipo);
        assertThat(tipoHabitacionService.save(tipo)).isEqualTo(tipo);
    }

    @Test
    void deleteShouldDelegateToRepository() {
        tipoHabitacionService.deleteById(1);
        verify(tipoHabitacionRepository).deleteById(1);
    }

    @Test
    void findByIdShouldReturnOptional() {
        var tipo = TestDataFactory.tipoHabitacion();
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(tipo));
        assertThat(tipoHabitacionService.findById(1)).contains(tipo);
    }

    @Test
    void findAllShouldReturnList() {
        when(tipoHabitacionRepository.findAll()).thenReturn(List.of(TestDataFactory.tipoHabitacion()));
        assertThat(tipoHabitacionService.findAll()).hasSize(1);
    }
}

