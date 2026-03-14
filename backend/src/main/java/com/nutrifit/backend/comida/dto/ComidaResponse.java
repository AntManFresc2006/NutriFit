package com.nutrifit.backend.comida.dto;

import java.time.LocalDate;

/**
 * DTO de salida para devolver comidas registradas.
 */
public class ComidaResponse {

    private Long id;
    private Long usuarioId;
    private LocalDate fecha;
    private String tipo;

    public ComidaResponse() {
    }

    public ComidaResponse(Long id, Long usuarioId, LocalDate fecha, String tipo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}