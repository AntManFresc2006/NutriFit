package com.nutrifit.client.model;

public class HidratacionDto {
    private double totalMl;
    private double objetivoMl;
    private int porcentaje;

    public HidratacionDto() {}

    public HidratacionDto(double totalMl, double objetivoMl, int porcentaje) {
        this.totalMl = totalMl;
        this.objetivoMl = objetivoMl;
        this.porcentaje = porcentaje;
    }

    public double getTotalMl() {
        return totalMl;
    }

    public void setTotalMl(double totalMl) {
        this.totalMl = totalMl;
    }

    public double getObjetivoMl() {
        return objetivoMl;
    }

    public void setObjetivoMl(double objetivoMl) {
        this.objetivoMl = objetivoMl;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(int porcentaje) {
        this.porcentaje = porcentaje;
    }
}
