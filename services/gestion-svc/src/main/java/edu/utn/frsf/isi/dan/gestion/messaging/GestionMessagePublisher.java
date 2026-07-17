package edu.utn.frsf.isi.dan.gestion.messaging;

import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.HotelCierreEvent;
import edu.utn.frsf.isi.dan.shared.TipoEvento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GestionMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    
    private static final String EXCHANGE = "dan.exchange";
    private static final String ROUTING_KEY_CREAR = "dan.habitacion.crear";
    private static final String ROUTING_KEY_ACTUALIZAR = "dan.habitacion.actualizar";
    private static final String ROUTING_KEY_PRECIO = "dan.habitacion.precio";
    private static final String ROUTING_KEY_ELIMINAR = "dan.habitacion.eliminar";
    private static final String ROUTING_KEY_CERRAR_HOTEL = "dan.hotel.cerrar";

    public void publishHabitacionEvent(HabitacionEvent event) {
        String routingKey = getRoutingKey(event.getTipoEvento());
        log.info("Publicando evento {} con routing key: {}", event.getTipoEvento(), routingKey);
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
        log.debug("Evento publicado exitosamente: {}", event);
    }

    public void publishHotelCierreEvent(HotelCierreEvent event) {
        log.info("Publicando evento de cierre de hotel ID: {}", event.getHotel().getId());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CERRAR_HOTEL, event);
        log.debug("Evento de cierre de hotel publicado exitosamente: {}", event);
    }

    private String getRoutingKey(TipoEvento tipoEvento) {
        return switch (tipoEvento) {
            case CREAR -> ROUTING_KEY_CREAR;
            case ACTUALIZAR_DATOS -> ROUTING_KEY_ACTUALIZAR;
            case ACTUALIZAR_PRECIO -> ROUTING_KEY_PRECIO;
            case ELIMINAR -> ROUTING_KEY_ELIMINAR;
            case CERRAR_HOTEL -> ROUTING_KEY_CERRAR_HOTEL;
        };
    }
}
