-- Función PostgreSQL: sp_resumen_diario (v1)
--
-- Equivalente PL/pgSQL de la versión original del stored procedure.
-- Calcula la ingesta nutricional diaria de un usuario.
-- La implementación activa en Java es JdbcResumenDiarioRepository;
-- esta función existe como alternativa demostrable.

CREATE OR REPLACE FUNCTION sp_resumen_diario(p_usuario_id BIGINT, p_fecha DATE)
RETURNS TABLE (
    usuario_id        BIGINT,
    fecha             DATE,
    kcal_totales      NUMERIC,
    proteinas_totales NUMERIC,
    grasas_totales    NUMERIC,
    carbos_totales    NUMERIC
)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT
        c.usuario_id,
        c.fecha,
        COALESCE(ROUND(SUM((a.kcal_por_100g * ca.gramos) / 100)::NUMERIC, 2), 0) AS kcal_totales,
        COALESCE(ROUND(SUM((a.proteinas_g   * ca.gramos) / 100)::NUMERIC, 2), 0) AS proteinas_totales,
        COALESCE(ROUND(SUM((a.grasas_g      * ca.gramos) / 100)::NUMERIC, 2), 0) AS grasas_totales,
        COALESCE(ROUND(SUM((a.carbos_g      * ca.gramos) / 100)::NUMERIC, 2), 0) AS carbos_totales
    FROM comidas c
    LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
    LEFT JOIN alimentos        a  ON a.id         = ca.alimento_id
    WHERE c.usuario_id = p_usuario_id
      AND c.fecha      = p_fecha
    GROUP BY c.usuario_id, c.fecha;
END;
$$;
