package com.nutrifit.backend.ia.service;

import com.nutrifit.backend.ia.dto.UsuarioIaConfigRequest;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigResponse;
import com.nutrifit.backend.ia.model.UsuarioIaConfig;
import com.nutrifit.backend.ia.repository.UsuarioIaConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementación del servicio de configuración de IA.
 * Proporciona conversión entre modelos y DTOs.
 */
@Service
public class UsuarioIaConfigServiceImpl implements UsuarioIaConfigService {

    private final UsuarioIaConfigRepository repository;

    public UsuarioIaConfigServiceImpl(UsuarioIaConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioIaConfigResponse> getConfig(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public UsuarioIaConfigResponse saveConfig(Long usuarioId, UsuarioIaConfigRequest request) {
        UsuarioIaConfig config = toModel(usuarioId, request);
        repository.save(usuarioId, config);
        return toResponse(config);
    }

    @Override
    @Transactional
    public void deleteConfig(Long usuarioId) {
        repository.deleteByUsuarioId(usuarioId);
    }

    private UsuarioIaConfig toModel(Long usuarioId, UsuarioIaConfigRequest request) {
        UsuarioIaConfig config = new UsuarioIaConfig();
        config.setUsuarioId(usuarioId);
        config.setProxyUrl(request.getProxyUrl().trim());
        config.setModel(request.getModel().trim());
        config.setApiKey(request.getApiKey().trim());
        return config;
    }

    private UsuarioIaConfigResponse toResponse(UsuarioIaConfig config) {
        return new UsuarioIaConfigResponse(
                config.getProxyUrl(),
                config.getModel(),
                config.getApiKey()
        );
    }
}
