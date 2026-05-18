package com.nutrifit.backend.alimento.dto;

/**
 * DTO de salida con datos de alimentos provenientes de la API Open Food Facts.
 */
public record AlimentoExternoResponse(
        String nombre,
        double kcalPor100g,
        double proteinasG,
        double grasasG,
        double carbosG,
        String fuente
) {}
