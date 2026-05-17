package com.nutrifit.backend.tendencias.dto;

import java.time.LocalDate;

public class MacrosTendenciaPoint {
    private String semana;
    private LocalDate inicioSemana;
    private double kcalPromedio;
    private double proteinasPromedio;
    private double carbosPromedio;
    private double grasasPromedio;

    public MacrosTendenciaPoint() {}

    public MacrosTendenciaPoint(String semana, LocalDate inicioSemana, double kcalPromedio,
                               double proteinasPromedio, double carbosPromedio, double grasasPromedio) {
        this.semana = semana;
        this.inicioSemana = inicioSemana;
        this.kcalPromedio = kcalPromedio;
        this.proteinasPromedio = proteinasPromedio;
        this.carbosPromedio = carbosPromedio;
        this.grasasPromedio = grasasPromedio;
    }

    public String getSemana() { return semana; }
    public void setSemana(String semana) { this.semana = semana; }

    public LocalDate getInicioSemana() { return inicioSemana; }
    public void setInicioSemana(LocalDate inicioSemana) { this.inicioSemana = inicioSemana; }

    public double getKcalPromedio() { return kcalPromedio; }
    public void setKcalPromedio(double kcalPromedio) { this.kcalPromedio = kcalPromedio; }

    public double getProteinasPromedio() { return proteinasPromedio; }
    public void setProteinasPromedio(double proteinasPromedio) { this.proteinasPromedio = proteinasPromedio; }

    public double getCarbosPromedio() { return carbosPromedio; }
    public void setCarbosPromedio(double carbosPromedio) { this.carbosPromedio = carbosPromedio; }

    public double getGrasasPromedio() { return grasasPromedio; }
    public void setGrasasPromedio(double grasasPromedio) { this.grasasPromedio = grasasPromedio; }
}
