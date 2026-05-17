# Backend de NutriFit

API REST con Spring Boot 3, JDBC (JdbcTemplate) y PostgreSQL.

## Estructura

```
src/main/java/
├── controller/        → endpoints HTTP (18 módulos)
├── service/           → lógica de negocio
├── repository/        → acceso a datos (JDBC)
├── dto/               → objetos de transferencia
├── config/            → configuración Spring (interceptores, CORS)
└── exception/         → excepciones personalizadas

src/main/resources/
├── db/migration/      → migraciones Flyway (V1–V22)
└── application-*.properties
```

## Requisitos

- Java 21
- Maven 3.8+
- PostgreSQL 12+

## Inicio rápido

### 1. Crear base de datos

```bash
createdb -U postgres nutrifit
```

O desde psql:
```sql
CREATE DATABASE nutrifit;
```

### 2. Configuración local

Crear `backend/src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nutrifit
spring.datasource.username=nutrifit
spring.datasource.password=nutrifit123
spring.datasource.driver-class-name=org.postgresql.Driver

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

server.port=8080

# Para desarrollo (opcional)
logging.level.root=INFO
logging.level.com.nutrifit=DEBUG
```

> No versionares credenciales reales. Este archivo va en `.gitignore`.

### 3. Ejecutar

```bash
cd backend
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

Backend arranca en `http://localhost:8080`.

## API (18 módulos)

### Autenticación
- `POST /api/auth/register` — registrar usuario
- `POST /api/auth/login` — login (retorna token opaco)
- `POST /api/auth/logout` — logout

### Catálogos
- `GET /api/alimentos` — listar alimentos
- `POST /api/alimentos` — crear alimento
- `PUT /api/alimentos/{id}` — actualizar
- `DELETE /api/alimentos/{id}` — eliminar
- `GET /api/ejercicios` — listar ejercicios
- `POST /api/ejercicios` — crear ejercicio

### Comidas
- `GET /api/comidas?fecha=YYYY-MM-DD` — comidas del día
- `POST /api/comidas` — crear comida
- `POST /api/comidas/{id}/alimentos` — añadir alimento a comida
- `DELETE /api/comidas/{id}/alimentos/{alimentoId}` — remover alimento

### Resumen y análisis
- `GET /api/resumen-diario?fecha=YYYY-MM-DD` — resumen nutricional del día
- `POST /api/resumen/evaluacion-ia` — evaluación IA de nutrición
- `GET /api/tendencias?dias=90` — tendencias (últimos 90 días)

### Perfil y configuración
- `GET /api/perfil/{id}` — perfil del usuario (BMR, TDEE)
- `PUT /api/perfil/{id}` — actualizar perfil
- `GET /api/ia-config` — config IA actual
- `PUT /api/ia-config` — actualizar config IA
- `DELETE /api/ia-config` — eliminar config IA

### Ejercicios y peso
- `POST /api/ejercicios-registro` — registrar ejercicio realizado
- `GET /api/ejercicios-registro?fecha=YYYY-MM-DD` — ejercicios del día
- `POST /api/peso-historial` — registrar peso
- `GET /api/peso-historial?dias=30` — historial de peso

### Otros
- `POST /api/hidratacion` — registrar hidratación
- `GET /api/hidratacion?fecha=YYYY-MM-DD` — hidratación del día
- `GET /api/plan-semanal` — plan de comidas semanal (IA)
- `POST /api/lista-compra` — añadir a lista
- `GET /api/lista-compra` — listar artículos
- `GET /api/escaner/{barcode}` — buscar alimento por código (OpenFoodFacts)
- `GET /api/retos` — listar retos disponibles
- `POST /api/retos/{id}/aceptar` — aceptar reto
- `GET /api/gamificacion` — estadísticas (racha, puntuación)
- `GET /api/health` — health check

Documentación interactiva: `http://localhost:8080/swagger-ui.html`

## Autenticación

Los tokens son strings opacos (UUID v4) almacenados en la tabla `sesiones`. Cada petición API valida el token mediante un `HandlerInterceptor`.

**Flujo**:
1. `POST /api/auth/login` → retorna token en respuesta
2. Cliente envía `Authorization: Bearer <token>` en siguientes peticiones
3. Interceptor valida el token contra `sesiones` tabla

## Migraciones

Flyway controla el esquema. Scripts en `src/main/resources/db/migration/`:

```
V1__initial_schema.sql
V2__add_table_X.sql
...
V22__add_table_Z.sql
```

Cada cambio en BD → nuevo script `V{N}__description.sql`.

## Tests

```bash
mvn test
```

## Despliegue

Backend se despliega automáticamente en Render al hacer push a `main`.

Ver `render.yaml` en la raíz del proyecto.

## Notas de desarrollo

- **JDBC directo**: no hay ORM (Hibernate, JPA). Usamos JdbcTemplate para queries personalizadas.
- **Validación**: implementada en service + DTOs con `@NotBlank`, `@NotNull`, etc.
- **CORS**: configurado para aceptar frontend en desarrollo (`localhost:5173`) y en producción.
- **Preflight**: `OPTIONS` permitidas sin autenticación para cumplir CORS.

## Autor

Antonio Manuel Fresco Gómez
