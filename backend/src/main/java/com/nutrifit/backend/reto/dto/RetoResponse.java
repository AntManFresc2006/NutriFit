package com.nutrifit.backend.reto.dto;

import java.time.LocalDate;

/**
 * DTO con detalles de un reto incluyendo estado del usuario.
 */
public class RetoResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private String tipo;
    private int metaValor;
    private int duracionDias;
    private int puntos;
    private String icono;
    private Long usuarioRetoId;
    private Integer progreso;
    private boolean aceptado;
    private boolean completado;
    private LocalDate fechaFin;

    public RetoResponse() {}

    public RetoResponse(Long id, String titulo, String descripcion, String tipo, int metaValor, int duracionDias, int puntos, String icono, Long usuarioRetoId, Integer progreso, boolean aceptado, boolean completado, LocalDate fechaFin) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.metaValor = metaValor;
        this.duracionDias = duracionDias;
        this.puntos = puntos;
        this.icono = icono;
        this.usuarioRetoId = usuarioRetoId;
        this.progreso = progreso;
        this.aceptado = aceptado;
        this.completado = completado;
        this.fechaFin = fechaFin;
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

    public Long getUsuarioRetoId() { return usuarioRetoId; }
    public void setUsuarioRetoId(Long usuarioRetoId) { this.usuarioRetoId = usuarioRetoId; }

    public Integer getProgreso() { return progreso; }
    public void setProgreso(Integer progreso) { this.progreso = progreso; }

    public boolean isAceptado() { return aceptado; }
    public void setAceptado(boolean aceptado) { this.aceptado = aceptado; }

    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
}
