# Anexo A — Guía de puesta en marcha

Este anexo describe los pasos necesarios para arrancar NutriFit en un entorno local desde cero, así como la configuración de los servicios en producción. El objetivo es que cualquier persona con acceso al repositorio pueda reproducir el entorno de desarrollo.

---

## A.1 Configuración en producción

NutriFit está actualmente desplegado en la nube:

- **Backend:** Spring Boot en Render (https://nutrifit-backend.onrender.com)
- **Frontend:** React en Vercel (https://nutrifit.vercel.app)
- **Base de datos:** PostgreSQL en Render

No es necesario hacer nada especial para acceder a la aplicación en producción; simplemente abrir https://nutrifit.vercel.app en el navegador.

---

## A.2 Configuración en desarrollo local

### A.2.1 Requisitos previos

Antes de ejecutar el proyecto deben estar instalados los siguientes componentes:

| Herramienta | Versión mínima | Uso |
|-------------|---------------|-----|
| Java (JDK)  | 21            | Compilación y ejecución de backend |
| Maven       | 3.9           | Gestión de dependencias y arranque |
| Node.js     | 18            | Runtime para frontend (npm) |
| npm         | 9+            | Gestor de paquetes de frontend |
| PostgreSQL  | 14            | Base de datos (opcional si se usa Render) |
| Git         | cualquiera    | Clonación del repositorio |

Para verificar que están disponibles:

```bash
java -version
mvn -version
node -version
npm -version
```

Maven y npm descargan automáticamente todas las dependencias en el primer arranque.

---

### A.2.2 Estructura del repositorio

El repositorio es un proyecto Maven multi-módulo con el siguiente árbol de primer nivel:

```
NutriFit/
├── pom.xml          ← POM padre
├── backend/         ← módulo Spring Boot 3
├── frontend/        ← módulo React 18 + TypeScript + Vite
└── docs/            ← documentación, ADRs, archivos HTTP
```

El POM padre declara `backend` y `frontend` como módulos (aunque el frontend se maneja con npm independientemente de Maven). Cada uno puede compilarse y ejecutarse de forma independiente.

---

### A.2.3 Preparación de la base de datos local

#### Opción 1: PostgreSQL local (recomendado para desarrollo)

Si no tienes PostgreSQL instalado, descárgalo desde https://www.postgresql.org/download/.

Tras la instalación, crear la base de datos y usuario para NutriFit:

```bash
# Conectarse a PostgreSQL como administrador
psql -U postgres

# Dentro del prompt de psql
CREATE DATABASE nutrifit;
CREATE USER nutrifit WITH ENCRYPTED PASSWORD 'nutrifit123';
GRANT ALL PRIVILEGES ON DATABASE nutrifit TO nutrifit;
```

#### Opción 2: Usar la base de datos remota de Render (más simple)

Si prefieres no instalar PostgreSQL localmente, puedes usar la base de datos remota en Render. En ese caso:

1. Obtén la URL de conexión remota de un administrador del proyecto.
2. Configura `application-local.properties` con esa URL (ver sección A.2.4).

---

### A.2.4 Configuración del backend

El archivo `backend/src/main/resources/application-local.properties` contiene la configuración para el entorno local:

```properties
# PostgreSQL local
spring.datasource.url=jdbc:postgresql://localhost:5432/nutrifit
spring.datasource.username=nutrifit
spring.datasource.password=nutrifit123
spring.datasource.driver-class-name=org.postgresql.Driver

# Flyway (gestión de migraciones automáticas)
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Servidor
server.port=8080

# IA (valores por defecto, el usuario puede configurar los suyos)
ai.openrouter.default-model=meta-llama/llama-3-8b-instruct
ai.openrouter.default-api-key=<solicitar-al-administrador>
```

**Nota:** Si usas PostgreSQL local con credenciales diferentes, actualiza `username` y `password`.

Si usas la base de datos remota de Render, reemplaza la línea `spring.datasource.url` con la URL proporcionada.

---

### A.2.5 Arranque del backend

Desde el directorio `backend/`:

**Linux / macOS**
```bash
cd backend
mvn -DskipTests spring-boot:run -Dspring-boot.run.profiles=local
```

**Windows**
```bash
cd backend
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

El flag `-DskipTests` evita ejecutar los 60 tests unitarios en cada arranque.

#### Señales de arranque correcto

El backend está listo cuando se muestran estas líneas en el log:

1. Flyway aplica las migraciones:
   ```
   Successfully applied N migrations to schema 'public'
   ```

2. Tomcat inicia:
   ```
   Tomcat started on port 8080 (http)
   ```

3. Spring Boot confirma:
   ```
   Started BackendApplication in X.XXX seconds
   ```

---

### A.2.6 Verificación del backend

#### A.2.6.1 Swagger UI

Con el servidor activo, abre en el navegador:

```
http://localhost:8080/swagger-ui.html
```

Swagger muestra todos los endpoints interactivos. Permite registrarse, hacer login y probar endpoints autenticados.

#### A.2.6.2 Health check

```bash
curl http://localhost:8080/api/health
```

Debe responder `{ "status": "UP" }`.

---

### A.2.7 Configuración del frontend

El archivo `.env.local` (en la raíz de `frontend/`) configura la URL de la API:

```env
VITE_API_URL=http://localhost:8080
```

Si el backend corre en otro puerto, actualiza este valor.

---

### A.2.8 Arranque del frontend

Desde el directorio `frontend/`:

```bash
cd frontend
npm install    # Primera vez solo
npm run dev
```

El desarrollo es con hot reload: los cambios se reflejan al guardar.

#### Señales de arranque correcto

Verás un mensaje como:

```
VITE v5.0.0  ready in 234 ms

➜  Local:   http://localhost:5173/
```

Abre `http://localhost:5173/` en el navegador. Deberías ver la pantalla de login de NutriFit.

---

### A.2.9 Orden de arranque recomendado

1. **PostgreSQL** (si corres local): asegúrate de que está activo
2. **Backend**: desde `backend/` con `mvn spring-boot:run ...`
3. **Frontend**: desde `frontend/` con `npm run dev`

El frontend puede iniciarse antes o después del backend; se reconecta automáticamente cuando el backend está listo.

---

## A.3 Flujo de prueba rápido

### Paso 1: Registrar usuario

1. Ve a http://localhost:5173/
2. Haz clic en «Crear cuenta»
3. Ingresa email, contraseña (mín. 6 caracteres) y nombre
4. Haz clic en «Registrar»

### Paso 2: Completar perfil

1. Tras login, ve a la sección «Perfil»
2. Ingresa sexo, fecha de nacimiento, altura, peso y nivel de actividad
3. Guarda

### Paso 3: Añadir comida

1. Ve a «Comidas»
2. Crea una comida (tipo: «Desayuno», «Comida», etc.)
3. Busca un alimento (ej., «pollo»)
4. Añade a la comida con una cantidad en gramos
5. Guarda

### Paso 4: Ver resumen

1. Ve a «Inicio»
2. Deberías ver calorías totales, proteínas, grasas e hidratos
3. Haz clic en «Evaluar con IA» para recibir análisis personalizado

---

## A.4 Solución de problemas

### Error: `Access denied for user 'nutrifit'@'localhost'`

**Causa:** las credenciales en `application-local.properties` no coinciden con las del usuario PostgreSQL.

**Solución:**
```bash
psql -U postgres
# Verifica que el usuario existe y la contraseña es correcta
\du
# Si no existe, créalo:
CREATE USER nutrifit WITH ENCRYPTED PASSWORD 'nutrifit123';
```

---

### Error: `Connection refused` al conectar a PostgreSQL

**Causa:** PostgreSQL no está activo, o está en otro puerto.

**Solución:**
```bash
# Verifica que está activo
pg_isready -h localhost -p 5432

# Si no está corriendo, inicia el servicio (Linux/macOS)
sudo systemctl start postgresql

# O (macOS con Homebrew)
brew services start postgresql
```

---

### Frontend no se conecta al backend

**Causa:** La URL en `.env.local` es incorrecta, o el backend no está corriendo.

**Solución:**
```bash
# Verifica que el backend está activo
curl http://localhost:8080/api/health

# Si responde, verifica la URL en frontend/.env.local
# Reinicia el frontend con Ctrl+C y npm run dev
```

---

### Puerto 8080 ya en uso

**Causa:** otra aplicación ocupa el puerto.

**Solución:** Cambia `server.port` en `application-local.properties` a otro puerto libre (ej., `8081`) y actualiza `VITE_API_URL` en `frontend/.env.local`.

---

### npm command not found

**Causa:** Node.js/npm no están en la variable de PATH.

**Solución:** reinstala Node.js desde https://nodejs.org/ y verifica con `node -version`.

---

## A.5 Construcción para producción

### Backend

```bash
cd backend
mvn clean package -DskipTests
# Genera backend/target/backend-1.0.0.jar
```

### Frontend

```bash
cd frontend
npm run build
# Genera frontend/dist/ con los archivos estáticos
```

Los artefactos se despliegan en Render (backend) y Vercel (frontend) mediante CD automático desde Git.

---

## A.6 Variables de entorno importantes

| Variable | Donde | Descripción |
|----------|-------|-------------|
| `VITE_API_URL` | `frontend/.env.local` | URL del backend (http://localhost:8080 en dev) |
| `spring.datasource.url` | `backend/application-local.properties` | Conexión PostgreSQL |
| `ai.openrouter.default-api-key` | `backend/application.properties` | Clave API OpenRouter (por defecto) |
| `POSTGRES_URL` | Render (variable de entorno del contenedor) | URL remota de BD en producción |

