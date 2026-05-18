package com.nutrifit.backend.perfil.repository;

import com.nutrifit.backend.perfil.model.Perfil;

import java.util.Optional;

/**
 * Contrato de acceso a datos para el perfil del usuario.
 */
public interface PerfilRepository {

    /**
     * Busca el perfil de un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return perfil encontrado, o vacío si el usuario no existe
     */
    Optional<Perfil> findById(Long id);

    /**
     * Actualiza los datos biométricos y de objetivo del usuario.
     *
     * @param id     identificador del usuario a actualizar
     * @param perfil objeto con los nuevos valores del perfil
     * @return el mismo objeto {@code perfil} tras la actualización
     */
    Perfil update(Long id, Perfil perfil);
}
