package com.nutrifit.client.model;

public class EjercicioDto {

    private Long id;
    private String nombre;
    private double met;
    private String categoria;

    public EjercicioDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getMet() { return met; }
    public void setMet(double met) { this.met = met; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @Override
    public String toString() {
        return nombre;
    }
}
