package com.nutrifit.backend.auth.security;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio encargado de generar tokens de sesión aleatorios.
 */
@Service
public class TokenService {

    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}