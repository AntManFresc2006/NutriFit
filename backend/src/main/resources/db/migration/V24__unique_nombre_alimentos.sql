-- Reasignar referencias en comida_alimentos al duplicado superviviente (min id)
UPDATE comida_alimentos ca
SET alimento_id = survivor.min_id
FROM (
    SELECT MIN(id) AS min_id, LOWER(nombre) AS nombre_lower
    FROM alimentos
    GROUP BY LOWER(nombre)
    HAVING COUNT(*) > 1
) survivor
JOIN alimentos dup ON LOWER(dup.nombre) = survivor.nombre_lower AND dup.id != survivor.min_id
WHERE ca.alimento_id = dup.id
  AND NOT EXISTS (
      SELECT 1 FROM comida_alimentos ca2
      WHERE ca2.comida_id = ca.comida_id AND ca2.alimento_id = survivor.min_id
  );

-- Eliminar filas de comida_alimentos que apuntan a duplicados no reasignables (conflicto de unicidad)
DELETE FROM comida_alimentos ca
USING (
    SELECT MIN(id) AS min_id, LOWER(nombre) AS nombre_lower
    FROM alimentos
    GROUP BY LOWER(nombre)
    HAVING COUNT(*) > 1
) survivor
JOIN alimentos dup ON LOWER(dup.nombre) = survivor.nombre_lower AND dup.id != survivor.min_id
WHERE ca.alimento_id = dup.id;

-- Eliminar alimentos duplicados conservando el de menor id
DELETE FROM alimentos
WHERE id NOT IN (
    SELECT MIN(id)
    FROM alimentos
    GROUP BY LOWER(nombre)
);

CREATE UNIQUE INDEX uq_alimentos_nombre_lower ON alimentos (LOWER(nombre));
