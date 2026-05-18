package com.nutrifit.backend.plansemanal.service;

import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;

import java.time.LocalDate;

/**
 * Servicio para generar y gestionar planes semanales de alimentación.
 */
public interface PlanSemanalService {

    /**
     * Genera o recupera un plan semanal existente.
     * Consulta IA (OpenRouter) si no existe previamente.
     * @param usuarioId ID del usuario.
     * @param semanaInicio Fecha de inicio de la semana.
     * @return Plan semanal generado o recuperado.
     */
    PlanSemanalResponse generarORecuperarPlan(Long usuarioId, LocalDate semanaInicio);

    /**
     * Obtiene un plan semanal existente.
     * @param usuarioId ID del usuario.
     * @param semanaInicio Fecha de inicio de la semana.
     * @return Plan si existe, null en caso contrario.
     */
    PlanSemanalResponse getPlan(Long usuarioId, LocalDate semanaInicio);

    /**
     * Elimina un plan semanal.
     * @param usuarioId ID del usuario.
     * @param semanaInicio Fecha de inicio de la semana.
     */
    void eliminarPlan(Long usuarioId, LocalDate semanaInicio);
}
