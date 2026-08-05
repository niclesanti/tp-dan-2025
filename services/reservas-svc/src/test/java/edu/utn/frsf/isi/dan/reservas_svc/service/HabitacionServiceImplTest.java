package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.TestDataFactory;
import edu.utn.frsf.isi.dan.reservas_svc.dto.HabitacionDisponibleDTO;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import edu.utn.frsf.isi.dan.shared.TarifaDTO;
import edu.utn.frsf.isi.dan.shared.TipoEvento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitacionServiceImplTest {

    @Mock
    private HabitacionRepository habitacionRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @InjectMocks
    private HabitacionServiceImpl habitacionService;

    @Test
    void handleEventCrearShouldSave() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.CREAR)
                .habitacion(HabitacionDTO.builder()
                        .habitacionId(101)
                        .capacidad(2)
                        .precioNoche(100.0)
                        .hotel(HotelDTO.builder().id(1).latitud(-32.95).longitud(-60.66).build())
                        .build())
                .build();

        when(habitacionRepository.save(any(Habitacion.class))).thenAnswer(i -> i.getArgument(0));
        habitacionService.handleEvent(event);
        verify(habitacionRepository).save(any(Habitacion.class));
    }

    @Test
    void handleEventActualizarDatosShouldThrowWhenNotFound() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ACTUALIZAR_DATOS)
                .habitacion(HabitacionDTO.builder().habitacionId(999).capacidad(2).precioNoche(100.0).build())
                .build();
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(Habitacion.class))).thenReturn(null);
        assertThatThrownBy(() -> habitacionService.handleEvent(event)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handleEventActualizarDatosShouldUpdateWhenFound() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ACTUALIZAR_DATOS)
                .habitacion(HabitacionDTO.builder().habitacionId(101).capacidad(3).precioNoche(150.0).build())
                .build();
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(Habitacion.class))).thenReturn(TestDataFactory.habitacion());
        habitacionService.handleEvent(event);
        verify(mongoTemplate).findAndModify(any(), any(), any(), eq(Habitacion.class));
    }

    @Test
    void handleEventActualizarPrecioShouldUpdateMany() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ACTUALIZAR_PRECIO)
                .tarifa(TarifaDTO.builder().tipoHabitacionId(1).nuevoPrecio(200.0).build())
                .build();
        when(mongoTemplate.updateMulti(any(), any(), eq(Habitacion.class)))
                .thenReturn(null);
        habitacionService.handleEvent(event);
        verify(mongoTemplate).updateMulti(any(), any(), eq(Habitacion.class));
    }

    @Test
    void handleEventEliminarShouldRemove() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ELIMINAR)
                .habitacion(HabitacionDTO.builder().habitacionId(101).build())
                .build();
        habitacionService.handleEvent(event);
        verify(mongoTemplate).remove(any(), eq(Habitacion.class));
    }

    @Test
    void handleEventCerrarHotelShouldThrowIllegalArgument() {
        var event = HabitacionEvent.builder().tipoEvento(TipoEvento.CERRAR_HOTEL).build();
        assertThatThrownBy(() -> habitacionService.handleEvent(event)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handleEventCrearShouldAllowNullHotelInPayload() {
        var event = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.CREAR)
                .habitacion(HabitacionDTO.builder().habitacionId(202).capacidad(2).precioNoche(90.0).hotel(null).build())
                .build();
        when(habitacionRepository.save(any(Habitacion.class))).thenAnswer(i -> i.getArgument(0));
        habitacionService.handleEvent(event);
        verify(habitacionRepository).save(any(Habitacion.class));
    }

    @Test
    void buscarDisponiblesShouldMatchBothObjectIdAndIntegerHabitacionId() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).exists(queryCaptor.capture(), eq(Reserva.class));
        var idHabitacion = (org.bson.Document) queryCaptor.getValue().getQueryObject().get("idHabitacion");
        var valores = (List<String>) idHabitacion.get("$in");
        assertThat(valores).containsExactlyInAnyOrder("hab-1", "101");
    }

    @Test
    void buscarDisponiblesShouldReturnPage() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                1, 50.0, 200.0, 4, List.of("WIFI"), null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1).extracting(HabitacionDisponibleDTO::getId).contains("hab-1");
    }

    @Test
    void buscarDisponiblesShouldSupportGeoFilterAndConflict() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(true);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                null, null, null, null, null, -32.95, -60.66, 3.0, PageRequest.of(0, 10));
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void buscarDisponiblesShouldApplyAmenitiesCriteria() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                null, null, null, null, List.of("WIFI", "PILETA"), null, null, null, PageRequest.of(0, 10));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Habitacion.class));
        var amenitiesDoc = (org.bson.Document) findInCriteria(queryCaptor.getValue(), "amenities");
        assertThat(amenitiesDoc).isNotNull();
        assertThat(amenitiesDoc.get("$all")).isEqualTo(List.of("WIFI", "PILETA"));
    }

    @Test
    void buscarDisponiblesShouldApplyNearSphereGeoCriteria() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                null, null, null, null, null, -32.95, -60.66, 3.0, PageRequest.of(0, 10));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Habitacion.class));
        var geoDoc = (org.bson.Document) findInCriteria(queryCaptor.getValue(), "hotel.ubicacion");
        assertThat(geoDoc).isNotNull();
        var nearSphereDoc = (org.bson.Document) geoDoc.get("$nearSphere");
        assertThat(nearSphereDoc).isNotNull();
        assertThat(nearSphereDoc.get("$maxDistance")).isEqualTo(3.0 * 1000.0);
    }

    @Test
    void toDisponibleDTOShouldExposeAmenitiesAndCoordinates() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

        var dto = page.getContent().get(0);
        assertThat(dto.getAmenities()).containsExactly("WIFI");
        assertThat(dto.getHotel().getLatitud()).isEqualTo(-32.95);
        assertThat(dto.getHotel().getLongitud()).isEqualTo(-60.66);
    }

    @Test
    void buscarDisponiblesShouldReturnEmptyPageWhenOffsetBeyondResults() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                null, null, null, null, null, null, null, null, PageRequest.of(5, 10));
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void buscarDisponiblesShouldWorkWithoutAnyOptionalFilter() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                null, null, null, null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void buscarDisponiblesShouldHandleIndividualPriceFiltersAndEmptyAmenities() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var in = Instant.now().plusSeconds(86400);
        var out = Instant.now().plusSeconds(172800);
        assertThat(habitacionService.buscarDisponibles(in, out, null, 80.0, null, null, List.of(), null, null, null, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
        assertThat(habitacionService.buscarDisponibles(in, out, null, null, 120.0, null, List.of(), null, null, null, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }

    @Test
    void buscarDisponiblesShouldIgnoreCapacityWhenZero() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);

        var page = habitacionService.buscarDisponibles(
                Instant.now().plusSeconds(86400), Instant.now().plusSeconds(172800),
                0, null, null, null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void buscarDisponiblesShouldIgnoreIncompleteGeoFilter() {
        var hab = TestDataFactory.habitacion();
        when(mongoTemplate.find(any(), eq(Habitacion.class))).thenReturn(List.of(hab));
        when(mongoTemplate.exists(any(), eq(Reserva.class))).thenReturn(false);
        var in = Instant.now().plusSeconds(86400);
        var out = Instant.now().plusSeconds(172800);

        assertThat(habitacionService.buscarDisponibles(in, out, null, null, null, null, null, -32.95, null, 3.0, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
        assertThat(habitacionService.buscarDisponibles(in, out, null, null, null, null, null, -32.95, -60.66, null, PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }

    @Test
    void simpleCrudMethodsShouldDelegate() {
        var hab = TestDataFactory.habitacion();
        when(habitacionRepository.findAll()).thenReturn(List.of(hab));
        when(habitacionRepository.findById("hab-1")).thenReturn(Optional.of(hab));
        when(habitacionRepository.save(hab)).thenReturn(hab);

        assertThat(habitacionService.findAll()).hasSize(1);
        assertThat(habitacionService.findById("hab-1")).contains(hab);
        assertThat(habitacionService.save(hab)).isEqualTo(hab);
        habitacionService.deleteById("hab-1");
        verify(habitacionRepository).deleteById("hab-1");
    }

    private static Object findInCriteria(Query query, String field) {
        var queryObject = query.getQueryObject();
        if (queryObject.containsKey(field)) {
            return queryObject.get(field);
        }
        if (queryObject.get("$and") instanceof List<?> andClauses) {
            for (Object clause : andClauses) {
                if (clause instanceof org.bson.Document doc && doc.containsKey(field)) {
                    return doc.get(field);
                }
            }
        }
        return null;
    }

}

