package com.nutrifit.backend.ejercicio.dto;

import java.time.LocalDate;

public class RegistroEjercicioResponse {

    private Long id;
    private Long usuarioId;
    private Long ejercicioId;
    private String nombreEjercicio;
    private String tipoEjercicio;
    private LocalDate fecha;
    private int duracionMin;
    private double kcalQuemadas;
    private String intensidad;
    private Integer numSeries;

    public RegistroEjercicioResponse() { /* para Jackson y row mappers */ }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getEjercicioId() { return ejercicioId; }
    public void setEjercicioId(Long ejercicioId) { this.ejercicioId = ejercicioId; }

    public String getNombreEjercicio() { return nombreEjercicio; }
    public void setNombreEjercicio(String nombreEjercicio) { this.nombreEjercicio = nombreEjercicio; }

    public String getTipoEjercicio() { return tipoEjercicio; }
    public void setTipoEjercicio(String tipoEjercicio) { this.tipoEjercicio = tipoEjercicio; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }

    public double getKcalQuemadas() { return kcalQuemadas; }
    public void setKcalQuemadas(double kcalQuemadas) { this.kcalQuemadas = kcalQuemadas; }

    public String getIntensidad() { return intensidad; }
    public void setIntensidad(String intensidad) { this.intensidad = intensidad; }

    public Integer getNumSeries() { return numSeries; }
    public void setNumSeries(Integer numSeries) { this.numSeries = numSeries; }
}
