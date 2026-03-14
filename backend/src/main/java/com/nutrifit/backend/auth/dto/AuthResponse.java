package com.nutrifit.backend.auth.dto;

/**
 * DTO de salida para respuestas de autenticación.
 */
public class AuthResponse {

    private Long usuarioId;
    private String nombre;
    private String email;
    private String token;

    public AuthResponse() {
    }

    public AuthResponse(Long usuarioId, String nombre, String email, String token) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.email = email;
        this.token = token;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}