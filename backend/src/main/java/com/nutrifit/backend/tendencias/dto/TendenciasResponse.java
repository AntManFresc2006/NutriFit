package com.nutrifit.backend.tendencias.dto;

import java.util.List;

public class TendenciasResponse {
    private List<PesoTendenciaPoint> peso;
    private List<NutriScoreTendenciaPoint> nutriScore;
    private List<MacrosTendenciaPoint> macros;
    private List<EjercicioTendenciaPoint> ejercicio;
    private Double pesoObjetivo;

    public TendenciasResponse() {}

    public TendenciasResponse(List<PesoTendenciaPoint> peso, List<NutriScoreTendenciaPoint> nutriScore,
                             List<MacrosTendenciaPoint> macros, List<EjercicioTendenciaPoint> ejercicio,
                             Double pesoObjetivo) {
        this.peso = peso;
        this.nutriScore = nutriScore;
        this.macros = macros;
        this.ejercicio = ejercicio;
        this.pesoObjetivo = pesoObjetivo;
    }

    public List<PesoTendenciaPoint> getPeso() { return peso; }
    public void setPeso(List<PesoTendenciaPoint> peso) { this.peso = peso; }

    public List<NutriScoreTendenciaPoint> getNutriScore() { return nutriScore; }
    public void setNutriScore(List<NutriScoreTendenciaPoint> nutriScore) { this.nutriScore = nutriScore; }

    public List<MacrosTendenciaPoint> getMacros() { return macros; }
    public void setMacros(List<MacrosTendenciaPoint> macros) { this.macros = macros; }

    public List<EjercicioTendenciaPoint> getEjercicio() { return ejercicio; }
    public void setEjercicio(List<EjercicioTendenciaPoint> ejercicio) { this.ejercicio = ejercicio; }

    public Double getPesoObjetivo() { return pesoObjetivo; }
    public void setPesoObjetivo(Double pesoObjetivo) { this.pesoObjetivo = pesoObjetivo; }
}
