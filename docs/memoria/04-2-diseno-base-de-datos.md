# 4.2 Diseño de la base de datos

El modelo de datos de NutriFit es relacional y se gestiona íntegramente a través de MariaDB. El esquema está versionado con Flyway y se compone de cinco tablas: `usuarios`, `alimentos`, `sesiones`, `comidas` y `comida_alimentos`. Este apartado describe la estructura de cada tabla, las relaciones entre ellas y las decisiones de diseño más relevantes.

---

## 4.2.1 Versionado del esquema con Flyway

El esquema no se define manualmente ni se recrea en cada arranque. Flyway aplica las migraciones en orden al iniciar el backend y registra cada ejecución en la tabla interna `flyway_schema_history`. Si el esquema ya está al día, Flyway lo valida y no ejecuta ninguna migración.

El directorio `backend/src/main/resources/db/migration/` contiene cinco scripts:

| Migración | Contenido |
|-----------|-----------|
| `V2__create_core_tables.sql` | Tablas `usuarios` y `alimentos` |
| `V3__auth_sessions.sql`      | Tabla `sesiones`                |
| `V4__seed_alimentos.sql`     | Datos iniciales del catálogo    |
| `V5__comidas.sql`            | Tablas `comidas` y `comida_alimentos` |

Este enfoque garantiza que cualquier entorno arrancado desde cero queda con el mismo esquema que el entorno de desarrollo, sin pasos manuales adicionales.

---

## 4.2.2 Tablas del modelo

### `usuarios`

Tabla central del modelo. Almacena las credenciales de acceso y los datos biométricos del usuario:

```sql
CREATE TABLE IF NOT EXISTS usuarios (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    nombre           VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    sexo             ENUM('H','M') NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    altura_cm        SMALLINT NOT NULL,
    peso_kg_actual   DECIMAL(5,2) NOT NULL,
    peso_objetivo    DECIMAL(5,2) NULL,
    nivel_actividad  ENUM('SEDENTARIO','LIGERO','MODERADO','ALTO','MUY_ALTO') NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuarios_email (email)
) ENGINE=InnoDB;
```

La columna `email` tiene restricción `UNIQUE`, lo que impide duplicados a nivel de base de datos con independencia de lo que haga la capa de aplicación. `password_hash` almacena el resultado de BCrypt; la contraseña en texto plano nunca se persiste.

Los campos biométricos (`sexo`, `fecha_nacimiento`, `altura_cm`, `peso_kg_actual`, `peso_objetivo`, `nivel_actividad`) se usan en el módulo de perfil para calcular la TMB y el TDEE con la fórmula de Mifflin-St Jeor. `peso_objetivo` es el único campo nullable del conjunto, ya que un usuario puede no haber definido un objetivo de peso.

### `alimentos`

Catálogo compartido de alimentos. No pertenece a ningún usuario:

```sql
CREATE TABLE IF NOT EXISTS alimentos (
    id             BIGINT NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(150) NOT NULL,
    porcion_g      DECIMAL(7,2) NULL,
    kcal_por_100g  DECIMAL(7,2) NOT NULL,
    proteinas_g    DECIMAL(7,2) NOT NULL,
    grasas_g       DECIMAL(7,2) NOT NULL,
    carbos_g       DECIMAL(7,2) NOT NULL,
    fuente         VARCHAR(100) NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_alimentos_nombre (nombre)
) ENGINE=InnoDB;
```

Los valores nutricionales se expresan por 100 g. El campo `porcion_g` recoge la porción de referencia habitual del alimento, pero es opcional. El campo `fuente` identifica el origen del registro; los diez alimentos cargados por la migración `V4__seed_alimentos.sql` tienen `fuente = 'seed'`.

El índice `idx_alimentos_nombre` acelera las búsquedas por nombre, que son la operación más frecuente sobre esta tabla desde el cliente.

### `sesiones`

Tabla de sesiones activas. Cada fila representa un token de acceso válido:

```sql
CREATE TABLE sesiones (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id  BIGINT NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
```

`token` almacena un UUID v4 generado con `SecureRandom`. La restricción `UNIQUE` impide colisiones en base de datos. `expires_at` marca el momento de expiración, fijado a siete días desde la creación. El mecanismo de autenticación se describe en detalle en la sección §7.2.

### `comidas`

Agrupa los ítems consumidos en un momento del día concreto:

```sql
CREATE TABLE comidas (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id  BIGINT NOT NULL,
    fecha       DATE NOT NULL,
    tipo        VARCHAR(30) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comidas_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
```

`fecha` identifica el día de la comida. `tipo` recoge el momento del día (por ejemplo, `DESAYUNO`, `MERIENDA`, `CENA`); los valores válidos están definidos en la lógica de aplicación, no como `ENUM` en base de datos, lo que permite añadirlos sin alterar el esquema.

### `comida_alimentos`

Tabla de unión entre `comidas` y `alimentos`. Registra qué alimentos componen cada comida y en qué cantidad:

```sql
CREATE TABLE comida_alimentos (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    comida_id    BIGINT NOT NULL,
    alimento_id  BIGINT NOT NULL,
    gramos       DECIMAL(7,2) NOT NULL,
    CONSTRAINT fk_comida_alimentos_comida
        FOREIGN KEY (comida_id) REFERENCES comidas(id) ON DELETE CASCADE,
    CONSTRAINT fk_comida_alimentos_alimento
        FOREIGN KEY (alimento_id) REFERENCES alimentos(id) ON DELETE RESTRICT
);
```

La columna `gramos` es el dato propio de la relación y hace posible el cálculo de valores nutricionales por proporción. El razonamiento detrás de este diseño se desarrolla en §4.2.4.

---

## 4.2.3 Relaciones y cardinalidades

El diagrama entidad-relación del proyecto (`docs/diagrams/er-diagram.puml`) refleja las siguientes cuatro relaciones:

**Tabla 4.1 — Relaciones del modelo de datos**

| Entidad origen | Cardinalidad | Entidad destino   | Comportamiento al borrar origen |
|---------------|:------------:|-------------------|---------------------------------|
| `usuarios`    | 1 — 0..*     | `sesiones`        | `ON DELETE CASCADE`             |
| `usuarios`    | 1 — 0..*     | `comidas`         | `ON DELETE CASCADE`             |
| `comidas`     | 1 — 0..*     | `comida_alimentos`| `ON DELETE CASCADE`             |
| `alimentos`   | 1 — 0..*     | `comida_alimentos`| `ON DELETE RESTRICT`            |

Un usuario puede tener cero o más sesiones activas simultáneas y cero o más comidas registradas. Una comida puede tener cero o más ítems. Un alimento puede aparecer en cero o más ítems de distintas comidas.

---

## 4.2.4 Decisiones de diseño

### Datos biométricos en `usuarios`, sin tabla `perfiles`

Los campos `sexo`, `fecha_nacimiento`, `altura_cm`, `peso_kg_actual`, `peso_objetivo` y `nivel_actividad` están directamente en `usuarios`, no en una tabla separada.

La alternativa habitual — una tabla `perfiles` con clave foránea a `usuarios` en relación 1:1 — tiene sentido cuando el perfil puede no existir, cuando lo gestiona un sistema distinto o cuando el esquema tiene muchos campos opcionales que conviene aislar. En NutriFit, los datos biométricos forman parte del modelo principal de usuario y se almacenan junto con sus credenciales. Separarlos en una tabla adicional habría introducido una unión extra sin aportar ventajas claras para el alcance del proyecto.: el cálculo de TMB y TDEE depende de estos datos desde el primer uso. Crear una tabla adicional solo para alojar seis columnas habría introducido una join innecesaria en cada cálculo de perfil sin ninguna ventaja estructural.

La nota en el diagrama ER lo resume: _«Almacena también los datos biométricos del perfil. No existe tabla "perfiles" separada»_.

### `comida_alimentos` como tabla de unión con dato propio

La relación entre comidas y alimentos no es puramente asociativa. Incluye `gramos`, el peso consumido de ese alimento en esa comida concreta.

Sin ese campo, no habría forma de calcular las calorías ni los macros de una comida: el catálogo `alimentos` almacena los valores por 100 g, y la cantidad real consumida varía en cada registro. La fórmula aplicada en el resumen diario es:

```
kcal_ítem = (kcal_por_100g × gramos) / 100
```

El mismo cálculo se aplica a proteínas, grasas y carbohidratos. `comida_alimentos` no es, por tanto, una tabla de paso transparente, sino la que contiene el dato imprescindible para toda la lógica nutricional del sistema.

### `ON DELETE CASCADE` en sesiones y comidas, `ON DELETE RESTRICT` en alimentos

Las dos reglas de borrado en cascada presentes en el esquema responden a la misma lógica: los datos dependientes no tienen sentido sin su propietario.

- Si se elimina un usuario, sus sesiones y sus comidas dejan de tener referencia válida. Mantenerlos produciría filas huérfanas sin posibilidad de recuperación. `CASCADE` garantiza que el borrado es atómico y completo.
- Si se elimina una comida, sus ítems en `comida_alimentos` son igualmente huérfanos. `CASCADE` aplica también aquí.

La regla `RESTRICT` en la relación `alimentos → comida_alimentos` tiene la dirección contraria: impide eliminar un alimento del catálogo si existe algún ítem que lo referencia. Esto protege la integridad del historial de comidas: si un alimento pudiera borrarse libremente, los registros anteriores quedarían con una referencia rota, y los cálculos de resumen diario de días pasados arrojarían resultados incorrectos o fallarían. `RESTRICT` convierte ese intento de borrado en un error explícito, forzando a la aplicación a decidir qué hacer antes de proceder.
