package com.nutrifit.backend.detective.dto;

import java.util.List;

public class DetectiveAnalisisDto {

    private Long id;
    private int diasAnalizados;
    private String estado;
    private DetectiveStatsDto estadisticas;
    private List<HallazgoDto> hallazgos;
    private String analisisIa;
    private String errorMsg;

    public DetectiveAnalisisDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getDiasAnalizados() { return diasAnalizados; }
    public void setDiasAnalizados(int diasAnalizados) { this.diasAnalizados = diasAnalizados; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public DetectiveStatsDto getEstadisticas() { return estadisticas; }
    public void setEstadisticas(DetectiveStatsDto estadisticas) { this.estadisticas = estadisticas; }

    public List<HallazgoDto> getHallazgos() { return hallazgos; }
    public void setHallazgos(List<HallazgoDto> hallazgos) { this.hallazgos = hallazgos; }

    public String getAnalisisIa() { return analisisIa; }
    public void setAnalisisIa(String analisisIa) { this.analisisIa = analisisIa; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
