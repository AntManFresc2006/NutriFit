# Anexo B — Referencia completa de endpoints de la API

El servidor escucha en `https://nutrifit-backend.onrender.com` (producción) o `http://localhost:8080` (desarrollo local).

Todos los cuerpos usan `application/json`. Cualquier error produce una respuesta uniforme:
```json
{
  "timestamp": "2026-05-17T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción del error",
  "path": "/api/alimentos"
}
```

**Nota de autenticación:** Excepto por `/api/auth/**`, `/api/escaner/**` y `/api/health`, todos los endpoints requieren cabecera `Authorization: Bearer <token>` donde `<token>` es el UUID obtenido al hacer login o registrarse.

Documentación interactiva: https://nutrifit-backend.onrender.com/swagger-ui.html

---

## B.1 Autenticación — `/api/auth`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| POST | `/api/auth/register` | Registra usuario y abre sesión | 201 | 400 |
| POST | `/api/auth/login` | Autentica usuario | 200 | 400, 401 |
| POST | `/api/auth/logout` | Invalida token y cierra sesión | 204 | 400 |

### `POST /api/auth/register`

**Cuerpo:**
```json
{
  "nombre": "Ana García",
  "email": "ana@ejemplo.com",
  "password": "segura123"
}
```

**Validaciones:**
- `nombre`: obligatorio, no vacío
- `email`: obligatorio, formato válido, no puede existir duplicado
- `password`: obligatorio, mínimo 6 caracteres

**Respuesta 201:**
```json
{
  "usuarioId": 1,
  "nombre": "Ana García",
  "email": "ana@ejemplo.com",
  "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Errores:**
- 400: email duplicado, campo obligatorio ausente, formato email inválido, contraseña muy corta
- Se devuelve siempre status 400 sin diferenciar entre "email duplicado" y "email inválido" (por seguridad)

### `POST /api/auth/login`

**Cuerpo:**
```json
{
  "email": "ana@ejemplo.com",
  "password": "segura123"
}
```

**Respuesta 200:** mismo formato que register

**Errores:**
- 400: campo obligatorio ausente
- 401: credenciales inválidas (el mensaje no diferencia entre "email no existe" y "contraseña incorrecta")

### `POST /api/auth/logout`

**Cabecera:** `Authorization: Bearer <token>`

**Sin cuerpo**

**Respuesta 204:** sin contenido

**Errores:**
- 400: token ausente o en blanco

---

## B.2 Alimentos — `/api/alimentos`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/alimentos` | Catálogo completo o filtrado | 200 | — |
| GET | `/api/alimentos/{id}` | Alimento por id | 200 | 404 |
| POST | `/api/alimentos` | Crea alimento | 201 | 400 |
| PUT | `/api/alimentos/{id}` | Actualiza alimento | 200 | 400, 404 |
| DELETE | `/api/alimentos/{id}` | Elimina alimento | 204 | 404 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/alimentos`

**Query params:**
- `q` (opcional): filtro por nombre, parcial, insensible a mayúsculas

**Respuesta 200:**
```json
[
  {
    "id": 11,
    "nombre": "Pollo a la plancha",
    "porcionG": 150,
    "kcalPor100g": 165,
    "proteinasG": 31,
    "grasasG": 3.6,
    "carbosG": 0,
    "fuente": "USDA"
  }
]
```

Sin resultados devuelve `[]`

### `GET /api/alimentos/{id}`

**Respuesta 200:** mismo formato que arriba, un objeto único

**Errores:** 404 si no existe

### `POST /api/alimentos`

**Cuerpo:**
```json
{
  "nombre": "Atún en lata",
  "porcionG": 100,
  "kcalPor100g": 110,
  "proteinasG": 26,
  "grasasG": 0.5,
  "carbosG": 0,
  "fuente": "OpenFoodFacts"
}
```

**Validaciones:**
- `nombre`: obligatorio, no vacío, se normaliza con trim
- `porcionG`: obligatorio, > 0
- `kcalPor100g`: obligatorio, ≥ 0
- `proteinasG`, `grasasG`, `carbosG`: obligatorios, ≥ 0
- `fuente`: opcional

**Respuesta 201:** mismo objeto con `id` asignado

**Errores:** 400 si campo falta o no cumple restricción

### `PUT /api/alimentos/{id}`

**Cuerpo:** mismo formato que POST

**Respuesta 200:** objeto actualizado

**Errores:**
- 400: validación fallida
- 404: alimento no existe

### `DELETE /api/alimentos/{id}`

**Respuesta 204:** sin contenido

**Errores:**
- 404: alimento no existe
- 400: alimento está referenciado en ítems de comida (ON DELETE RESTRICT)

---

## B.3 Comidas — `/api/comidas`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/comidas` | Comidas del usuario por fecha | 200 | — |
| POST | `/api/comidas` | Crea comida | 201 | 400 |
| DELETE | `/api/comidas/{id}` | Elimina comida (en cascada ítems) | 204 | 404 |
| GET | `/api/comidas/{comidaId}/items` | Ítems de una comida con cálculos | 200 | 404 |
| POST | `/api/comidas/{comidaId}/items` | Añade ítem a comida | 201 | 400, 404 |
| DELETE | `/api/comidas/{comidaId}/items/{itemId}` | Elimina ítem de comida | 204 | 404 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/comidas`

**Query params:**
- `usuarioId` (long, obligatorio)
- `fecha` (YYYY-MM-DD, obligatorio)

**Respuesta 200:**
```json
[
  {
    "id": 5,
    "usuarioId": 1,
    "fecha": "2026-05-17",
    "tipo": "DESAYUNO"
  }
]
```

Sin comidas devuelve `[]`

### `POST /api/comidas`

**Query param:** `usuarioId` (long)

**Cuerpo:**
```json
{
  "fecha": "2026-05-17",
  "tipo": "ALMUERZO"
}
```

**Validaciones:**
- `fecha`: YYYY-MM-DD, obligatorio
- `tipo`: obligatorio, se normaliza a mayúsculas

**Respuesta 201:** objeto comida con `id` asignado

**Errores:** 400 si campo falta

### `GET /api/comidas/{comidaId}/items`

**Respuesta 200:**
```json
[
  {
    "itemId": 42,
    "comidaId": 5,
    "alimentoId": 11,
    "nombre": "Pollo a la plancha",
    "gramos": 150,
    "kcalEstimadas": 247.5,
    "proteinasEstimadas": 46.5,
    "grasasEstimadas": 5.4,
    "carbosEstimados": 0
  }
]
```

Los valores `*Estimados` se calculan como: `valor_por_100g * gramos / 100`

Sin ítems devuelve `[]`

**Errores:** 404 si comida no existe

### `POST /api/comidas/{comidaId}/items`

**Cuerpo:**
```json
{
  "alimentoId": 11,
  "gramos": 150.0
}
```

**Validaciones:**
- `alimentoId`: obligatorio, debe existir
- `gramos`: obligatorio, > 0

**Respuesta 201:** sin contenido (201 empty)

**Errores:**
- 400: validación fallida
- 404: comida o alimento no existe

### `DELETE /api/comidas/{comidaId}/items/{itemId}`

**Respuesta 204:** sin contenido

**Errores:** 404 si ítem no existe o no pertenece a comida

### `DELETE /api/comidas/{id}`

**Respuesta 204:** sin contenido. Los ítems se eliminan en cascada.

**Errores:** 404 si comida no existe

---

## B.4 Resumen diario — `/api/resumen-diario`

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| GET | `/api/resumen-diario` | Totales nutricionales del día | 200 |

**Cabecera requerida:** `Authorization: Bearer <token>`

**Query params:**
- `usuarioId` (long, obligatorio)
- `fecha` (YYYY-MM-DD, obligatorio)

**Respuesta 200:**
```json
{
  "usuarioId": 1,
  "fecha": "2026-05-17",
  "kcalTotales": 2150.75,
  "proteinasTotales": 87.5,
  "grasasTotales": 68.25,
  "carbosTotales": 285.0
}
```

Si no hay comidas registradas, todos los totales son `0.0` (nunca `null`)

---

## B.5 Evaluación con IA — `/api/resumen/evaluacion-ia`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| POST | `/api/resumen/evaluacion-ia` | Evalúa desempeño nutricional | 200 | 400 |

**Cabecera requerida:** `Authorization: Bearer <token>`

**Cuerpo:**
```json
{
  "usuarioId": 1,
  "fecha": "2026-05-17",
  "kcalConsumidas": 2150.75,
  "kcalQuemadas": 350.0,
  "proteinasTotales": 87.5,
  "grasasTotales": 68.25,
  "carbosTotales": 285.0,
  "balanceReal": 1800.75,
  "tdee": 2200.0
}
```

**Respuesta 200:**
```json
{
  "evaluacion": "Tu ingesta de calorías está ligeramente por debajo de tu TDEE. El balance de macronutrientes es adecuado con proteína suficiente (87.5g). Mantén la consistencia y ajusta según tus objetivos específicos."
}
```

El texto es generado por IA (OpenRouter). El servicio usa la configuración del usuario si existe, o valores por defecto del servidor.

**Errores:** 400 si campo falta

---

## B.6 Perfil — `/api/perfil`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/perfil/{id}` | Perfil con TMB y TDEE | 200 | 404 |
| PUT | `/api/perfil/{id}` | Actualiza perfil | 200 | 400, 404 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/perfil/{id}`

**Respuesta 200:**
```json
{
  "id": 1,
  "nombre": "Ana García",
  "email": "ana@ejemplo.com",
  "sexo": "M",
  "fechaNacimiento": "1990-03-15",
  "alturaCm": 160,
  "pesoKgActual": 58.5,
  "pesoObjetivo": 55.0,
  "nivelActividad": "MODERADO",
  "tmb": 1289.0,
  "tdee": 1997.95
}
```

`tmb` (Tasa Metabólica Basal) y `tdee` (Gasto Energético Diario Total) se calculan con la fórmula de Mifflin-St Jeor a partir de los datos biométricos.

`pesoObjetivo` puede ser `null`

**Errores:** 404 si usuario no existe

### `PUT /api/perfil/{id}`

**Cuerpo:**
```json
{
  "sexo": "M",
  "fechaNacimiento": "1990-03-15",
  "alturaCm": 160,
  "pesoKgActual": 58.0,
  "pesoObjetivo": 55.0,
  "nivelActividad": "MODERADO"
}
```

**Validaciones:**
- `sexo`: obligatorio; `"H"` (hombre) o `"M"` (mujer)
- `fechaNacimiento`: YYYY-MM-DD, obligatorio, anterior a hoy
- `alturaCm`: obligatorio, 100–250
- `pesoKgActual`: obligatorio, ≥ 20
- `pesoObjetivo`: opcional, puede omitirse o ser `null`
- `nivelActividad`: obligatorio; `"SEDENTARIO"`, `"LIGERO"`, `"MODERADO"`, `"ALTO"` o `"MUY_ALTO"`

**Respuesta 200:** objeto perfil actualizado con `tmb` y `tdee` recalculados

**Errores:**
- 400: validación fallida
- 404: usuario no existe

---

## B.7 Ejercicios — `/api/ejercicios`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/ejercicios` | Catálogo o búsqueda | 200 | — |
| GET | `/api/ejercicios/{id}` | Ejercicio por id | 200 | 404 |
| POST | `/api/ejercicios` | Crea ejercicio en catálogo | 201 | 400 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/ejercicios`

**Query param:**
- `q` (opcional): filtro por nombre, parcial, insensible a mayúsculas

**Respuesta 200:**
```json
[
  {
    "id": 1,
    "nombre": "Correr",
    "met": 8.0,
    "categoria": "CARDIO"
  }
]
```

Sin resultados devuelve `[]`

### `GET /api/ejercicios/{id}`

**Respuesta 200:** objeto único

**Errores:** 404 si no existe

### `POST /api/ejercicios`

**Cuerpo:**
```json
{
  "nombre": "Pilates",
  "met": 3.5,
  "categoria": "FLEXIBILIDAD"
}
```

**Validaciones:**
- `nombre`: obligatorio, no vacío
- `met`: obligatorio, > 0 (Metabolic Equivalent of Task)
- `categoria`: obligatorio, no vacío

**Respuesta 201:** objeto con `id` asignado

**Errores:** 400 si validación falla

---

## B.8 Registro de ejercicios — `/api/ejercicios-registro`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/ejercicios-registro` | Registros de usuario por fecha | 200 | — |
| POST | `/api/ejercicios-registro` | Registra sesión | 201 | 400, 404 |
| DELETE | `/api/ejercicios-registro/{id}` | Elimina registro | 204 | 404 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/ejercicios-registro`

**Query params:**
- `usuarioId` (long, obligatorio)
- `fecha` (YYYY-MM-DD, obligatorio)

**Respuesta 200:**
```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "ejercicioId": 1,
    "nombreEjercicio": "Correr",
    "fecha": "2026-05-17",
    "duracionMin": 45,
    "kcalQuemadas": 450.0
  }
]
```

Sin registros devuelve `[]`

### `POST /api/ejercicios-registro`

**Query param:** `usuarioId` (long)

**Cuerpo:**
```json
{
  "ejercicioId": 1,
  "fecha": "2026-05-17",
  "duracionMin": 45
}
```

**Validaciones:**
- `ejercicioId`: obligatorio, debe existir en catálogo
- `fecha`: YYYY-MM-DD, obligatorio
- `duracionMin`: obligatorio, > 0

**Cálculo automático:**
El backend calcula `kcalQuemadas = MET × peso_kg × (duracionMin / 60.0)` usando el MET del ejercicio y el peso actual del perfil del usuario.

**Respuesta 201:** objeto registro con `kcalQuemadas` calculadas

**Errores:**
- 400: validación fallida
- 404: ejercicio no existe o usuario no tiene perfil configurado

### `DELETE /api/ejercicios-registro/{id}`

**Query param:** `usuarioId` (long) — se valida que el registro pertenece a este usuario

**Respuesta 204:** sin contenido

**Errores:** 404 si registro no existe o no pertenece al usuario

---

## B.9 Hidratación — `/api/hidratacion`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/hidratacion` | Consumo de agua del día | 200 | — |
| POST | `/api/hidratacion` | Registra consumo de agua | 201 | 400 |
| DELETE | `/api/hidratacion/{id}` | Elimina registro | 204 | 404 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/hidratacion`

**Query params:**
- `usuarioId` (long, obligatorio)
- `fecha` (YYYY-MM-DD, obligatorio)

**Respuesta 200:**
```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "fecha": "2026-05-17",
    "cantidadMl": 250,
    "registroEn": "2026-05-17T10:30:00"
  }
]
```

Sin registros devuelve `[]`

### `POST /api/hidratacion`

**Query param:** `usuarioId` (long)

**Cuerpo:**
```json
{
  "cantidadMl": 250
}
```

**Validaciones:**
- `cantidadMl`: obligatorio, > 0

**Respuesta 201:** objeto registro

**Errores:** 400 si validación falla

### `DELETE /api/hidratacion/{id}`

**Query param:** `usuarioId` (long)

**Respuesta 204:** sin contenido

**Errores:** 404 si registro no existe

---

## B.10 Plan semanal con IA — `/api/plan-semanal`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| POST | `/api/plan-semanal` | Genera plan semanal | 200 | 400 |
| GET | `/api/plan-semanal` | Obtiene plan semanal | 200 | 404 |
| DELETE | `/api/plan-semanal` | Elimina plan | 204 | 404 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `POST /api/plan-semanal`

**Query param:** `usuarioId` (long)

**Cuerpo:**
```json
{
  "semanaInicio": "2026-05-17"
}
```

El backend genera un plan de comidas para 7 días usando IA (OpenRouter) considerando el perfil del usuario.

**Respuesta 200:**
```json
{
  "usuarioId": 1,
  "semanaInicio": "2026-05-17",
  "planJson": "{ ... plan detallado en JSON ... }",
  "creadoEn": "2026-05-17T10:30:00"
}
```

**Errores:** 400 si validación falla

### `GET /api/plan-semanal`

**Query params:**
- `usuarioId` (long, obligatorio)
- `semanaInicio` (YYYY-MM-DD, obligatorio)

**Respuesta 200:** objeto plan

**Errores:** 404 si no existe plan para esa semana

### `DELETE /api/plan-semanal`

**Query params:** `usuarioId`, `semanaInicio`

**Respuesta 204:** sin contenido

---

## B.11 Retos y gamificación — `/api/retos`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/retos` | Lista retos del usuario | 200 | — |
| POST | `/api/retos/aceptar` | Acepta reto | 200 | 400, 404 |
| POST | `/api/retos/sincronizar` | Sincroniza progreso | 200 | — |
| DELETE | `/api/retos/{usuarioRetoId}` | Abandona reto | 204 | 404 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/retos`

**Query param:** `usuarioId` (long)

**Respuesta 200:**
```json
[
  {
    "usuarioRetoId": 1,
    "retoId": 5,
    "nombre": "Consumir 100g de proteína diaria",
    "descripcion": "Registra sesiones de ...",
    "estado": "ACTIVO",
    "progreso": 85,
    "fechaAceptacion": "2026-05-10"
  }
]
```

### `POST /api/retos/aceptar`

**Query param:** `usuarioId`

**Cuerpo:**
```json
{
  "retoId": 5
}
```

**Respuesta 200:** objeto del reto aceptado

**Errores:** 404 si reto no existe

### `POST /api/retos/sincronizar`

**Query params:**
- `usuarioId` (long)
- `fecha` (YYYY-MM-DD)

Recalcula el progreso de todos los retos activos del usuario para esa fecha.

**Respuesta 200:** lista de retos con progreso actualizado

### `DELETE /api/retos/{usuarioRetoId}`

**Query param:** `usuarioId`

**Respuesta 204:** sin contenido

---

## B.12 Lista de compra — `/api/lista-compra`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/lista-compra` | Lista agrupada por categoría | 200 | — |
| POST | `/api/lista-compra` | Añade ítem | 201 | 400 |
| PATCH | `/api/lista-compra/{id}/toggle` | Marca/desmarca como completado | 200 | 404 |
| DELETE | `/api/lista-compra/{id}` | Elimina ítem | 204 | 404 |
| DELETE | `/api/lista-compra/completados` | Elimina ítems completados | 204 | — |
| GET | `/api/lista-compra/sugerencias` | Genera sugerencias con IA | 200 | — |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/lista-compra`

**Query param:** `usuarioId` (long)

**Respuesta 200:**
```json
{
  "FRUTAS": [
    { "id": 1, "nombre": "Manzana", "cantidad": 2, "unidad": "kg", "completado": false }
  ],
  "VERDURAS": [
    { "id": 2, "nombre": "Lechuga", "cantidad": 1, "unidad": "ud", "completado": true }
  ]
}
```

### `POST /api/lista-compra`

**Query param:** `usuarioId`

**Cuerpo:**
```json
{
  "nombre": "Atún",
  "cantidad": 3,
  "unidad": "latas",
  "categoria": "CONSERVAS"
}
```

**Respuesta 201:** objeto ítem con `id`

### `PATCH /api/lista-compra/{id}/toggle`

**Query param:** `usuarioId`

Invierte el estado `completado`.

**Respuesta 200:** objeto ítem actualizado

**Errores:** 404 si ítem no existe

### `DELETE /api/lista-compra/{id}`

**Query param:** `usuarioId`

**Respuesta 204:** sin contenido

### `DELETE /api/lista-compra/completados`

**Query param:** `usuarioId`

Elimina todos los ítems con `completado = true`.

**Respuesta 204:** sin contenido

### `GET /api/lista-compra/sugerencias`

**Query param:** `usuarioId`

Genera sugerencias de compra basadas en el análisis nutritivo y patrones del usuario usando IA.

**Respuesta 200:**
```json
{
  "sugerencias": "Basándome en tu consumo diario, te sugiero...",
  "generadoEn": "2026-05-17T10:30:00"
}
```

---

## B.13 Escáner de código de barras — `/api/escaner`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/escaner/{barcode}` | Busca alimento por código | 200 | 404 |

**Sin autenticación requerida** (endpoint público)

**Path param:** `barcode` (string, ej., `5000112142630`)

**Respuesta 200:**
```json
{
  "nombre": "Coca Cola 33 cl",
  "kcalPor100g": 43,
  "proteinasG": 0,
  "grasasG": 0,
  "carbosG": 10.6,
  "fuente": "OpenFoodFacts"
}
```

Consulta OpenFoodFacts API (sin autenticación).

**Errores:** 404 si el código de barras no existe en OpenFoodFacts

---

## B.14 Gamificación — `/api/gamificacion`

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| GET | `/api/gamificacion` | Puntos y logros del usuario | 200 |

**Cabecera requerida:** `Authorization: Bearer <token>`

**Query params:**
- `usuarioId` (long, obligatorio)
- `fecha` (YYYY-MM-DD, obligatorio)

**Respuesta 200:**
```json
{
  "usuarioId": 1,
  "fecha": "2026-05-17",
  "puntosDelDia": 150,
  "puntosAcumulados": 3500,
  "logros": [
    { "nombre": "Primer registro", "descripcion": "Registra tu primer alimento", "alcanzado": true }
  ]
}
```

---

## B.15 Historial de peso — `/api/peso-historial`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/peso-historial` | Histórico de peso | 200 | — |
| POST | `/api/peso-historial` | Registra peso (upsert) | 201 | 400 |
| DELETE | `/api/peso-historial` | Elimina registro | 204 | 404 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/peso-historial`

**Query params:**
- `usuarioId` (long, obligatorio)
- `limit` (int, opcional, default 30): últimos N registros

**Respuesta 200:**
```json
[
  { "fecha": "2026-05-17", "pesoKg": 58.5 },
  { "fecha": "2026-05-16", "pesoKg": 58.6 }
]
```

### `POST /api/peso-historial`

**Query param:** `usuarioId`

**Cuerpo:**
```json
{
  "fecha": "2026-05-17",
  "pesoKg": 58.5
}
```

Si ya existe registro para esa fecha, se actualiza (upsert).

**Respuesta 201:** objeto registro

### `DELETE /api/peso-historial`

**Query params:**
- `usuarioId` (long)
- `fecha` (YYYY-MM-DD)

**Respuesta 204:** sin contenido

---

## B.16 Tendencias — `/api/tendencias`

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| GET | `/api/tendencias` | Análisis de tendencias | 200 |

**Cabecera requerida:** `Authorization: Bearer <token>`

**Query params:**
- `usuarioId` (long, obligatorio)
- `dias` (int, opcional, default 30, max 90): período de análisis

**Respuesta 200:**
```json
{
  "usuarioId": 1,
  "dias": 30,
  "promedioDiarioKcal": 2150.0,
  "promedioProteinas": 87.5,
  "promedioGrasas": 68.0,
  "promedioCarbos": 285.0,
  "tendencia": "ESTABLE",
  "cambioFecha": "2026-04-17"
}
```

`tendencia` puede ser `"ESTABLE"`, `"MEJORANDO"` o `"EMPEORANDO"` según la comparación entre primero y último período.

---

## B.17 Configuración de IA personalizada — `/api/ia-config`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/ia-config` | Obtiene configuración | 200 | 404 |
| PUT | `/api/ia-config` | Actualiza configuración | 200 | 400 |
| DELETE | `/api/ia-config` | Elimina configuración | 204 | 404 |

**Cabecera requerida:** `Authorization: Bearer <token>`

### `GET /api/ia-config`

**Query param:** `usuarioId` (long)

**Respuesta 200:**
```json
{
  "usuarioId": 1,
  "proxyUrl": "https://api.openrouter.ai/api/v1",
  "model": "meta-llama/llama-3-70b-instruct",
  "apiKey": "sk-or-v1-xxx..."
}
```

**Errores:** 404 si usuario no tiene configuración personalizada (usará valores por defecto)

### `PUT /api/ia-config`

**Query param:** `usuarioId`

**Cuerpo:**
```json
{
  "proxyUrl": "https://api.openrouter.ai/api/v1",
  "model": "openai/gpt-4",
  "apiKey": "sk-or-v1-xxx..."
}
```

Todos los campos son obligatorios. Sobrescribe configuración previa.

**Respuesta 200:** objeto configuración actualizado

### `DELETE /api/ia-config`

**Query param:** `usuarioId`

Elimina configuración personalizada; futuras evaluaciones usarán valores por defecto del servidor.

**Respuesta 204:** sin contenido

---

## B.18 Health — `/api/health`

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| GET | `/api/health` | Estado de la aplicación | 200 |

**Sin autenticación requerida** (endpoint público)

**Respuesta 200:**
```json
{
  "status": "UP"
}
```

Útil para verificar que el servidor está activo (usado por Render health checks y monitores).

---

## Resumen de módulos

| Módulo | Endpoints | Autenticación |
|--------|-----------|---|
| Autenticación | 3 | NO (register, login, logout) |
| Alimentos | 5 | SÍ |
| Comidas | 6 | SÍ |
| Resumen diario | 1 | SÍ |
| Evaluación IA | 1 | SÍ |
| Perfil | 2 | SÍ |
| Ejercicios | 3 | SÍ |
| Registro ejercicios | 3 | SÍ |
| Hidratación | 3 | SÍ |
| Plan semanal | 3 | SÍ |
| Retos | 4 | SÍ |
| Lista de compra | 6 | SÍ |
| Escáner | 1 | NO |
| Gamificación | 1 | SÍ |
| Peso historial | 3 | SÍ |
| Tendencias | 1 | SÍ |
| IA config | 3 | SÍ |
| Health | 1 | NO |
| **Total** | **54** | |

