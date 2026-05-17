# Backend de NutriFit

API REST con Spring Boot, JDBC y MariaDB.

## Estructura

- `src/main/java/`: código fuente por módulos (auth, alimentos, comidas, etc.)
- `src/main/resources/db/migration/`: migraciones Flyway
- `src/test/`: tests unitarios

## Requisitos

- Java 17
- Maven
- MariaDB

## Inicio rápido

### 1. Base de datos

```bash
CREATE DATABASE nutrifit;
```

### 2. Configuración

Crear `backend/src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/nutrifit
spring.datasource.username=nutrifit
spring.datasource.password=nutrifit123
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

server.port=8080
```

### 3. Ejecutar

**Linux/Mac**
```bash
cd backend
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Windows**
```bash
cd backend
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

El servidor arranca en `http://localhost:8080`.

## API

Endpoints principales:

- `POST /api/auth/register` — registro
- `POST /api/auth/login` — login
- `POST /api/auth/logout` — logout
- `GET /api/alimentos` — listar alimentos
- `POST /api/alimentos` — crear alimento
- `GET /api/comidas` — listar comidas
- `POST /api/comidas` — crear comida
- `GET /api/resumen-diario` — resumen del día
- `GET /api/perfil/{id}` — perfil del usuario

Ver `docs/` para la referencia completa.

## Tests

```bash
mvn test
```

## Migraciones

Flyway gestiona el esquema. Los scripts están en `src/main/resources/db/migration/`, numerados (`V1`, `V2`, etc.).

Cada cambio en la BD requiere un nuevo script.

## Autor

Antonio Manuel Fresco Gómez
