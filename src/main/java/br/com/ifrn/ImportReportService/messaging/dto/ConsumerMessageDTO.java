package br.com.ifrn.ImportReportService.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String linha;       // conteúdo da linha da planilha
    private String status;      // SUCCESS ou ERROR
    private String detalheErro; // em caso de erro
}
