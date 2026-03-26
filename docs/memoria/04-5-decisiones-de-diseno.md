# 4.5 Decisiones de diseño

A lo largo del desarrollo de NutriFit se tomaron decisiones técnicas que condicionaron la estructura del proyecto. Algunas se documentaron de forma explícita en registros de decisión de arquitectura (ADR) en `docs/decisions/`; otras emergen directamente del código. Esta sección reúne las más relevantes, explicando en cada caso el problema que motivó la decisión y las consecuencias que implica.

---

## 4.5.1 Acceso a datos con Spring JDBC en lugar de JPA/Hibernate

**ADR de referencia:** [0004 — Acceso a datos con Spring JDBC en lugar de ORM](../decisions/0004-no-orm-jdbc.md)

El acceso a la base de datos se implementa íntegramente con `JdbcTemplate` y `RowMapper` manuales. No se usa JPA, Spring Data ni Hibernate en ningún módulo.

El esquema de NutriFit es fijo desde el diseño inicial y está gestionado por Flyway. Las consultas son directas: búsquedas por id, filtros por usuario y fecha, y alguna agregación para el resumen diario. En ese contexto, JPA habría añadido complejidad sin aportar ventaja real.

El argumento principal es de trazabilidad. Con `JdbcTemplate`, cada consulta es SQL visible en una línea concreta:

```java
// JdbcAlimentoRepository.java
return jdbcTemplate.query(
    "SELECT * FROM alimentos WHERE nombre LIKE ?",
    rowMapper,
    "%" + q + "%"
);
```

Con Hibernate, la misma operación implica comprender el ciclo de vida de la sesión, el momento de ejecución real del SQL y el comportamiento del proxy. En una defensa de TFG, responder «¿qué SQL genera esta línea?» es inmediato con JDBC y requiere depuración con JPA.

Un argumento adicional es la ausencia de interferencias con Flyway. Configurar JPA con `ddl-auto=validate` junto a Flyway requiere atención extra para que ambos no entren en conflicto al arrancar. Con JDBC, Flyway gestiona el esquema sin ninguna interferencia.

La consecuencia aceptada es más boilerplate: cada repositorio declara sus propias consultas y su propio `RowMapper`. Todos los módulos siguen la misma estructura:

```
XxxRepository (interfaz) → JdbcXxxRepository (implementación con JdbcTemplate)
```

Esta uniformidad facilita la navegación del código y hace que la separación entre contrato e implementación sea explícita en todos los módulos: `auth`, `alimento`, `comida`, `resumen` y `perfil`.

---

## 4.5.2 Autenticación con token opaco en lugar de JWT

**ADR de referencia:** [0005 — Autenticación con token opaco en base de datos en lugar de JWT](../decisions/0005-token-auth.md)

La autenticación se basa en un UUID aleatorio almacenado en la tabla `sesiones`, con fecha de expiración y borrado explícito al hacer logout. No se usa JWT ni ninguna librería de tokens firmados.

La razón central es que JWT no resuelve bien el logout. Un token JWT firmado sigue siendo válido aunque el usuario lo haya invalidado, hasta que expira por sí solo. Invalidarlo antes requeriría mantener una lista negra en base de datos, que es exactamente la misma infraestructura que ya se tiene con el token en `sesiones`, pero con mayor complejidad.

Con el token opaco, el logout es un `DELETE` sobre una fila:

```java
// AuthServiceImpl.java
public void logout(String token) {
    sesionRepository.deleteByToken(token);
}
```

Después de esa llamada, el token no existe y cualquier petición que lo presente no encontrará fila válida. No hay estado residual.

El segundo argumento es la ausencia de gestión de claves criptográficas. JWT con HMAC requiere una clave secreta que debe almacenarse de forma segura, rotarse cuando se compromete y configurarse en cada entorno. En una aplicación de escritorio local eso es complejidad sin beneficio real. `UUID.randomUUID()` usa `SecureRandom` internamente y produce tokens suficientemente imprevisibles para el alcance del proyecto.

La consecuencia asumida es una consulta a base de datos por cada petición autenticada para verificar el token. Para una aplicación de escritorio de un único usuario concurrente, ese coste es irrelevante.

---

## 4.5.3 Cálculo del resumen diario con SQL, no en Java

**ADR de referencia:** [0003 — Implementación del resumen diario nutricional](../decisions/0003-daily-summary-scope.md)

El resumen diario —kilocalorías totales y macronutrientes por usuario y fecha— se calcula mediante una única consulta SQL con `LEFT JOIN`, `SUM`, `ROUND` y `COALESCE`:

```java
// JdbcResumenDiarioRepository.java
String sql = """
    SELECT
        c.usuario_id,
        c.fecha,
        COALESCE(ROUND(SUM((a.kcal_por_100g * ca.gramos) / 100), 2), 0) AS kcal_totales,
        COALESCE(ROUND(SUM((a.proteinas_g * ca.gramos) / 100), 2), 0) AS proteinas_totales,
        COALESCE(ROUND(SUM((a.grasas_g * ca.gramos) / 100), 2), 0) AS grasas_totales,
        COALESCE(ROUND(SUM((a.carbos_g * ca.gramos) / 100), 2), 0) AS carbos_totales
    FROM comidas c
    LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
    LEFT JOIN alimentos a ON a.id = ca.alimento_id
    WHERE c.usuario_id = ? AND c.fecha = ?
    GROUP BY c.usuario_id, c.fecha
    """;
```

La alternativa descartada era traer todas las comidas e ítems al servicio Java y calcular los totales en memoria. Eso habría requerido múltiples consultas y lógica de agregación en la capa de aplicación, desaprovechando las capacidades de agregación del motor de base de datos.

El uso de `LEFT JOIN` garantiza que la consulta devuelve fila aunque no haya ítems en la fecha solicitada. `COALESCE` convierte los nulos que produce `SUM` sobre un conjunto vacío en ceros, de forma que el cliente siempre recibe un objeto con valores numéricos válidos, no nulos.

La tercera opción considerada —precalcular y almacenar los resúmenes en una tabla dedicada— se descartó por introducir complejidad de sincronización sin necesidad en esta fase.

---

## 4.5.4 Arquitectura por capas con interfaz e implementación separadas

Esta decisión no tiene ADR propio pero es consistente en todos los módulos del backend. Cada módulo sigue la misma estructura vertical:

```
controller/   ← recibe la petición HTTP, delega en el servicio
service/      ← interfaz + implementación con la lógica de negocio
repository/   ← interfaz + implementación JDBC
model/        ← entidades de dominio
dto/          ← objetos de entrada y salida de la API
```

La separación entre interfaz e implementación en la capa de servicio y en la capa de repositorio permite sustituir cada componente en los tests unitarios sin levantar Spring. En `AuthServiceImplTest`, por ejemplo, `SesionRepository` y `PasswordService` son mocks; el test prueba la lógica del servicio sin base de datos. Si los repositorios fueran clases concretas sin interfaz, el aislamiento no sería posible sin `@SpringBootTest`.

Esta estructura también hace que añadir un nuevo módulo sea predecible: el patrón ya existe, solo hay que seguirlo.

---

## 4.5.5 Manejo de errores centralizado con respuesta uniforme

El backend expone un `GlobalExceptionHandler` anotado con `@RestControllerAdvice` que intercepta todas las excepciones lanzadas en cualquier capa y las convierte en una respuesta JSON con estructura fija:

```json
{
  "timestamp": "2026-03-14T02:47:21",
  "status": 400,
  "error": "Bad Request",
  "message": "La porción debe ser mayor que 0",
  "path": "/api/alimentos"
}
```

Sin este componente, Spring devolvería respuestas de error con formatos distintos según el tipo de excepción. El `GlobalExceptionHandler` unifica el contrato: el cliente JavaFX sabe que cualquier error, sea de validación, de recurso no encontrado o de credenciales inválidas, tendrá siempre el mismo esquema y puede leerlo del mismo modo.

Los tipos de excepción del dominio (`ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`) mapean directamente a códigos HTTP específicos (404, 400, 401). Las excepciones no contempladas retornan siempre HTTP 500 con un mensaje genérico, sin exponer información interna del servidor.

---

## 4.5.6 Cliente HTTP nativo de Java en el módulo JavaFX

El cliente JavaFX consume la API REST usando `java.net.http.HttpClient`, disponible de forma nativa desde Java 11, sin dependencias externas como OkHttp o Retrofit.

Cada módulo del cliente tiene su propia clase de servicio (`AlimentoApiClient`, `AuthApiClient`, `PerfilApiClient`, etc.) que construye las peticiones, las envía y deserializa la respuesta con Jackson. La serialización y deserialización JSON es la única dependencia externa del cliente para la comunicación con la API.

Esta decisión reduce el número de dependencias del módulo `client` y mantiene el código de red legible sin frameworks adicionales. Para el volumen de operaciones del proyecto —un único usuario, peticiones síncronas— la API nativa es suficiente y su comportamiento es directo de razonar.

---

## 4.5.7 Cálculo de TMB y TDEE en el servicio de backend, no en el cliente

El módulo de perfil calcula la Tasa Metabólica Basal (TMB) y el Gasto Energético Total Diario (TDEE) en `PerfilServiceImpl`, aplicando la fórmula de Mifflin-St Jeor:

```java
// PerfilServiceImpl.java
private double calcularTmb(Perfil perfil) {
    int edad = Period.between(perfil.getFechaNacimiento(), LocalDate.now()).getYears();
    double base = 10 * perfil.getPesoKgActual()
            + 6.25 * perfil.getAlturaCm()
            - 5 * edad;
    return perfil.getSexo() == Sexo.H ? base + 5 : base - 161;
}
```

Los valores calculados se devuelven en `PerfilResponse` como campos adicionales a los datos biométricos brutos. El cliente recibe el resultado ya calculado y lo muestra directamente.

Calcular en el backend en lugar de en el cliente tiene dos ventajas concretas. Primera: la lógica está en un único lugar y puede probarse de forma aislada con tests unitarios, como se hace en `PerfilServiceImplTest`. Segunda: si en el futuro el cliente cambia de tecnología (de JavaFX a web, por ejemplo), la fórmula no se duplica ni se pierde.
