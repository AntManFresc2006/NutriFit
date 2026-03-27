package com.nutrifit.backend.auth.repository;

import com.nutrifit.backend.auth.model.Sesion;

import java.util.Optional;

/**
 * Contrato de acceso a datos para sesiones.
 */
public interface SesionRepository {

    Sesion save(Sesion sesion);

    Optional<Sesion> findByToken(String token);

    void deleteByToken(String token);
}