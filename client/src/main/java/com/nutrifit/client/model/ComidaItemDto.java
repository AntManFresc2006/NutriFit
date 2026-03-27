package com.nutrifit.client.model;

/**
 * DTO plano para deserializar el detalle nutricional de un item de comida.
 */
public class ComidaItemDto {

    private Long itemId;
    private Long comidaId;
    private Long alimentoId;
    private String nombre;
    private double gramos;
    private double kcalEstimadas;
    private double proteinasEstimadas;
    private double grasasEstimadas;
    private double carbosEstimados;

    public ComidaItemDto() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getGramos() {
        return gramos;
    }

    public void setGramos(double gramos) {
        this.gramos = gramos;
    }

    public double getKcalEstimadas() {
        return kcalEstimadas;
    }

    public void setKcalEstimadas(double kcalEstimadas) {
        this.kcalEstimadas = kcalEstimadas;
    }

    public double getProteinasEstimadas() {
        return proteinasEstimadas;
    }

    public void setProteinasEstimadas(double proteinasEstimadas) {
        this.proteinasEstimadas = proteinasEstimadas;
    }

    public double getGrasasEstimadas() {
        return grasasEstimadas;
    }

    public void setGrasasEstimadas(double grasasEstimadas) {
        this.grasasEstimadas = grasasEstimadas;
    }

    public double getCarbosEstimados() {
        return carbosEstimados;
    }

    public void setCarbosEstimados(double carbosEstimados) {
        this.carbosEstimados = carbosEstimados;
    }
}
