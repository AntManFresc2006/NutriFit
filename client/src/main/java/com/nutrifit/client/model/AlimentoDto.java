package com.nutrifit.client.model;

/**
 * DTO plano usado por el cliente JavaFX para serializar y deserializar
 * las respuestas JSON de la API REST.
 */
public class AlimentoDto {

    private Long id;
    private String nombre;
    private Double porcionG;
    private Double kcalPor100g;
    private Double proteinasG;
    private Double grasasG;
    private Double carbosG;
    private String fuente;

    public AlimentoDto() {
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

    public Double getPorcionG() {
        return porcionG;
    }

    public void setPorcionG(Double porcionG) {
        this.porcionG = porcionG;
    }

    public Double getKcalPor100g() {
        return kcalPor100g;
    }

    public void setKcalPor100g(Double kcalPor100g) {
        this.kcalPor100g = kcalPor100g;
    }

    public Double getProteinasG() {
        return proteinasG;
    }

    public void setProteinasG(Double proteinasG) {
        this.proteinasG = proteinasG;
    }

    public Double getGrasasG() {
        return grasasG;
    }

    public void setGrasasG(Double grasasG) {
        this.grasasG = grasasG;
    }

    public Double getCarbosG() {
        return carbosG;
    }

    public void setCarbosG(Double carbosG) {
        this.carbosG = carbosG;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }
}
