package com.nutrifit.backend.ia.repository;

import com.nutrifit.backend.ia.model.UsuarioIaConfig;

import java.util.Optional;

/**
 * Interfaz de repositorio para gestionar configuración de IA por usuario.
 */
public interface UsuarioIaConfigRepository {

    /**
     * Obtiene la configuración de IA de un usuario.
     * @param usuarioId ID del usuario.
     * @return Configuración si existe.
     */
    Optional<UsuarioIaConfig> findByUsuarioId(Long usuarioId);

    /**
     * Guarda o actualiza la configuración de IA de un usuario.
     * @param usuarioId ID del usuario.
     * @param config Datos de configuración.
     */
    void save(Long usuarioId, UsuarioIaConfig config);

    /**
     * Elimina la configuración de IA de un usuario.
     * @param usuarioId ID del usuario.
     */
    void deleteByUsuarioId(Long usuarioId);
}
