CREATE TABLE comidas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comidas_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE comida_alimentos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comida_id BIGINT NOT NULL,
    alimento_id BIGINT NOT NULL,
    gramos DECIMAL(7,2) NOT NULL,
    CONSTRAINT fk_comida_alimentos_comida
        FOREIGN KEY (comida_id) REFERENCES comidas(id) ON DELETE CASCADE,
    CONSTRAINT fk_comida_alimentos_alimento
        FOREIGN KEY (alimento_id) REFERENCES alimentos(id) ON DELETE RESTRICT
);