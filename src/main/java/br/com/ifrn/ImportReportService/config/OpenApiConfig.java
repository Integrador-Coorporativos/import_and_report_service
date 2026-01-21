package br.com.ifrn.ImportReportService.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("SADT - Import And Report Service")
                        .version("1.0.0")
                        .description("""
                                API para processamento de planilhas e geração de relatóris acadêmicos do IFRN.
                                """)
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Servidor Local de Desenvolvimento"),
                        new Server()
                                .url("https://if-performance-import-and-report-service.zgx7iz.easypanel.host")
                                .description("Servidor de Produção")
                ))
                .tags(List.of(
                        new Tag().name("Processamento de Planilhas")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação do Projeto")
                        .url("https://github.com/Integrador-Coorporativos/import_and_report_service"));
        return openAPI;
    }
}
