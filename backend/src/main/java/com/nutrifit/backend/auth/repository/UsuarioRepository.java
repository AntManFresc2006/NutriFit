package com.nutrifit.backend.auth.repository;

import com.nutrifit.backend.auth.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> findByEmail(String email);

    Usuario save(Usuario usuario);
}