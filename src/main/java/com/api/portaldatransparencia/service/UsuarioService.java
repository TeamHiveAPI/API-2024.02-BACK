package com.api.portaldatransparencia.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.portaldatransparencia.model.Usuario;
import com.api.portaldatransparencia.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario salvarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> buscarUsuarios(String email, String senha) {
        return usuarioRepository.buscarUsuarios(email, senha);
    }

    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public void deletarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    // Validação de senha
    public boolean validarSenha(String senha) {
        String regex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*])(?=.{8,50})";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(senha).matches();
    }
}
