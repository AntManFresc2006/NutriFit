package com.nutrifit.client.model;

/**
 * DTO del resumen diario devuelto por el backend.
 */
public class ResumenDiarioDto {

    private Long usuarioId;
    private String fecha;
    private double kcalTotales;
    private double proteinasTotales;
    private double grasasTotales;
    private double carbosTotales;

    public ResumenDiarioDto() {
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
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
}
