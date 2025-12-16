package br.com.ifrn.ClassService.messaging.consumer;

import br.com.ifrn.ClassService.config.rabbitmq.RabbitMQConfig;
import br.com.ifrn.ClassService.messaging.dto.ConsumerMessageDTO;
import br.com.ifrn.ClassService.messaging.dto.CreateClassMessageDTO;
import br.com.ifrn.ClassService.services.MessagingReceiveService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @Autowired
    private MessagingReceiveService messagingReceiveService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESPONSE)
    public void receiveMessage(CreateClassMessageDTO message) {
        try {
            if (message != null) {
                //Classes classe = messagingReceiveService.procMessage(message);
                //System.out.println(classe);
            }
            System.out.println("Linha processada com sucesso: \n");

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem: " + e.getMessage());
            e.printStackTrace();
            // opcional: enviar para fila de erro
        }
    }



    @RabbitListener(queues = RabbitMQConfig.QUEUE_ERROR)
    public void receiveError(ConsumerMessageDTO message) {
        // Aqui você pode gerar planilha de retorno ou logar
    }
}

