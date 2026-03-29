package com.nutrifit.backend.evaluacion.service;

import java.time.LocalDate;

/**
 * Contrato del servicio que evalúa el día nutricional del usuario usando IA.
 */
public interface EvaluacionIaService {

    /**
     * Genera una evaluación en lenguaje natural del día nutricional del usuario.
     *
     * @param usuarioId id del usuario
     * @param fecha     día a evaluar
     * @return texto de evaluación generado por el modelo de IA
     */
    String evaluarDia(Long usuarioId, LocalDate fecha);
}
