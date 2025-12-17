package br.com.ifrn.ImportReportService.messaging.producer;

import br.com.ifrn.ImportReportService.config.rabbitmq.RabbitMQConfig;
import br.com.ifrn.ImportReportService.messaging.dto.ImportMessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(ImportMessageDTO message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "planilha.class-service-producer", message);
    }

    public void sendError(ImportMessageDTO message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "planilha.error", message);
    }
}

