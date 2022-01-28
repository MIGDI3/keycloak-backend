package com.entlegy.keycloakbackend.service;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import com.entlegy.keycloakbackend.dto.ResponseMessage;
import com.entlegy.keycloakbackend.model.Usuarios;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
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

    private RealmResource getRealmResource(String realmSeek, String accesToken) {
        Keycloak kc = KeycloakBuilder.builder().serverUrl(serverURL).realm(realmSeek).username("admin")
                .password("admin").clientId("admin-cli").authorization(accesToken)
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
        return kc.realm(realm);
    }

    private UsersResource getUsersResource() {
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }

    public RealmEventsConfigRepresentation getListRealms() {
        // List<RealmResource> resultado = null;
        RealmResource rr = getRealmResource("tutorial", getTokenAccess());
        RealmEventsConfigRepresentation realmEventsConfig = rr.getRealmEventsConfig();
        return realmEventsConfig;
    }

    private String getTokenAccess() {
        Keycloak kc = KeycloakBuilder.builder().serverUrl(serverURL).realm("master").username("admin")
                .password("admin").clientId("admin-cli").grantType("password")
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
        TokenManager tokenManager = kc.tokenManager();
        return tokenManager.getAccessTokenString();
    }

    private String getUserId(String usuario) {
        String p = getTokenAccess();
        RealmResource rr = getRealmResource("tutorial", p);
        List<UserRepresentation> lstUsers = rr.users().list();
        boolean sw = false;
        int i = 0;
        int indice = -1;
        while (i < lstUsers.size() && !sw) {
            if (lstUsers.get(i).getUsername().equals(usuario)) {
                indice = i;
                sw = true;
            }
            i++;
        }
        if (indice == -1) {
            return "";
        } else {
            return lstUsers.get(indice).getId();
        }
    }

    public List<UserRepresentation> getUserRealm() {
        String p = getTokenAccess();
        RealmResource rr = getRealmResource("tutorial", p);
        return rr.users().list();
    }

    public List<RoleRepresentation> getRolesDisponibles() {
        String p = getTokenAccess();
        RealmResource rr = getRealmResource("tutorial", p);
        return rr.roles().list();
    }

    public List<ClientRepresentation> getClientesDisponibles() {
        String p = getTokenAccess();
        RealmResource rr = getRealmResource("tutorial", p);
        return rr.clients().findAll();
    }

    public List<ClientRepresentation> getClientbyUser(String usuario) {
        String p = getTokenAccess();

        String userId = getUserId(usuario);
        String url = serverURL + "/tutorial/groups/" + userId
                + "/role-mappings/clients/tutorial-frontend/available";
        Keycloak kc = KeycloakBuilder.builder().serverUrl(url).username("admin")
                .password("admin")
                .clientId("admin-cli").authorization(p)
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();
        RealmResource rr = kc.realm("tutorial");

        if (!userId.isEmpty()) {
            return rr.clients().findAll();
        } else {
            return null;
        }
    }

}
