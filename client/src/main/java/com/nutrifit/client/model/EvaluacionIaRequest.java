package com.nutrifit.client.model;

public class EvaluacionIaRequest {

    private Long usuarioId;
    private String fecha;
    private double kcalConsumidas;
    private double kcalQuemadas;
    private double proteinasTotales;
    private double grasasTotales;
    private double carbosTotales;
    private double tdee;
    private double balanceReal;

    public EvaluacionIaRequest() {
        // requerido por Jackson
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public double getKcalConsumidas() { return kcalConsumidas; }
    public void setKcalConsumidas(double kcalConsumidas) { this.kcalConsumidas = kcalConsumidas; }

    public double getKcalQuemadas() { return kcalQuemadas; }
    public void setKcalQuemadas(double kcalQuemadas) { this.kcalQuemadas = kcalQuemadas; }

    public double getProteinasTotales() { return proteinasTotales; }
    public void setProteinasTotales(double proteinasTotales) { this.proteinasTotales = proteinasTotales; }

    public double getGrasasTotales() { return grasasTotales; }
    public void setGrasasTotales(double grasasTotales) { this.grasasTotales = grasasTotales; }

    public double getCarbosTotales() { return carbosTotales; }
    public void setCarbosTotales(double carbosTotales) { this.carbosTotales = carbosTotales; }

    public double getTdee() { return tdee; }
    public void setTdee(double tdee) { this.tdee = tdee; }

    public double getBalanceReal() { return balanceReal; }
    public void setBalanceReal(double balanceReal) { this.balanceReal = balanceReal; }
}
