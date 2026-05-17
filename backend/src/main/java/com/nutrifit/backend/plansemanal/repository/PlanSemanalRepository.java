package com.nutrifit.backend.plansemanal.repository;

import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;

import java.time.LocalDate;
import java.util.Optional;

public interface PlanSemanalRepository {

    PlanSemanalResponse save(Long usuarioId, LocalDate semanaInicio, String planJson);

    Optional<PlanSemanalResponse> findByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio);

    void deleteByUsuarioAndSemana(Long usuarioId, LocalDate semanaInicio);
}
