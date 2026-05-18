package com.nutrifit.backend.ejercicio.service;

import com.nutrifit.backend.ejercicio.dto.EjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.EjercicioResponse;

import java.util.List;

/**
 * Servicio de lógica de negocio para el catálogo de ejercicios.
 */
public interface EjercicioService {

    /**
     * Obtiene el catálogo de ejercicios con filtros opcionales.
     *
     * @param query texto para buscar por nombre (opcional)
     * @param tipo tipo de ejercicio para filtrar (opcional)
     * @return lista de ejercicios que cumplen los filtros
     */
    List<EjercicioResponse> findAll(String query, String tipo);

    /**
     * Obtiene un ejercicio específico del catálogo.
     *
     * @param id identificador del ejercicio
     * @return datos del ejercicio
     * @throws com.nutrifit.backend.common.exception.ResourceNotFoundException si no existe
     */
    EjercicioResponse findById(Long id);

    /**
     * Crea un nuevo ejercicio en el catálogo.
     *
     * @param request datos del ejercicio a crear
     * @return ejercicio creado con id asignado
     */
    EjercicioResponse save(EjercicioRequest request);
}
