package com.nutrifit.backend.tendencias.dto;

import java.time.LocalDate;

public class NutriScoreTendenciaPoint {
    private LocalDate fecha;
    private int score;
    private String grade;

    public NutriScoreTendenciaPoint() {}

    public NutriScoreTendenciaPoint(LocalDate fecha, int score, String grade) {
        this.fecha = fecha;
        this.score = score;
        this.grade = grade;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
