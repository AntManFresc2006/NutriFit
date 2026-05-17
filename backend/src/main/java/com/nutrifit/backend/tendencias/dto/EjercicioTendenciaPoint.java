package com.nutrifit.backend.tendencias.dto;

import java.time.LocalDate;

public class EjercicioTendenciaPoint {
    private LocalDate fecha;
    private boolean tuvoEjercicio;
    private int duracionMin;
    private double kcalQuemadas;

    public EjercicioTendenciaPoint() {}

    public EjercicioTendenciaPoint(LocalDate fecha, boolean tuvoEjercicio, int duracionMin, double kcalQuemadas) {
        this.fecha = fecha;
        this.tuvoEjercicio = tuvoEjercicio;
        this.duracionMin = duracionMin;
        this.kcalQuemadas = kcalQuemadas;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public boolean isTuvoEjercicio() { return tuvoEjercicio; }
    public void setTuvoEjercicio(boolean tuvoEjercicio) { this.tuvoEjercicio = tuvoEjercicio; }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }

    public double getKcalQuemadas() { return kcalQuemadas; }
    public void setKcalQuemadas(double kcalQuemadas) { this.kcalQuemadas = kcalQuemadas; }
}
