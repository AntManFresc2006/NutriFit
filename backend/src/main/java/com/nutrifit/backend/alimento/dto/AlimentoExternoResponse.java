package com.nutrifit.backend.alimento.dto;

public record AlimentoExternoResponse(
        String nombre,
        double kcalPor100g,
        double proteinasG,
        double grasasG,
        double carbosG,
        String fuente
) {}
