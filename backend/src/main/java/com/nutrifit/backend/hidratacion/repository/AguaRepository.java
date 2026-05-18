package com.nutrifit.backend.hidratacion.repository;

import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para registros de ingesta de agua.
 */
public interface AguaRepository {

    /**
     * Guarda un nuevo registro de ingesta de agua.
     *
     * @param usuarioId identificador del usuario
     * @param request datos de la ingesta (fecha y cantidad)
     * @return registro guardado con id asignado
     */
    AguaResponse save(Long usuarioId, AguaRequest request);

    /**
     * Obtiene todos los registros de agua de un usuario en una fecha.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha a consultar
     * @return lista de registros ordenados por hora
     */
    List<AguaResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);

    /**
     * Obtiene un registro de agua por su identificador.
     *
     * @param id identificador del registro
     * @return registro si existe
     */
    Optional<AguaResponse> findById(Long id);

    /**
     * Elimina un registro de agua.
     *
     * @param id identificador del registro a eliminar
     */
    void deleteById(Long id);
}
