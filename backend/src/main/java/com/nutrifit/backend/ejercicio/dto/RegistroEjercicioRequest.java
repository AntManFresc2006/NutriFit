package com.nutrifit.backend.ejercicio.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Solicitud para registrar un ejercicio realizado por el usuario.
 */
public class RegistroEjercicioRequest {

    @NotNull(message = "El ejercicioId es obligatorio")
    private Long ejercicioId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private int duracionMin;   // minutos para AEROBICO; 0 para ANAEROBICO

    private String intensidad; // BAJA | MEDIA | ALTA  (solo ANAEROBICO)

    private Integer numSeries; // número de series      (solo ANAEROBICO)

    public Long getEjercicioId() { return ejercicioId; }
    public void setEjercicioId(Long ejercicioId) { this.ejercicioId = ejercicioId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }

    public String getIntensidad() { return intensidad; }
    public void setIntensidad(String intensidad) { this.intensidad = intensidad; }

    public Integer getNumSeries() { return numSeries; }
    public void setNumSeries(Integer numSeries) { this.numSeries = numSeries; }
}
