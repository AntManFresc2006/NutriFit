package com.nutrifit.backend.auth.repository;

import com.nutrifit.backend.auth.model.Sesion;

/**
 * Contrato de acceso a datos para sesiones.
 */
public interface SesionRepository {

    Sesion save(Sesion sesion);

    void deleteByToken(String token);
}