package com.nutrifit.backend.comida.repository;

import com.nutrifit.backend.comida.dto.ComidaItemDetalleResponse;
import com.nutrifit.backend.comida.model.Comida;
import com.nutrifit.backend.comida.model.ComidaAlimento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para comidas.
 * Define las operaciones de persistencia de comidas y sus relaciones con alimentos.
 */
public interface ComidaRepository {

    /**
     * Obtiene las comidas de un usuario para un día específico.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha de las comidas buscadas
     * @return lista de comidas del día especificado
     */
    List<Comida> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);

    /**
     * Busca una comida por su identificador.
     *
     * @param id identificador de la comida
     * @return Optional con la comida si existe
     */
    Optional<Comida> findById(Long id);

    /**
     * Guarda una nueva comida en la base de datos.
     *
     * @param comida comida a persistir
     * @return comida guardada con su id asignado
     */
    Comida save(Comida comida);

    /**
     * Elimina una comida por su identificador.
     *
     * @param id identificador de la comida a eliminar
     * @return true si la eliminación fue exitosa
     */
    boolean deleteById(Long id);

    /**
     * Añade un alimento a una comida con la cantidad especificada en gramos.
     *
     * @param comidaId identificador de la comida
     * @param alimentoId identificador del alimento
     * @param gramos cantidad consumida en gramos
     */
    void addAlimentoToComida(Long comidaId, Long alimentoId, double gramos);

    /**
     * Obtiene los items de una comida con sus macros calculados según los gramos.
     *
     * @param comidaId identificador de la comida
     * @return lista de items con información nutricional estimada
     */
    List<ComidaItemDetalleResponse> findDetalleItemsByComidaId(Long comidaId);

    /**
     * Busca un item de comida-alimento por su identificador.
     *
     * @param itemId identificador del item
     * @return Optional con el item si existe
     */
    Optional<ComidaAlimento> findItemById(Long itemId);

    /**
     * Elimina un item de comida-alimento.
     *
     * @param itemId identificador del item a eliminar
     * @return true si la eliminación fue exitosa
     */
    boolean deleteItemById(Long itemId);
}