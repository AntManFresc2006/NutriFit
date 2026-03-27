package com.nutrifit.backend.resumen.repository;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Implementación JDBC del resumen diario nutricional.
 */
@Repository
public class JdbcResumenDiarioRepository implements ResumenDiarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcResumenDiarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha) {
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

            return new ResumenDiarioResponse(usuarioId, fecha, 0, 0, 0, 0, 0);
        }, usuarioId, fecha, usuarioId, fecha);
    }
}
