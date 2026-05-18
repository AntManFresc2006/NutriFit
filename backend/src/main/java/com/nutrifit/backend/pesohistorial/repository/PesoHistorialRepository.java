package com.nutrifit.backend.pesohistorial.repository;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio de acceso a datos para el historial de peso del usuario.
 */
public interface PesoHistorialRepository {

    /**
     * Obtiene el historial de peso de un usuario, limitado a los últimos N registros.
     *
     * @param usuarioId identificador del usuario
     * @param limit número máximo de registros a devolver
     * @return lista de registros ordenados por fecha descendente
     */
    List<PesoHistorialResponse> findByUsuario(Long usuarioId, int limit);

    /**
     * Crea o actualiza un registro de peso para una fecha específica.
     *
     * <p>Si ya existe un registro en esa fecha, se actualiza; si no, se crea uno nuevo.</p>
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha del registro
     * @param pesoKg peso en kilogramos
     * @return registro guardado o actualizado
     */
    PesoHistorialResponse upsert(Long usuarioId, LocalDate fecha, double pesoKg);

    /**
     * Elimina el registro de peso de un usuario en una fecha específica.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha a eliminar
     */
    void deleteByUsuarioAndFecha(Long usuarioId, LocalDate fecha);
}
