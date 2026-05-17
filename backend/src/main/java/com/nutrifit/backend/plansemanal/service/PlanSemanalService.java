package com.nutrifit.backend.plansemanal.service;

import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;

import java.time.LocalDate;

public interface PlanSemanalService {

    PlanSemanalResponse generarORecuperarPlan(Long usuarioId, LocalDate semanaInicio);

    PlanSemanalResponse getPlan(Long usuarioId, LocalDate semanaInicio);

    void eliminarPlan(Long usuarioId, LocalDate semanaInicio);
}
