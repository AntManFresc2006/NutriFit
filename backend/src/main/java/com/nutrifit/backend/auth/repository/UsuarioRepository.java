package com.nutrifit.backend.auth.repository;

import com.nutrifit.backend.auth.model.Usuario;

import java.util.Optional;

/**
 * Repositorio de usuarios: persistencia y recuperación de credenciales.
 */
public interface UsuarioRepository {

    /**
     * Busca un usuario por su email.
     *
     * @param email correo electrónico (búsqueda case-insensitive)
     * @return usuario si existe, vacío en otro caso
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Guarda un nuevo usuario en la base de datos.
     *
     * @param usuario usuario a insertar con email único
     * @return usuario guardado con ID generado
     */
    Usuario save(Usuario usuario);
}