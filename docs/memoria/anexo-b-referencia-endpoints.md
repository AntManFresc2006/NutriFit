# Anexo B — Referencia de endpoints de la API

El servidor escucha en `http://localhost:8080`. Todos los cuerpos usan `application/json`. Cualquier error produce una respuesta con la estructura `{ "timestamp", "status", "error", "message", "path" }`. Documentación interactiva: `http://localhost:8080/swagger-ui.html`. Peticiones ejecutables: `docs/api/`.

---

## B.1 Autenticación — `/api/auth`

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| POST | `/api/auth/register` | Registra usuario y abre sesión automáticamente | 201 |
| POST | `/api/auth/login` | Autentica usuario existente | 200 |
| POST | `/api/auth/logout` | Invalida el token de sesión activo | 204 |

**`register` y `login`** comparten el mismo objeto de respuesta:
```json
{ "usuarioId": 1, "nombre": "...", "email": "...", "token": "uuid-v4" }
```

**Cuerpo de `register`:**

| Campo | Restricciones |
|---|---|
| `nombre` | obligatorio, no vacío |
| `email` | obligatorio, formato email válido |
| `password` | obligatorio, mínimo 6 caracteres |

→ **400** si falta un campo, el email ya está registrado, el formato es inválido o la contraseña es demasiado corta.

**Cuerpo de `login`:** `email` (obligatorio, formato válido) y `password` (obligatorio, no vacío).
→ **400** si falta un campo; **401** si las credenciales son inválidas (el mensaje de error es idéntico tanto si el email no existe como si la contraseña es incorrecta).

**`logout`:** cabecera `Authorization: Bearer <token>`, sin cuerpo. → **400** si el token es nulo o en blanco.

---

## B.2 Alimentos — `/api/alimentos`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/alimentos` | Catálogo completo o filtrado por `?q=` | 200 | — |
| GET | `/api/alimentos/{id}` | Alimento por id | 200 | 404 |
| POST | `/api/alimentos` | Crea alimento | 201 | 400 |
| PUT | `/api/alimentos/{id}` | Actualiza alimento completo | 200 | 400, 404 |
| DELETE | `/api/alimentos/{id}` | Elimina alimento | 204 | 404 |

**Campos de `AlimentoRequest`** (cuerpo de POST y PUT):

| Campo | Restricciones |
|---|---|
| `nombre` | obligatorio, no vacío |
| `porcionG` | obligatorio, > 0 |
| `kcalPor100g` | obligatorio, ≥ 0 |
| `proteinasG` | obligatorio, ≥ 0 |
| `grasasG` | obligatorio, ≥ 0 |
| `carbosG` | obligatorio, ≥ 0 |
| `fuente` | opcional |

`AlimentoResponse` incluye los mismos campos más `id`. El parámetro `q` realiza búsqueda parcial insensible a mayúsculas; sin `q` devuelve el catálogo completo. Si no hay resultados, devuelve `[]`.

> DELETE falla si el alimento está referenciado en algún ítem de comida (restricción `ON DELETE RESTRICT` en `comida_alimentos`).

---

## B.3 Comidas — `/api/comidas`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/comidas` | Lista comidas de un usuario por fecha | 200 | — |
| POST | `/api/comidas` | Crea comida | 201 | 400 |
| DELETE | `/api/comidas/{id}` | Elimina comida y sus ítems en cascada | 204 | 404 |
| GET | `/api/comidas/{comidaId}/items` | Lista ítems con valores nutricionales calculados | 200 | 404 |
| POST | `/api/comidas/{comidaId}/items` | Añade ítem a comida | 201 | 400, 404 |
| DELETE | `/api/comidas/{comidaId}/items/{itemId}` | Elimina ítem de comida | 204 | 404 |

**`GET /api/comidas`** — query params: `usuarioId` (long) y `fecha` (YYYY-MM-DD), ambos obligatorios. Devuelve `[]` si no hay comidas.

**`POST /api/comidas`** — query param: `usuarioId`. Cuerpo: `fecha` (YYYY-MM-DD, obligatorio) y `tipo` (string, obligatorio; se normaliza a mayúsculas). Devuelve `ComidaResponse`: `{ "id", "usuarioId", "fecha", "tipo" }`.

**`POST /api/comidas/{comidaId}/items`** — cuerpo: `alimentoId` (long, obligatorio) y `gramos` (double, > 0). Sin cuerpo en la respuesta. El 404 se produce si la comida o el alimento referenciado no existen.

**`GET /api/comidas/{comidaId}/items`** — respuesta con valores calculados ponderados por gramos:
```json
[{
  "itemId": 1, "comidaId": 1, "alimentoId": 11, "nombre": "Pollo a la plancha",
  "gramos": 120.0, "kcalEstimadas": 198.0, "proteinasEstimadas": 37.2,
  "grasasEstimadas": 4.32, "carbosEstimados": 0.0
}]
```

**`DELETE /api/comidas/{comidaId}/items/{itemId}`** — el 404 se produce si el ítem no existe o si no pertenece a la comida indicada en la ruta.

---

## B.4 Resumen diario — `/api/resumen-diario`

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| GET | `/api/resumen-diario` | Totales nutricionales del día | 200 |

Query params: `usuarioId` (long) y `fecha` (YYYY-MM-DD), ambos obligatorios.

Respuesta: `{ "usuarioId", "fecha", "kcalTotales", "proteinasTotales", "grasasTotales", "carbosTotales" }`. Si no hay comidas registradas para la fecha consultada, todos los totales son `0.0`.

---

## B.5 Perfil — `/api/perfil`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/perfil/{id}` | Perfil con TMB y TDEE calculados | 200 | 404 |
| PUT | `/api/perfil/{id}` | Actualiza perfil; recalcula TMB y TDEE | 200 | 400, 404 |

**Cuerpo de PUT (`PerfilUpdateRequest`):**

| Campo | Restricciones |
|---|---|
| `sexo` | obligatorio; `"H"` o `"M"` |
| `fechaNacimiento` | obligatorio, YYYY-MM-DD, anterior a hoy |
| `alturaCm` | obligatorio, 100–250 |
| `pesoKgActual` | obligatorio, ≥ 20,0 |
| `pesoObjetivo` | opcional, puede omitirse o enviarse `null` |
| `nivelActividad` | obligatorio; `"SEDENTARIO"`, `"LIGERO"`, `"MODERADO"`, `"ALTO"` o `"MUY_ALTO"` |

`PerfilResponse` incluye todos los campos del perfil más `tmb` y `tdee` (redondeados a dos decimales), y los campos de identificación `nombre` y `email`. `pesoObjetivo` puede ser `null`.

---

## B.6 Ejercicios — `/api/ejercicios`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/ejercicios` | Catálogo completo o filtrado por `?q=` | 200 | — |
| GET | `/api/ejercicios/{id}` | Ejercicio por id | 200 | 404 |
| POST | `/api/ejercicios` | Crea un ejercicio en el catálogo | 201 | 400 |

Todos los endpoints requieren cabecera `Authorization: Bearer <token>`.

**`GET /api/ejercicios`** — parámetro opcional `?q=` para filtrado parcial por nombre, insensible a mayúsculas. Sin `?q=` devuelve el catálogo completo. Si no hay resultados, devuelve `[]`.

**Cuerpo de POST (`EjercicioRequest`):**

| Campo | Restricciones |
|---|---|
| `nombre` | obligatorio, no vacío |
| `met` | obligatorio, > 0 |
| `categoria` | obligatorio, no vacío |

`EjercicioResponse` incluye los mismos campos más `id`.

---

## B.7 Registro de ejercicios — `/api/ejercicios-registro`

| Método | Ruta | Descripción | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/ejercicios-registro` | Registros de un usuario para una fecha | 200 | — |
| POST | `/api/ejercicios-registro` | Registra una sesión de ejercicio | 201 | 400, 404 |
| DELETE | `/api/ejercicios-registro/{id}` | Elimina un registro propio | 204 | 404 |

Todos los endpoints requieren cabecera `Authorization: Bearer <token>`.

**`GET /api/ejercicios-registro`** — query params: `usuarioId` (long) y `fecha` (YYYY-MM-DD), ambos obligatorios. Devuelve `[]` si no hay registros para esa fecha.

**`POST /api/ejercicios-registro`** — query param: `usuarioId`. Cuerpo: `ejercicioId` (long, obligatorio), `fecha` (YYYY-MM-DD, obligatorio) y `duracionMin` (int, > 0). El backend calcula `kcalQuemadas` aplicando MET × peso_kg × (duracionMin / 60) con el peso del perfil del usuario en ese momento y lo persiste en el registro. → **404** si el ejercicio o el perfil del usuario no existen.

`RegistroEjercicioResponse`: `{ "id", "usuarioId", "ejercicioId", "nombreEjercicio", "fecha", "duracionMin", "kcalQuemadas" }`.

**`DELETE /api/ejercicios-registro/{id}`** — query param: `usuarioId`. → **404** si el registro no existe o no pertenece al usuario indicado.
