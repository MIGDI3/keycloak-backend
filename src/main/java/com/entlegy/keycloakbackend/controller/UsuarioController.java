package com.entlegy.keycloakbackend.controller;

import com.entlegy.keycloakbackend.dto.ResponseMessage;
import com.entlegy.keycloakbackend.model.Usuarios;
import com.entlegy.keycloakbackend.service.KeycloakService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

}
