package com.nutrifit.backend.alimento.dto;

import java.math.BigDecimal;

/**
 * DTO de salida con los datos nutricionales extraídos de una foto mediante análisis con IA.
 */
public class EscanearFotoResponse {

    private String nombre;
    private Double kcalPor100g;
    private Double proteinas;
    private Double grasas;
    private Double carbos;
    private Double porcion;

    public EscanearFotoResponse() {
    }

    public EscanearFotoResponse(String nombre, Double kcalPor100g, Double proteinas, Double grasas, Double carbos, Double porcion) {
        this.nombre = nombre;
        this.kcalPor100g = kcalPor100g;
        this.proteinas = proteinas;
        this.grasas = grasas;
        this.carbos = carbos;
        this.porcion = porcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getKcalPor100g() {
        return kcalPor100g;
    }

    public void setKcalPor100g(Double kcalPor100g) {
        this.kcalPor100g = kcalPor100g;
    }

    public Double getProteinas() {
        return proteinas;
    }

    public void setProteinas(Double proteinas) {
        this.proteinas = proteinas;
    }

    public Double getGrasas() {
        return grasas;
    }

    public void setGrasas(Double grasas) {
        this.grasas = grasas;
    }

    public Double getCarbos() {
        return carbos;
    }

    public void setCarbos(Double carbos) {
        this.carbos = carbos;
    }

    public Double getPorcion() {
        return porcion;
    }

    public void setPorcion(Double porcion) {
        this.porcion = porcion;
    }
}
