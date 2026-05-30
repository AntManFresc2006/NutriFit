# 5.7 Módulos avanzados: IA, gamificación y extensiones

Tras los módulos de núcleo nutritivo (alimentos, comidas, resumen, perfil, ejercicios), NutriFit incorpora seis módulos avanzados que extienden la funcionalidad hacia análisis con IA, seguimiento longitudinal y engagement del usuario.

---

## 5.7.1 Hidratación: seguimiento de consumo de agua

### Problema que resuelve

La hidratación es un pilar de la salud que no cabe en los macronutrientes. Un usuario necesita registrar cuánta agua bebe diariamente y recibir feedback sobre si cumple su objetivo (típicamente 2–3 litros diarios).

### Modelo de datos

**Tabla `hidratacion`:**

| Columna | Tipo SQL | Descripción |
|---------|----------|-------------|
| `id` | `BIGSERIAL PK` | Clave primaria |
| `usuario_id` | `BIGINT FK` | Usuario propietario del registro |
| `fecha` | `DATE` | Fecha del registro |
| `cantidad_ml` | `INTEGER` | Mililitros consumidos en ese registro |
| `registrado_en` | `TIMESTAMP` | Timestamp de creación |

No hay agregación por día en la tabla; el servicio suma todos los registros de un día para obtener el total.

### API

- `GET /api/hidratacion?usuarioId=<id>&fecha=<YYYY-MM-DD>`: lista registros del día, devuelve `[]` si no hay. El cliente suma los valores para mostrar total.
- `POST /api/hidratacion?usuarioId=<id>` con body `{ cantidadMl: int }`: registra un consumo puntual (ej., vaso de agua de 250 ml).
- `DELETE /api/hidratacion/{id}?usuarioId=<id>`: elimina un registro.

### Interfaz del cliente

React muestra un contador visual con barra de progreso hacia el objetivo diario (2000 ml por defecto). Cada click en «Beber agua» añade un registro de 250 ml. El usuario puede registrar cantidades personalizadas.

La lógica es simple: recupera todos los registros del día, suma `cantidad_ml` y compara contra el objetivo. Si se alcanza, muestra una celebración visual.

---

## 5.7.2 Plan semanal con IA: generación automática

### Problema que resuelve

El usuario quiere un plan de comidas para la semana sin tener que diseñarlo manualmente. Proporcionado su perfil y restricciones dietéticas, una IA puede generar un menú equilibrado y variado.

### Arquitectura

`PlanSemanalService` coordina:

1. **Recuperación del perfil del usuario:** extrae sexo, edad, peso, TDEE y objetivo de peso.
2. **Consulta de configuración de IA:** obtiene proxyUrl, modelo y apiKey del usuario (o usa valores por defecto).
3. **Construcción del prompt:** especifica un menú para 7 días respetando el TDEE, con distribución de macronutrientes razonable.
4. **Llamada a OpenRouter:** envía el prompt a través del proxy configurado.
5. **Persistencia:** almacena el plan generado en JSON en la tabla `planes_semanales`.

### Modelo de datos

**Tabla `planes_semanales`:**

| Columna | Tipo SQL | Descripción |
|---------|----------|-------------|
| `id` | `BIGSERIAL PK` | Clave primaria |
| `usuario_id` | `BIGINT FK` | Usuario propietario |
| `semana_inicio` | `DATE` | Lunes de la semana del plan (YYYY-MM-DD) |
| `plan_json` | `TEXT` | Plan completo en JSON (estructura libre) |
| `creado_en` | `TIMESTAMP` | Timestamp de generación |

### API

- `POST /api/plan-semanal?usuarioId=<id>` con body `{ semanaInicio: "YYYY-MM-DD" }`: genera e persiste el plan. Responde con el objeto generado.
- `GET /api/plan-semanal?usuarioId=<id>&semanaInicio=<YYYY-MM-DD>`: obtiene plan previamente generado.
- `DELETE /api/plan-semanal?usuarioId=<id>&semanaInicio=<YYYY-MM-DD>`: elimina plan.

### Fallback de IA

Si la llamada a OpenRouter falla (timeout, clave inválida, modelo no disponible), el servicio captura la excepción y genera un plan genérico predeterminado basado en recomendaciones estándar de nutrición. El usuario siempre recibe un plan útil.

---

## 5.7.3 Retos y gamificación: engagement mediante objetivos

### Problema que resuelve

Motivar al usuario a mantener consistencia en su seguimiento nutricional mediante retos cortos («Consume 100 g de proteína diaria durante 7 días») y un sistema de puntos.

### Modelo de datos

**Tabla `retos`:** catálogo de retos disponibles (compartido por todos los usuarios)

| Columna | Tipo SQL | Descripción |
|---------|----------|-------------|
| `id` | `BIGSERIAL PK` | Clave primaria |
| `nombre` | `VARCHAR(200)` | Nombre del reto |
| `descripcion` | `TEXT` | Descripción detallada |
| `tipo` | `VARCHAR(50)` | Tipo: `"PROTEINA"`, `"CALORIAS"`, `"AGUA"`, etc. |
| `duracion_dias` | `SMALLINT` | Duración en días |
| `valor_objetivo` | `NUMERIC(10,2)` | Valor a alcanzar (ej., 100 para 100g de proteína) |

**Tabla `usuario_retos`:** seguimiento de aceptación y progreso

| Columna | Tipo SQL | Descripción |
|---------|----------|-------------|
| `id` | `BIGSERIAL PK` | Clave primaria |
| `usuario_id` | `BIGINT FK` | Usuario que aceptó el reto |
| `reto_id` | `BIGINT FK` | Reto del catálogo |
| `estado` | `VARCHAR(20)` | `"ACTIVO"`, `"COMPLETADO"`, `"ABANDONADO"` |
| `progreso_actual` | `NUMERIC(10,2)` | Progreso acumulado |
| `fecha_aceptacion` | `DATE` | Cuando se aceptó |
| `fecha_completado` | `DATE` | Cuando se completó (null si no) |

### Lógica de sincronización

Cada día, cuando el usuario abre la aplicación, la API sincroniza el progreso de todos sus retos activos:

1. Recupera todos los `usuario_retos` con `estado = "ACTIVO"`.
2. Para cada reto, calcula el progreso actual (ej., suma de proteína consumida hoy).
3. Si el progreso ≥ objetivo diario, suma un punto al reto.
4. Si el reto lleva N días activos y tiene N puntos, marca como `"COMPLETADO"` y suma puntos globales al usuario.

**Endpoint:** `POST /api/retos/sincronizar?usuarioId=<id>&fecha=<YYYY-MM-DD>` — recalcula y devuelve estado actual de todos los retos.

### Puntuación global

**Tabla `puntuacion`:**

| Columna | Tipo SQL | Descripción |
|---------|----------|-------------|
| `id` | `BIGSERIAL PK` | Clave primaria |
| `usuario_id` | `BIGINT FK` | Usuario |
| `fecha` | `DATE` | Fecha del cálculo |
| `puntos_dia` | `INTEGER` | Puntos ganados ese día |
| `puntos_acumulados` | `INTEGER` | Total acumulado |

Se calcula diariamente como:
- +10 por registro de alimento
- +20 por completar un reto diario
- +5 por registrar ejercicio
- +5 por alcanzar objetivo de hidratación

---

## 5.7.4 Detective Nutricional IA: análisis forense del historial

### Problema que resuelve

El usuario registra datos durante semanas pero no tiene una visión global de sus patrones. El Detective Nutricional analiza el historial de los últimos N días y genera un informe detallado: tendencias, déficits recurrentes, días problemáticos y recomendaciones priorizadas.

### Arquitectura

`DetectiveService` coordina:

1. **Agregación de datos históricos:** `DetectiveRepository` ejecuta una consulta con JOIN sobre `comidas`, `comida_alimentos`, `alimentos` y `ejercicios_registro` para los últimos N días del usuario.
2. **Construcción del prompt:** resume los promedios diarios (kcal, proteínas, grasas, carbos, ejercicio) y los compara con el TDEE del usuario.
3. **Llamada asíncrona a OpenRouter:** `DetectiveIaAsync` gestiona la llamada en un hilo aparte para no bloquear la petición HTTP. El estado del análisis se persiste en `detective_analisis`.
4. **Polling del cliente:** el frontend hace `GET /api/detective` periódicamente hasta que el campo `estado` pasa de `PROCESANDO` a `COMPLETADO`.

### Modelo de datos

**Tabla `detective_analisis`:**

| Columna | Tipo SQL | Descripción |
|---------|----------|-------------|
| `id` | `BIGSERIAL PK` | Clave primaria |
| `usuario_id` | `BIGINT FK UNIQUE` | Un análisis activo por usuario |
| `estado` | `VARCHAR(20)` | `PROCESANDO`, `COMPLETADO`, `ERROR` |
| `dias_analizados` | `INTEGER` | Período del análisis |
| `resultado_json` | `TEXT` | Informe completo generado por IA |
| `creado_en` | `TIMESTAMP` | Timestamp de inicio |
| `completado_en` | `TIMESTAMP` | Timestamp de fin (null si pendiente) |

### API

- `POST /api/detective?usuarioId=<id>&dias=30`: inicia un análisis forense. Requiere rate limiting (5 req/min/usuario). Devuelve inmediatamente con `estado: PROCESANDO`.
- `GET /api/detective?usuarioId=<id>`: obtiene el análisis actual. 404 si nunca se ha iniciado.

### Interfaz del cliente

React muestra un estado de carga mientras el análisis está en curso. Al completarse, renderiza el informe en secciones (fortalezas, déficits, patrones, recomendaciones). Si la IA falla, muestra un mensaje de error sin exponer detalles internos.

---

## 5.7.5 Escáner de código de barras: integración con OpenFoodFacts

### Problema que resuelve

Registrar un alimento tecleando su nombre es lento. Un código de barras identifica unívocamente un producto. La IA puede recuperar sus nutrientes de una base de datos pública.

### Arquitectura

`EscanerService` encapsula la lógica:

1. **Búsqueda en OpenFoodFacts:** consulta la API pública (sin autenticación) con el código de barras.
2. **Mapeo a `Alimento`:** extrae nombre, kcal, proteínas, grasas, carbos de la respuesta JSON.
3. **Creación local:** si no existe el alimento en la BD de NutriFit, lo crea con fuente `"OpenFoodFacts"`.
4. **Caché opcional:** algunos clientes podrían cachear resultados localmente.

### API

- `GET /api/escaner/{barcode}`: busca el código en OpenFoodFacts. **No requiere autenticación.**

**Respuesta 200:**
```json
{
  "id": null,  // será asignado si se inserta en BD
  "nombre": "Coca Cola 33 cl",
  "kcalPor100g": 43,
  "proteinasG": 0,
  "grasasG": 0,
  "carbosG": 10.6,
  "fuente": "OpenFoodFacts"
}
```

**Errores:**
- 404: código no encontrado en OpenFoodFacts.

### Caso de uso

1. Usuario abre escáner en la aplicación y captura código de barras (dispositivo real) o ingresa manualmente.
2. Frontend realiza `GET /api/escaner/{barcode}`.
3. Backend consulta OpenFoodFacts.
4. Se devuelven los nutrientes.
5. El usuario puede registrar el alimento directamente sin buscarlo en el catálogo.

---

## 5.7.6 Historial de peso y tendencias: seguimiento longitudinal

### Problema que resuelve

El peso es un indicador importante pero volátil. El usuario necesita registrar el peso periódicamente y analizar tendencias (¿está bajando, subiendo o estable?) sin obsesionarse con fluctuaciones diarias.

### Modelo de datos

**Tabla `peso_historial`:**

| Columna | Tipo SQL | Descripción |
|---------|----------|-------------|
| `id` | `BIGSERIAL PK` | Clave primaria |
| `usuario_id` | `BIGINT FK` | Usuario |
| `fecha` | `DATE` | Fecha de pesada |
| `peso_kg` | `NUMERIC(6,2)` | Peso en kilogramos |

Índice único sobre `(usuario_id, fecha)` garantiza un registro por día. `POST` con fecha existente actualiza el registro (upsert).

### API

- `GET /api/peso-historial?usuarioId=<id>&limit=30`: últimos N registros (máx. 90).
- `POST /api/peso-historial?usuarioId=<id>` con body `{ fecha, pesoKg }`: upsert.
- `DELETE /api/peso-historial?usuarioId=<id>&fecha=<YYYY-MM-DD>`: elimina registro.

### Análisis de tendencias

`TendenciasService` calcula:

1. **Promedio nutricional:** para los últimos N días (default 30, máx. 90), suma todos los consumos y divide entre días.
2. **Cambio de peso:** diferencia entre peso inicial (hace 30 días) y actual.
3. **Tendencia:** clasificación simple:
   - `"ESTABLE"` si cambio ± 0,5 kg
   - `"MEJORANDO"` si cambio < −0,5 kg (bajando)
   - `"EMPEORANDO"` si cambio > +0,5 kg (subiendo)

**API:** `GET /api/tendencias?usuarioId=<id>&dias=30` devuelve promedios y tendencia.

**Datos devueltos:**
```json
{
  "usuarioId": 1,
  "dias": 30,
  "promedioDiarioKcal": 2150.0,
  "promedioProteinas": 87.5,
  "promedioGrasas": 68.0,
  "promedioCarbos": 285.0,
  "promedioPeso": 58.4,
  "tendencia": "ESTABLE",
  "cambioFecha": "2026-04-17"
}
```

---

## 5.7.7 Configuración de IA personalizada: usuario como propietario de credenciales

### Problema que resuelve

El servidor tiene una configuración por defecto de OpenRouter (modelo, clave API), pero algunos usuarios podrían querer usar su propio modelo o proveedor. Permitir configuración por usuario maximiza flexibilidad.

### Modelo de datos

**Tabla `ia_config`:**

| Columna | Tipo SQL | Descripción |
|---------|----------|-------------|
| `id` | `BIGSERIAL PK` | Clave primaria |
| `usuario_id` | `BIGINT FK UNIQUE` | Usuario (máximo una config por usuario) |
| `proxy_url` | `VARCHAR(500)` | URL del proxy (ej., OpenRouter endpoint) |
| `modelo` | `VARCHAR(200)` | Identificador del modelo (ej., `openai/gpt-4`) |
| `api_key` | `VARCHAR(500)` | Clave API (almacenada en texto plano en MVP) |
| `actualizado_en` | `TIMESTAMP` | Timestamp de última actualización |

### API

- `GET /api/ia-config?usuarioId=<id>`: obtiene config del usuario. 404 si no existe (usará default).
- `PUT /api/ia-config?usuarioId=<id>` con body `{ proxyUrl, modelo, apiKey }`: actualiza.
- `DELETE /api/ia-config?usuarioId=<id>`: elimina (vuelve a default).

### Fallback en servicios de IA

Cada servicio que utiliza IA implementa el mismo patrón:

```java
// EvaluacionIaService.java (pseudocódigo)
public String evaluarConIA(EvaluacionIaRequest request) {
    // 1. Buscar config del usuario
    IaConfig config = iaConfigRepository.findByUsuarioId(request.getUsuarioId())
        .orElse(null);
    
    // 2. Si no existe, usar default
    if (config == null) {
        config = new IaConfig(
            defaultProxyUrl,
            defaultModel,
            defaultApiKey
        );
    }
    
    // 3. Llamar a OpenRouter (o proxy) con esa config
    return openRouterClient.call(config, prompt);
}
```

### Medidas de seguridad

**Protección SSRF:** `proxyUrl` se valida en dos capas antes de usarse. El DTO exige formato `https://dominio/...` mediante `@Pattern`. El servicio bloquea programáticamente todos los rangos de IP privada (localhost, 127.x, 10.x, 172.16-31.x, 192.168.x, 169.254.x). Una URL como `http://localhost:8080/admin` o `http://169.254.169.254/` es rechazada con HTTP 400 antes de que se realice ninguna petición HTTP saliente.

**Rate limiting en prueba de configuración:** el endpoint `POST /api/ia-config/test` está sujeto al mismo `IaRateLimiter` (5 req/min/usuario) que el resto de endpoints IA, para evitar que se use como herramienta de escaneo de servicios externos.

**Enmascaramiento de API key:** `GET /api/ia-config` devuelve la clave con los primeros caracteres reemplazados por `••••`, mostrando solo los últimos cuatro. La clave real nunca se expone en respuestas HTTP.

### Limitaciones conocidas

- Las claves API se almacenan en texto plano en la BD (MVP). En producción deberían cifrarse.

---

## 5.7.8 Integración de módulos avanzados en el frontend

React expone todas estas funcionalidades a través de un dashboard que:

1. **Resumen:** muestra calorías, macros, hidratación, peso, puntos del día.
2. **Plan semanal:** botón para generar plan (asíncrono), desplegable con el menú.
3. **Retos:** panel de retos activos, botón para aceptar nuevos.
4. **Detective Nutricional:** botón para iniciar análisis forense IA, indicador de progreso y renderizado del informe por secciones.
5. **Escáner:** permite capturar código de barras (ej., via cámara web si está disponible, o ingreso manual).
6. **Historial de peso:** gráfico de peso a lo largo del tiempo, tendencia actual.
7. **Configuración:** formulario para personalizar modelo y clave API de IA, con botón de prueba de conexión.

La interfaz integra Framer Motion para transiciones suaves. Los datos se cargan con hooks personalizados que manejan estado y errores de forma elegante.

---

## Cierre de la sección

Los módulos avanzados (5.7.1–5.7.7) transforman NutriFit de una herramienta de registro en una plataforma de análisis y engagement. La integración con IA mediante OpenRouter permite generación de planes, evaluaciones personalizadas y análisis forense nutricional. La gamificación incentiva el uso consistente. El escáner reduce fricción en el registro. El historial y las tendencias proporcionan perspectiva longitudinal. Cada módulo se construyó con fallback explícito (si IA falla, devuelve default; si OpenFoodFacts no responde, devuelve 404) para garantizar robustez incluso con dependencias externas frágiles.

