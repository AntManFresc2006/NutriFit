CREATE TABLE ejercicios (
    id         BIGSERIAL    PRIMARY KEY,
    nombre     VARCHAR(150) NOT NULL,
    met        DECIMAL(4,2) NOT NULL,
    categoria  VARCHAR(50)  NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ejercicios_nombre ON ejercicios (nombre);

CREATE TABLE ejercicios_registro (
    id             BIGSERIAL    PRIMARY KEY,
    usuario_id     BIGINT       NOT NULL,
    ejercicio_id   BIGINT       NOT NULL,
    fecha          DATE         NOT NULL,
    duracion_min   SMALLINT     NOT NULL,
    kcal_quemadas  DECIMAL(7,2) NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ej_reg_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuarios(id)   ON DELETE CASCADE,
    CONSTRAINT fk_ej_reg_ejercicio FOREIGN KEY (ejercicio_id) REFERENCES ejercicios(id) ON DELETE RESTRICT
);
