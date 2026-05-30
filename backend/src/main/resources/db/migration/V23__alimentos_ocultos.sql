CREATE TABLE alimentos_ocultos (
    usuario_id  BIGINT NOT NULL,
    alimento_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, alimento_id),
    FOREIGN KEY (usuario_id)  REFERENCES usuarios(id)  ON DELETE CASCADE,
    FOREIGN KEY (alimento_id) REFERENCES alimentos(id) ON DELETE CASCADE
);
