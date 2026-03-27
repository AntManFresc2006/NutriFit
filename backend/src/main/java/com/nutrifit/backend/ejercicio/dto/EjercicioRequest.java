package com.nutrifit.backend.ejercicio.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EjercicioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El MET es obligatorio")
    @DecimalMin(value = "0.1", message = "El MET debe ser al menos 0.1")
    @DecimalMax(value = "20.0", message = "El MET no puede superar 20.0")
    private Double met;

    private String categoria;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getMet() { return met; }
    public void setMet(Double met) { this.met = met; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
