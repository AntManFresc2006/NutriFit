-- flyway:splitStatements=false
-- Actualización del stored procedure sp_resumen_diario
--
-- Motivo: la versión original (V8) solo calculaba la ingesta nutricional.
-- Esta versión añade el gasto calórico de ejercicios (kcal_quemadas_totales)
-- y corrige el caso de días con ejercicio pero sin comidas registradas:
-- la versión anterior usaba FROM comidas como tabla base, lo que hacía que
-- esos días devolvieran 0 en todos los campos incluyendo kcal_quemadas_totales.
--
-- Se usa el mismo patrón que JdbcResumenDiarioRepository: dos subqueries de
-- agregación sin GROUP BY (que en MySQL siempre devuelven exactamente una fila,
-- incluso sin datos) unidas con JOIN, garantizando siempre un resultado.

DROP PROCEDURE IF EXISTS sp_resumen_diario;

CREATE PROCEDURE sp_resumen_diario(
    IN p_usuario_id BIGINT,
    IN p_fecha      DATE
)
BEGIN
    SELECT
        p_usuario_id                                       AS usuario_id,
        p_fecha                                            AS fecha,
        COALESCE(ROUND(i.kcal_totales,      2), 0)        AS kcal_totales,
        COALESCE(ROUND(i.proteinas_totales, 2), 0)        AS proteinas_totales,
        COALESCE(ROUND(i.grasas_totales,    2), 0)        AS grasas_totales,
        COALESCE(ROUND(i.carbos_totales,    2), 0)        AS carbos_totales,
        COALESCE(ROUND(e.kcal_quemadas_totales, 2), 0)    AS kcal_quemadas_totales
    FROM (
        SELECT
            SUM((a.kcal_por_100g * ca.gramos) / 100) AS kcal_totales,
            SUM((a.proteinas_g   * ca.gramos) / 100) AS proteinas_totales,
            SUM((a.grasas_g      * ca.gramos) / 100) AS grasas_totales,
            SUM((a.carbos_g      * ca.gramos) / 100) AS carbos_totales
        FROM comidas c
        LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
        LEFT JOIN alimentos        a  ON a.id         = ca.alimento_id
        WHERE c.usuario_id = p_usuario_id AND c.fecha = p_fecha
    ) i
    JOIN (
        SELECT SUM(kcal_quemadas) AS kcal_quemadas_totales
        FROM ejercicios_registro
        WHERE usuario_id = p_usuario_id AND fecha = p_fecha
    ) e ON 1=1;
END;
