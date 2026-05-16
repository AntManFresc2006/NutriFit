package com.nutrifit.backend.ejercicio.dto;

public record EjercicioExternoResponse(
        String nombre,
        double met,
        String categoria
) {}
