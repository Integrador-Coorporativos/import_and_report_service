package br.com.ifrn.ClassService.messaging.producer;

import br.com.ifrn.ClassService.config.rabbitmq.RabbitMQConfig;
import br.com.ifrn.ClassService.messaging.dto.CreateClassMessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(CreateClassMessageDTO message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "planilha.class-service-producer", message);
    }

    public void sendError(CreateClassMessageDTO message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "planilha.error", message);
    }
}

