package com.api.portaldatransparencia.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.api.portaldatransparencia.model.Usuario;
import com.api.portaldatransparencia.model.UsuarioRole;
import com.api.portaldatransparencia.service.UsuarioService;

@Configuration
public class DataInitializer {

    @Autowired
    private UsuarioService usuarioService;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            if (usuarioService.listarUsuarios().isEmpty()) {
                Usuario usuario = new Usuario();
                usuario.setEmail("admin@example.com");
                usuario.setSenha("Admin@123");
                usuario.setRole(UsuarioRole.ROLE_ADMIN); // Ajustado para usar enum
                try {
                    usuarioService.salvarUsuario(usuario);
                    System.out.println("Usuário admin inicializado.");
                } catch (IllegalArgumentException e) {
                    System.err.println("Erro ao inicializar usuário admin: " + e.getMessage());
                }
            } else {
                System.out.println("Usuários já cadastrados no sistema.");
            }
        };
    }
}
