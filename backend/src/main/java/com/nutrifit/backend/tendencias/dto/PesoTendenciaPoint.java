package com.nutrifit.backend.tendencias.dto;

import java.time.LocalDate;

/**
 * Punto de la serie temporal de peso corporal del usuario.
 */
public class PesoTendenciaPoint {
    private LocalDate fecha;
    private double pesoKg;

    public PesoTendenciaPoint() {}

    public PesoTendenciaPoint(LocalDate fecha, double pesoKg) {
        this.fecha = fecha;
        this.pesoKg = pesoKg;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }
}
