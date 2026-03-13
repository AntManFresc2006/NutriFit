-- Core tables for NutriFit

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    sexo ENUM('H','M') NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    altura_cm SMALLINT NOT NULL,
    peso_kg_actual DECIMAL(5,2) NOT NULL,
    peso_objetivo DECIMAL(5,2) NULL,
    nivel_actividad ENUM('SEDENTARIO','LIGERO','MODERADO','ALTO','MUY_ALTO') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuarios_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alimentos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    porcion_g DECIMAL(7,2) NULL,
    kcal_por_100g DECIMAL(7,2) NOT NULL,
    proteinas_g DECIMAL(7,2) NOT NULL,
    grasas_g DECIMAL(7,2) NOT NULL,
    carbos_g DECIMAL(7,2) NOT NULL,
    fuente VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_alimentos_nombre (nombre)
) ENGINE=InnoDB;