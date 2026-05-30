-- Trigger PostgreSQL: Limpieza automática de sesiones expiradas (v1)
--
-- Función que elimina sesiones cuya fecha de expiración ha pasado.
-- Se dispara automáticamente después de cada INSERT en la tabla sesiones,
-- evitando que se acumulen tokens expirados en la base de datos.

CREATE OR REPLACE FUNCTION fn_limpiar_sesiones_expiradas()
RETURNS void
LANGUAGE sql AS $$
  DELETE FROM sesiones WHERE expires_at < NOW();
$$;

-- Trigger que ejecuta la función de limpieza
-- Se dispara una vez por cada sentencia INSERT, no por fila,
-- para optimizar el rendimiento en inserciones en lote.

CREATE TRIGGER trg_limpiar_sesiones
AFTER INSERT ON sesiones
FOR EACH STATEMENT
EXECUTE FUNCTION fn_limpiar_sesiones_expiradas();
