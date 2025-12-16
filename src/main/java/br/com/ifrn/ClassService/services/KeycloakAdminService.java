package br.com.ifrn.ClassService.services;

import br.com.ifrn.ClassService.config.keycloak.KeycloakAdminConfig;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeycloakAdminService {

    @Autowired
    KeycloakAdminConfig keycloakAdminConfig;

    public Response createKeycloakUser(String username, String name) {
        Keycloak keycloak = keycloakAdminConfig.createKeycloakAdminClient();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(true);
        user.setFirstName(name);

        Response response = keycloak.realm(keycloakAdminConfig.getEnvKeycloak().realm()).users().create(user);
        return response;

    }

    public UserRepresentation findKeycloakUser(String username) {
        Keycloak keycloak = keycloakAdminConfig.createKeycloakAdminClient(); // cliente admin

        List<UserRepresentation> users = keycloak.realm(keycloakAdminConfig.getEnvKeycloak().realm())
                .users()
                .search(username);

        if (users.isEmpty()) {
            return null;
        }

        UserRepresentation user = users.get(0);
        return user;

    }
}
