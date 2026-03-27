package com.nutrifit.backend.ejercicio.model;

import java.time.LocalDate;

public class RegistroEjercicio {

    private Long id;
    private Long usuarioId;
    private Long ejercicioId;
    private LocalDate fecha;
    private int duracionMin;
    private double kcalQuemadas;

    public RegistroEjercicio() {}

    public RegistroEjercicio(Long id, Long usuarioId, Long ejercicioId,
                             LocalDate fecha, int duracionMin, double kcalQuemadas) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.ejercicioId = ejercicioId;
        this.fecha = fecha;
        this.duracionMin = duracionMin;
        this.kcalQuemadas = kcalQuemadas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getEjercicioId() { return ejercicioId; }
    public void setEjercicioId(Long ejercicioId) { this.ejercicioId = ejercicioId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }

    public double getKcalQuemadas() { return kcalQuemadas; }
    public void setKcalQuemadas(double kcalQuemadas) { this.kcalQuemadas = kcalQuemadas; }
}
