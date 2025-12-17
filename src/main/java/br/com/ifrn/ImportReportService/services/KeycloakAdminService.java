package br.com.ifrn.ImportReportService.services;

import br.com.ifrn.ImportReportService.config.keycloak.KeycloakAdminConfig;
import br.com.ifrn.ImportReportService.dto.ImporterDTO;
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

    public String findOrCreateUser(ImporterDTO importerDTO) {
        String userId = "";
        UserRepresentation user = findKeycloakUser(importerDTO.getRegistration());
        if (user == null){
            Response response = createKeycloakUser(importerDTO.getRegistration(), importerDTO.getName());

            if (response.getStatus() == 201) {//fazer lógica de atualização de dados do usuário

                userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                System.out.println("Usuário criado com ID: " + userId);
            } else {
                System.out.println("Erro ao criar usuário: " + response.getStatus() + " " + response.getStatusInfo());
                System.out.println(response.readEntity(String.class));
            }
        }else{
            userId = user.getId();
        }
        return userId;
    }
}
