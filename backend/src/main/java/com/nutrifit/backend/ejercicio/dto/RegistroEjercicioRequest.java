package com.nutrifit.backend.ejercicio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RegistroEjercicioRequest {

    @NotNull(message = "El ejercicioId es obligatorio")
    private Long ejercicioId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @Min(value = 1, message = "La duración mínima es 1 minuto")
    @Max(value = 999, message = "La duración máxima es 999 minutos")
    private int duracionMin;

    public Long getEjercicioId() { return ejercicioId; }
    public void setEjercicioId(Long ejercicioId) { this.ejercicioId = ejercicioId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }
}
