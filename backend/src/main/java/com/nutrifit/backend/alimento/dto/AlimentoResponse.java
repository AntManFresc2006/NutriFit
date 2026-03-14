package com.nutrifit.backend.alimento.dto;

import java.math.BigDecimal;

public class AlimentoResponse {

    private Long id;
    private String nombre;
    private BigDecimal porcionG;
    private BigDecimal kcalPor100g;
    private BigDecimal proteinasG;
    private BigDecimal grasasG;
    private BigDecimal carbosG;
    private String fuente;

    public AlimentoResponse() {
    }

    public AlimentoResponse(Long id, String nombre, BigDecimal porcionG, BigDecimal kcalPor100g,
                            BigDecimal proteinasG, BigDecimal grasasG, BigDecimal carbosG, String fuente) {
        this.id = id;
        this.nombre = nombre;
        this.porcionG = porcionG;
        this.kcalPor100g = kcalPor100g;
        this.proteinasG = proteinasG;
        this.grasasG = grasasG;
        this.carbosG = carbosG;
        this.fuente = fuente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPorcionG() {
        return porcionG;
    }

    public void setPorcionG(BigDecimal porcionG) {
        this.porcionG = porcionG;
    }

    public BigDecimal getKcalPor100g() {
        return kcalPor100g;
    }

    public void setKcalPor100g(BigDecimal kcalPor100g) {
        this.kcalPor100g = kcalPor100g;
    }

    public BigDecimal getProteinasG() {
        return proteinasG;
    }

    public void setProteinasG(BigDecimal proteinasG) {
        this.proteinasG = proteinasG;
    }

    public BigDecimal getGrasasG() {
        return grasasG;
    }

    public void setGrasasG(BigDecimal grasasG) {
        this.grasasG = grasasG;
    }

    public BigDecimal getCarbosG() {
        return carbosG;
    }

    public void setCarbosG(BigDecimal carbosG) {
        this.carbosG = carbosG;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }
}