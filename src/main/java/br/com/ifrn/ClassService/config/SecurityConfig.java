package br.com.ifrn.ClassService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // Endereço de rede CORRETO (para comunicação interna e JWKS URI)
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String correctIssuerUri;

    // Lista de endpoints públicos do Swagger/SpringDoc
    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/api/swagger-ui/**",
            "/swagger-ui.html",
            "/api/docs/**" // Seu caminho personalizado
    };

    /**
     * Define o conversor que mapeia as 'claims' do Keycloak (JWT) para as Autoridades (Roles) do Spring Security.
     * @return JwtAuthenticationConverter configurado.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // 1. O nome da 'claim' onde o Keycloak coloca as roles (geralmente realm_access.roles)
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");

        // 2. Adiciona o prefixo ROLE_ exigido pelo Spring Security
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        // 3. Usa o método correto para definir o conversor de autoridade
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    /**
     * Define o JwtDecoder customizado para aceitar múltiplos Issuers (para contornar o problema de hostname do Docker/Keycloak).
     * @return JwtDecoder configurado.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // 1. Cria o decoder padrão usando a URI CORRETA (para buscar as chaves)
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(correctIssuerUri);

        // 2. Lista de validadores
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();

        // Adiciona o validador de tempo padrão (iat, exp)
        validators.add(new JwtTimestampValidator());

        // 3. Define a lista dos Issuers válidos:
        // A) URI Correta (host.docker.internal) - Para buscar as chaves
        // B) URI Incorreta/Teimosa (localhost) - Para validar o 'iss' claim

        final List<String> validIssuers = List.of(
                correctIssuerUri,
                correctIssuerUri.replace("host.docker.internal", "localhost")
        );

        // 4. CRIA UM VALIDADOR CUSTOMIZADO PARA MÚLTIPLOS ISSUERS (Corrige o erro de compilação)
        OAuth2TokenValidator<Jwt> multiIssuerValidator = new OAuth2TokenValidator<Jwt>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt token) {
                // Se o token não tem claim de Issuer, falha
                if (token.getIssuer() == null) {
                    return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_issuer", "O 'iss' claim esta ausente no token.", null));
                }

                String issuer = token.getIssuer().toString();

                // Checa se o issuer do token está na lista de emissores válidos
                if (validIssuers.contains(issuer)) {
                    return OAuth2TokenValidatorResult.success();
                }

                // Se não estiver, retorna falha detalhada
                String validIssuersStr = validIssuers.stream().collect(Collectors.joining(", "));
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_issuer", "O 'iss' claim ('" + issuer + "') não é um dos emissores validos: " + validIssuersStr, null)
                );
            }
        };

        // 5. Adiciona o novo validador customizado à lista
        validators.add(multiIssuerValidator);

        // 6. Cria o validador delegado
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(validators);

        // 7. Aplica o validador customizado ao decoder
        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // PERMITE ACESSO PÚBLICO AO SWAGGER e APIs públicas
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        // Qualquer outra requisição deve ser autenticada
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        // O Spring Security irá automaticamente usar o JwtDecoder @Bean que criamos
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
