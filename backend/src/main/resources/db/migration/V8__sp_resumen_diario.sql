-- flyway:splitStatements=false
-- Stored procedure sp_resumen_diario
--
-- Motivación: encapsular la lógica de agregación nutricional diaria en la base
-- de datos para (a) poder reutilizarla desde múltiples clientes sin duplicar SQL,
-- (b) facilitar el análisis de rendimiento de la query directamente en el motor
-- mediante EXPLAIN en el cuerpo del SP, y (c) servir como punto de comparación
-- A/B frente a la implementación JDBC inline (JdbcResumenDiarioRepository) antes
-- de decidir cuál promover como implementación activa.
--
-- Nota Flyway: se usa "flyway:splitStatements=false" para que Flyway no parta
-- el archivo en cada punto y coma interior del cuerpo del procedimiento.
-- No se necesita la directiva DELIMITER propia del cliente mysql/mariadb.

DROP PROCEDURE IF EXISTS sp_resumen_diario;

CREATE PROCEDURE sp_resumen_diario(
    IN p_usuario_id BIGINT,
    IN p_fecha      DATE
)
BEGIN
    SELECT
        c.usuario_id,
        c.fecha,
        COALESCE(ROUND(SUM((a.kcal_por_100g  * ca.gramos) / 100), 2), 0) AS kcal_totales,
        COALESCE(ROUND(SUM((a.proteinas_g    * ca.gramos) / 100), 2), 0) AS proteinas_totales,
        COALESCE(ROUND(SUM((a.grasas_g       * ca.gramos) / 100), 2), 0) AS grasas_totales,
        COALESCE(ROUND(SUM((a.carbos_g       * ca.gramos) / 100), 2), 0) AS carbos_totales
    FROM comidas c
    LEFT JOIN comida_alimentos ca ON ca.comida_id  = c.id
    LEFT JOIN alimentos        a  ON a.id          = ca.alimento_id
    WHERE c.usuario_id = p_usuario_id
      AND c.fecha      = p_fecha
    GROUP BY c.usuario_id, c.fecha;
END;
