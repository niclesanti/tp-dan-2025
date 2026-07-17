package edu.utn.frsf.isi.dan.reservas_svc.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import edu.utn.frsf.isi.dan.reservas_svc.service.HabitacionService;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;


@Component
@Log4j2
@RequiredArgsConstructor
public class GestionMessageListener {

    private final HabitacionService habitacionService;

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "habitacion.topic", durable = "true"),
            exchange = @Exchange(value = "dan.exchange", type = "topic"),
            key = "dan.habitacion.#"
        ),
        ackMode = "MANUAL"
    )
    public void receiveMessage(HabitacionEvent habitacionEvent, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag ){   
        try {
            log.info("[RabbitMQ] Evento de habitación recibido: {}", habitacionEvent);
            // Spring/Jackson deserializó automáticamente el JSON al objeto
            habitacionService.handleEvent(habitacionEvent);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error procesando evento de habitación: {}", e.getMessage(), e);
            // No hago nada: el mensaje NO se reentrega ni se requeuea
            try {
                channel.basicReject(deliveryTag, false);
            } catch (IOException e1) {
                log.error("Error rechazando mensaje: {}", e.getMessage());
            } 
        }
    }
}
