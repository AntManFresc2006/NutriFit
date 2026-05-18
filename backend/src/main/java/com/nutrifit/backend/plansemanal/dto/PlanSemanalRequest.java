package com.nutrifit.backend.plansemanal.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para solicitar un plan semanal con fecha de inicio.
 */
public class PlanSemanalRequest {
    @NotNull(message = "La fecha de inicio de semana es obligatoria")
    private LocalDate semanaInicio;

    public PlanSemanalRequest() {
    }

    public PlanSemanalRequest(LocalDate semanaInicio) {
        this.semanaInicio = semanaInicio;
    }

    public LocalDate getSemanaInicio() {
        return semanaInicio;
    }

    public void setSemanaInicio(LocalDate semanaInicio) {
        this.semanaInicio = semanaInicio;
    }
}
