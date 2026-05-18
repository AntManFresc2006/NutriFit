package com.nutrifit.backend.listacompra.repository;

import com.nutrifit.backend.listacompra.model.ListaCompraItem;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemRequest;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemResponse;

import java.util.List;

/**
 * Interfaz de repositorio para gestionar los artículos de la lista de compra.
 */
public interface ListaCompraRepository {
    /**
     * Obtiene todos los artículos de un usuario.
     * @param usuarioId ID del usuario.
     * @return Lista de artículos ordenados por categoría.
     */
    List<ListaCompraItem> findByUsuario(Long usuarioId);

    /**
     * Crea un nuevo artículo en la lista de compra.
     * @param usuarioId ID del usuario propietario.
     * @param req Datos del nuevo artículo.
     * @return Artículo creado con ID generado.
     */
    ListaCompraItemResponse save(Long usuarioId, ListaCompraItemRequest req);

    /**
     * Alterna el estado de completado de un artículo.
     * @param id ID del artículo.
     * @param usuarioId ID del usuario propietario (verificación).
     * @return Artículo actualizado o null si no existe.
     */
    ListaCompraItemResponse toggleCompletado(Long id, Long usuarioId);

    /**
     * Elimina un artículo de la lista de compra.
     * @param id ID del artículo.
     * @param usuarioId ID del usuario propietario (verificación).
     * @return true si se eliminó correctamente.
     */
    boolean deleteById(Long id, Long usuarioId);

    /**
     * Elimina todos los artículos completados de un usuario.
     * @param usuarioId ID del usuario.
     */
    void deleteCompletados(Long usuarioId);

    /**
     * Obtiene los alimentos más usados de un usuario en su historial de comidas.
     * @param usuarioId ID del usuario.
     * @param limit Cantidad máxima de alimentos a devolver.
     * @return Lista de nombres de alimentos más usados.
     */
    List<String> findAlimentosMasUsados(Long usuarioId, int limit);
}
