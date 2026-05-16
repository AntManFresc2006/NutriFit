CREATE TABLE peso_historial (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha       DATE NOT NULL,
    peso_kg     NUMERIC(5,2) NOT NULL,
    UNIQUE (usuario_id, fecha)
);

CREATE INDEX idx_peso_historial_usuario_fecha ON peso_historial(usuario_id, fecha DESC);
