package com.nutrifit.backend.listacompra.model;

import java.time.LocalDateTime;

/**
 * Representa un artículo en la lista de compra de un usuario.
 */
public class ListaCompraItem {
    private Long id;
    private Long usuarioId;
    private String nombre;
    private String cantidad;
    private String categoria;
    private boolean completado;
    private LocalDateTime createdAt;

    public ListaCompraItem() {}

    public ListaCompraItem(Long id, Long usuarioId, String nombre, String cantidad, String categoria, boolean completado, LocalDateTime createdAt) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.completado = completado;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

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
