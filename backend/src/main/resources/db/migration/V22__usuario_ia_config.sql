CREATE TABLE usuario_ia_config (
    usuario_id   BIGINT PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    proxy_url    VARCHAR(255) NOT NULL,
    model        VARCHAR(255) NOT NULL,
    api_key      VARCHAR(512) NOT NULL,
    created_at   TIMESTAMP DEFAULT NOW(),
    updated_at   TIMESTAMP DEFAULT NOW()
);
