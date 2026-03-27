package com.nutrifit.backend.ejercicio.dto;

import java.time.LocalDate;

public class RegistroEjercicioResponse {

    private Long id;
    private Long usuarioId;
    private Long ejercicioId;
    private String nombreEjercicio;
    private LocalDate fecha;
    private int duracionMin;
    private double kcalQuemadas;

    public RegistroEjercicioResponse(Long id, Long usuarioId, Long ejercicioId,
                                     String nombreEjercicio, LocalDate fecha,
                                     int duracionMin, double kcalQuemadas) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.ejercicioId = ejercicioId;
        this.nombreEjercicio = nombreEjercicio;
        this.fecha = fecha;
        this.duracionMin = duracionMin;
        this.kcalQuemadas = kcalQuemadas;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public Long getEjercicioId() { return ejercicioId; }
    public String getNombreEjercicio() { return nombreEjercicio; }
    public LocalDate getFecha() { return fecha; }
    public int getDuracionMin() { return duracionMin; }
    public double getKcalQuemadas() { return kcalQuemadas; }
}
