package com.nutrifit.client.model;

/**
 * Solicitud para registrar o actualizar el peso del usuario en una fecha.
 */
public class PesoHistorialRequestDto {
    private String fecha;
    private Double pesoKg;

    public PesoHistorialRequestDto() {}

    public PesoHistorialRequestDto(String fecha, Double pesoKg) {
        this.fecha = fecha;
        this.pesoKg = pesoKg;
    }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Double getPesoKg() { return pesoKg; }
    public void setPesoKg(Double pesoKg) { this.pesoKg = pesoKg; }
}
