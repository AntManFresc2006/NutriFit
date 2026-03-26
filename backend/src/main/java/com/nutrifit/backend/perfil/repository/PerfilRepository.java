package com.nutrifit.backend.perfil.repository;

import com.nutrifit.backend.perfil.model.Perfil;

import java.util.Optional;

/**
 * Contrato de acceso a datos para el perfil del usuario.
 */
public interface PerfilRepository {

    Optional<Perfil> findById(Long id);

    Perfil update(Long id, Perfil perfil);
}
