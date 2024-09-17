package com.api.portaldatransparencia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.portaldatransparencia.model.Usuario;
import com.api.portaldatransparencia.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/cadastro")
    public ResponseEntity<String> cadastrarUsuario(@RequestBody Usuario usuario) {
        List<Usuario> usuariosExistentes = usuarioService.buscarUsuarios(usuario.getEmail(), null);
        if (!usuariosExistentes.isEmpty()) {
            return ResponseEntity.badRequest().body("Email já está em uso");
        }

        if (!usuarioService.validarSenha(usuario.getSenha())) {
            return ResponseEntity.badRequest().body("A senha deve conter entre 8 e 50 caracteres, uma letra maiúscula, um número e um caractere especial");
        }

        Usuario novoUsuario = usuarioService.salvarUsuario(usuario);
        return ResponseEntity.ok("Usuário cadastrado com sucesso");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUsuario(@RequestBody Usuario usuario) {
        List<Usuario> usuarios = usuarioService.buscarUsuarios(usuario.getEmail(), usuario.getSenha());
        if (!usuarios.isEmpty()) {
            return ResponseEntity.ok("Login realizado com sucesso!");
        }
        return ResponseEntity.status(401).body("Credenciais inválidas!");
    }
}
