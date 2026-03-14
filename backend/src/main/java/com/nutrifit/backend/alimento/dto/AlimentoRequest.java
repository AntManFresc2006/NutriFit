package com.nutrifit.backend.alimento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de entrada utilizado para crear o actualizar alimentos desde la API.
 * Incluye validaciones sobre los campos recibidos.
 */

public class AlimentoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "La porción es obligatoria")
    @DecimalMin(value = "0.01", message = "La porción debe ser mayor que 0")
    private BigDecimal porcionG;

    @NotNull(message = "Las kcal por 100g son obligatorias")
    @DecimalMin(value = "0.0", inclusive = true, message = "Las kcal por 100g no pueden ser negativas")
    private BigDecimal kcalPor100g;

    @NotNull(message = "Las proteínas son obligatorias")
    @DecimalMin(value = "0.0", inclusive = true, message = "Las proteínas no pueden ser negativas")
    private BigDecimal proteinasG;

    @NotNull(message = "Las grasas son obligatorias")
    @DecimalMin(value = "0.0", inclusive = true, message = "Las grasas no pueden ser negativas")
    private BigDecimal grasasG;

    @NotNull(message = "Los carbohidratos son obligatorios")
    @DecimalMin(value = "0.0", inclusive = true, message = "Los carbohidratos no pueden ser negativos")
    private BigDecimal carbosG;

    private String fuente;

    public AlimentoRequest() {
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