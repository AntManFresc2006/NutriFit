CREATE TABLE retos (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    meta_valor INT NOT NULL,
    duracion_dias INT NOT NULL DEFAULT 7,
    puntos INT NOT NULL DEFAULT 50,
    icono VARCHAR(10) NOT NULL DEFAULT '🏆'
);

CREATE TABLE usuario_retos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    reto_id BIGINT NOT NULL REFERENCES retos(id) ON DELETE CASCADE,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    progreso INT NOT NULL DEFAULT 0,
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_completado TIMESTAMP,
    UNIQUE(usuario_id, reto_id, fecha_inicio)
);

CREATE INDEX idx_usuario_retos_usuario ON usuario_retos(usuario_id);
CREATE INDEX idx_usuario_retos_completado ON usuario_retos(usuario_id, completado);

-- Retos predefinidos
INSERT INTO retos (titulo, descripcion, tipo, meta_valor, duracion_dias, puntos, icono) VALUES
('Semana proteica', 'Alcanza tu objetivo de proteínas 5 días de esta semana', 'PROTEINA', 5, 7, 100, '💪'),
('Balance perfecto', 'Mantén el balance calórico dentro de ±300 kcal 4 días seguidos', 'BALANCE', 4, 7, 75, '⚖️'),
('Semana activa', 'Registra ejercicio al menos 4 días esta semana', 'EJERCICIO', 4, 7, 100, '🏃'),
('Come con variedad', 'Come 4 o más alimentos distintos cada día durante 5 días', 'VARIEDAD', 5, 7, 75, '🥗'),
('Racha de fuego', 'Mantén una racha de registro de 7 días consecutivos', 'RACHA', 7, 7, 150, '🔥'),
('Hidratación total', 'Alcanza 2000 ml de agua diarios durante 5 días', 'HIDRATACION', 5, 7, 75, '💧'),
('Nutrición perfecta', 'Consigue NutriGrade A o B durante 3 días seguidos', 'NUTRISCORE', 3, 7, 200, '⭐');
