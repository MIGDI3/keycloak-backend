package com.entlegy.keycloakbackend.service;

import java.util.Arrays;

import javax.ws.rs.core.Response;

import com.entlegy.keycloakbackend.dto.ResponseMessage;
import com.entlegy.keycloakbackend.model.Usuarios;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String serverURL;
    @Value("${keycloak.realm}")
    private String realm;

    public Object[] createUser(Usuarios usuarios) {
        ResponseMessage message = new ResponseMessage();
        int statusId = 0;
        try {
            UsersResource usersResource = getUsersResource();
            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setUsername(usuarios.getUserName());
            userRepresentation.setLastName(usuarios.getLastName());
            userRepresentation.setFirstName(usuarios.getFirstName());
            userRepresentation.setEmail(usuarios.getEmail());
            userRepresentation.setEnabled(true);

            Response result = usersResource.create(userRepresentation);
            statusId = result.getStatus();
            if (statusId == 201) {
                String path = result.getLocation().getPath();
                String userId = path.substring(path.lastIndexOf("/") + 1);
                CredentialRepresentation passwordCredential = new CredentialRepresentation();
                passwordCredential.setTemporary(false);
                passwordCredential.setType(CredentialRepresentation.PASSWORD);
                passwordCredential.setValue(usuarios.getPassword());
                usersResource.get(userId).resetPassword(passwordCredential);

                RealmResource realmResource = getRealmResource();
                RoleRepresentation roleRepresentation = realmResource.roles().get("realm-user").toRepresentation();
                realmResource.users().get(userId).roles().realmLevel().add(Arrays.asList(roleRepresentation));
                message.setMessage("Usuario Creado con éxito");
            } else if (statusId == 409) {
                message.setMessage("Ese usuario ya existe");
            } else {
                message.setMessage("Error al crear usuario");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Object[] { statusId, message };
    }

    private RealmResource getRealmResource() {
        Keycloak kc = KeycloakBuilder.builder().serverUrl(serverURL).realm("master").username("admin")
                .password("admin").clientId("admin-cli")
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
        return kc.realm(realm);
    }

    private UsersResource getUsersResource() {
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }
}
