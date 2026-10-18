package pe.edu.cibertec.gasmapocho.msmovimientos.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MovimientoPublicador {

    private final RabbitTemplate rabbitTemplate;

    public MovimientoPublicador(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(MovimientoEvento evento) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY, evento);
    }
}
