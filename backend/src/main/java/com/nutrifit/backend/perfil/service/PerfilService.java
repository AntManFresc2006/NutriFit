package com.nutrifit.backend.perfil.service;

import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.dto.PerfilUpdateRequest;

/**
 * Contrato de la lógica de negocio del perfil de usuario.
 */
public interface PerfilService {

    PerfilResponse getPerfil(Long id);

    PerfilResponse updatePerfil(Long id, PerfilUpdateRequest request);
}
