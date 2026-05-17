# 0007 — PostgreSQL como base de datos en lugar de MariaDB

## Decisión

La base de datos de NutriFit se implementa en **PostgreSQL** (versión 14+),
alojada en Render, en lugar de MariaDB como se planificó inicialmente.

---

## Contexto

El proyecto comenzó con MariaDB como base de datos local para desarrollo.
Sin embargo, al pasar a un entorno de producción surgieron dos restricciones:

1. **Compatibilidad de hosting**: Render (hosting gratuito) proporciona PostgreSQL gestionado, no MariaDB
2. **Migrabilidad**: PostgreSQL es más estándar en ecosistema Java/Spring Boot moderno

En ese contexto se evaluó:

| BD | Ventaja | Inconveniente |
|---|---|---|
| MariaDB (original) | Compatible con desarrollo local | No disponible en Render free tier |
| PostgreSQL (elegida) | Disponible en Render, mejor soporte Spring Boot | Cambio de sintaxis SQL |

---

## Motivo de la elección

### 1. Disponibilidad en Render (hosting libre)

Render ofrece:
- PostgreSQL gestionado con backups automáticos
- 90 días de retención de datos
- HTTPS y SSL automáticos
- Variables de entorno para connection string

MariaDB no está disponible en free tier de Render.

```yaml
# render.yaml
databases:
  - name: nutrifit
    engine: postgresql
    version: "14"
```

### 2. Mejor integración con Spring Boot + Flyway

PostgreSQL es el estándar de facto en ecosistema Spring:

- **JDBC driver**: `org.postgresql:postgresql` es muy maduro
- **Tipos nativos**: TIMESTAMP, SERIAL, UUID (mejor que AUTO_INCREMENT)
- **Flyway**: soporte first-class para PostgreSQL
- **Índices y funciones**: más potentes (ej. full-text search)

### 3. Sintaxis SQL más moderna

PostgreSQL ofrece:

```sql
-- Generación de claves
id BIGSERIAL PRIMARY KEY  -- Mejor que AUTO_INCREMENT

-- Timestamps por defecto
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

-- Tipos mejorados
token UUID UNIQUE
status VARCHAR(50)

-- Índices con nombre explícito
CREATE INDEX idx_usuario_email ON usuarios(email);
```

MariaDB usa:

```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- Sin soporte nativo para UUID
```

### 4. Migraciones Flyway más predecibles

Con PostgreSQL:
- `V1__init.sql`: Sintaxis estándar PostgreSQL
- Versionado automático en tabla `flyway_schema_history`
- No hay incompatibilidades entre development y production

Con MariaDB:
- Sintaxis similar pero con pequeñas diferencias (ej. `AUTO_INCREMENT` en MariaDB vs `SERIAL` en PostgreSQL)
- Cambiar de MariaDB en dev a PostgreSQL en prod requiere migración de scripts

### 5. Mejor manejo de transacciones y locks

PostgreSQL:
- ACID completo con aislamiento MVCC
- Mejor para aplicaciones con múltiples usuarios
- Locks optimistas y pesimistas más predecibles

---

## Alternativas consideradas

### Opción 1: Mantener MariaDB en dev, usar MariaDB en Render
**Rechazada porque:**
- Render no soporta MariaDB en free tier
- Habría que migrar a otra plataforma pagada
- Inconsistencia dev vs prod

### Opción 2: H2 en memoria para desarrollo
**Rechazada porque:**
- Sintaxis SQL distinta a production (PostgreSQL)
- No permite debugging de queries reales
- Cambios en schema son más frágiles

### Opción 3: Docker local con PostgreSQL en compose
**Aceptada como complemento:**
- Desarrollo local con `docker-compose up` ejecuta PostgreSQL igual a Render
- Permite testing con sintaxis idéntica a producción
- Opcional pero recomendado

---

## Diseño elegido

**Configuración de desarrollo:**

```yaml
# docker-compose.yml (opcional pero recomendado)
services:
  postgres:
    image: postgres:14-alpine
    environment:
      POSTGRES_DB: nutrifit
      POSTGRES_PASSWORD: dev
    ports:
      - "5432:5432"
```

**Configuración de producción en Render:**

```yaml
# render.yaml
databases:
  - name: nutrifit
    engine: postgresql
    version: "14"
    plan: free
    numComputeUnits: 0.25
```

**Driver JDBC:**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>
```

**Connection pool:**

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nutrifit
spring.datasource.username=postgres
spring.datasource.password=dev
spring.datasource.hikari.maximum-pool-size=10
```

**Migraciones Flyway:**

```
src/main/resources/db/migration/
├── V1__init.sql
├── V2__add_usuarios.sql
├── ...
└── V22__add_registro_intensidad_anaer.sql
```

---

## Consecuencias asumidas

### Ventajas

1. **Hosting gestionado libre**: Render proporciona backup y HTTPS
2. **Migraciones predecibles**: Flyway con PostgreSQL es estándar
3. **Mejor tooling**: pgAdmin, DBeaver, psql son excelentes
4. **Escalabilidad**: PostgreSQL es más robusto que MariaDB
5. **Coincide con development**: Docker compose con PostgreSQL replica exactamente prod

### Inconvenientes

1. **Cambio de sintaxis**: Requirió reescribir scripts de MariaDB a PostgreSQL
   - `AUTO_INCREMENT` → `SERIAL` / `BIGSERIAL`
   - Algunas funciones de fecha cambian
   - `UNSIGNED` no existe en PostgreSQL (usamos CHECK constraints)

2. **Curva de aprendizaje pequeña**: Diferencias en tipos de datos
   - Mitigation: documentación clara en `docs/db/`

3. **Cold start en Render**: (~30s en primer acceso)
   - No es responsabilidad de PostgreSQL

---

## Decisiones técnicas relacionadas

- **Flyway**: versionado de migraciones en 22 versiones (V1 a V22)
- **JdbcTemplate**: acceso manual a datos sin ORM, SQL explícito
- **RowMapper**: cada entidad mapea manualmente sus resultados
- **Índices**: creados explícitamente en migraciones Flyway

---

## Alcance implementado

✅ PostgreSQL 14 en Render
✅ 22 migraciones Flyway (V1–V22)
✅ 18 módulos backend usando JdbcTemplate
✅ Tablas: usuarios, sesiones, alimentos, comidas, comida_alimentos, resumen_diario, perfil, ejercicios, etc.
✅ Full ACID compliance
✅ Backups automáticos en Render

---

## Próximo paso recomendado

Mantener sincronizado un `docker-compose.yml` en repositorio para que nuevos
desarrolladores puedan reproducir environment exactamente. Considerar agregar
extensiones de PostgreSQL (ej. `uuid-ossp`, full-text search) en V23+ si es necesario.
