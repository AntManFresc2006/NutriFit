package com.nutrifit.client.model;

public class RegistroHidratacionDto {
    private int cantidadMl;
    private String fuente;
    private String fecha;

    public RegistroHidratacionDto() {}

    public RegistroHidratacionDto(int cantidadMl, String fuente, String fecha) {
        this.cantidadMl = cantidadMl;
        this.fuente = fuente;
        this.fecha = fecha;
    }

    public int getCantidadMl() {
        return cantidadMl;
    }

    public void setCantidadMl(int cantidadMl) {
        this.cantidadMl = cantidadMl;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
