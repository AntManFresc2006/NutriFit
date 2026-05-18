package com.nutrifit.backend.listacompra.service;

import com.nutrifit.backend.listacompra.dto.ListaCompraItemResponse;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemRequest;
import com.nutrifit.backend.listacompra.dto.SugerenciasResponse;

import java.util.Map;
import java.util.List;

/**
 * Servicio para gestionar la lista de compra personal del usuario.
 */
public interface ListaCompraService {
    /**
     * Obtiene todos los artículos del usuario agrupados por categoría.
     * @param usuarioId ID del usuario.
     * @return Mapa con categorías como claves y listas de artículos como valores.
     */
    Map<String, List<ListaCompraItemResponse>> getItems(Long usuarioId);

    /**
     * Añade un nuevo artículo a la lista de compra.
     * @param usuarioId ID del usuario.
     * @param req Datos del nuevo artículo.
     * @return Artículo creado con ID generado.
     */
    ListaCompraItemResponse addItem(Long usuarioId, ListaCompraItemRequest req);

    /**
     * Cambia el estado de completado de un artículo.
     * @param usuarioId ID del usuario.
     * @param itemId ID del artículo.
     * @return Artículo actualizado.
     */
    ListaCompraItemResponse toggle(Long usuarioId, Long itemId);

    /**
     * Elimina un artículo de la lista.
     * @param usuarioId ID del usuario.
     * @param itemId ID del artículo.
     * @return true si se eliminó correctamente.
     */
    boolean delete(Long usuarioId, Long itemId);

    /**
     * Elimina todos los artículos completados del usuario.
     * @param usuarioId ID del usuario.
     */
    void clearCompletados(Long usuarioId);

    /**
     * Obtiene sugerencias de alimentos basadas en el historial de comidas.
     * @param usuarioId ID del usuario.
     * @return Respuesta con lista de sugerencias.
     */
    SugerenciasResponse getSugerencias(Long usuarioId);
}
