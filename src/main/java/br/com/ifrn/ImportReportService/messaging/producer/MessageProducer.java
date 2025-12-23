package br.com.ifrn.ImportReportService.messaging.producer;

import br.com.ifrn.ImportReportService.config.rabbitmq.RabbitMQConfig;
import br.com.ifrn.ImportReportService.config.security.SecurityContextService;
import br.com.ifrn.ImportReportService.messaging.dto.ImportMessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {

    @Autowired
    SecurityContextService  securityContextService;


    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(ImportMessageDTO payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                "planilha.class-service-producer",
                payload, // 1. Primeiro passamos o dado (DTO)
                message -> { // 2. Depois editamos o "envelope" (Headers)
                    String realUserId = securityContextService.getCurrentUserId();
                    message.getMessageProperties().setHeader("X-ORIGIN-USER-ID", realUserId);
                    return message;
                }
        );
    }

    public void sendError(ImportMessageDTO message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "planilha.error", message);
    }
}

