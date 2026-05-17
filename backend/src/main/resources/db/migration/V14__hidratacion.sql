CREATE TABLE agua_registro (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    cantidad_ml INT NOT NULL,
    hora TIME NOT NULL DEFAULT CURRENT_TIME,
    CONSTRAINT chk_agua_cantidad CHECK (cantidad_ml > 0 AND cantidad_ml <= 5000)
);

CREATE INDEX idx_agua_registro_usuario_fecha ON agua_registro(usuario_id, fecha);
