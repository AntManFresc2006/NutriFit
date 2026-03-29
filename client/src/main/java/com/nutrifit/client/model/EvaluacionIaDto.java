package com.nutrifit.client.model;

/**
 * DTO de salida con el texto de evaluación nutricional generado por la IA.
 */
public class EvaluacionIaDto {

    private String evaluacion;

    public EvaluacionIaDto() {
    }

    public String getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(String evaluacion) {
        this.evaluacion = evaluacion;
    }
}
