package com.nutrifit.backend.comida.model;

/**
 * Relación entre una comida y un alimento concreto con sus gramos consumidos.
 */
public class ComidaAlimento {

    private Long id;
    private Long comidaId;
    private Long alimentoId;
    private double gramos;

    public ComidaAlimento() {
    }

    public ComidaAlimento(Long id, Long comidaId, Long alimentoId, double gramos) {
        this.id = id;
        this.comidaId = comidaId;
        this.alimentoId = alimentoId;
        this.gramos = gramos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComidaId() {
        return comidaId;
    }

    public void setComidaId(Long comidaId) {
        this.comidaId = comidaId;
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