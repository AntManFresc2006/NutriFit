package com.nutrifit.backend.ejercicio.dto;

/**
 * Respuesta con datos de un ejercicio obtenido de la API externa Wger.
 */
public record EjercicioExternoResponse(
        String nombre,
        double met,
        String categoria
) {}
