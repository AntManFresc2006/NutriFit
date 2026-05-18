package com.nutrifit.backend.listacompra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar un artículo en la lista de compra.
 */
public class ListaCompraItemRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    private String cantidad;

    @Pattern(regexp = "PROTEINAS|VERDURAS|FRUTAS|LACTEOS|CEREALES|BEBIDAS|OTROS",
             message = "Categoría inválida")
    private String categoria;

    public ListaCompraItemRequest() {}

    public ListaCompraItemRequest(String nombre, String cantidad, String categoria) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.categoria = categoria;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCantidad() { return cantidad; }
    public void setCantidad(String cantidad) { this.cantidad = cantidad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
