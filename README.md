# NutriFit

[![CI](https://github.com/AntManFresc2006/NutriFit/actions/workflows/ci.yml/badge.svg)](https://github.com/AntManFresc2006/NutriFit/actions/workflows/ci.yml)
[![Backend](https://img.shields.io/badge/backend-Render-46E3B7?logo=render&logoColor=white)](https://nutrifit-backend-ndoj.onrender.com/api/health)
[![Frontend](https://img.shields.io/badge/frontend-Vercel-black?logo=vercel&logoColor=white)](https://nutri-fit-snowy.vercel.app)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev)
[![License](https://img.shields.io/badge/licencia-uso_académico-blue)](./LICENSE)

Aplicación web de seguimiento nutricional y deportivo, inspirada en MyFitnessPal. Registra comidas, ejercicios, peso e hidratación, con evaluaciones de IA personalizadas.

## Arquitectura

- **Backend**: Spring Boot 3 + JDBC (JdbcTemplate) + PostgreSQL
- **Frontend**: React 18 + TypeScript + Vite + Tailwind CSS + Framer Motion
- **Base de datos**: PostgreSQL (migraciones Flyway V1–V22)
- **Despliegue**: Backend en Render, frontend en Vercel

## Tecnologías principales

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21, Spring Boot 3, Maven |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS |
| Base de datos | PostgreSQL |
| Migraciones | Flyway |
| Auth | Tokens opacos (UUID v4) en tabla `sesiones` |
| APIs externas | OpenRouter (IA), OpenFoodFacts (escáner) |

## Estructura del repositorio

```
NutriFit/
├── backend/           → API REST, lógica de negocio, acceso a datos
├── frontend/          → aplicación React + TypeScript
├── docs/              → documentación técnica
└── docker-compose.yml → configuración Docker (opcional)
```

## Desarrollo local

### 1. Backend (Spring Boot)

**Requisitos**: Java 21, Maven, PostgreSQL

```bash
cd backend
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

Backend arranca en `http://localhost:8080`.

### 2. Frontend (React)

**Requisitos**: Node.js 18+, npm

```bash
cd frontend
npm install
npm run dev
```

Frontend arranca en `http://localhost:5173`.

### 3. Variables de entorno

**Frontend** (`frontend/.env.local`):
```
VITE_API_URL=http://localhost:8080
```

**Backend** (`backend/src/main/resources/application-local.properties`):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nutrifit
spring.datasource.username=nutrifit
spring.datasource.password=nutrifit123
spring.datasource.driver-class-name=org.postgresql.Driver

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

server.port=8080
```

## Docker (opcional para desarrollo local)

Levantar backend + PostgreSQL:

```bash
docker-compose up --build
```

Parar y limpiar:

```bash
docker-compose down -v
```

> **Nota**: En desarrollo local es más rápido ejecutar el backend y frontend directamente sin Docker.

## Módulos de API (18 endpoints)

- `/api/auth` — login, register, logout
- `/api/alimentos` — catálogo de alimentos
- `/api/comidas` — comidas diarias + alimentos
- `/api/resumen-diario` — resumen nutricional del día
- `/api/resumen/evaluacion-ia` — evaluación IA de nutrición
- `/api/perfil` — perfil usuario, BMR, TDEE
- `/api/ejercicios` — catálogo de ejercicios
- `/api/ejercicios-registro` — log de sesiones de ejercicio
- `/api/hidratacion` — seguimiento de hidratación
- `/api/plan-semanal` — plan de comidas generado por IA
- `/api/retos` — desafíos de fitness
- `/api/lista-compra` — lista de la compra + sugerencias IA
- `/api/escaner/{barcode}` — escáner de códigos de barras (OpenFoodFacts)
- `/api/gamificacion` — estadísticas (racha, puntuación)
- `/api/peso-historial` — historial de peso
- `/api/tendencias` — tendencias nutricionales (90 días)
- `/api/ia-config` — configuración IA del usuario
- `/api/health` — health check

## Tests

| Módulo | Tests | Framework |
|---|---|---|
| Backend (Spring Boot) | 91 | JUnit 5 + Mockito + AssertJ |
| Cliente JavaFX | 24 | JUnit 5 + Mockito + AssertJ |
| Frontend (React) | 3 | Vitest |
| **Total** | **118** | |

Los 91 tests del backend cubren la capa de servicio al completo. Los servicios con lógica de negocio propia (auth, comidas, perfil, hidratación, peso, gamificación) tienen el 100% de cobertura de líneas. Controladores y repositorios se verifican mediante pruebas manuales con archivos `.http` en `docs/api/`.

```bash
# Ejecutar tests backend
cd backend && mvn test

# Ejecutar tests cliente JavaFX
cd client && mvn test
```

## Documentación de API

Swagger/OpenAPI disponible en `http://localhost:8080/swagger-ui.html` (backend corriendo).

## Despliegue

- **Backend**: Render (build automático desde `main`)
- **Frontend**: Vercel (build automático desde `main`)

Ver configuraciones en `backend/render.yaml` y `frontend/vercel.json`.

## FAQ / Troubleshooting

### El backend tarda 30-60 segundos en responder la primera petición

El backend está desplegado en Render con el plan gratuito, que suspende la instancia tras 15 minutos de inactividad. La primera petición la reanuda, lo que añade ese tiempo de arranque. Las peticiones siguientes responden con normalidad. No hay solución en el plan gratuito; en producción real se usaría un plan de pago o un ping periódico.

### Error `401 Unauthorized` en todas las peticiones tras un tiempo de inactividad

El token de sesión caduca a los 7 días. Si ves 401 de forma persistente, cierra sesión y vuelve a autenticarte. En el cliente JavaFX, el token se guarda únicamente en memoria: al cerrar la aplicación la sesión se pierde y hay que hacer login de nuevo.

### El frontend no llega al backend: error de CORS en la consola del navegador

El backend solo acepta peticiones desde `https://nutri-fit-snowy.vercel.app` y `http://localhost:5173`. Si el frontend está desplegado en otra URL de Vercel, actualiza `allowedOrigins` en `WebMvcConfig.java`:

```java
config.setAllowedOrigins(List.of("https://tu-url.vercel.app", "http://localhost:5173"));
```

Este problema apareció durante el desarrollo al cambiar el nombre del proyecto en Vercel (commit `9febf47`).

### Flyway lanza `Migration checksum mismatch` al arrancar el backend

Ocurre si se modifica un archivo de migración ya aplicado (los archivos `V*.sql` en `backend/src/main/resources/db/migration` son inmutables una vez ejecutados). Soluciones:

```sql
-- Opción 1: reparar el checksum (solo en desarrollo)
-- añadir spring.flyway.repair=true en application-local.properties y arrancar una vez

-- Opción 2: recrear la base de datos desde cero
DROP DATABASE nutrifit;
CREATE DATABASE nutrifit;
```

### Las funciones de IA no responden o devuelven error 500

Las evaluaciones de IA requieren una clave válida de OpenRouter. Si la clave no está configurada o está expirada, el servicio lanza una excepción no controlada. Comprueba la variable de entorno en Render (`OPENROUTER_API_KEY`) o en `application-local.properties` en desarrollo. La funcionalidad de escáner de códigos de barras es independiente de IA y seguirá funcionando.

### `mvn spring-boot:run` falla con `UnsupportedClassVersionError`

El proyecto requiere Java 21. Comprueba la versión activa:

```bash
java -version   # debe mostrar 21.x.x
```

Si tienes varias versiones instaladas, usa `JAVA_HOME` para seleccionar la correcta:

```bash
JAVA_HOME=/usr/lib/jvm/java-21 mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

---

## Privacidad

NutriFit almacena datos de salud (peso, calorías, ejercicio, hidratación). La aplicación aplica BCrypt para contraseñas, eliminación en cascada de todos los datos del usuario y caducidad de sesiones a 7 días. Ver [docs/memoria/07-seguridad.md](docs/memoria/07-seguridad.md#76-privacidad-y-rgpd) para el análisis completo de tratamiento de datos conforme al RGPD.

## Licencia

Proyecto personal.
