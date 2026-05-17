# 4.2 Diseño de la base de datos

El modelo de datos de NutriFit es relacional y se gestiona íntegramente a través de PostgreSQL. El esquema está versionado con Flyway en veintidós migraciones (V1 a V22) y se compone de más de veinte tablas que cubren desde el núcleo de autenticación y nutrición hasta integraciones avanzadas de IA, gamificación e historial. Este apartado describe la estructura de las tablas principales, las relaciones entre ellas y las decisiones de diseño más relevantes.

---

## 4.2.1 Versionado del esquema con Flyway

El esquema no se define manualmente ni se recrea en cada arranque. Flyway aplica las migraciones en orden al iniciar el backend y registra cada ejecución en la tabla interna `flyway_schema_history`. Si el esquema ya está al día, Flyway lo valida y no ejecuta ninguna migración.

El directorio `backend/src/main/resources/db/migration/` contiene veintidós scripts organizados por dominio funcional:

| Rango | Propósito |
|-------|-----------|
| V1–V5 | Núcleo: usuarios, autenticación, alimentos, comidas |
| V6–V7 | Ejercicios: tipos de ejercicios, registro de sesiones |
| V8–V9 | Resumen diario: procedimiento almacenado para agregación nutricional |
| V10–V13 | Índices, constraints, peso histórico, hidratación |
| V14–V15 | Planes semanales (IA), retos y gamificación |
| V16–V19 | Lista de compra, tipos de ejercicios adicionales, sugerencias |
| V20–V22 | Intensidad anaeróbica, constraint fixes, configuración de IA |

Este enfoque garantiza que cualquier entorno arrancado desde cero queda con el mismo esquema que el entorno de desarrollo, sin pasos manuales adicionales. PostgreSQL es compatible con todas las características utilizadas: tipos `NUMERIC`, `TIMESTAMP`, restricciones de clave foránea con borrado en cascada, e índices.

---

## 4.2.2 Tablas del modelo de datos

### `usuarios`

Tabla central del modelo. Almacena las credenciales de acceso y los datos biométricos del usuario:

```sql
CREATE TABLE usuarios (
    id               BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    nombre           VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    sexo             CHAR(1) NOT NULL,  -- 'H' o 'M'
    fecha_nacimiento DATE NOT NULL,
    altura_cm        SMALLINT NOT NULL,
    peso_kg_actual   NUMERIC(5,2) NOT NULL,
    peso_objetivo    NUMERIC(5,2),
    nivel_actividad  VARCHAR(20) NOT NULL,  -- 'SEDENTARIO', 'LIGERO', 'MODERADO', 'ALTO', 'MUY_ALTO'
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX idx_usuarios_email ON usuarios(email);
```

La columna `email` tiene restricción `UNIQUE`, lo que impide duplicados a nivel de base de datos. `password_hash` almacena el resultado de BCrypt (típicamente 60 caracteres); la contraseña en texto plano nunca se persiste.

Los campos biométricos se usan en el módulo de perfil para calcular la TMB (Tasa Metabólica Basal) y el TDEE (Gasto Energético Total Diario) mediante la fórmula de Mifflin-St Jeor. `peso_objetivo` es el único campo nullable del conjunto, ya que un usuario puede no haber definido un objetivo de peso.

### `sesiones`

Tabla de sesiones activas. Cada fila representa un token de acceso válido:

```sql
CREATE TABLE sesiones (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    usuario_id  BIGINT NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
CREATE INDEX idx_sesiones_token ON sesiones(token);
CREATE INDEX idx_sesiones_usuario_id ON sesiones(usuario_id);
```

`token` almacena un UUID v4 generado con `SecureRandom`. La restricción `UNIQUE` previene colisiones. `expires_at` marca el momento de expiración, fijado a siete días desde la creación. El `HandlerInterceptor` consulta esta tabla en cada petición para validar el token. Si ha expirado, se devuelve 401 Unauthorized.

### `alimentos`

Catálogo compartido de alimentos. No pertenece a ningún usuario específico:

```sql
CREATE TABLE alimentos (
    id             BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    nombre         VARCHAR(150) NOT NULL,
    porcion_g      NUMERIC(7,2),
    kcal_por_100g  NUMERIC(7,2) NOT NULL,
    proteinas_g    NUMERIC(7,2) NOT NULL,
    grasas_g       NUMERIC(7,2) NOT NULL,
    carbos_g       NUMERIC(7,2) NOT NULL,
    fuente         VARCHAR(100),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_alimentos_nombre ON alimentos(nombre);
```

Los valores nutricionales se expresan por 100 g. El campo `porcion_g` recoge la porción de referencia habitual del alimento, pero es opcional. El campo `fuente` identifica el origen del registro; los alimentos cargados por migraciones semilla tienen `fuente = 'seed'`, mientras que los agregados por OpenFoodFacts tienen `fuente = 'openfoodfacts'`.

El índice `idx_alimentos_nombre` acelera las búsquedas por nombre, que son la operación más frecuente.

### `comidas`

Agrupa los ítems consumidos en un momento del día concreto:

```sql
CREATE TABLE comidas (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    usuario_id  BIGINT NOT NULL,
    fecha       DATE NOT NULL,
    tipo        VARCHAR(30) NOT NULL,  -- 'DESAYUNO', 'ALMUERZO', 'CENA', etc.
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
CREATE INDEX idx_comidas_usuario_fecha ON comidas(usuario_id, fecha);
```

`fecha` identifica el día de la comida. `tipo` recoge el momento del día; los valores válidos están definidos en la lógica de aplicación, no como `ENUM` en base de datos, lo que permite añadirlos sin alterar el esquema.

### `comida_alimentos` (ítems de comidas)

Tabla de unión entre `comidas` y `alimentos`. Registra qué alimentos componen cada comida y en qué cantidad:

```sql
CREATE TABLE comida_alimentos (
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    comida_id    BIGINT NOT NULL,
    alimento_id  BIGINT NOT NULL,
    gramos       NUMERIC(7,2) NOT NULL,
    FOREIGN KEY (comida_id) REFERENCES comidas(id) ON DELETE CASCADE,
    FOREIGN KEY (alimento_id) REFERENCES alimentos(id) ON DELETE RESTRICT
);
CREATE INDEX idx_comida_alimentos_comida_id ON comida_alimentos(comida_id);
```

La columna `gramos` es el dato propio de la relación y es imprescindible para el cálculo de valores nutricionales por proporción. La fórmula aplicada en el resumen diario es:

```
kcal_ítem = (kcal_por_100g × gramos) / 100
```

### `ejercicios_tipo`

Catálogo de tipos de ejercicios con sus factores MET:

```sql
CREATE TABLE ejercicios_tipo (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    factor_met  NUMERIC(4,2) NOT NULL,  -- 3.5 para caminar, 7.0 para correr, etc.
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ejercicios_tipo_nombre ON ejercicios_tipo(nombre);
```

### `ejercicios_registro`

Registro de sesiones de ejercicio de cada usuario:

```sql
CREATE TABLE ejercicios_registro (
    id                  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    usuario_id          BIGINT NOT NULL,
    ejercicio_tipo_id   BIGINT NOT NULL,
    fecha               DATE NOT NULL,
    duracion_min        NUMERIC(6,2) NOT NULL,
    peso_kg_usado       NUMERIC(5,2) NOT NULL,  -- Peso del usuario en el momento del registro
    kcal_quemadas       NUMERIC(7,2) NOT NULL,  -- Calculadas: MET × peso × (duracion / 60)
    intensidad_anaerob  VARCHAR(20),  -- 'BAJA', 'MEDIA', 'ALTA', etc. (opcional)
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (ejercicio_tipo_id) REFERENCES ejercicios_tipo(id)
);
CREATE INDEX idx_ejercicios_registro_usuario_fecha ON ejercicios_registro(usuario_id, fecha);
```

### `peso_historial`

Historial de pesajes del usuario:

```sql
CREATE TABLE peso_historial (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    usuario_id  BIGINT NOT NULL,
    peso_kg     NUMERIC(5,2) NOT NULL,
    fecha       DATE NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
CREATE INDEX idx_peso_historial_usuario_fecha ON peso_historial(usuario_id, fecha);
```

### `hidratacion_registro`

Registro de consumo de agua:

```sql
CREATE TABLE hidratacion_registro (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    usuario_id  BIGINT NOT NULL,
    volumen_ml  NUMERIC(6,0) NOT NULL,
    fecha       DATE NOT NULL,
    hora        TIME,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
CREATE INDEX idx_hidratacion_usuario_fecha ON hidratacion_registro(usuario_id, fecha);
```

### `plan_semanal`

Planes nutricionales generados por IA:

```sql
CREATE TABLE plan_semanal (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    usuario_id  BIGINT NOT NULL,
    contenido   TEXT NOT NULL,  -- Plan generado por OpenRouter
    fecha_gen   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
CREATE INDEX idx_plan_semanal_usuario_fecha ON plan_semanal(usuario_id, fecha_gen);
```

### `reto`

Catálogo de retos disponibles:

```sql
CREATE TABLE reto (
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(255),
    puntos       INT NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### `usuario_reto`

Asignación de retos a usuarios:

```sql
CREATE TABLE usuario_reto (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    usuario_id      BIGINT NOT NULL,
    reto_id         BIGINT NOT NULL,
    fecha_asigna    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_completa  TIMESTAMP,
    completado      BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (reto_id) REFERENCES reto(id),
    UNIQUE (usuario_id, reto_id)  -- Un reto activo por vez
);
```

### `lista_compra`

Lista de compra del usuario:

```sql
CREATE TABLE lista_compra (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    usuario_id  BIGINT NOT NULL,
    alimento_id BIGINT NOT NULL,
    cantidad_g  NUMERIC(7,2),
    comprado    BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (alimento_id) REFERENCES alimentos(id) ON DELETE CASCADE
);
CREATE INDEX idx_lista_compra_usuario ON lista_compra(usuario_id);
```

### `usuario_ia_config`

Configuración personal de OpenRouter por usuario:

```sql
CREATE TABLE usuario_ia_config (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    usuario_id  BIGINT NOT NULL UNIQUE,
    proxy_url   VARCHAR(255) NOT NULL DEFAULT 'https://openrouter.ai/api/v1',
    modelo      VARCHAR(100) NOT NULL DEFAULT 'gpt-3.5-turbo',
    api_key     VARCHAR(500) NOT NULL,  -- Se recomienda cifrar en producción
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
```

---

## 4.2.3 Relaciones y cardinalidades

El modelo de datos incluye las siguientes relaciones:

**Tabla 4.1 — Relaciones del modelo de datos**

| Entidad origen | Cardinalidad | Entidad destino | Comportamiento al borrar |
|---|---|---|---|
| `usuarios` | 1 — 0..* | `sesiones` | `ON DELETE CASCADE` |
| `usuarios` | 1 — 0..* | `comidas` | `ON DELETE CASCADE` |
| `usuarios` | 1 — 0..* | `ejercicios_registro` | `ON DELETE CASCADE` |
| `usuarios` | 1 — 0..* | `peso_historial` | `ON DELETE CASCADE` |
| `usuarios` | 1 — 0..* | `hidratacion_registro` | `ON DELETE CASCADE` |
| `usuarios` | 1 — 0..* | `plan_semanal` | `ON DELETE CASCADE` |
| `usuarios` | 1 — 0..* | `usuario_reto` | `ON DELETE CASCADE` |
| `usuarios` | 1 — 0..* | `lista_compra` | `ON DELETE CASCADE` |
| `usuarios` | 1 — 1 | `usuario_ia_config` | `ON DELETE CASCADE` |
| `comidas` | 1 — 0..* | `comida_alimentos` | `ON DELETE CASCADE` |
| `alimentos` | 1 — 0..* | `comida_alimentos` | `ON DELETE RESTRICT` |
| `alimentos` | 1 — 0..* | `lista_compra` | `ON DELETE CASCADE` |
| `ejercicios_tipo` | 1 — 0..* | `ejercicios_registro` | `ON DELETE RESTRICT` |
| `reto` | 1 — 0..* | `usuario_reto` | `ON DELETE CASCADE` |

Un usuario puede tener cero o más sesiones activas, comidas registradas, ejercicios, pesajes, registros de hidratación, planes, retos asignados y una configuración de IA única. Una comida puede tener cero o más ítems.

---

## 4.2.4 Decisiones de diseño

### Datos biométricos integrados en `usuarios`

Los campos `sexo`, `fecha_nacimiento`, `altura_cm`, `peso_kg_actual`, `peso_objetivo` y `nivel_actividad` están directamente en `usuarios`, no en una tabla separada `perfiles`.

La alternativa habitual — una tabla `perfiles` con clave foránea a `usuarios` en relación 1:1 — tiene sentido cuando el perfil puede no existir, cuando lo gestiona un sistema distinto o cuando el esquema tiene muchos campos opcionales que conviene aislar. En NutriFit, los datos biométricos forman parte del modelo principal de usuario y se almacenan junto con sus credenciales. El cálculo de TMB y TDEE depende de estos datos desde el primer uso. Crear una tabla adicional solo para alojar seis columnas habría introducido un join innecesario sin ventajas estructurales.

### `comida_alimentos` como tabla de unión con dato propio

La relación entre comidas y alimentos no es puramente asociativa. Incluye `gramos`, el peso consumido de ese alimento en esa comida concreta.

Sin ese campo, no habría forma de calcular las calorías ni los macros de una comida: el catálogo `alimentos` almacena los valores por 100 g, y la cantidad real consumida varía en cada registro. La fórmula aplicada es:

```
kcal_ítem = (kcal_por_100g × gramos) / 100
```

El mismo cálculo se aplica a proteínas, grasas y carbohidratos. `comida_alimentos` no es, por tanto, una tabla de paso transparente, sino la que contiene el dato imprescindible para toda la lógica nutricional.

### `ON DELETE CASCADE` y `ON DELETE RESTRICT`

Las reglas de borrado en cascada presentes en el esquema responden a la misma lógica: los datos dependientes no tienen sentido sin su propietario.

- Si se elimina un usuario, sus sesiones, comidas, ejercicios, pesos, hidratación, planes, retos y configuración de IA dejan de tener referencia válida. `CASCADE` garantiza que el borrado es atómico y completo.
- Si se elimina una comida, sus ítems en `comida_alimentos` son igualmente dependientes. `CASCADE` aplica.

La regla `RESTRICT` en las relaciones `alimentos → comida_alimentos` y `ejercicios_tipo → ejercicios_registro` tiene la dirección contraria: impide eliminar un alimento o tipo de ejercicio si existe algún registro que lo referencia. Esto protege la integridad del historial:
- Si un alimento pudiera borrarse libremente, los registros anteriores quedarían con una referencia rota.
- Si un tipo de ejercicio pudiera borrarse, los registros de sesiones anteriores serían incoherentes.

`RESTRICT` convierte esos intentos de borrado en errores explícitos, forzando a la aplicación a decidir qué hacer antes de proceder.

### PostgreSQL en lugar de MariaDB

El proyecto inicialmente especificaba MariaDB, pero PostgreSQL ha reemplazado a MariaDB como motor de base de datos en la implementación final. PostgreSQL proporciona:

- Mejor soporte para tipos avanzados (`JSON`, `NUMERIC` con precisión arbitraria, arrays)
- Mejor rendimiento en queries analíticas y complejas
- Mejor integración con plataformas de hosting modernas (Render, Railway, Supabase)
- Mejor compatibilidad con Flyway y drivers Java estándar
- Mejor soporte para funciones y procedimientos almacenados

Todas las migraciones están escritas en sintaxis PostgreSQL estándar. El driver JDBC es `org.postgresql:postgresql`, gestionado por el BOM de Spring Boot.

### Índices para optimización de queries

Se han añadido índices en columnas de búsqueda frecuente:
- `idx_usuarios_email`: búsqueda rápida durante login
- `idx_sesiones_token`: validación rápida de token en cada petición
- `idx_alimentos_nombre`: búsqueda parcial de alimentos
- `idx_comidas_usuario_fecha`: listado de comidas por usuario y día
- `idx_comida_alimentos_comida_id`: obtención de ítems de una comida
- `idx_ejercicios_tipo_nombre`: búsqueda de tipos de ejercicios
- `idx_ejercicios_registro_usuario_fecha`: historial de ejercicios por usuario
- `idx_peso_historial_usuario_fecha`: evolución del peso
- `idx_hidratacion_usuario_fecha`: consumo de agua por día
- `idx_lista_compra_usuario`: lista de compra del usuario

Estos índices aceleran las operaciones más frecuentes sin sacrificar el rendimiento en escritura.

### `usuario_ia_config` como relación 1:1

Cada usuario tiene a lo sumo una configuración de OpenRouter. Se usa una relación 1:1 mediante `UNIQUE (usuario_id)` en lugar de crear una tabla separada de configuración genérica. Esto es más simple que una tabla de configuración key-value y refleja la realidad del dominio: los parámetros de IA (proxy_url, modelo, api_key) son específicos de OpenRouter.

Si en el futuro se requiere soporte para otros proveedores de IA, se puede evolucionar el esquema sin romper la funcionalidad existente.
