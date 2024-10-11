package com.api.portaldatransparencia.dto;

import com.api.portaldatransparencia.model.UsuarioRole;

public class CadastroDTO {
    private String email;
    private String senha;
    private UsuarioRole role;

    // Construtor
    public CadastroDTO(String email, String senha, UsuarioRole role) {
        this.email = email;
        this.senha = senha;
        this.role = role;
    }

    // Métodos getters
    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public UsuarioRole getRole() {
        return role;
    }

    // Métodos setters (opcional)
    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setRole(UsuarioRole role) {
        this.role = role;
    }
}
