package com.nutrifit.backend.auth.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de hashing de contraseñas usando BCrypt.
 * BCrypt añade salt automáticamente y es lento a propósito (protege contra ataques de fuerza bruta).
 */
@Service
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Genera el hash BCrypt de una contraseña en texto plano. Cada hash es único debido al salt.
     *
     * @param rawPassword contraseña sin encriptar
     * @return hash BCrypt (seguro para almacenar en BD)
     */
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con su hash BCrypt almacenado.
     *
     * @param rawPassword   contraseña a verificar
     * @param passwordHash  hash almacenado en BD
     * @return {@code true} si la contraseña es correcta
     */
    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }
}