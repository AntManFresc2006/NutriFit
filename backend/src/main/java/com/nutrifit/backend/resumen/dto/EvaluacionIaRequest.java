package com.nutrifit.backend.resumen.dto;

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
    private double kcalMedia7d;
    private double proteinasMedia7d;
    private int diasConEjercicio7d;
    private double balanceMedia7d;

    public EvaluacionIaRequest() {
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

    public double getKcalMedia7d() { return kcalMedia7d; }
    public void setKcalMedia7d(double kcalMedia7d) { this.kcalMedia7d = kcalMedia7d; }

    public double getProteinasMedia7d() { return proteinasMedia7d; }
    public void setProteinasMedia7d(double proteinasMedia7d) { this.proteinasMedia7d = proteinasMedia7d; }

    public int getDiasConEjercicio7d() { return diasConEjercicio7d; }
    public void setDiasConEjercicio7d(int diasConEjercicio7d) { this.diasConEjercicio7d = diasConEjercicio7d; }

    public double getBalanceMedia7d() { return balanceMedia7d; }
    public void setBalanceMedia7d(double balanceMedia7d) { this.balanceMedia7d = balanceMedia7d; }
}
