package com.nutrifit.backend.auth.model;

/**
 * Modelo de usuario: credenciales e información básica de autenticación.
 */
public class Usuario {

    private Long id;
    private String nombre;
    private String email;
    private String passwordHash;

    public Usuario() {
    }

    /**
     * Construye un usuario con todos sus campos.
     *
     * @param id            identificador único
     * @param nombre        nombre del usuario
     * @param email         correo electrónico (debe ser único)
     * @param passwordHash  hash BCrypt de la contraseña
     */
    public Usuario(Long id, String nombre, String email, String passwordHash) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}