package com.api.portaldatransparencia.service;

import com.api.portaldatransparencia.infra.security.TokenService;
import com.api.portaldatransparencia.model.Usuario;
import com.api.portaldatransparencia.model.UsuarioRole;
import com.api.portaldatransparencia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String authenticateUser(String login, String senha) throws IllegalArgumentException {
        // Verifique se o usuário existe
        Optional<Usuario> usuario = usuarioRepository.findByEmail(login);
        if (usuario.isPresent()) {
            // Verifica se a senha informada corresponde à senha criptografada
            if (passwordEncoder.matches(senha, usuario.get().getSenha())) {
                // Gera o token se a autenticação for bem-sucedida
                UsuarioRole role = usuario.get().getRole();
                return tokenService.generateToken(login, role);
            }
        }

        throw new IllegalArgumentException("Usuário ou senha inválidos");
    }
}
