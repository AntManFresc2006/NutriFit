package com.nutrifit.backend.ia.service;

import com.nutrifit.backend.ia.dto.UsuarioIaConfigRequest;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigResponse;

import java.util.Optional;

/**
 * Servicio para gestionar la configuración personalizada de IA del usuario.
 */
public interface UsuarioIaConfigService {

    /**
     * Obtiene la configuración de IA de un usuario.
     * @param usuarioId ID del usuario.
     * @return Configuración si existe.
     */
    Optional<UsuarioIaConfigResponse> getConfig(Long usuarioId);

    /**
     * Guarda o actualiza la configuración de IA de un usuario.
     * @param usuarioId ID del usuario.
     * @param request Datos de configuración.
     * @return Configuración guardada.
     */
    UsuarioIaConfigResponse saveConfig(Long usuarioId, UsuarioIaConfigRequest request);

    /**
     * Elimina la configuración de IA de un usuario.
     * @param usuarioId ID del usuario.
     */
    void deleteConfig(Long usuarioId);
}
