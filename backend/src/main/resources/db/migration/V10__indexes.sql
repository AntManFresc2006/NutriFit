-- Índices para columnas de búsqueda frecuente
-- Sin ellos, cada petición autenticada y cada consulta de comidas/ejercicios
-- hace un full table scan sobre tablas que crecen con el uso.

CREATE INDEX IF NOT EXISTS idx_sesiones_token
    ON sesiones (token);

CREATE INDEX IF NOT EXISTS idx_comidas_usuario_fecha
    ON comidas (usuario_id, fecha);

CREATE INDEX IF NOT EXISTS idx_ejercicios_registro_usuario_fecha
    ON ejercicios_registro (usuario_id, fecha);
