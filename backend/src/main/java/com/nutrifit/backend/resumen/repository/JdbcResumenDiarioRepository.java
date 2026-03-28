package com.nutrifit.backend.resumen.repository;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Agrega en una sola query SQL la ingesta nutricional y el gasto calórico del día.
 *
 * <p>Se usa {@code jdbcTemplate.query} con un {@code ResultSetExtractor} en lugar
 * de {@code queryForObject} porque la query puede devolver cero filas (día sin comidas)
 * y en ese caso hay que retornar un resumen con todos los valores a cero, no lanzar
 * una excepción de "no rows".</p>
 */
@Repository
public class JdbcResumenDiarioRepository implements ResumenDiarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcResumenDiarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha) {
        /*
         * Estructura de la query:
         *
         *  - La tabla principal es `comidas` (una fila por comida del día).
         *  - LEFT JOIN a `comida_alimentos` y `alimentos` para calcular los macros
         *    proporcionales a los gramos registrados: (kcal_por_100g * gramos) / 100.
         *    LEFT JOIN en lugar de INNER porque una comida puede existir sin alimentos.
         *  - Subconsulta sobre `ejercicios_registro` que suma las kcal quemadas del día.
         *    Se precalcula como subquery y se une al resultado principal para evitar
         *    un producto cartesiano si hubiera varios registros de ejercicio.
         *  - COALESCE(..., 0) para que días sin datos devuelvan cero en lugar de NULL.
         *  - El `usuarioId` y `fecha` aparecen dos veces: una en la subconsulta de
         *    ejercicios y otra en el WHERE principal; por eso se pasan 4 parámetros.
         */
        String sql = """
                SELECT
                    c.usuario_id,
                    c.fecha,
                    COALESCE(ROUND(SUM((a.kcal_por_100g * ca.gramos) / 100), 2), 0) AS kcal_totales,
                    COALESCE(ROUND(SUM((a.proteinas_g * ca.gramos) / 100), 2), 0) AS proteinas_totales,
                    COALESCE(ROUND(SUM((a.grasas_g * ca.gramos) / 100), 2), 0) AS grasas_totales,
                    COALESCE(ROUND(SUM((a.carbos_g * ca.gramos) / 100), 2), 0) AS carbos_totales,
                    COALESCE(er.kcal_ejercicio, 0) AS kcal_quemadas_totales
                FROM comidas c
                LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                LEFT JOIN alimentos a ON a.id = ca.alimento_id
                LEFT JOIN (
                    SELECT usuario_id, fecha, COALESCE(ROUND(SUM(kcal_quemadas), 2), 0) AS kcal_ejercicio
                    FROM ejercicios_registro
                    WHERE usuario_id = ? AND fecha = ?
                    GROUP BY usuario_id, fecha
                ) er ON er.usuario_id = c.usuario_id AND er.fecha = c.fecha
                WHERE c.usuario_id = ? AND c.fecha = ?
                GROUP BY c.usuario_id, c.fecha, er.kcal_ejercicio
                """;

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return new ResumenDiarioResponse(
                        rs.getLong("usuario_id"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getDouble("kcal_totales"),
                        rs.getDouble("proteinas_totales"),
                        rs.getDouble("grasas_totales"),
                        rs.getDouble("carbos_totales"),
                        rs.getDouble("kcal_quemadas_totales")
                );
            }

            // Sin comidas registradas ese día: devolver ceros en lugar de 404
            return new ResumenDiarioResponse(usuarioId, fecha, 0, 0, 0, 0, 0);
        }, usuarioId, fecha, usuarioId, fecha);
    }
}
