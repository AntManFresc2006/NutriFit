package com.nutrifit.backend.resumen.dto;

import java.time.LocalDate;

/**
 * DTO de salida para el resumen nutricional diario de un usuario.
 */
public class ResumenDiarioResponse {

    private Long usuarioId;
    private LocalDate fecha;
    private double kcalTotales;
    private double proteinasTotales;
    private double grasasTotales;
    private double carbosTotales;
    private double kcalQuemadasTotales;
    private double balanceNeto;

    public ResumenDiarioResponse() {
    }

    public ResumenDiarioResponse(
            Long usuarioId,
            LocalDate fecha,
            double kcalTotales,
            double proteinasTotales,
            double grasasTotales,
            double carbosTotales,
            double kcalQuemadasTotales
    ) {
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.kcalTotales = kcalTotales;
        this.proteinasTotales = proteinasTotales;
        this.grasasTotales = grasasTotales;
        this.carbosTotales = carbosTotales;
        this.kcalQuemadasTotales = kcalQuemadasTotales;
        this.balanceNeto = kcalTotales - kcalQuemadasTotales;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getKcalTotales() {
        return kcalTotales;
    }

    public void setKcalTotales(double kcalTotales) {
        this.kcalTotales = kcalTotales;
    }

    public double getProteinasTotales() {
        return proteinasTotales;
    }

    public void setProteinasTotales(double proteinasTotales) {
        this.proteinasTotales = proteinasTotales;
    }

    public double getGrasasTotales() {
        return grasasTotales;
    }

    public void setGrasasTotales(double grasasTotales) {
        this.grasasTotales = grasasTotales;
    }

    public double getCarbosTotales() {
        return carbosTotales;
    }

    public void setCarbosTotales(double carbosTotales) {
        this.carbosTotales = carbosTotales;
    }

    public double getKcalQuemadasTotales() {
        return kcalQuemadasTotales;
    }

    public void setKcalQuemadasTotales(double kcalQuemadasTotales) {
        this.kcalQuemadasTotales = kcalQuemadasTotales;
    }

    public double getBalanceNeto() {
        return balanceNeto;
    }

    public void setBalanceNeto(double balanceNeto) {
        this.balanceNeto = balanceNeto;
    }
}
