package com.nutrifit.backend.escaner.dto;

public record EscanerResponse(
        String nombre,
        String marca,
        double kcalPor100g,
        double proteinasPor100g,
        double grasasPor100g,
        double carbosPor100g,
        String imagenUrl
) {}
