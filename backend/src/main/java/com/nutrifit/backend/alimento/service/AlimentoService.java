package com.nutrifit.backend.alimento.service;

import com.nutrifit.backend.alimento.dto.AlimentoRequest;
import com.nutrifit.backend.alimento.dto.AlimentoResponse;
import com.nutrifit.backend.alimento.dto.EscanearFotoResponse;

import java.util.List;

/**
 * Servicio para la lógica de negocio del módulo de alimentos.
 * Coordina las operaciones CRUD y búsquedas en la base de datos.
 */
public interface AlimentoService {

    /**
     * Obtiene todos los alimentos o filtra por nombre según la consulta.
     *
     * @param query texto opcional para filtrar alimentos por nombre
     * @return lista de alimentos en formato de respuesta
     */
    List<AlimentoResponse> findAll(String query);

    /**
     * Obtiene un alimento específico por su identificador.
     *
     * @param id identificador del alimento
     * @return alimento en formato de respuesta
     */
    AlimentoResponse findById(Long id);

    /**
     * Guarda un nuevo alimento en la base de datos.
     *
     * @param request datos del alimento a crear
     * @return alimento creado en formato de respuesta
     */
    AlimentoResponse save(AlimentoRequest request);

    /**
     * Actualiza un alimento existente.
     *
     * @param id identificador del alimento a actualizar
     * @param request nuevos datos del alimento
     * @return alimento actualizado en formato de respuesta
     */
    AlimentoResponse update(Long id, AlimentoRequest request);

    /**
     * Elimina un alimento por su identificador.
     *
     * @param id identificador del alimento a eliminar
     * @return true si la eliminación fue exitosa
     */
    boolean deleteById(Long id);

    /**
     * Analiza una foto de un alimento codificada en Base64 para extraer información nutricional.
     *
     * @param imagenBase64 imagen codificada en Base64
     * @param mimeType tipo MIME de la imagen
     * @return información nutricional extraída de la foto
     * @throws Exception si hay error en el procesamiento de la imagen
     */
    EscanearFotoResponse escanearFoto(String imagenBase64, String mimeType) throws Exception;
}