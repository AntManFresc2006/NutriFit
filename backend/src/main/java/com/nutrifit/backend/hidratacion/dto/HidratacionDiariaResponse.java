package com.nutrifit.backend.hidratacion.dto;

import java.time.LocalDate;
import java.util.List;

public class HidratacionDiariaResponse {

    private LocalDate fecha;
    private int totalMl;
    private int objetivoMl;
    private int porcentaje;
    private List<AguaResponse> registros;

    public HidratacionDiariaResponse() {}

    public HidratacionDiariaResponse(LocalDate fecha, int totalMl, int objetivoMl, int porcentaje, List<AguaResponse> registros) {
        this.fecha = fecha;
        this.totalMl = totalMl;
        this.objetivoMl = objetivoMl;
        this.porcentaje = porcentaje;
        this.registros = registros;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getTotalMl() { return totalMl; }
    public void setTotalMl(int totalMl) { this.totalMl = totalMl; }

    public int getObjetivoMl() { return objetivoMl; }
    public void setObjetivoMl(int objetivoMl) { this.objetivoMl = objetivoMl; }

    public int getPorcentaje() { return porcentaje; }
    public void setPorcentaje(int porcentaje) { this.porcentaje = porcentaje; }

    public List<AguaResponse> getRegistros() { return registros; }
    public void setRegistros(List<AguaResponse> registros) { this.registros = registros; }
}
