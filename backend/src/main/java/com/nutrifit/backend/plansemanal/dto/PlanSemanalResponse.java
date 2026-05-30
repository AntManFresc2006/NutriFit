package com.nutrifit.backend.plansemanal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO con el plan semanal de alimentación generado por IA.
 */
public class PlanSemanalResponse {

    private Long id;
    private LocalDate semanaInicio;
    private String planJson;
    private LocalDateTime createdAt;
    private String estado;
    private String errorMsg;

    public PlanSemanalResponse() {
    }

    public PlanSemanalResponse(Long id, LocalDate semanaInicio, String planJson,
                               LocalDateTime createdAt, String estado, String errorMsg) {
        this.id = id;
        this.semanaInicio = semanaInicio;
        this.planJson = planJson;
        this.createdAt = createdAt;
        this.estado = estado;
        this.errorMsg = errorMsg;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getSemanaInicio() { return semanaInicio; }
    public void setSemanaInicio(LocalDate semanaInicio) { this.semanaInicio = semanaInicio; }

    public String getPlanJson() { return planJson; }
    public void setPlanJson(String planJson) { this.planJson = planJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
