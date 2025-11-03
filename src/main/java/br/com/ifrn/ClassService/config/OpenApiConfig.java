package br.com.ifrn.ClassService.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {
    // URL base do Keycloak para o Realm IFRN
    private static final String KEYCLOAK_SERVER_URL = "http://host.docker.internal:8080/realms/ifrn/protocol/openid-connect";
    // Endpoint para obtenção de tokens
    private static final String TOKEN_URL = KEYCLOAK_SERVER_URL + "/token";

    // ID do Cliente (constante para a aplicação class-service)
    private static final String CLIENT_ID = "class-service";

    // Chave Secreta do Cliente (Deve ser copiada do Keycloak. Use uma chave real para autenticar!)
    // ATENÇÃO: Nunca fixe o Client Secret em código de produção.
    private static final String CLIENT_SECRET = "xfLpnS0lW9ze82ZgVdZA0jTHSw2sodoC";
    @Bean
    public OpenAPI customOpenAPI() {
        // Define as extensões para pré-preencher Client ID e Client Secret no Swagger UI
        Map<String, Object> clientExtensions = Map.of(
                "x-client-id", CLIENT_ID,
                "x-client-secret", CLIENT_SECRET
        );

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
                                .url("http://localhost:8081")
                                .description("Servidor Local de Desenvolvimento")
                ))
                .tags(List.of(
                        new Tag().name("Classes").description("Operações relacionadas a Turmas"),
                        new Tag().name("Courses").description("Operações relacionadas a Cursos"),
                        new Tag().name("Comments").description("Operações relacionadas a Comentários")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação do Projeto")
                        .url("https://github.com/Integrador-Coorporativos/class_service"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )

                        // ----------------------------------------------------
                        // 1. ESQUEMA DE AUTENTICAÇÃO: PASSWORD (USUÁRIO/SENHA)
                        // ----------------------------------------------------
                        .addSecuritySchemes("OAuth2Password",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("Autenticação via Keycloak (Fluxo: Usuário/Senha)")
                                        .flows(new OAuthFlows()
                                                .password(new OAuthFlow()
                                                        .tokenUrl(TOKEN_URL)
                                                        .scopes(new Scopes()
                                                                .addString("openid", "Acesso à identidade e perfis básicos")
                                                                .addString("profile", "Acesso ao perfil do usuário"))
                                                )
                                        )
                                        // ADICIONA AS EXTENSÕES PARA PRÉ-PREENCHER CLIENT ID E SECRET
                                        .extensions(clientExtensions)
                        )
                        // ----------------------------------------------------
                        // 2. ESQUEMA DE AUTENTICAÇÃO: CLIENT CREDENTIALS
                        // (Fluxo adicionado para testes de Serviço-a-Serviço)
                        // ----------------------------------------------------
                        .addSecuritySchemes("OAuth2ClientCredentials",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("Autenticação via Keycloak (Fluxo: Serviço-a-Serviço)")
                                        .flows(new OAuthFlows()
                                                .clientCredentials(new OAuthFlow()
                                                        .tokenUrl(TOKEN_URL)
                                                        .scopes(new Scopes()
                                                                .addString("openid", "Acesso à identidade e perfis básicos"))
                                                )
                                        )
                                        // ADICIONA AS EXTENSÕES PARA PRÉ-PREENCHER CLIENT ID E SECRET
                                        .extensions(clientExtensions)
                        )

                )
                .addSecurityItem(new SecurityRequirement().addList("OAuth2Password"))
                .addSecurityItem(new SecurityRequirement().addList("OAuth2ClientCredentials")) // Adiciona a nova dependência de segurança
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
