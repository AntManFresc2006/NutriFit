package com.nutrifit.backend.pesohistorial.dto;

/**
 * Respuesta con los datos de un registro de peso histórico.
 */
public class PesoHistorialResponse {

    private Long id;
    private String fecha;
    private double pesoKg;

    public PesoHistorialResponse() {}

    public PesoHistorialResponse(Long id, String fecha, double pesoKg) {
        this.id = id;
        this.fecha = fecha;
        this.pesoKg = pesoKg;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }
}
