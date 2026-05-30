package com.nutrifit.backend.plansemanal.repository;

import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Interfaz de repositorio para gestionar planes semanales de alimentación.
 */
public interface PlanSemanalRepository {

    /**
     * Crea un registro con estado GENERANDO y devuelve el id asignado.
     */
    Long createGenerando(Long usuarioId, LocalDate semanaInicio);

    /**
     * Actualiza el plan tras la generación asíncrona (LISTO o ERROR).
     */
    void updateFinalizado(Long id, String planJson, String estado, String errorMsg);

    /**
     * Busca un plan semanal por usuario y semana.
     */
    Optional<PlanSemanalResponse> findByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio);

    /**
     * Elimina un plan semanal.
     */
    void deleteByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio);
}
