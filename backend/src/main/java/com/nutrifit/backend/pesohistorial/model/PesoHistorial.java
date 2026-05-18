package com.nutrifit.backend.pesohistorial.model;

import java.time.LocalDate;

/**
 * Modelo que representa el registro histórico del peso de un usuario en una fecha.
 */
public class PesoHistorial {

    private Long id;
    private Long usuarioId;
    private LocalDate fecha;
    private double pesoKg;

    public PesoHistorial() {}

    public PesoHistorial(Long id, Long usuarioId, LocalDate fecha, double pesoKg) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.pesoKg = pesoKg;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }
}
