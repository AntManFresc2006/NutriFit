CREATE TABLE detective_analisis (
    id              BIGSERIAL    PRIMARY KEY,
    usuario_id      BIGINT       NOT NULL REFERENCES usuarios(id),
    dias_analizados INT          NOT NULL,
    estadisticas_json TEXT,
    hallazgos_json    TEXT,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'ANALIZANDO',
    analisis_ia     TEXT,
    error_msg       TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_detective_usuario UNIQUE (usuario_id)
);
