package com.nutrifit.backend.auth.repository;

import com.nutrifit.backend.auth.model.Usuario;

import java.util.Optional;

/**
 * Contrato de acceso a datos para usuarios en el contexto de autenticación.
 */
public interface UsuarioRepository {

    Optional<Usuario> findByEmail(String email);

    Usuario save(Usuario usuario);
}