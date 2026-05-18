package com.nutrifit.backend.ejercicio.repository;

import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.dto.RecuperacionResponse;
import com.nutrifit.backend.ejercicio.model.RegistroEjercicio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para registros de ejercicio de usuarios.
 */
public interface RegistroEjercicioRepository {

    /**
     * Obtiene todos los registros de ejercicio de un usuario en una fecha concreta.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha a consultar
     * @return lista de registros con datos enriquecidos
     */
    List<RegistroEjercicioResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);

    /**
     * Obtiene un registro de ejercicio por su identificador.
     *
     * @param id identificador del registro
     * @return registro si existe
     */
    Optional<RegistroEjercicio> findById(Long id);

    /**
     * Guarda un nuevo registro de ejercicio.
     *
     * @param registro registro a guardar
     * @return registro guardado con id asignado
     */
    RegistroEjercicio save(RegistroEjercicio registro);

    /**
     * Elimina un registro de ejercicio.
     *
     * @param id identificador del registro a eliminar
     * @return true si se eliminó, false si no existía
     */
    boolean deleteById(Long id);

    /**
     * Obtiene el último ejercicio intensivo registrado en una fecha (MET > 5).
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha a consultar
     * @return datos de recuperación si existe ejercicio intensivo
     */
    Optional<RecuperacionResponse> findUltimoIntensivoHoy(Long usuarioId, LocalDate fecha);
}
