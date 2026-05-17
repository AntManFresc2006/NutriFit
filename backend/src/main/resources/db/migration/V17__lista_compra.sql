CREATE TABLE lista_compra (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre VARCHAR(200) NOT NULL,
    cantidad VARCHAR(50),
    categoria VARCHAR(50) NOT NULL DEFAULT 'OTROS',
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_lista_compra_categoria CHECK (
        categoria IN ('PROTEINAS', 'VERDURAS', 'FRUTAS', 'LACTEOS', 'CEREALES', 'BEBIDAS', 'OTROS')
    )
);

CREATE INDEX idx_lista_compra_usuario ON lista_compra(usuario_id);
CREATE INDEX idx_lista_compra_completado ON lista_compra(usuario_id, completado);
