CREATE TABLE plan_semanal (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    semana_inicio DATE NOT NULL,
    plan_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(usuario_id, semana_inicio)
);

CREATE INDEX idx_plan_semanal_usuario ON plan_semanal(usuario_id);
