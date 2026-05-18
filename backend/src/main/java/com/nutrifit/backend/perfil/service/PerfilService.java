package com.nutrifit.backend.perfil.service;

import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.dto.PerfilUpdateRequest;

/**
 * Contrato de la lógica de negocio del perfil de usuario.
 */
public interface PerfilService {

    /**
     * Obtiene el perfil del usuario con TMB y TDEE calculados.
     *
     * @param id identificador del usuario
     * @return perfil completo con valores biométricos y calóricos
     */
    PerfilResponse getPerfil(Long id);

    /**
     * Actualiza los datos biométricos y el objetivo del usuario.
     * Recalcula TMB y TDEE con los nuevos valores.
     *
     * @param id      identificador del usuario
     * @param request datos biométricos y de objetivo a actualizar
     * @return perfil actualizado con TMB y TDEE recalculados
     */
    PerfilResponse updatePerfil(Long id, PerfilUpdateRequest request);
}
