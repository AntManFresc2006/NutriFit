package com.nutrifit.backend.alimento.repository;

import com.nutrifit.backend.alimento.model.Alimento;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para la entidad Alimento.
 * Define las operaciones CRUD y de búsqueda necesarias para persistencia.
 */
public interface AlimentoRepository {

    /**
     * Recupera todos los alimentos almacenados.
     *
     * @return lista completa de alimentos
     */
    List<Alimento> findAll();

    /**
     * Busca alimentos por nombre.
     *
     * @param query texto a buscar
     * @return lista de alimentos coincidentes
     */
    List<Alimento> searchByNombre(String query);

    /**
     * Busca un alimento por su id.
     *
     * @param id identificador del alimento
     * @return Optional con el alimento si existe
     */
    Optional<Alimento> findById(Long id);

    /**
     * Guarda un nuevo alimento.
     *
     * @param alimento alimento a persistir
     * @return alimento guardado
     */
    Alimento save(Alimento alimento);

    /**
     * Actualiza un alimento existente.
     *
     * @param id identificador del alimento
     * @param alimento nuevos datos
     * @return alimento actualizado
     */
    Alimento update(Long id, Alimento alimento);

    /**
     * Elimina un alimento por su id.
     *
     * @param id identificador del alimento
     * @return true si la eliminación fue correcta
     */
    boolean deleteById(Long id);
}