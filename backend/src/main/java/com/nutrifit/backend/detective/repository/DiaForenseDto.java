package com.nutrifit.backend.detective.repository;

import java.time.LocalDate;

public class DiaForenseDto {
    private LocalDate fecha;
    private boolean tieneRegistro;
    private double kcal;
    private double proteinas;

    public DiaForenseDto() {}

    public DiaForenseDto(LocalDate fecha, boolean tieneRegistro, double kcal, double proteinas) {
        this.fecha = fecha;
        this.tieneRegistro = tieneRegistro;
        this.kcal = kcal;
        this.proteinas = proteinas;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public boolean isTieneRegistro() { return tieneRegistro; }
    public void setTieneRegistro(boolean tieneRegistro) { this.tieneRegistro = tieneRegistro; }

    public double getKcal() { return kcal; }
    public void setKcal(double kcal) { this.kcal = kcal; }

    public double getProteinas() { return proteinas; }
    public void setProteinas(double proteinas) { this.proteinas = proteinas; }
}
