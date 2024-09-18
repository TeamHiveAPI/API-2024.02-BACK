package com.api.portaldatransparencia.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    // Cadastrar novo usuário
    @PostMapping("/cadastro")
    public ResponseEntity<Usuario> cadastrarUsuario(@RequestBody Usuario usuario) {
        // Verifica se já existe um usuário com o mesmo email
        List<Usuario> usuariosExistentes = usuarioService.buscarUsuarios(usuario.getEmail(), null);
        if (!usuariosExistentes.isEmpty()) {
            return ResponseEntity.badRequest().body(null); // Retorna erro se o email já estiver em uso
        }
        Usuario novoUsuario = usuarioService.salvarUsuario(usuario);
        return ResponseEntity.ok(novoUsuario); // Retorna o usuário criado com sucesso
    }

    // Login de usuário
    @PostMapping("/login")
    public ResponseEntity<String> loginUsuario(@RequestBody Usuario usuario) {
        List<Usuario> usuarios = usuarioService.buscarUsuarios(usuario.getEmail(), usuario.getSenha());
        if (!usuarios.isEmpty()) {
            return ResponseEntity.ok("Login realizado com sucesso!"); // Login bem-sucedido
        }
        return ResponseEntity.status(401).body("Credenciais inválidas!"); // Retorna erro se as credenciais não forem válidas
    }

    // Listar todos os usuários
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.buscarUsuarioPorId(id);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualizar usuário por ID
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioAtualizado) {
        Optional<Usuario> usuarioExistente = usuarioService.buscarUsuarioPorId(id);
        if (usuarioExistente.isPresent()) {
            usuarioAtualizado.setId(id);
            Usuario usuarioSalvo = usuarioService.salvarUsuario(usuarioAtualizado);
            return ResponseEntity.ok(usuarioSalvo); // Retorna o usuário atualizado
        }
        return ResponseEntity.notFound().build(); // Retorna erro se o usuário não for encontrado
    }

    // Deletar usuário por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        if (usuarioService.buscarUsuarioPorId(id).isPresent()) {
            usuarioService.deletarUsuario(id);
            return ResponseEntity.noContent().build(); // Retorna 204 No Content
        }
        return ResponseEntity.notFound().build(); // Retorna 404 se o usuário não for encontrado
    }
}
