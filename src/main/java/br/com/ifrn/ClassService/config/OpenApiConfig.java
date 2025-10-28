package br.com.ifrn.ClassService.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
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
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Acompanhamento de Desempenho e Comportamento")
                        .version("1.0.0")
                        .description("""
                                API para monitoramento acadêmico e comportamental de turmas e alunos do IFRN.
                                Permite gerenciamento de turmas, cursos, alunos, avaliações, desempenho e comentários.
                                """)
                        .contact(new Contact()
                                .name("Eduardo Lima")
                                .email("ferreira.lima1@escolar.ifrn.edu.br")
                                .url("https://github.com/eduardoferreiralima"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor Local de Desenvolvimento")
                ))
                .tags(List.of(
                        new Tag().name("Classes").description("Operações relacionadas a Turmas"),
                        new Tag().name("Courses").description("Operações relacionadas a Cursos"),
                        new Tag().name("Comments").description("Operações relacionadas a Comentários")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação do Projeto")
                        .url("https://github.com/eduardoferreiralima/atividadeJPA"))
                .components(new Components());
    }
}
