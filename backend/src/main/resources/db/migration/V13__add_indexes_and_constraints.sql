-- Part 1: Indexes on foreign key columns
-- V10 already added composite indexes on (usuario_id, fecha) for comidas and ejercicios_registro
-- Here we add missing single-column FK indexes for JOIN performance

CREATE INDEX IF NOT EXISTS idx_sesiones_usuario_id ON sesiones(usuario_id);
CREATE INDEX IF NOT EXISTS idx_comida_alimentos_comida_id ON comida_alimentos(comida_id);
CREATE INDEX IF NOT EXISTS idx_comida_alimentos_alimento_id ON comida_alimentos(alimento_id);
CREATE INDEX IF NOT EXISTS idx_ejercicios_registro_ejercicio_id ON ejercicios_registro(ejercicio_id);

-- Part 2: CHECK constraints on numeric fields
-- Using DO block pattern because PostgreSQL doesn't support IF NOT EXISTS for CHECK constraints

DO $$ BEGIN
  ALTER TABLE usuarios ADD CONSTRAINT chk_usuarios_altura_cm CHECK (altura_cm >= 50 AND altura_cm <= 300);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE usuarios ADD CONSTRAINT chk_usuarios_peso_kg_actual CHECK (peso_kg_actual >= 10 AND peso_kg_actual <= 600);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE usuarios ADD CONSTRAINT chk_usuarios_peso_objetivo CHECK (peso_objetivo IS NULL OR (peso_objetivo >= 10 AND peso_objetivo <= 600));
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE comida_alimentos ADD CONSTRAINT chk_comida_alimentos_gramos CHECK (gramos > 0);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE ejercicios_registro ADD CONSTRAINT chk_ejercicios_registro_duracion_min CHECK (duracion_min >= 1 AND duracion_min <= 1440);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE ejercicios_registro ADD CONSTRAINT chk_ejercicios_registro_kcal_quemadas CHECK (kcal_quemadas >= 0);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE ejercicios ADD CONSTRAINT chk_ejercicios_met CHECK (met > 0);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE alimentos ADD CONSTRAINT chk_alimentos_kcal_por_100g CHECK (kcal_por_100g >= 0);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE alimentos ADD CONSTRAINT chk_alimentos_proteinas_g CHECK (proteinas_g >= 0);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE alimentos ADD CONSTRAINT chk_alimentos_grasas_g CHECK (grasas_g >= 0);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE alimentos ADD CONSTRAINT chk_alimentos_carbos_g CHECK (carbos_g >= 0);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER TABLE peso_historial ADD CONSTRAINT chk_peso_historial_peso_kg CHECK (peso_kg >= 10 AND peso_kg <= 600);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- Part 3: Add created_at to peso_historial if it doesn't exist
ALTER TABLE peso_historial ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
