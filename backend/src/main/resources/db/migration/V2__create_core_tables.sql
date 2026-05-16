-- Core tables for NutriFit

CREATE TABLE IF NOT EXISTS usuarios (
    id               BIGSERIAL    PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    sexo             VARCHAR(1)   NOT NULL CHECK (sexo IN ('H', 'M')),
    fecha_nacimiento DATE         NOT NULL,
    altura_cm        SMALLINT     NOT NULL,
    peso_kg_actual   DECIMAL(5,2) NOT NULL,
    peso_objetivo    DECIMAL(5,2) NULL,
    nivel_actividad  VARCHAR(20)  NOT NULL CHECK (nivel_actividad IN ('SEDENTARIO','LIGERO','MODERADO','ALTO','MUY_ALTO')),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS alimentos (
    id            BIGSERIAL    PRIMARY KEY,
    nombre        VARCHAR(150) NOT NULL,
    porcion_g     DECIMAL(7,2) NULL,
    kcal_por_100g DECIMAL(7,2) NOT NULL,
    proteinas_g   DECIMAL(7,2) NOT NULL,
    grasas_g      DECIMAL(7,2) NOT NULL,
    carbos_g      DECIMAL(7,2) NOT NULL,
    fuente        VARCHAR(100) NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alimentos_nombre ON alimentos (nombre);