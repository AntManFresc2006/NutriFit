-- Los ejercicios anaeróbicos no tienen duración; se registran con duracion_min = 0
ALTER TABLE ejercicios_registro DROP CONSTRAINT IF EXISTS chk_ejercicios_registro_duracion_min;
ALTER TABLE ejercicios_registro ADD CONSTRAINT chk_ejercicios_registro_duracion_min
    CHECK (duracion_min >= 0 AND duracion_min <= 1440);
