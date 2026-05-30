package com.nutrifit.backend.detective.dto;

public class HallazgoDto {

    private String severidad;
    private String titulo;
    private String descripcion;

    public HallazgoDto() {}

    public HallazgoDto(String severidad, String titulo, String descripcion) {
        this.severidad = severidad;
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public String getSeveridad() { return severidad; }
    public void setSeveridad(String severidad) { this.severidad = severidad; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
