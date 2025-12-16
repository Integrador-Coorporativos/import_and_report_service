package br.com.ifrn.ClassService.config.keycloak;

import lombok.Data;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
public class KeycloakAdminConfig {

    @Autowired
    KeycloakPropertiesConfig envKeycloak;

    public Keycloak createKeycloakAdminClient() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(envKeycloak.serverUrl())
                .realm("master") // realm onde o admin user existe
                .clientId("admin-cli") // client para admin
                .clientSecret(envKeycloak.clientSecret()) // se necessário
                .username(envKeycloak.adminUser()) // usuário admin
                .password(envKeycloak.adminPassword())
                .grantType(OAuth2Constants.PASSWORD)
                .build();
        return keycloak;
    }

}
