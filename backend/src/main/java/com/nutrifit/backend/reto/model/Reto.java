package com.nutrifit.backend.reto.model;

public class Reto {
    private Long id;
    private String titulo;
    private String descripcion;
    private String tipo;
    private int metaValor;
    private int duracionDias;
    private int puntos;
    private String icono;

    public Reto() {}

    public Reto(Long id, String titulo, String descripcion, String tipo, int metaValor, int duracionDias, int puntos, String icono) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.metaValor = metaValor;
        this.duracionDias = duracionDias;
        this.puntos = puntos;
        this.icono = icono;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getMetaValor() { return metaValor; }
    public void setMetaValor(int metaValor) { this.metaValor = metaValor; }

    public int getDuracionDias() { return duracionDias; }
    public void setDuracionDias(int duracionDias) { this.duracionDias = duracionDias; }

    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
}
