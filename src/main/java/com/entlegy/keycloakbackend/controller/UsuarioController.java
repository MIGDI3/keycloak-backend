package com.entlegy.keycloakbackend.controller;

import java.util.List;

import javax.ws.rs.Produces;

import com.entlegy.keycloakbackend.dto.ResponseMessage;
import com.entlegy.keycloakbackend.model.Usuarios;
import com.entlegy.keycloakbackend.service.KeycloakService;

import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UsuarioController {

    @Autowired
    public KeycloakService keycloakService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessage> create(@RequestBody Usuarios usuario) {
        Object[] obj = keycloakService.createUser(usuario);
        int status = (int) obj[0];
        ResponseMessage message = (ResponseMessage) obj[1];
        return ResponseEntity.status(status).body(message);
    }

    @GetMapping("/listRealm")
    @Produces(value = "application/json")
    public RealmEventsConfigRepresentation getRealms() {
        RealmEventsConfigRepresentation rr = keycloakService.getListRealms();
        // String res = rr.toString();
        return rr;
    }

    @GetMapping("/rolesDisponibles")
    @Produces(value = "application/json")
    public List<RoleRepresentation> getRolesDisponibles() {
        List<RoleRepresentation> rolesDisponibles = keycloakService.getRolesDisponibles();
        return rolesDisponibles;
    }

    @GetMapping("/clientesDisponibles")
    @Produces(value = "application/json")
    public List<ClientRepresentation> getClientesDisponibles() {
        return keycloakService.getClientesDisponibles();
    }

    @GetMapping("/usuariosDisponibles")
    @Produces(value = "application/json")
    public List<UserRepresentation> getUsuariosDisponibles() {
        return keycloakService.getUserRealm();
    }

    @GetMapping("/clienteByUsuario/{userName}")
    @Produces(value = "application/json")
    public List<ClientRepresentation> getClientebyUsuario(@PathVariable("userName") String nameUser) {
        return keycloakService.getClientbyUser(nameUser);
    }
}
