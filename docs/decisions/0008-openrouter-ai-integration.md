# 0008 — Integración de IA vía OpenRouter con configuración personalizable por usuario

## Decisión

NutriFit integra modelos de lenguaje mediante **OpenRouter** con arquitectura de fallback:
- **Configuración por defecto**: servidor proporciona claves de API (gemma + deepseek)
- **Configuración personalizada**: usuario puede usar sus propias claves de OpenRouter o proxy alternativo
- **Almacenamiento**: tabla `usuario_ia_config` permite override por usuario

---

## Contexto

NutriFit requiere IA para varias funcionalidades:

1. **Evaluación nutricional**: análisis de comidas registradas y recomendaciones
2. **Generador de planes**: crear planes semanales personalizados
3. **Sugerencias de compra**: recomendaciones de alimentos basadas en objetivos
4. **Análisis de fotos**: posible análisis de platos fotografiados

Para ello, se evaluaron tres enfoques:

| Opción | Ventaja | Inconveniente |
|---|---|---|
| API propia de LLM (OpenAI) | Simple, una sola API | Requiere claves, coste directo |
| Agregador (OpenRouter) | Múltiples modelos, fallback | Otro intermediario |
| Modelos locales (Ollama) | Sin coste, privacidad | Lentitud, overhead de servidor |

---

## Motivo de la elección

### 1. Múltiples proveedores con una API unificada

OpenRouter actúa como proxy:

```
NutriFit (backend)
    ↓
OpenRouter API
    ├─ OpenAI (GPT-4, GPT-3.5)
    ├─ Anthropic (Claude)
    ├─ Google (Gemini)
    ├─ Meta (Llama)
    ├─ Mistral
    └─ Otros (deepseek, gemma, etc.)
```

**Beneficio**: Usuario elige el modelo sin cambiar código.

```java
// Mismo código para cualquier modelo
HttpPost request = new HttpPost("https://openrouter.io/api/v1/chat/completions");
request.setHeader("Authorization", "Bearer " + apiKey);
request.setHeader("HTTP-Referer", "https://nutrifit.app");

ObjectNode json = mapper.createObjectNode();
json.put("model", configuredModel);  // "openai/gpt-4" o "anthropic/claude-opus"
json.putPOJO("messages", messages);
```

### 2. Arquitectura de fallback para resiliencia

La configuración del servidor proporciona dos modelos por defecto:

```json
{
  "proxyUrl": "https://openrouter.io/api/v1/chat/completions",
  "model": "google/gemma-2-9b-it",
  "fallbackModel": "deepseek/deepseek-chat",
  "apiKey": "sk-or-..." // clave del servidor
}
```

Si gemma falla o está sobrecargado:

```java
// Pseudocódigo
try {
    response = callOpenRouter(gemmaModel, apiKey);
} catch (Exception e) {
    response = callOpenRouter(deepseekModel, apiKey);  // fallback
}
```

**Beneficio**: No hay 100% de failures en IA.

### 3. Permitir claves personales sin obligar

Tabla `usuario_ia_config`:

```sql
CREATE TABLE usuario_ia_config (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    proxy_url VARCHAR(255),
    model VARCHAR(255),
    api_key VARCHAR(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
```

**Flujo:**

1. Usuario registra su propia clave OpenAI, Anthropic, etc.
2. Backend valida que la clave sea válida haciendo test request
3. Si válida, almacena en `usuario_ia_config`
4. Peticiones futuras usan clave del usuario
5. Si falla, fallback a clave del servidor

```java
// En AIServiceImpl
String apiKey = getConfiguredKey(usuarioId);  // de usuario_ia_config si existe
if (apiKey == null) {
    apiKey = serverDefaultKey;  // fallback del servidor
}
```

### 4. Gestión de costes

- **Servidor proporciona**: gemma (gratis en OpenRouter) y deepseek (muy barato)
- **Usuario avanzado**: puede usar GPT-4 con su clave
- **Presupuesto del servidor**: acotado (limite de calls)
- **Transparencia**: usuario ve qué modelo usa y cuánto cuesta

### 5. Sin cambiar arquitectura según usuario

Mismo endpoint REST devuelve respuesta IA:

```
POST /api/comida/{id}/evaluacion-ia
→ Devuelve: { evaluacion, recomendaciones, ... }
```

Backend determina qué modelo usar, no cliente.

---

## Alternativas consideradas

### Opción 1: Solo API de OpenAI directamente
**Rechazada porque:**
- Clave única, sin fallback
- Modelo fijo (GPT-4 es caro)
- Usuario no puede usar su propia clave

### Opción 2: Modelos locales con Ollama
**Rechazada porque:**
- Ollama es lento y consume recursos
- Render free tier solo da 256 MB RAM
- Complicaría mucho el servidor
- Latencia inaceptable (5-10s por petición)

### Opción 3: Sin IA en MVP
**Rechazada porque:**
- IA es requisito clave del TFG
- Las funcionalidades de IA generan valor

---

## Diseño elegido

**Flujo de ingesta de comida con evaluación IA:**

```
1. Usuario registra comida (POST /api/comida)
   └─ Comida almacenada sin IA

2. Usuario pide evaluación (GET /api/comida/{id}/evaluacion-ia)
   └─ Backend:
      ├─ Carga comida y sus items
      ├─ Comprueba usuario_ia_config
      ├─ Llama a OpenRouter con clave personalizada o servidor
      ├─ Cachea respuesta 1 hora
      └─ Devuelve JSON con evaluación

3. Respuesta ejemplo:
   {
     "modelUsed": "google/gemma-2-9b-it",
     "evaluacion": "La comida tiene buena proporción proteica...",
     "recomendaciones": ["Aumentar fibra", "..."],
     "macrosEstimados": { "kcal": 850, ... },
     "timestamp": "2026-05-17T10:30:00Z"
   }
```

**Arquitectura de módulos:**

```
backend/src/main/java/com/nutrifit/
├── ia/
│   ├── OpenRouterService.java       // Llamadas HTTP a OpenRouter
│   ├── AIPromptBuilder.java         // Construcción de prompts
│   ├── AIConfigService.java         // Lectura/write de usuario_ia_config
│   └── AIResponseParser.java        // Parsing de respuestas JSON
├── comida/
│   └── ComidaController.java        // Endpoint GET /{id}/evaluacion-ia
└── usuario/
    └── UsuarioIAConfigController.java // CRUD de usuario_ia_config
```

---

## Seguridad y limitaciones

### Claves almacenadas en plaintext

**Limitación conocida**: Las claves de OpenRouter se almacenan sin encriptación en `usuario_ia_config`.

**Por qué en MVP:**
- TFG académico, no producción
- Render no tiene secret management
- Usuarios son grupo pequeño de testers

**Solución futura:**
- Encriptar con AES-256 + KMS
- O integrar con AWS Secrets Manager / HashiCorp Vault

### Rate limiting

```java
// OpenRouter permite 20 req/min en free tier
// Backend implementa:
Cache<String, LocalDateTime> lastAICall = CacheBuilder.newBuilder()
    .expireAfterWrite(3, TimeUnit.MINUTES)
    .build();

if (lastAICall.getIfPresent(userId) != null) {
    throw new RateLimitExceededException("Demasiadas llamadas a IA");
}
```

### Datos personales en prompts

**Consideración**: Prompts incluyen comidas/macros del usuario.

**Privacidad:**
- OpenRouter transmite datos a sus servidores
- Usuarios con clave propia: controlan qué modelo recibe sus datos
- Documentado en términos de servicio

---

## Flujos de uso

### Flujo 1: Usuario típico (sin clave personalizada)

```
1. Registra comida: "Pollo 150g + arroz 100g"
2. Pide evaluación
3. Backend: Llama gemma con clave del servidor
4. Respuesta: "Buena proporción proteica, falta fibra"
5. Usuario ve recomendación
```

### Flujo 2: Usuario avanzado (con clave de OpenAI)

```
1. En perfil, carga su clave de OpenAI
2. Backend valida con test request
3. Almacena en usuario_ia_config
4. Próximas evaluaciones usan GPT-4 en lugar de gemma
5. Backend gestiona costes (notifica al usuario del consumo)
```

### Flujo 3: Fallback por error

```
1. Usuario pide evaluación
2. gemma falla (timeout, rate limit)
3. Backend automáticamente intenta deepseek
4. deepseek responde
5. Usuario no nota nada
```

---

## Decisiones técnicas relacionadas

- **Caching**: Respuestas IA se cachean 1 hora para evitar llamadas redundantes
- **Async**: Las llamadas a OpenRouter son síncronas pero con timeout (30s)
- **Error handling**: Si ambos modelos fallan, devolver sugerencia genérica
- **Auditoría**: Log de cada llamada a IA (modelo, timestamp, usuario)

---

## Alcance implementado

✅ Integración OpenRouter en backend
✅ Evaluación de comidas con IA
✅ Generación de planes semanales
✅ Sugerencias de compra
✅ Tabla `usuario_ia_config` para claves personalizadas
✅ Fallback model (gemma + deepseek)
✅ Caching de respuestas

---

## Alcance que queda fuera

- Encriptación de claves en BD
- Fine-tuning de modelos personalizados
- Análisis de fotos de comidas
- Integration con Whisper API (análisis de audio)
- Estadísticas de consumo de tokens

---

## Próximo paso recomendado

Implementar encriptación de claves en `usuario_ia_config` antes de pasar a producción.
Considerar integrar webhook de OpenRouter para notificaciones en tiempo real de costes.
