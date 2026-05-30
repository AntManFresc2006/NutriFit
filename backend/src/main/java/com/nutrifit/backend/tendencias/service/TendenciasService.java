package com.nutrifit.backend.tendencias.service;

import com.nutrifit.backend.tendencias.dto.TendenciasResponse;

import java.time.LocalDate;

/**
 * Contrato de la lógica de negocio para el análisis de tendencias nutricionales.
 */
public interface TendenciasService {

    /**
     * Obtiene las series históricas de peso, NutriScore, macros y ejercicio.
     *
     * @param usuarioId identificador del usuario
     * @param dias      número de días hacia atrás desde fechaFin
     * @param fechaFin  fecha de fin del período (null = hoy)
     * @return objeto con las cuatro series de tendencias y el peso objetivo
     */
    TendenciasResponse getTendencias(Long usuarioId, int dias, LocalDate fechaFin);
}
