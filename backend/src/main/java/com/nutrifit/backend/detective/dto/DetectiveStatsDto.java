package com.nutrifit.backend.detective.dto;

import java.util.Map;

public class DetectiveStatsDto {

    private int diasAnalizados;
    private int diasConRegistro;
    private int diasSinRegistro;
    private double tasaRegistro;
    private double kcalMediaDiaria;
    private double deficitRealMedio;
    private int diasSobreObjetivo;
    private double proteinaMediaDiaria;
    private double proteinaObjetivo;
    private int diasCumpliendoProteina;
    private double proteinaCumplimiento;
    private Map<String, Double> kcalPorDiaSemana;
    private String peorDiaSemana;
    private String mejorDiaSemana;

    public DetectiveStatsDto() {}

    public int getDiasAnalizados() { return diasAnalizados; }
    public void setDiasAnalizados(int diasAnalizados) { this.diasAnalizados = diasAnalizados; }

    public int getDiasConRegistro() { return diasConRegistro; }
    public void setDiasConRegistro(int diasConRegistro) { this.diasConRegistro = diasConRegistro; }

    public int getDiasSinRegistro() { return diasSinRegistro; }
    public void setDiasSinRegistro(int diasSinRegistro) { this.diasSinRegistro = diasSinRegistro; }

    public double getTasaRegistro() { return tasaRegistro; }
    public void setTasaRegistro(double tasaRegistro) { this.tasaRegistro = tasaRegistro; }

    public double getKcalMediaDiaria() { return kcalMediaDiaria; }
    public void setKcalMediaDiaria(double kcalMediaDiaria) { this.kcalMediaDiaria = kcalMediaDiaria; }

    public double getDeficitRealMedio() { return deficitRealMedio; }
    public void setDeficitRealMedio(double deficitRealMedio) { this.deficitRealMedio = deficitRealMedio; }

    public int getDiasSobreObjetivo() { return diasSobreObjetivo; }
    public void setDiasSobreObjetivo(int diasSobreObjetivo) { this.diasSobreObjetivo = diasSobreObjetivo; }

    public double getProteinaMediaDiaria() { return proteinaMediaDiaria; }
    public void setProteinaMediaDiaria(double proteinaMediaDiaria) { this.proteinaMediaDiaria = proteinaMediaDiaria; }

    public double getProteinaObjetivo() { return proteinaObjetivo; }
    public void setProteinaObjetivo(double proteinaObjetivo) { this.proteinaObjetivo = proteinaObjetivo; }

    public int getDiasCumpliendoProteina() { return diasCumpliendoProteina; }
    public void setDiasCumpliendoProteina(int diasCumpliendoProteina) { this.diasCumpliendoProteina = diasCumpliendoProteina; }

    public double getProteinaCumplimiento() { return proteinaCumplimiento; }
    public void setProteinaCumplimiento(double proteinaCumplimiento) { this.proteinaCumplimiento = proteinaCumplimiento; }

    public Map<String, Double> getKcalPorDiaSemana() { return kcalPorDiaSemana; }
    public void setKcalPorDiaSemana(Map<String, Double> kcalPorDiaSemana) { this.kcalPorDiaSemana = kcalPorDiaSemana; }

    public String getPeorDiaSemana() { return peorDiaSemana; }
    public void setPeorDiaSemana(String peorDiaSemana) { this.peorDiaSemana = peorDiaSemana; }

    public String getMejorDiaSemana() { return mejorDiaSemana; }
    public void setMejorDiaSemana(String mejorDiaSemana) { this.mejorDiaSemana = mejorDiaSemana; }
}
