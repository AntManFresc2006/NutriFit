package com.nutrifit.backend.auth.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para sesiones de usuario.
 */
public class Sesion {

    private Long id;
    private Long usuarioId;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public Sesion() {
    }

    public Sesion(Long id, Long usuarioId, String token, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}