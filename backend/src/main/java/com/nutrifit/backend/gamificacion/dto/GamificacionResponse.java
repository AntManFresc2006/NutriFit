package com.nutrifit.backend.gamificacion.dto;

public class GamificacionResponse {
    private int racha;
    private int nutriScore;
    private String nutriGrade;
    private boolean cumpleProteina;
    private boolean cumpleBalance;
    private boolean cumpleEjercicio;
    private boolean cumpleVariedad;

    public GamificacionResponse() {
    }

    public GamificacionResponse(int racha, int nutriScore, String nutriGrade,
                               boolean cumpleProteina, boolean cumpleBalance,
                               boolean cumpleEjercicio, boolean cumpleVariedad) {
        this.racha = racha;
        this.nutriScore = nutriScore;
        this.nutriGrade = nutriGrade;
        this.cumpleProteina = cumpleProteina;
        this.cumpleBalance = cumpleBalance;
        this.cumpleEjercicio = cumpleEjercicio;
        this.cumpleVariedad = cumpleVariedad;
    }

    public int getRacha() {
        return racha;
    }

    public void setRacha(int racha) {
        this.racha = racha;
    }

    public int getNutriScore() {
        return nutriScore;
    }

    public void setNutriScore(int nutriScore) {
        this.nutriScore = nutriScore;
    }

    public String getNutriGrade() {
        return nutriGrade;
    }

    public void setNutriGrade(String nutriGrade) {
        this.nutriGrade = nutriGrade;
    }

    public boolean isCumpleProteina() {
        return cumpleProteina;
    }

    public void setCumpleProteina(boolean cumpleProteina) {
        this.cumpleProteina = cumpleProteina;
    }

    public boolean isCumpleBalance() {
        return cumpleBalance;
    }

    public void setCumpleBalance(boolean cumpleBalance) {
        this.cumpleBalance = cumpleBalance;
    }

    public boolean isCumpleEjercicio() {
        return cumpleEjercicio;
    }

    public void setCumpleEjercicio(boolean cumpleEjercicio) {
        this.cumpleEjercicio = cumpleEjercicio;
    }

    public boolean isCumpleVariedad() {
        return cumpleVariedad;
    }

    public void setCumpleVariedad(boolean cumpleVariedad) {
        this.cumpleVariedad = cumpleVariedad;
    }
}
