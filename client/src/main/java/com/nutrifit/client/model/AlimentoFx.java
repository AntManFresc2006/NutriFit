package com.nutrifit.client.model;

import javafx.beans.property.*;

public class AlimentoFx {

    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final DoubleProperty porcionG = new SimpleDoubleProperty();
    private final DoubleProperty kcalPor100g = new SimpleDoubleProperty();
    private final DoubleProperty proteinasG = new SimpleDoubleProperty();
    private final DoubleProperty grasasG = new SimpleDoubleProperty();
    private final DoubleProperty carbosG = new SimpleDoubleProperty();
    private final StringProperty fuente = new SimpleStringProperty();

    public long getId() {
        return id.get();
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public LongProperty idProperty() {
        return id;
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public double getPorcionG() {
        return porcionG.get();
    }

    public void setPorcionG(double porcionG) {
        this.porcionG.set(porcionG);
    }

    public DoubleProperty porcionGProperty() {
        return porcionG;
    }

    public double getKcalPor100g() {
        return kcalPor100g.get();
    }

    public void setKcalPor100g(double kcalPor100g) {
        this.kcalPor100g.set(kcalPor100g);
    }

    public DoubleProperty kcalPor100gProperty() {
        return kcalPor100g;
    }

    public double getProteinasG() {
        return proteinasG.get();
    }

    public void setProteinasG(double proteinasG) {
        this.proteinasG.set(proteinasG);
    }

    public DoubleProperty proteinasGProperty() {
        return proteinasG;
    }

    public double getGrasasG() {
        return grasasG.get();
    }

    public void setGrasasG(double grasasG) {
        this.grasasG.set(grasasG);
    }

    public DoubleProperty grasasGProperty() {
        return grasasG;
    }

    public double getCarbosG() {
        return carbosG.get();
    }

    public void setCarbosG(double carbosG) {
        this.carbosG.set(carbosG);
    }

    public DoubleProperty carbosGProperty() {
        return carbosG;
    }

    public String getFuente() {
        return fuente.get();
    }

    public void setFuente(String fuente) {
        this.fuente.set(fuente);
    }

    public StringProperty fuenteProperty() {
        return fuente;
    }
}
