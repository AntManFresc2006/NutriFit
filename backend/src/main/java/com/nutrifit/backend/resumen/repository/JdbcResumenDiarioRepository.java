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
                    COALESCE(ROUND(SUM((a.carbos_g * ca.gramos) / 100), 2), 0) AS carbos_totales
                FROM comidas c
                LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                LEFT JOIN alimentos a ON a.id = ca.alimento_id
                WHERE c.usuario_id = ? AND c.fecha = ?
                GROUP BY c.usuario_id, c.fecha
                """;

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return new ResumenDiarioResponse(
                        rs.getLong("usuario_id"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getDouble("kcal_totales"),
                        rs.getDouble("proteinas_totales"),
                        rs.getDouble("grasas_totales"),
                        rs.getDouble("carbos_totales")
                );
            }

            return new ResumenDiarioResponse(usuarioId, fecha, 0, 0, 0, 0);
        }, usuarioId, fecha);
    }
}
