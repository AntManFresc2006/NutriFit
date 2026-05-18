package com.nutrifit.backend.ejercicio.repository;

import com.nutrifit.backend.ejercicio.model.Ejercicio;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para ejercicios del catálogo.
 */
public interface EjercicioRepository {

    /**
     * Obtiene todos los ejercicios del catálogo.
     *
     * @return lista de ejercicios
     */
    List<Ejercicio> findAll();

    /**
     * Obtiene todos los ejercicios de un tipo específico.
     *
     * @param tipo tipo de ejercicio (AEROBICO, ANAEROBICO, FLEXIBILIDAD, etc.)
     * @return lista de ejercicios del tipo especificado
     */
    List<Ejercicio> findByTipo(String tipo);

    /**
     * Busca ejercicios cuyo nombre contenga la consulta (case-insensitive).
     *
     * @param query texto a buscar
     * @return lista de ejercicios coincidentes
     */
    List<Ejercicio> searchByNombre(String query);

    /**
     * Busca ejercicios por nombre y tipo.
     *
     * @param query texto a buscar
     * @param tipo tipo de ejercicio
     * @return lista de ejercicios coincidentes
     */
    List<Ejercicio> searchByNombreAndTipo(String query, String tipo);

    /**
     * Obtiene un ejercicio por su identificador.
     *
     * @param id identificador del ejercicio
     * @return ejercicio si existe
     */
    Optional<Ejercicio> findById(Long id);

    /**
     * Guarda un nuevo ejercicio en el catálogo.
     *
     * @param ejercicio ejercicio a guardar
     * @return ejercicio guardado con id asignado
     */
    Ejercicio save(Ejercicio ejercicio);
}
