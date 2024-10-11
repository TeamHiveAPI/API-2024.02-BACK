package com.api.portaldatransparencia.dto;

public class AuthenticationDTO {
    private String email;
    private String senha;

    public AuthenticationDTO(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}
