package com.nutrifit.backend.comida.service;

import com.nutrifit.backend.comida.dto.ComidaAlimentoRequest;
import com.nutrifit.backend.comida.dto.ComidaItemDetalleResponse;
import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la lógica de negocio del módulo de comidas.
 * Coordina operaciones CRUD de comidas y sus relaciones con alimentos.
 */
public interface ComidaService {

    /**
     * Obtiene las comidas registradas por un usuario en un día específico.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha de las comidas buscadas
     * @return lista de comidas del día
     */
    List<ComidaResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);

    /**
     * Crea una nueva comida para un usuario.
     *
     * @param usuarioId identificador del usuario propietario
     * @param request datos de la comida a crear
     * @return comida creada con su id asignado
     */
    ComidaResponse save(Long usuarioId, ComidaRequest request);

    /**
     * Elimina una comida y todos sus items asociados.
     *
     * @param id identificador de la comida a eliminar
     */
    void deleteById(Long id);

    /**
     * Añade un alimento a una comida con la cantidad especificada.
     *
     * @param comidaId identificador de la comida
     * @param request datos del alimento y cantidad
     */
    void addAlimentoToComida(Long comidaId, ComidaAlimentoRequest request);

    /**
     * Obtiene los alimentos de una comida con sus macros calculados.
     *
     * @param comidaId identificador de la comida
     * @return lista de items con información nutricional estimada
     */
    List<ComidaItemDetalleResponse> findDetalleItemsByComidaId(Long comidaId);

    /**
     * Elimina un alimento específico de una comida.
     *
     * @param comidaId identificador de la comida
     * @param itemId identificador del item a eliminar
     */
    void deleteItem(Long comidaId, Long itemId);
}