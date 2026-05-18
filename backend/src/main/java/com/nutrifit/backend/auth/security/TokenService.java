package com.nutrifit.backend.auth.security;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio de generación de tokens de sesión. Utiliza UUID v4 como token (128 bits aleatorios).
 */
@Service
public class TokenService {

    /**
     * Genera un token de sesión único usando UUID aleatorio.
     *
     * @return token UUID v4 sin guiones
     */
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}