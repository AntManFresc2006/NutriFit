package com.nutrifit.backend.ejercicio.model;

public class Ejercicio {

    private Long id;
    private String nombre;
    private double met;
    private String categoria;
    private String tipo;

    public Ejercicio() {}

    public Ejercicio(Long id, String nombre, double met, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.met = met;
        this.categoria = categoria;
    }

    public Ejercicio(Long id, String nombre, double met, String categoria, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.met = met;
        this.categoria = categoria;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getMet() { return met; }
    public void setMet(double met) { this.met = met; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
