package com.nutrifit.backend.pesohistorial.service;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión del historial de peso del usuario.
 */
public interface PesoHistorialService {

    /**
     * Obtiene el historial de peso de un usuario.
     *
     * @param usuarioId identificador del usuario
     * @param limit número máximo de registros a devolver
     * @return lista de registros ordenados por fecha
     */
    List<PesoHistorialResponse> findByUsuario(Long usuarioId, int limit);

    /**
     * Crea o actualiza el registro de peso para una fecha específica.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha del registro
     * @param pesoKg peso en kilogramos
     * @return registro guardado o actualizado
     */
    PesoHistorialResponse upsert(Long usuarioId, LocalDate fecha, double pesoKg);

    /**
     * Elimina el registro de peso de una fecha específica.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha a eliminar
     */
    void deleteByUsuarioAndFecha(Long usuarioId, LocalDate fecha);
}
