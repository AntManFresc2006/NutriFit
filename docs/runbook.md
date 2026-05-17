# Runbook de NutriFit

Guía para ejecutar y desarrollar NutriFit en local.

## Objetivo

Poner en marcha el proyecto (backend + frontend) y resolver problemas comunes de arranque.

---

## Requisitos previos

Instala antes de empezar:

- **Java 21** (o compatible)
- **Maven 3.8+**
- **Node.js 18+** y npm
- **PostgreSQL 12+**
- **Git**

Verificar versiones:

```bash
java -version
mvn -version
node -version
npm -version
psql --version
```

---

## 1. Configurar base de datos (PostgreSQL)

### 1.1 Crear base de datos

```bash
createdb -U postgres nutrifit
```

O desde psql:

```sql
\connect postgres
CREATE DATABASE nutrifit;
```

### 1.2 Crear usuario de aplicación (recomendado)

```sql
CREATE USER nutrifit WITH PASSWORD 'nutrifit123';
GRANT ALL PRIVILEGES ON DATABASE nutrifit TO nutrifit;
```

> **Nota**: En desarrollo local, credenciales simples son aceptables. En producción, usar contraseñas fuertes.

### 1.3 Verificar conexión

```bash
psql -U nutrifit -d nutrifit -c "SELECT version();"
```

---

## 2. Configurar backend (Spring Boot)

### 2.1 Crear archivo de propiedades

```bash
touch backend/src/main/resources/application-local.properties
```

### 2.2 Rellenar propiedades

```properties
# Base de datos PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/nutrifit
spring.datasource.username=nutrifit
spring.datasource.password=nutrifit123
spring.datasource.driver-class-name=org.postgresql.Driver

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Servidor
server.port=8080

# Logging (opcional)
logging.level.root=INFO
logging.level.com.nutrifit=DEBUG
```

> **⚠️ Importante**: No versionar `application-local.properties` con credenciales reales. Añadir a `.gitignore` si no está ya.

### 2.3 Arrancar backend

```bash
cd backend
mvn clean install -DskipTests
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Qué esperar**:
- Compilación de Maven
- Flyway valida/aplica migraciones
- Tomcat arranca en `http://localhost:8080`
- Logs mostrarán `Started NutriFitApplication in X.XXX seconds`

**Verificar**:

```bash
curl http://localhost:8080/api/health
```

Debería responder algo como:
```json
{"status":"UP"}
```

---

## 3. Configurar frontend (React)

### 3.1 Instalar dependencias

```bash
cd frontend
npm install
```

### 3.2 Variables de entorno

Crear `frontend/.env.local`:

```
VITE_API_URL=http://localhost:8080
```

### 3.3 Arrancar frontend

```bash
npm run dev
```

**Qué esperar**:
- Vite compila TypeScript
- Servidor de desarrollo arranca en `http://localhost:5173`
- Logs mostrarán `ready in X ms`
- Hot reload habilitado

**Verificar**:

Abrir navegador en `http://localhost:5173`. Debería ver página de login.

---

## 4. Flujo recomendado de arranque

1. Verificar PostgreSQL está corriendo
2. Arrancar backend (`mvn spring-boot:run`)
3. Verificar salud del backend (`curl localhost:8080/api/health`)
4. Arrancar frontend (`npm run dev`)
5. Abrir navegador en `http://localhost:5173`

---

## 5. Usar Docker (opcional)

Si prefieres no instalar PostgreSQL localmente:

```bash
docker-compose up --build
```

Esto levanta:
- PostgreSQL en puerto 5432
- Backend en puerto 8080 (con DB ya iniciada)

Frontend sigue arrancando localmente:

```bash
cd frontend && npm run dev
```

Parar contenedores:

```bash
docker-compose down -v
```

---

## 6. Problemas frecuentes

### 6.1 `psql: error: connection to server at "localhost" failed`

**Causa**: PostgreSQL no está corriendo.

**Solución**:

Linux:
```bash
sudo systemctl start postgresql
```

macOS:
```bash
brew services start postgresql
```

Windows: Abrir "Services" y iniciar "PostgreSQL".

---

### 6.2 `Access denied for user 'nutrifit'`

**Causa**: Credenciales incorrectas en `application-local.properties`.

**Solución**:
1. Verificar usuario existe: `psql -U postgres -c "\du"`
2. Verificar contraseña coincide
3. Verificar permisos: `GRANT ALL PRIVILEGES ON DATABASE nutrifit TO nutrifit;`

---

### 6.3 `Flyway validation failed`

**Causa**: Migraciones previas falladas o base de datos corrupta.

**Solución**:

```bash
# Borrar base de datos y recriar
dropdb -U postgres nutrifit
createdb -U postgres nutrifit

# Ejecutar backend again (Flyway aplicará migraciones desde 0)
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

---

### 6.4 Puerto 8080 ocupado

**Causa**: Otra aplicación usando puerto 8080.

**Solución**:

Linux/macOS:
```bash
lsof -i :8080
kill -9 <PID>
```

Windows:
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

O cambiar puerto en `application-local.properties`:
```properties
server.port=8081
```

Y actualizar `VITE_API_URL` en `frontend/.env.local`.

---

### 6.5 Port 5173 ocupado (frontend)

**Causa**: Instancia anterior de Vite no terminó.

**Solución**:

```bash
# Matar proceso Vite
lsof -i :5173
kill -9 <PID>

# Intentar de nuevo
npm run dev
```

O permitir puerto diferente:
```bash
npm run dev -- --port 5174
```

---

### 6.6 CORS error en navegador

**Causa**: Backend no acepta frontend en `localhost:5173`.

**Verificar**:

En `backend/src/main/java/config/CorsConfig.java`, debe incluir:

```java
.allowedOrigins("http://localhost:5173", ...)
```

Si no existe archivo de config, crear uno.

---

### 6.7 Autenticación rechazada

**Causa**: Token no válido o usuario no existe.

**Solución**:
1. Registrarse primero (`POST /api/auth/register`)
2. Luego login (`POST /api/auth/login`)
3. Token se almacena en localStorage

**Verificar en navegador**:

```javascript
// Console (F12)
localStorage.getItem('token')
```

Debería mostrar algo como `"550e8400-e29b-41d4-a716-446655440000"`.

---

## 7. Verificación de entorno

Checklist mínimo:

| Componente | Comando | Esperado |
|---|---|---|
| PostgreSQL | `psql -U nutrifit -d nutrifit -c "SELECT 1"` | Responde `1` |
| Backend compile | `mvn clean install -DskipTests` | Éxito sin errores |
| Backend health | `curl http://localhost:8080/api/health` | JSON con `"status":"UP"` |
| Frontend build | `npm run build` en `frontend/` | Crea carpeta `dist/` |
| Frontend dev | `npm run dev` en `frontend/` | Arranca en `localhost:5173` |

---

## 8. APIs útiles para testing

**Registrarse**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123","nombre":"Test","apellidos":"User"}'
```

**Login**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'
```

Respuesta contiene token: `{"token":"..."}`

**Usar token en siguiente petición**:
```bash
curl http://localhost:8080/api/perfil/1 \
  -H "Authorization: Bearer <token-de-arriba>"
```

**Swagger UI**:

```
http://localhost:8080/swagger-ui.html
```

Interfaz interactiva para probar todos los endpoints.

---

## 9. Migraciones de base de datos

Si necesitas modificar el esquema:

1. Crear script en `backend/src/main/resources/db/migration/`
2. Nombre: `V{N}__description.sql` (ej: `V23__add_column_x.sql`)
3. Flyway lo ejecutará automáticamente al arrancar

**Ejemplo**:

```sql
-- V23__add_column_objetivos.sql
ALTER TABLE usuarios ADD COLUMN objetivos TEXT;
```

---

## 10. Limpieza

Para un arranque completamente limpio:

```bash
# Backend
mvn clean
rm backend/src/main/resources/application-local.properties

# Frontend
rm -rf frontend/node_modules frontend/dist frontend/.env.local
npm install

# Base de datos (⚠️ borra datos)
dropdb -U postgres nutrifit
createdb -U postgres nutrifit
```

Luego seguir secciones 1–4 de nuevo.

---

## 11. Logs y debugging

### Backend

Ver logs en tiempo real:

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=local" 2>&1 | grep -E "ERROR|WARN|INFO"
```

Aumentar verbosidad en `application-local.properties`:

```properties
logging.level.com.nutrifit=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Frontend

Abrir DevTools (F12), pestaña "Console" para ver logs de JavaScript.

O ejecutar con debugging:

```bash
npm run dev -- --debug
```

---

## Preguntas frecuentes

**P: ¿Cómo cambio la contraseña de PostgreSQL?**

```sql
ALTER USER nutrifit WITH PASSWORD 'nueva_password';
```

**P: ¿Puedo usar SQLite en lugar de PostgreSQL?**

No recomendado. El proyecto está optimizado para PostgreSQL. Cambiar requeriría actualizar driver JDBC, migraciones y posiblemente queries.

**P: ¿Dónde guardo archivos de configuración sensibles?**

En máquina local, en `.env.local` o `application-local.properties`, NUNCA versionados. Para producción, usar variables de entorno del servidor (Render, Vercel).

**P: ¿Cómo limpio datos sin borrar estructura de BD?**

```sql
TRUNCATE TABLE usuarios CASCADE;
-- (cuidado: borra todos los usuarios)
```

O eliminar datos específicos:

```sql
DELETE FROM usuarios WHERE email = 'test@test.com';
```

---

## Contacto y soporte

Antonio Manuel Fresco Gómez
