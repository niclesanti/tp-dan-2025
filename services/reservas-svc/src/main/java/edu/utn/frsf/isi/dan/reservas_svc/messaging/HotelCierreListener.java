package edu.utn.frsf.isi.dan.reservas_svc.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.repository.ReservaRepository;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import edu.utn.frsf.isi.dan.shared.HotelCierreEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotelCierreListener {

    private final ObjectMapper objectMapper;
    private final ReservaRepository reservaRepository;

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "hotel.cierre.topic", durable = "true"),
            exchange = @Exchange(value = "dan.exchange", type = "topic"),
            key = "dan.hotel.cerrar"
        ),
        ackMode = "MANUAL"
    )
    public void receiveHotelCierreEvent(HotelCierreEvent cierreEvent, Channel channel, 
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("[RabbitMQ] Evento de cierre de hotel recibido para hotel ID: {}", cierreEvent.getHotel().getId());
            // Spring/Jackson deserializó automáticamente el JSON al objeto
            
            procesarCierreHotel(cierreEvent);
            
            channel.basicAck(deliveryTag, false);
            log.info("Evento de cierre procesado exitosamente para hotel ID: {}", 
                    cierreEvent.getHotel().getId());
        } catch (Exception e) {
            log.error("Error procesando evento de cierre de hotel: {}", e.getMessage(), e);
            try {
                channel.basicReject(deliveryTag, false);
            } catch (IOException ioEx) {
                log.error("Error rechazando mensaje: {}", ioEx.getMessage());
            }
        }
    }

    private void procesarCierreHotel(HotelCierreEvent cierreEvent) {
        log.info("Creando reservas BLOQUEADAS para {} habitaciones del hotel ID: {}", 
                cierreEvent.getHabitaciones().size(), cierreEvent.getHotel().getId());
        
        List<Reserva> reservasBloqueadas = new ArrayList<>();
        Instant ahora = Instant.now();
        
        for (HabitacionDTO habitacion : cierreEvent.getHabitaciones()) {
            Reserva reserva = Reserva.builder()
                    .idHabitacion(habitacion.getHabitacionId().toString())
                    .hotelId(cierreEvent.getHotel().getId())
                    .createdAt(ahora)
                    .checkIn(ahora)
                    .checkOut(null)  // null indica que el cierre es indefinido
                    .precioNoche(0.0)
                    .precioTotal(0.0)
                    .huesped(null)   // No hay huésped para reservas de cierre
                    .pagos(new ArrayList<>())
                    .clientReview(null)
                    .hostReview(null)
                    .estadoReserva(EstadoReserva.BLOQUEADA)
                    .build();
            
            reservasBloqueadas.add(reserva);
        }
        
        reservaRepository.saveAll(reservasBloqueadas);
        log.info("Se crearon {} reservas BLOQUEADAS para el hotel ID: {}", 
                reservasBloqueadas.size(), cierreEvent.getHotel().getId());
    }
}
