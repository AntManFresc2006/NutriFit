package com.nutrifit.backend.listacompra.dto;

import java.util.List;

/**
 * DTO con sugerencias de alimentos basadas en historial.
 */
public class SugerenciasResponse {
    private List<String> sugerencias;

    public SugerenciasResponse() {}

    public SugerenciasResponse(List<String> sugerencias) {
        this.sugerencias = sugerencias;
    }

    public List<String> getSugerencias() { return sugerencias; }
    public void setSugerencias(List<String> sugerencias) { this.sugerencias = sugerencias; }
}
