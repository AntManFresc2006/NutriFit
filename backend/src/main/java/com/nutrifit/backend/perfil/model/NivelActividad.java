package com.nutrifit.backend.perfil.model;

/**
 * Nivel de actividad física del usuario.
 * Cada valor lleva su factor multiplicador para calcular el TDEE a partir del TMB.
 */
public enum NivelActividad {

    SEDENTARIO(1.2),
    LIGERO(1.375),
    MODERADO(1.55),
    ALTO(1.725),
    MUY_ALTO(1.9);

    private final double factor;

    NivelActividad(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}
