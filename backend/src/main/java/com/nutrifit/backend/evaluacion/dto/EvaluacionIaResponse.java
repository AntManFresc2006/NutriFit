package com.nutrifit.backend.evaluacion.dto;

/**
 * DTO de salida con el texto de evaluación nutricional generado por la IA.
 */
public class EvaluacionIaResponse {

    private String evaluacion;

    public EvaluacionIaResponse() {
    }

    public EvaluacionIaResponse(String evaluacion) {
        this.evaluacion = evaluacion;
    }

    public String getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(String evaluacion) {
        this.evaluacion = evaluacion;
    }
}
