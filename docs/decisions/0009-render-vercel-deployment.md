# 0009 — Despliegue en Render (backend) y Vercel (frontend)

## Decisión

NutriFit se despliega en dos plataformas de hosting gratuito:
- **Backend (Spring Boot)**: Render, con PostgreSQL gestionado
- **Frontend (React)**: Vercel, con auto-deploy desde main branch

---

## Contexto

Un TFG académico necesita demostración accesible sin costo.
Se evaluaron opciones:

| Plataforma | Backend | Frontend | BD | Coste |
|---|---|---|---|---|
| Heroku | Sí | No ideal | PostgreSQL | $7/mes (dejó free tier) |
| AWS | Sí | Sí | PostgreSQL | Variable, fácil sobrepasar |
| Render | Sí | Sí | PostgreSQL | Gratis (respuestas lentas) |
| Vercel | No | Sí | N/A | Gratis |
| Railway | Sí | Sí | PostgreSQL | Gratis (100 horas/mes) |

**Conclusión**: Render (backend) + Vercel (frontend) es 100% gratis y suficientemente rápido para demo.

---

## Motivo de la elección

### 1. Render para backend Spring Boot

**Ventajas:**
- PostgreSQL gestionado incluido (14+)
- Planes free tier con límites claros
- HTTPS automático
- Variables de entorno fáciles
- Backups automáticos (90 días)
- Conecta automáticamente con BD al desplegar

```yaml
# render.yaml
services:
  - type: web
    name: nutrifit-api
    runtime: java
    plan: free
    buildCommand: "mvn clean package -DskipTests"
    startCommand: "java -jar target/nutrifit-*.jar"
    envVars:
      - key: DATABASE_URL
        fromDatabase:
          name: nutrifit
          property: connectionString

databases:
  - name: nutrifit
    engine: postgresql
    version: "14"
    plan: free
```

**Limitaciones del free tier:**
- ~30s cold start (primer acceso)
- 0.5 vCPU compartido
- 512 MB RAM
- Respuesta lenta si muchos usuarios concurrentes
- **Tolerable para TFG/demo**, no para producción

### 2. Vercel para frontend React

**Ventajas:**
- Creadores de Next.js (React first-class support)
- Auto-deploy desde GitHub en cada push a main
- HTTPS automático
- CDN global para assets estáticos
- Serverless Functions (no necesitamos)
- Cero configuración

```bash
# Conexión automática
1. Push a GitHub main branch
   ↓
2. Vercel webhook dispara
   ↓
3. Vercel clona, npm install, npm run build
   ↓
4. Despliega /dist en CDN global (~2 minutos)
   ↓
5. URL actualizada automáticamente
```

**No requiere archivo de configuración** (Vercel detecta Vite automáticamente):

```json
// vercel.json (opcional, detalles)
{
  "buildCommand": "npm run build",
  "outputDirectory": "./dist"
}
```

### 3. Separación limpia: frontend ≠ backend

Este enfoque refuerza la arquitectura de dos repositorios:

```
nutrifit-backend (GitHub)       nutrifit-frontend (GitHub)
↓                               ↓
Render (Spring Boot + DB)       Vercel (React SPA)
api.nutrifit.app                nutrifit.vercel.app
```

**Ventajas:**
- Deploy independiente (no esperar a que compile backend)
- Rollback independiente
- Escalado independiente
- Frontend en CDN (rápido)
- Backend en servidor dedicado

### 4. Costo: $0/mes

- Render: free tier (respuestas lentas OK para demo)
- Vercel: free tier (muy rápido, CDN global)
- GitHub: gratis con public repo
- PostgreSQL: incluido en Render
- HTTPS: ambas plataformas

**Alternativa pagada:** ~$12/mes (Render Standard + Vercel Pro).

---

## Alternativas consideradas

### Opción 1: Heroku (antiguo estándar)
**Rechazada porque:**
- Eliminó free tier en nov 2022
- Ahora: $7/mes mínimo por dyno
- No es viable para TFG sin presupuesto

### Opción 2: Railway
**Evaluada pero rechazada:**
- Free tier: 100 horas de compute/mes (~3 horas/día)
- Suficiente para desarrollo pero insuficiente para demo sostenida
- $5/mes para plan ilimitado

### Opción 3: AWS (EC2 + RDS + CloudFront)
**Rechazada porque:**
- Complejo de configurar
- Fácil sobrepasar free tier y recibir factura
- Overkill para TFG

### Opción 4: DigitalOcean App Platform
**Rechazada porque:**
- $5/mes mínimo
- Requiere tarjeta de crédito
- Más complejo que Render

---

## Diseño elegido

**Arquitectura de despliegue:**

```
GitHub (single repo nutrifit-backend)
├── render.yaml              ← Configuración Render
├── src/main/java/...        ← Código Spring Boot
├── src/main/resources/db/migration/  ← Flyway migrations
└── pom.xml

GitHub (repo separado nutrifit-frontend)
├── vite.config.ts           ← Detecta Vercel automáticamente
├── package.json
├── src/                     ← React + TypeScript
├── public/
└── .env.production          ← VITE_BACKEND_URL=https://api.nutrifit.app
```

**Pipeline de CI/CD:**

```
Developer      GitHub      Render          Vercel          PostgreSQL
    │             │           │              │                 │
    ├─ git push main
    │             ├─ webhook ──→ pull, build, test
    │             │             Java jar + startup
    │             │             ↓
    │             │             Connected to shared DB
    │             │ (30s cold start)
    │             │
    │             └─ webhook ──→ npm install, npm run build
    │                           ↓
    │                           dist/ → CDN global
    │                           (~2 min, automatic)
    │
    └─ npm run dev (local Vite)
      (http://localhost:5173 + localhost:8080)
```

**Variables de entorno:**

Backend (Render):
```
DATABASE_URL=postgres://...
OPENROUTER_API_KEY=...
OPENROUTER_MODEL=google/gemma-2-9b-it
ALLOWED_ORIGINS=https://nutrifit.vercel.app,http://localhost:5173
```

Frontend (Vercel):
```
VITE_BACKEND_URL=https://api.nutrifit.app
```

---

## Cold start y rendimiento

### Backend (Render): ~30s cold start

**Problema**: Primer acceso tarda 30 segundos (JVM inicia).

**Por qué:**
- Render suspende aplicación sin tráfico
- Al acceder: necesita iniciar JVM, conectar BD, cargar Spring context
- JVM startup es intrínsecamente lento

**Soluciones:**
1. **Uptimerobot**: Ping cada 15 min para evitar cold start
   ```bash
   curl https://api.nutrifit.app/actuator/health every 15min
   ```

2. **Aceptar el cold start**: "Es demo, usuarios entienden"

3. **Upgrade a Render Standard**: ~$7/mes para evitar suspensión

**Recomendación para TFG**: Solución 1 (Uptimerobot free).

### Frontend (Vercel): ~1s

**Muy rápido**: Archivos estáticos en CDN global, no hay startup.

---

## CORS y configuración de dominios

Ambos frontales (prod + dev) necesitan acceder al backend:

```
http://localhost:5173 (dev)           → api.nutrifit.app
https://nutrifit.vercel.app (prod)    → api.nutrifit.app
```

**Backend CORS configuration:**

```java
// CorsConfig.java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "https://nutrifit.vercel.app"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

## Decisiones técnicas relacionadas

- **Health check**: Actuator endpoint `/actuator/health` para Uptimerobot
- **Logging**: Logs en stdout (Render los captura automáticamente)
- **Migrations**: Flyway auto-ejecuta en cada startup
- **Assets estáticos**: Frontend servido desde Vercel CDN (no desde backend)

---

## Alcance implementado

✅ Render backend con PostgreSQL
✅ Vercel frontend con auto-deploy
✅ render.yaml configurado
✅ CORS para localhost + Vercel domain
✅ Backups automáticos en Render
✅ HTTPS en ambas plataformas
✅ Variables de entorno en ambas plataformas

---

## Limitaciones del free tier

| Limitación | Render | Vercel |
|---|---|---|
| Cold start | ~30s primero | ~1s |
| Respuestas lentes | Si muchos usuarios | Rápido (CDN) |
| Almacenamiento | 90 días BD, 1GB | Ilimitado (estático) |
| Computación | 0.5 vCPU | Serverless (rápido) |
| Cuando esperar | Demo TFG | No aplica |

---

## Próximo paso recomendado

1. Configurar Uptimerobot (free) para ping cada 15 min (evita cold start)
2. Monitorear logs en Render durante demo para detectar errores
3. Si performance insuficiente, considerar Render Standard (~$7/mes)
4. Documentar URL actual y credenciales en CLAUDE.md para acceso rápido durante demo
