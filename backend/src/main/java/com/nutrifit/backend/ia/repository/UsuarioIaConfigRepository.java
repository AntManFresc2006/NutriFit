package com.nutrifit.backend.ia.repository;

import com.nutrifit.backend.ia.model.UsuarioIaConfig;

import java.util.Optional;

public interface UsuarioIaConfigRepository {

    Optional<UsuarioIaConfig> findByUsuarioId(Long usuarioId);

    void save(Long usuarioId, UsuarioIaConfig config);

    void deleteByUsuarioId(Long usuarioId);
}
