package com.nutrifit.client.model;

/**
 * DTO plano para deserializar la respuesta JSON de registros de ejercicio del backend.
 * La fecha llega como String ISO (ej. "2026-03-27").
 */
public class RegistroEjercicioDto {

    private Long id;
    private Long usuarioId;
    private Long ejercicioId;
    private String nombreEjercicio;
    private String fecha;
    private int duracionMin;
    private double kcalQuemadas;

    public RegistroEjercicioDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getEjercicioId() { return ejercicioId; }
    public void setEjercicioId(Long ejercicioId) { this.ejercicioId = ejercicioId; }

    public String getNombreEjercicio() { return nombreEjercicio; }
    public void setNombreEjercicio(String nombreEjercicio) { this.nombreEjercicio = nombreEjercicio; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }

    public double getKcalQuemadas() { return kcalQuemadas; }
    public void setKcalQuemadas(double kcalQuemadas) { this.kcalQuemadas = kcalQuemadas; }
}
