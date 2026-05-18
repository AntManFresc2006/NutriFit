package com.nutrifit.backend.pesohistorial.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Solicitud para registrar o actualizar el peso del usuario en una fecha.
 */
public class PesoHistorialRequest {

    @NotNull(message = "La fecha es requerida en formato yyyy-MM-dd")
    private String fecha;

    @NotNull(message = "El peso es requerido")
    private Double pesoKg;

    public PesoHistorialRequest() {}

    public PesoHistorialRequest(String fecha, Double pesoKg) {
        this.fecha = fecha;
        this.pesoKg = pesoKg;
    }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Double getPesoKg() { return pesoKg; }
    public void setPesoKg(Double pesoKg) { this.pesoKg = pesoKg; }

    public boolean isValidPeso() {
        return pesoKg != null && pesoKg >= 20.0 && pesoKg <= 500.0;
    }
}
