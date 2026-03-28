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
         *  - Dos subqueries de agregación independientes sin GROUP BY, una para la
         *    ingesta (comidas + alimentos) y otra para el gasto (ejercicios_registro).
         *  - En MySQL, SELECT SUM(...) sin GROUP BY siempre devuelve EXACTAMENTE UNA
         *    fila aunque no haya datos coincidentes: devuelve NULL, no cero filas.
         *    El JOIN de dos subqueries de una fila produce siempre una fila.
         *  - Esto corrige el caso en que un día tiene ejercicio pero ninguna comida:
         *    la query anterior usaba FROM comidas como tabla base, por lo que esos
         *    días devolvían cero en todos los campos incluyendo kcal_quemadas_totales.
         *  - COALESCE(..., 0) convierte los NULL de días sin datos en 0.
         *  - usuarioId y fecha se pasan dos veces (una por cada subquery).
         */
        String sql = """
                SELECT
                    COALESCE(ROUND(i.kcal_totales,      2), 0) AS kcal_totales,
                    COALESCE(ROUND(i.proteinas_totales, 2), 0) AS proteinas_totales,
                    COALESCE(ROUND(i.grasas_totales,    2), 0) AS grasas_totales,
                    COALESCE(ROUND(i.carbos_totales,    2), 0) AS carbos_totales,
                    COALESCE(ROUND(e.kcal_quemadas_totales, 2), 0) AS kcal_quemadas_totales
                FROM (
                    SELECT
                        SUM((a.kcal_por_100g * ca.gramos) / 100) AS kcal_totales,
                        SUM((a.proteinas_g   * ca.gramos) / 100) AS proteinas_totales,
                        SUM((a.grasas_g      * ca.gramos) / 100) AS grasas_totales,
                        SUM((a.carbos_g      * ca.gramos) / 100) AS carbos_totales
                    FROM comidas c
                    LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                    LEFT JOIN alimentos        a  ON a.id         = ca.alimento_id
                    WHERE c.usuario_id = ? AND c.fecha = ?
                ) i
                JOIN (
                    SELECT SUM(kcal_quemadas) AS kcal_quemadas_totales
                    FROM ejercicios_registro
                    WHERE usuario_id = ? AND fecha = ?
                ) e ON 1=1
                """;

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return new ResumenDiarioResponse(
                        usuarioId,
                        fecha,
                        rs.getDouble("kcal_totales"),
                        rs.getDouble("proteinas_totales"),
                        rs.getDouble("grasas_totales"),
                        rs.getDouble("carbos_totales"),
                        rs.getDouble("kcal_quemadas_totales")
                );
            }
            // Rama defensiva: no debería alcanzarse con la query actual,
            // que siempre devuelve una fila.
            return new ResumenDiarioResponse(usuarioId, fecha, 0, 0, 0, 0, 0);
        }, usuarioId, fecha, usuarioId, fecha);
    }
}
