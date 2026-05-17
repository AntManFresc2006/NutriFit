# Notas de base de datos - ejercicios y gasto energético

## Objetivo
El módulo de ejercicios permite registrar actividades físicas realizadas por un usuario, calcular el gasto energético total mediante METs (Metabolic Equivalent of Task) y contribuir al balance calórico neto del día.

## Tablas relacionadas

### `ejercicios` (catálogo)
Tabla de referencia con ejercicios predefinidos.

Campos principales:
- `id`: identificador único
- `nombre`: nombre del ejercicio ("Correr", "Ciclismo", "Natación", etc.)
- `intensidad`: categoría de intensidad (`BAJA`, `MODERADA`, `ALTA`, `MUY_ALTA`)
- `met_base`: valor MET base del ejercicio (ej. correr a 10 km/h = 9.8 METs)
- `descripcion`: detalles opcionales

Ejemplo de datos:

| id | nombre | intensidad | met_base | descripcion |
|----|--------|-----------|----------|-------------|
| 1 | Caminar | BAJA | 3.5 | A 5 km/h en terreno plano |
| 2 | Correr | MODERADA | 9.8 | A 10 km/h |
| 3 | Ciclismo | MODERADA | 7.5 | A ritmo moderado, 16-19 km/h |
| 4 | Natación | ALTA | 8.0 | Nado de ritmo moderado |
| 5 | HIIT | MUY_ALTA | 12.0 | Entrenamiento de alta intensidad |

---

### `ejercicios_registro` (registro de usuario)
Tabla que registra cada sesión de ejercicio realizada por un usuario.

Campos principales:
- `id`: identificador único
- `usuario_id`: usuario que realiza el ejercicio
- `ejercicio_id`: referencia al ejercicio en tabla `ejercicios`
- `fecha`: fecha en que se realiza el ejercicio
- `minutos`: duración en minutos
- `gasto_calorico_estimado`: cálculo derivado (no se almacena, se calcula)
- `created_at`: timestamp de creación del registro

Relaciones:
- `ejercicios_registro` N:1 `usuarios`
- `ejercicios_registro` N:1 `ejercicios`

Ejemplo:

| id | usuario_id | ejercicio_id | fecha | minutos | created_at |
|----|-----------|--------------|-------|---------|------------|
| 1 | 5 | 2 | 2026-05-17 | 30 | 2026-05-17T08:00:00 |
| 2 | 5 | 3 | 2026-05-17 | 45 | 2026-05-17T17:30:00 |

---

## Cálculo de gasto energético (METs)

### Fórmula base

```
Gasto energético = (MET × peso corporal en kg × duración en horas)
```

Ejemplo:
- Ejercicio: Correr (MET = 9.8)
- Usuario: 75 kg
- Duración: 30 minutos (0.5 horas)
- Gasto: 9.8 × 75 × 0.5 = **367.5 kcal**

### Fórmula en SQL

La mayoría de cálculos se realizan en la consulta de resumen diario:

```sql
SELECT
    er.usuario_id,
    er.fecha,
    COALESCE(ROUND(SUM((e.met_base * u.peso_actual_kg * (er.minutos::FLOAT / 60))), 2), 0) 
        AS gasto_ejercicio_kcal
FROM ejercicios_registro er
JOIN ejercicios e ON e.id = er.ejercicio_id
JOIN usuarios u ON u.id = er.usuario_id
WHERE er.usuario_id = ? AND er.fecha = ?
GROUP BY er.usuario_id, er.fecha;
```

---

## Intensidad y ajustes de MET

La tabla `ejercicios` proporciona valores MET base, pero la intensidad del usuario puede modificar este valor:

| Intensidad | Multiplicador | Ejemplo |
|-----------|--------------|---------|
| BAJA | 0.8–0.9 | Cardio ligero, senderismo lento |
| MODERADA | 1.0–1.2 | Correr a ritmo conversacional |
| ALTA | 1.3–1.5 | Correr competición, spinning intenso |
| MUY_ALTA | 1.6–2.0+ | HIIT, crossfit, sprints |

**Implementación**:

Si el usuario reporta "Correr MUY_ALTA" en lugar de "MODERADA":
- Base MET (correr): 9.8
- Multiplicador intensidad: 1.5
- MET ajustado: 9.8 × 1.5 = 14.7

---

## Integración con resumen diario

El resumen diario de cada usuario incorpora:

```json
{
  "fecha": "2026-05-17",
  "usuario_id": 5,
  "nutricion": {
    "kcal_consumidas": 2100,
    "proteinas_g": 80,
    "grasas_g": 65,
    "carbos_g": 280
  },
  "ejercicio": {
    "gasto_calorico_kcal": 450,
    "ejercicios_realizados": 2,
    "duracion_total_minutos": 75
  },
  "balance": {
    "kcal_neto": 1650,  // 2100 - 450
    "respecto_tdee": "deficit",  // Si TDEE = 2000
    "recomendacion": "Balance ligero negativo, perfecto para déficit controlado"
  }
}
```

---

## Consultas típicas del módulo

### 1. Registrar un ejercicio

```java
// EjerciciosRegistroRepository.java
public void registrarEjercicio(long usuarioId, long ejercicioId, 
                               LocalDate fecha, int minutos) {
    String sql = "INSERT INTO ejercicios_registro " +
                 "(usuario_id, ejercicio_id, fecha, minutos, created_at) " +
                 "VALUES (?, ?, ?, ?, NOW())";
    
    jdbcTemplate.update(sql, usuarioId, ejercicioId, fecha, minutos);
}
```

### 2. Obtener ejercicios de un usuario en una fecha

```java
// EjerciciosRegistroRepository.java
public List<EjercicioRegistro> obtenerPorUsuarioYFecha(long usuarioId, LocalDate fecha) {
    String sql = "SELECT er.*, e.nombre, e.met_base " +
                 "FROM ejercicios_registro er " +
                 "JOIN ejercicios e ON e.id = er.ejercicio_id " +
                 "WHERE er.usuario_id = ? AND er.fecha = ? " +
                 "ORDER BY er.created_at DESC";
    
    return jdbcTemplate.query(sql, ejercicioRowMapper, usuarioId, fecha);
}
```

### 3. Calcular gasto total de un usuario en una fecha

```java
// EjerciciosRegistroRepository.java
public double calcularGastoTotal(long usuarioId, LocalDate fecha) {
    String sql = "SELECT COALESCE(ROUND(SUM(e.met_base * u.peso_actual_kg * " +
                 "(er.minutos::FLOAT / 60)), 2), 0) " +
                 "FROM ejercicios_registro er " +
                 "JOIN ejercicios e ON e.id = er.ejercicio_id " +
                 "JOIN usuarios u ON u.id = er.usuario_id " +
                 "WHERE er.usuario_id = ? AND er.fecha = ?";
    
    return jdbcTemplate.queryForObject(sql, Double.class, usuarioId, fecha);
}
```

### 4. Listar catálogo de ejercicios

```java
// EjerciciosRepository.java
public List<Ejercicio> listarTodos() {
    String sql = "SELECT * FROM ejercicios ORDER BY intensidad, nombre";
    return jdbcTemplate.query(sql, ejercicioRowMapper);
}
```

---

## Validación y reglas de negocio

### Validaciones en aplicación

- `minutos > 0` (obligatorio registrar al menos 1 minuto)
- `ejercicio_id` debe existir en tabla `ejercicios`
- `usuario_id` debe existir en tabla `usuarios`
- `fecha` no puede ser futura (registros históricos solamente)
- `usuario_id` en request debe coincidir con usuario autenticado (no registrar para otros)

### Consideraciones de cálculo

1. **Peso corporal**: Se toma de `usuarios.peso_actual_kg` en el momento del resumen
   - Si el usuario cambia de peso, el histórico se recalcula con el peso actual
   - Alternativa futura: almacenar peso al momento del ejercicio

2. **Factores no incluidos**:
   - Edad (afecta a metabolismo)
   - Sexo (afecta a metabolismo)
   - Condición física
   - Temperatura ambiental
   - Altitud

   Estas mejoras pueden implementarse en versión futura.

3. **METs son estimaciones**:
   - Los valores MET son promedios poblacionales
   - La variación individual puede ser ±20%
   - Para cálculos precisos, se requiere prueba de laboratorio

---

## Endpoints del módulo

```http
# Registrar un ejercicio
POST /api/ejercicios-registro
{
  "ejercicioId": 2,
  "fecha": "2026-05-17",
  "minutos": 30
}

# Obtener ejercicios de un usuario en una fecha
GET /api/ejercicios-registro?usuarioId=5&fecha=2026-05-17

# Obtener catálogo de ejercicios
GET /api/ejercicios

# Obtener detalles de un ejercicio
GET /api/ejercicios/{id}

# Calcular gasto total (incluido en resumen diario)
GET /api/resumen-diario?usuarioId=5&fecha=2026-05-17
```

---

## Estado actual

✅ Tabla `ejercicios` con catálogo de ~30 ejercicios predefinidos
✅ Tabla `ejercicios_registro` para registro de usuario
✅ Cálculo de METs en SQL
✅ Integración con resumen diario
✅ Balance calórico neto (consumo - gasto)
✅ Endpoints REST implementados
✅ Frontend React con interfaz para registrar ejercicios

---

## Limitaciones actuales

- No se almacena el peso al momento del ejercicio
- METs no ajustables por intensidad percibida del usuario (se usan valores fijos)
- No hay factor por edad/sexo
- No se registra recuperación cardíaca post-ejercicio

---

## Próximos pasos previstos

- Historial de pesos para cálculos retrospectivos precisos
- Editor visual de intensidad del ejercicio
- Sincronización con pulseras inteligentes (Fitbit, Apple Watch) si es posible
- Gráficos de actividad semanal/mensual
