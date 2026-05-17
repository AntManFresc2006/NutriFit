package com.nutrifit.backend.ia.service;

import com.nutrifit.backend.ia.dto.UsuarioIaConfigRequest;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigResponse;

import java.util.Optional;

public interface UsuarioIaConfigService {

    Optional<UsuarioIaConfigResponse> getConfig(Long usuarioId);

    UsuarioIaConfigResponse saveConfig(Long usuarioId, UsuarioIaConfigRequest request);

    void deleteConfig(Long usuarioId);
}
