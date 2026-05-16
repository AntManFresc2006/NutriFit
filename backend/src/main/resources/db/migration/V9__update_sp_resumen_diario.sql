-- Función PostgreSQL: sp_resumen_diario (v2, activa)
--
-- Añade kcal_quemadas_totales y corrige el caso de días con ejercicio
-- pero sin comidas registradas usando dos subqueries de agregación
-- unidas con CROSS JOIN, igual que JdbcResumenDiarioRepository.

DROP FUNCTION IF EXISTS sp_resumen_diario(BIGINT, DATE);

CREATE OR REPLACE FUNCTION sp_resumen_diario(p_usuario_id BIGINT, p_fecha DATE)
RETURNS TABLE (
    usuario_id            BIGINT,
    fecha                 DATE,
    kcal_totales          NUMERIC,
    proteinas_totales     NUMERIC,
    grasas_totales        NUMERIC,
    carbos_totales        NUMERIC,
    kcal_quemadas_totales NUMERIC
)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT
        p_usuario_id::BIGINT                                              AS usuario_id,
        p_fecha                                                           AS fecha,
        COALESCE(ROUND(i.kcal_totales::NUMERIC,          2), 0)          AS kcal_totales,
        COALESCE(ROUND(i.proteinas_totales::NUMERIC,     2), 0)          AS proteinas_totales,
        COALESCE(ROUND(i.grasas_totales::NUMERIC,        2), 0)          AS grasas_totales,
        COALESCE(ROUND(i.carbos_totales::NUMERIC,        2), 0)          AS carbos_totales,
        COALESCE(ROUND(e.kcal_quemadas_totales::NUMERIC, 2), 0)          AS kcal_quemadas_totales
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
    CROSS JOIN (
        SELECT COALESCE(SUM(kcal_quemadas), 0) AS kcal_quemadas_totales
        FROM ejercicios_registro
        WHERE usuario_id = p_usuario_id AND fecha = p_fecha
    ) e;
END;
$$;
