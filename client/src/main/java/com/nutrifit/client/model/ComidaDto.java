package com.nutrifit.client.model;

/**
 * DTO plano para deserializar la respuesta JSON de comidas del backend.
 * La fecha llega como String ISO (ej. "2026-03-27") porque Spring Boot
 * serializa LocalDate sin timestamps por defecto.
 */
public class ComidaDto {

    private Long id;
    private Long usuarioId;
    private String fecha;
    private String tipo;

    public ComidaDto() {
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

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
