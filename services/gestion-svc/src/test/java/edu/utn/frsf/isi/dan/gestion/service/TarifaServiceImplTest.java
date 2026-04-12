package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.TestDataFactory;
import edu.utn.frsf.isi.dan.gestion.dao.TarifaRepository;
import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.mapper.SharedDTOMapper;
import edu.utn.frsf.isi.dan.gestion.mapper.TarifaMapper;
import edu.utn.frsf.isi.dan.gestion.messaging.GestionMessagePublisher;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;
import edu.utn.frsf.isi.dan.shared.TarifaDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarifaServiceImplTest {

    @Mock
    private TarifaRepository tarifaRepository;
    @Mock
    private TipoHabitacionRepository tipoHabitacionRepository;
    @Mock
    private TarifaMapper tarifaMapper;
    @Mock
    private SharedDTOMapper sharedDTOMapper;
    @Mock
    private GestionMessagePublisher messagePublisher;

    @InjectMocks
    private TarifaServiceImpl tarifaService;

    @Test
    void crearTarifaNormalShouldCloseCurrentAndCreateNewOne() {
        var req = TestDataFactory.tarifaDTORequestNormal();
        var tipo = TestDataFactory.tipoHabitacion();
        var vigente = TestDataFactory.tarifaVigente();
        var nueva = Tarifa.builder().tipoHabitacion(tipo).precioNoche(50000.0).build();
        var guardada = Tarifa.builder().id(3).tipoHabitacion(tipo).fechaInicio(LocalDate.now()).precioNoche(50000.0).build();
        var response = TestDataFactory.tarifaDTOResponse();

        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(tipo));
        when(tarifaRepository.buscarTarifasVigentesEnFecha(any(), any())).thenReturn(List.of(vigente), List.of());
        when(tarifaMapper.toEntity(req)).thenReturn(nueva);
        when(tarifaRepository.save(any(Tarifa.class))).thenReturn(guardada);
        when(tarifaMapper.toResponse(guardada)).thenReturn(response);
        when(sharedDTOMapper.toTarifaDTO(any())).thenReturn(TarifaDTO.builder().tipoHabitacionId(1).nuevoPrecio(50000.0).build());

        var result = tarifaService.crearTarifa(req);

        assertThat(result).isEqualTo(response);
        verify(messagePublisher).publishHabitacionEvent(any());
    }

    @Test
    void crearTarifaPromocionalShouldFailWhenDatesAreInvalid() {
        var req = new edu.utn.frsf.isi.dan.gestion.dto.TarifaDTORequest(LocalDate.now().plusDays(5), LocalDate.now().plusDays(1), 1, 100.0);
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.tipoHabitacion()));
        assertThatThrownBy(() -> tarifaService.crearTarifa(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crearTarifaShouldIgnorePublisherError() {
        var req = TestDataFactory.tarifaDTORequestNormal();
        var tipo = TestDataFactory.tipoHabitacion();
        var nueva = Tarifa.builder().tipoHabitacion(tipo).precioNoche(50000.0).build();
        var guardada = Tarifa.builder().id(3).tipoHabitacion(tipo).fechaInicio(LocalDate.now()).precioNoche(50000.0).build();
        var response = TestDataFactory.tarifaDTOResponse();

        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(tipo));
        when(tarifaRepository.buscarTarifasVigentesEnFecha(any(), any())).thenReturn(List.of(), List.of());
        when(tarifaMapper.toEntity(req)).thenReturn(nueva);
        when(tarifaRepository.save(any(Tarifa.class))).thenReturn(guardada);
        when(tarifaMapper.toResponse(guardada)).thenReturn(response);
        when(sharedDTOMapper.toTarifaDTO(any())).thenReturn(TarifaDTO.builder().tipoHabitacionId(1).nuevoPrecio(50000.0).build());
        doThrow(new RuntimeException("mq")).when(messagePublisher).publishHabitacionEvent(any());

        assertThat(tarifaService.crearTarifa(req)).isEqualTo(response);
    }

    @Test
    void eliminarTarifaShouldFailWhenSingleRate() {
        var tarifa = TestDataFactory.tarifaVigente();
        when(tarifaRepository.findById(1)).thenReturn(Optional.of(tarifa));
        when(tarifaRepository.countByTipoHabitacionId(1)).thenReturn(1L);
        assertThatThrownBy(() -> tarifaService.eliminarTarifa(1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eliminarTarifaVigenteShouldPromotePrevious() {
        var tipo = TipoHabitacion.builder().id(1).build();
        var vigente = Tarifa.builder().id(1).tipoHabitacion(tipo).fechaInicio(LocalDate.now().minusDays(2)).fechaFin(null).precioNoche(100.0).build();
        var anterior = Tarifa.builder().id(2).tipoHabitacion(tipo).fechaInicio(LocalDate.now().minusDays(5)).fechaFin(LocalDate.now().minusDays(1)).precioNoche(80.0).build();
        when(tarifaRepository.findById(1)).thenReturn(Optional.of(vigente));
        when(tarifaRepository.countByTipoHabitacionId(1)).thenReturn(2L);
        when(tarifaRepository.buscarTarifasAnteriores(1, vigente.getFechaInicio())).thenReturn(List.of(anterior));

        tarifaService.eliminarTarifa(1);

        verify(tarifaRepository).delete(vigente);
        verify(tarifaRepository).save(anterior);
    }

    @Test
    void buscarTarifaByIdShouldThrowWhenMissing() {
        when(tarifaRepository.findById(9)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> tarifaService.buscarTarifaPorId(9)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void buscarTarifasShouldReturnPage() {
        var tarifa = TestDataFactory.tarifaVigente();
        when(tarifaRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(tarifa)));
        when(tarifaMapper.toResponse(tarifa)).thenReturn(TestDataFactory.tarifaDTOResponse());
        assertThat(tarifaService.buscarTarifas(PageRequest.of(0, 10)).getContent()).hasSize(1);
    }

    @Test
    void crearTarifaShouldFailWhenOnlyOneDateIsProvided() {
        var req = new edu.utn.frsf.isi.dan.gestion.dto.TarifaDTORequest(LocalDate.now().plusDays(1), null, 1, 100.0);
        assertThatThrownBy(() -> tarifaService.crearTarifa(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crearTarifaShouldFailWhenTipoHabitacionDoesNotExist() {
        var req = TestDataFactory.tarifaDTORequestNormal();
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarifaService.crearTarifa(req)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void crearTarifaPromocionalShouldFailWhenBaseRateDoesNotExist() {
        var req = TestDataFactory.tarifaDTORequestPromo();
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(TestDataFactory.tipoHabitacion()));
        when(tarifaRepository.buscarTarifasVigentesEnFecha(any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> tarifaService.crearTarifa(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crearTarifaPromocionalShouldFailWhenClosingBaseIsInvalid() {
        var inicio = LocalDate.now().plusDays(2);
        var req = new edu.utn.frsf.isi.dan.gestion.dto.TarifaDTORequest(inicio, inicio.plusDays(1), 1, 80.0);
        var tipo = TestDataFactory.tipoHabitacion();
        var base = Tarifa.builder().id(9).tipoHabitacion(tipo).fechaInicio(inicio).fechaFin(null).precioNoche(100.0).build();
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(tipo));
        when(tarifaRepository.buscarTarifasVigentesEnFecha(any(), eq(inicio))).thenReturn(List.of(base));

        assertThatThrownBy(() -> tarifaService.crearTarifa(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crearTarifaPromocionalShouldCreatePromoAndPostRate() {
        var req = TestDataFactory.tarifaDTORequestPromo();
        var tipo = TestDataFactory.tipoHabitacion();
        var base = Tarifa.builder().id(9).tipoHabitacion(tipo).fechaInicio(LocalDate.now().minusDays(20)).fechaFin(null).precioNoche(100.0).build();
        var promo = Tarifa.builder().id(10).tipoHabitacion(tipo).fechaInicio(req.fechaInicio()).fechaFin(req.fechaFin()).precioNoche(req.precioNoche()).build();
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(tipo));
        when(tarifaRepository.buscarTarifasVigentesEnFecha(any(), eq(req.fechaInicio()))).thenReturn(List.of(base));
        when(tarifaMapper.toEntity(req)).thenReturn(Tarifa.builder().build());
        when(tarifaRepository.save(any(Tarifa.class))).thenReturn(base, promo, Tarifa.builder().id(11).tipoHabitacion(tipo).build());
        when(tarifaMapper.toResponse(promo)).thenReturn(TestDataFactory.tarifaDTOResponse());
        when(sharedDTOMapper.toTarifaDTO(any())).thenReturn(TarifaDTO.builder().tipoHabitacionId(1).nuevoPrecio(80.0).build());

        var response = tarifaService.crearTarifa(req);
        assertThat(response.id()).isEqualTo(1);
    }

    @Test
    void crearTarifaNormalShouldFailWhenVigenteCannotBeClosed() {
        var req = TestDataFactory.tarifaDTORequestNormal();
        var tipo = TestDataFactory.tipoHabitacion();
        var vigente = Tarifa.builder().id(2).tipoHabitacion(tipo).fechaInicio(LocalDate.now()).fechaFin(null).precioNoche(100.0).build();
        when(tipoHabitacionRepository.findById(1)).thenReturn(Optional.of(tipo));
        when(tarifaRepository.buscarTarifasVigentesEnFecha(any(), any())).thenReturn(List.of(vigente));

        assertThatThrownBy(() -> tarifaService.crearTarifa(req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eliminarTarifaShouldDeleteNonVigente() {
        var tipo = TipoHabitacion.builder().id(1).build();
        var noVigente = Tarifa.builder().id(3).tipoHabitacion(tipo).fechaInicio(LocalDate.now().minusDays(20)).fechaFin(LocalDate.now().minusDays(10)).build();
        when(tarifaRepository.findById(3)).thenReturn(Optional.of(noVigente));
        when(tarifaRepository.countByTipoHabitacionId(1)).thenReturn(2L);

        tarifaService.eliminarTarifa(3);

        verify(tarifaRepository).delete(noVigente);
    }

    @Test
    void eliminarTarifaShouldDeleteWhenStartDateIsInFuture() {
        var tipo = TipoHabitacion.builder().id(1).build();
        var futura = Tarifa.builder().id(4).tipoHabitacion(tipo).fechaInicio(LocalDate.now().plusDays(3)).fechaFin(null).build();
        when(tarifaRepository.findById(4)).thenReturn(Optional.of(futura));
        when(tarifaRepository.countByTipoHabitacionId(1)).thenReturn(2L);

        tarifaService.eliminarTarifa(4);

        verify(tarifaRepository).delete(futura);
    }

    @Test
    void eliminarTarifaVigenteWithoutPreviousShouldDeleteOnly() {
        var tipo = TipoHabitacion.builder().id(1).build();
        var vigente = Tarifa.builder().id(1).tipoHabitacion(tipo).fechaInicio(LocalDate.now().minusDays(2)).fechaFin(null).build();
        when(tarifaRepository.findById(1)).thenReturn(Optional.of(vigente));
        when(tarifaRepository.countByTipoHabitacionId(1)).thenReturn(2L);
        when(tarifaRepository.buscarTarifasAnteriores(1, vigente.getFechaInicio())).thenReturn(List.of());

        tarifaService.eliminarTarifa(1);

        verify(tarifaRepository).delete(vigente);
    }
}

