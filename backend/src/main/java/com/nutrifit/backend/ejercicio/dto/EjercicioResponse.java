package com.nutrifit.backend.ejercicio.dto;

public class EjercicioResponse {

    private Long id;
    private String nombre;
    private double met;
    private String categoria;
    private String tipo;

    public EjercicioResponse(Long id, String nombre, double met, String categoria, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.met = met;
        this.categoria = categoria;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public double getMet() { return met; }
    public String getCategoria() { return categoria; }
    public String getTipo() { return tipo; }
}
