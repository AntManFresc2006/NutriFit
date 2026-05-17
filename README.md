# NutriFit

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

## Documentación de API

Swagger/OpenAPI disponible en `http://localhost:8080/swagger-ui.html` (backend corriendo).

## Despliegue

- **Backend**: Render (build automático desde `main`)
- **Frontend**: Vercel (build automático desde `main`)

Ver configuraciones en `backend/render.yaml` y `frontend/vercel.json`.

## Licencia

Proyecto personal.
