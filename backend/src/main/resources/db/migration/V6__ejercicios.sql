CREATE TABLE ejercicios (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre     VARCHAR(150) NOT NULL,
    met        DECIMAL(4,2) NOT NULL,
    categoria  VARCHAR(50)  NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ejercicios_nombre (nombre)
);

CREATE TABLE ejercicios_registro (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id     BIGINT       NOT NULL,
    ejercicio_id   BIGINT       NOT NULL,
    fecha          DATE         NOT NULL,
    duracion_min   SMALLINT     NOT NULL,
    kcal_quemadas  DECIMAL(7,2) NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ej_reg_usuario
        FOREIGN KEY (usuario_id)   REFERENCES usuarios(id)   ON DELETE CASCADE,
    CONSTRAINT fk_ej_reg_ejercicio
        FOREIGN KEY (ejercicio_id) REFERENCES ejercicios(id) ON DELETE RESTRICT,
    KEY idx_ej_reg_usuario_fecha (usuario_id, fecha)
);
