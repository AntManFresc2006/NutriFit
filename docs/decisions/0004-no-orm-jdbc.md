# 0004 — Acceso a datos con Spring JDBC en lugar de ORM

## Decisión

El acceso a la base de datos se realiza íntegramente con **Spring JDBC** (`JdbcTemplate`)
y asignación manual mediante `RowMapper`.
No se usa JPA, Hibernate ni ningún otro ORM.

---

## Contexto

NutriFit es una aplicación con un esquema de base de datos fijo, conocido desde el diseño
inicial y gestionado mediante migraciones Flyway.
Las consultas son directas y acotadas: búsquedas por id, filtros simples por fecha o usuario
y alguna agregación para el resumen diario.

En ese contexto surgió la elección entre dos enfoques habituales en Spring Boot:

| Enfoque | Herramienta |
|---|---|
| ORM con mapeo automático | Spring Data JPA + Hibernate |
| Acceso JDBC explícito | Spring JDBC + `JdbcTemplate` |

---

## Motivo de la elección

### 1. El código hace exactamente lo que parece

Con `JdbcTemplate`, cada consulta es SQL visible y directo:

```java
// JdbcAlimentoRepository.java
return jdbcTemplate.query(
    "SELECT * FROM alimentos WHERE nombre LIKE ?",
    rowMapper,
    "%" + q + "%"
);
```

Con JPA, la misma operación implica entender proxies, sesiones de Hibernate y cuándo
se ejecuta realmente el SQL. En una defensa de TFG, responder «¿qué SQL lanza esta
línea de código?» es inmediato con JDBC y requiere depuración con JPA.

### 2. Sin colisión con Flyway

JPA en modo `ddl-auto=validate` o `create` interfiere con Flyway.
Mantener ambos funcionando de forma predecible requiere configuración adicional
y es una fuente habitual de errores de arranque.
Con JDBC, Flyway gestiona el esquema sin ninguna interferencia.

### 3. La asignación es explícita y trazable

Cada módulo tiene su propio `RowMapper`:

```java
// AlimentoRowMapper.java
public Alimento mapRow(ResultSet rs, int rowNum) throws SQLException {
    Alimento a = new Alimento();
    a.setId(rs.getLong("id"));
    a.setNombre(rs.getString("nombre"));
    // ...
    return a;
}
```

Si un campo cambia de nombre en la migración SQL, el error aparece aquí,
en una línea concreta. Con JPA, el error suele aparecer en tiempo de arranque
con un stack trace profundo de Hibernate.

### 4. Insertar con clave generada es transparente

```java
// JdbcAlimentoRepository.java
KeyHolder keyHolder = new GeneratedKeyHolder();
jdbcTemplate.update(connection -> {
    PreparedStatement ps = connection.prepareStatement(
        "INSERT INTO alimentos (...) VALUES (...)",
        Statement.RETURN_GENERATED_KEYS
    );
    // bind params
    return ps;
}, keyHolder);
long id = keyHolder.getKey().longValue();
```

El mecanismo es estándar JDBC; no hay magia de ORM que justificar.

---

## Consecuencias asumidas

**Más boilerplate.** Cada repositorio tiene más líneas que su equivalente con
Spring Data JPA (`extends JpaRepository<Alimento, Long>`). Es el coste explícito
de la elección.

**Sin dirty checking.** Las entidades no se actualizan automáticamente al modificar
sus campos en memoria. Hay que llamar al método `update()` del repositorio.
Esto es intencionado: fuerza a ser explícito sobre cuándo se persiste algo.

**Sin lazy loading.** Las relaciones entre tablas se cargan únicamente cuando el
repositorio hace la consulta correspondiente. No hay colecciones que se carguen
de forma inesperada al serializar a JSON.

---

## Alcance de esta decisión

Esta decisión aplica a todos los módulos del backend:
`auth`, `alimento`, `comida`, `resumen` y `perfil`.
Todos implementan la misma estructura:
`Repository (interfaz) → JdbcXxxRepository (implementación con JdbcTemplate)`.
