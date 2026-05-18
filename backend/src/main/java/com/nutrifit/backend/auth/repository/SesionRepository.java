package com.nutrifit.backend.auth.repository;

import com.nutrifit.backend.auth.model.Sesion;

import java.util.Optional;

/**
 * Repositorio de sesiones: persistencia y recuperación de tokens de autenticación.
 */
public interface SesionRepository {

    /**
     * Guarda una nueva sesión en la base de datos.
     *
     * @param sesion sesión a insertar (sin ID previamente asignado)
     * @return sesión guardada con ID generado
     */
    Sesion save(Sesion sesion);

    /**
     * Busca una sesión por su token.
     *
     * @param token token de autenticación
     * @return sesión si existe, vacío en otro caso
     */
    Optional<Sesion> findByToken(String token);

    /**
     * Elimina una sesión por su token (invalida la autenticación).
     *
     * @param token token de autenticación
     */
    void deleteByToken(String token);
}