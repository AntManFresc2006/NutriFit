-- Índices para columnas de búsqueda frecuente
-- Sin ellos, cada petición autenticada y cada consulta de comidas/ejercicios
-- hace un full table scan sobre tablas que crecen con el uso.

CREATE INDEX idx_sesiones_token
    ON sesiones (token);

CREATE INDEX idx_comidas_usuario_fecha
    ON comidas (usuario_id, fecha);

CREATE INDEX idx_ejercicios_registro_usuario_fecha
    ON ejercicios_registro (usuario_id, fecha);
