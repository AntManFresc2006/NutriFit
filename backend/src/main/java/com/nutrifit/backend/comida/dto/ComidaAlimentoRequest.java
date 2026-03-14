package com.nutrifit.backend.comida.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para añadir un alimento a una comida.
 */
public class ComidaAlimentoRequest {

    @NotNull(message = "El id del alimento es obligatorio")
    private Long alimentoId;

    @DecimalMin(value = "0.01", message = "Los gramos deben ser mayores que 0")
    private double gramos;

    public ComidaAlimentoRequest() {
    }

    public Long getAlimentoId() {
        return alimentoId;
    }

    public void setAlimentoId(Long alimentoId) {
        this.alimentoId = alimentoId;
    }

    public double getGramos() {
        return gramos;
    }

    public void setGramos(double gramos) {
        this.gramos = gramos;
    }
}