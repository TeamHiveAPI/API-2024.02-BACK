package com.api.portaldatransparencia.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.portaldatransparencia.model.Usuario;
import com.api.portaldatransparencia.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Usuario salvarUsuario(Usuario usuario) {
        if (!validarSenha(usuario.getSenha())) {
            throw new IllegalArgumentException("Senha inválida: deve conter pelo menos 8 caracteres, uma letra maiúscula, um número e um caractere especial.");
        }
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> buscarUsuarios(String email, String senha) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Usuario> cq = cb.createQuery(Usuario.class);
        Root<Usuario> usuario = cq.from(Usuario.class);

        List<Predicate> predicates = new ArrayList<>();

        if (email != null && !email.isEmpty()) {
            predicates.add(cb.like(cb.lower(usuario.get("email")), "%" + email.toLowerCase() + "%"));
        }

        if (senha != null && !senha.isEmpty()) {
            predicates.add(cb.equal(usuario.get("senha"), senha));
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        return entityManager.createQuery(cq).getResultList();
    }

    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Usuario> cq = cb.createQuery(Usuario.class);
        Root<Usuario> usuario = cq.from(Usuario.class);

        Predicate idPredicate = cb.equal(usuario.get("id"), id);
        cq.where(idPredicate);

        Usuario resultado = entityManager.createQuery(cq).getSingleResult();
        return Optional.ofNullable(resultado);
    }

    public void deletarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public boolean validarSenha(String senha) {
        String regex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])(?=.{8,50}).*$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(senha).matches();
    }
}
