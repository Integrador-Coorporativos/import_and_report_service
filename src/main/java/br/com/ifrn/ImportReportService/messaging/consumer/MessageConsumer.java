package br.com.ifrn.ImportReportService.messaging.consumer;

import br.com.ifrn.ImportReportService.config.rabbitmq.RabbitMQConfig;
import br.com.ifrn.ImportReportService.messaging.dto.ConsumerMessageDTO;
import br.com.ifrn.ImportReportService.messaging.dto.ImportMessageDTO;
import br.com.ifrn.ImportReportService.services.MessagingReceiveService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @Autowired
    private MessagingReceiveService messagingReceiveService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESPONSE)
    public void receiveMessage(ImportMessageDTO message) {
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

