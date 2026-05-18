package com.nutrifit.backend.ejercicio.dto;

/**
 * Respuesta con sugerencias de recuperación post-ejercicio.
 */
public class RecuperacionResponse {
    private boolean tieneEjercicioIntensivo;
    private String ejercicioNombre;
    private Double met;
    private Integer sugerenciaProteinaG;
    private Integer sugerenciaCarbosG;

    public RecuperacionResponse() {}

    public RecuperacionResponse(boolean tieneEjercicioIntensivo, String ejercicioNombre,
                                 Double met, Integer sugerenciaProteinaG, Integer sugerenciaCarbosG) {
        this.tieneEjercicioIntensivo = tieneEjercicioIntensivo;
        this.ejercicioNombre = ejercicioNombre;
        this.met = met;
        this.sugerenciaProteinaG = sugerenciaProteinaG;
        this.sugerenciaCarbosG = sugerenciaCarbosG;
    }

    public boolean isTieneEjercicioIntensivo() {
        return tieneEjercicioIntensivo;
    }

    public void setTieneEjercicioIntensivo(boolean tieneEjercicioIntensivo) {
        this.tieneEjercicioIntensivo = tieneEjercicioIntensivo;
    }

    public String getEjercicioNombre() {
        return ejercicioNombre;
    }

    public void setEjercicioNombre(String ejercicioNombre) {
        this.ejercicioNombre = ejercicioNombre;
    }

    public Double getMet() {
        return met;
    }

    public void setMet(Double met) {
        this.met = met;
    }

    public Integer getSugerenciaProteinaG() {
        return sugerenciaProteinaG;
    }

    public void setSugerenciaProteinaG(Integer sugerenciaProteinaG) {
        this.sugerenciaProteinaG = sugerenciaProteinaG;
    }

    public Integer getSugerenciaCarbosG() {
        return sugerenciaCarbosG;
    }

    public void setSugerenciaCarbosG(Integer sugerenciaCarbosG) {
        this.sugerenciaCarbosG = sugerenciaCarbosG;
    }
}
