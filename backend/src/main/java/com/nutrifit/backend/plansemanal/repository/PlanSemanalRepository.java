package com.nutrifit.backend.plansemanal.repository;

import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Interfaz de repositorio para gestionar planes semanales de alimentación.
 */
public interface PlanSemanalRepository {

    /**
     * Guarda o actualiza un plan semanal.
     * @param usuarioId ID del usuario propietario del plan.
     * @param semanaInicio Fecha de inicio de la semana.
     * @param planJson Plan en formato JSON.
     * @return Plan guardado.
     */
    PlanSemanalResponse save(Long usuarioId, LocalDate semanaInicio, String planJson);

    /**
     * Busca un plan semanal por usuario y semana.
     * @param usuarioId ID del usuario.
     * @param semanaInicio Fecha de inicio de la semana.
     * @return Plan si existe.
     */
    Optional<PlanSemanalResponse> findByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio);

    /**
     * Elimina un plan semanal.
     * @param usuarioId ID del usuario.
     * @param semanaInicio Fecha de inicio de la semana.
     */
    void deleteByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio);
}
