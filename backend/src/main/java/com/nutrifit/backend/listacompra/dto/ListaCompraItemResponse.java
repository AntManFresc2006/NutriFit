package com.nutrifit.backend.listacompra.dto;

import java.time.LocalDateTime;

/**
 * DTO con datos de un artículo de la lista de compra.
 */
public class ListaCompraItemResponse {
    private Long id;
    private String nombre;
    private String cantidad;
    private String categoria;
    private boolean completado;
    private LocalDateTime createdAt;

    public ListaCompraItemResponse() {}

    public ListaCompraItemResponse(Long id, String nombre, String cantidad, String categoria, boolean completado, LocalDateTime createdAt) {
        this.id = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.completado = completado;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCantidad() { return cantidad; }
    public void setCantidad(String cantidad) { this.cantidad = cantidad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
