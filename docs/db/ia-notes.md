# Notas de base de datos - Integración de IA y configuración de usuario

## Objetivo
El módulo de IA permite que cada usuario configure su propia integración de modelos de lenguaje (via OpenRouter, OpenAI, Anthropic, etc.), manteniendo privacidad de datos y permitiendo fallback automático a claves del servidor si el usuario no configura las suyas.

---

## Tabla principal

### `usuario_ia_config` (configuración de IA por usuario)
Tabla que almacena las preferencias y credenciales de IA para cada usuario.

Campos principales:
- `id`: identificador único
- `usuario_id`: usuario propietario de la configuración (UNIQUE)
- `proxy_url`: URL del proxy de IA (ej. `https://openrouter.io/api/v1/chat/completions`)
- `model`: modelo preferido (ej. `google/gemma-2-9b-it`, `openai/gpt-4`)
- `api_key`: clave de API del usuario (stored in plaintext, TFG limitation)
- `is_active`: booleano indicando si usar esta configuración
- `created_at`: fecha de creación de la configuración
- `updated_at`: fecha última actualización

Relación:
- `usuario_ia_config` 1:1 `usuarios`

Ejemplo:

| id | usuario_id | proxy_url | model | api_key | is_active | created_at |
|----|-----------|-----------|-------|---------|-----------|------------|
| 1 | 5 | https://openrouter.io/api/v1/chat/completions | google/gemma-2-9b-it | NULL | true | 2026-03-01 |
| 2 | 6 | https://api.openai.com/v1/chat/completions | gpt-4 | sk-user-key... | true | 2026-04-15 |
| 3 | 7 | https://openrouter.io/api/v1/chat/completions | anthropic/claude-opus | sk-or-user... | true | 2026-05-01 |

---

## Arquitectura de fallback

NutriFit implementa una cadena de fallback para asegurar que la IA siempre está disponible:

```
1. Intenta con clave personalizada del usuario (si existe y está activa)
   ↓ (si falla)
2. Intenta con modelo alternativo del usuario (si existe)
   ↓ (si falla)
3. Intenta con clave del servidor + modelo primario (gemma)
   ↓ (si falla)
4. Intenta con clave del servidor + modelo fallback (deepseek)
   ↓ (si falla)
5. Devuelve respuesta genérica sin IA
```

Ejemplo en código:

```java
// AIServiceImpl.java
public String evaluarComida(long usuarioId, long comidaId) {
    try {
        // 1. Intentar con configuración del usuario
        UsuarioIAConfig config = iaConfigRepository.obtenerPorUsuarioId(usuarioId);
        
        if (config != null && config.isActive()) {
            try {
                return llamarOpenRouter(config.getProxyUrl(), 
                                       config.getModel(), 
                                       config.getApiKey(),
                                       prompt);
            } catch (Exception e) {
                log.warn("Config de usuario falló, intentando fallback", e);
            }
        }
        
        // 3. Intentar con clave del servidor
        try {
            return llamarOpenRouter(serverProxyUrl, 
                                   serverModel, // gemma
                                   serverApiKey,
                                   prompt);
        } catch (Exception e) {
            log.warn("Modelo primario del servidor falló, intentando fallback", e);
        }
        
        // 4. Intentar con modelo fallback
        try {
            return llamarOpenRouter(serverProxyUrl, 
                                   fallbackModel, // deepseek
                                   serverApiKey,
                                   prompt);
        } catch (Exception e) {
            log.error("Todos los modelos fallaron", e);
        }
        
        // 5. Respuesta genérica
        return generarRespuestaGenericaSinIA(comidaId);
        
    } catch (Exception e) {
        log.error("Error crítico en IA", e);
        throw new AIServiceException("Servicio de IA no disponible");
    }
}
```

---

## Configuración del servidor (valores por defecto)

```properties
# application.properties
openrouter.api-key=sk-or-SERVER_KEY_HERE
openrouter.proxy-url=https://openrouter.io/api/v1/chat/completions

# Modelo primario (gratis en OpenRouter)
openrouter.model.primary=google/gemma-2-9b-it

# Modelo fallback (muy barato)
openrouter.model.fallback=deepseek/deepseek-chat

# Límite de calls por usuario por día
openrouter.ratelimit.calls-per-day=20
```

---

## Construcción de prompts

Los prompts se construyen de forma dinámica según el contexto:

### Prompt 1: Evaluación de comida

```java
// AIPromptBuilder.java
public String construirPromptEvaluacionComida(Comida comida, 
                                             List<ComidaAlimento> items,
                                             Usuario usuario) {
    StringBuilder prompt = new StringBuilder();
    
    prompt.append("Eres un nutricionista experto. Evalúa la siguiente comida:\n\n");
    
    prompt.append("Información del usuario:\n");
    prompt.append("- Edad: ").append(usuario.getEdadAnos()).append(" años\n");
    prompt.append("- Peso: ").append(usuario.getPesoActualKg()).append(" kg\n");
    prompt.append("- Objetivo: ").append(usuario.getObjetivo()).append("\n");
    prompt.append("- TDEE: ").append(calcularTDEE(usuario)).append(" kcal/día\n\n");
    
    prompt.append("Comida registrada:\n");
    double totalKcal = 0;
    for (ComidaAlimento item : items) {
        double kcal = (item.getAlimento().getKcalPor100g() * item.getGramos()) / 100;
        prompt.append("- ").append(item.getAlimento().getNombre())
              .append(": ").append(item.getGramos()).append("g (")
              .append(String.format("%.0f", kcal)).append(" kcal)\n");
        totalKcal += kcal;
    }
    
    prompt.append("\nTotales: ").append(String.format("%.0f", totalKcal)).append(" kcal\n");
    
    prompt.append("\nProporciona:\n");
    prompt.append("1. Análisis breve de macronutrientes\n");
    prompt.append("2. Puntuación 1-10 según objetivo del usuario\n");
    prompt.append("3. 2-3 recomendaciones específicas\n");
    prompt.append("4. Qué sería una comida perfecta para este usuario\n");
    prompt.append("\nResponde en JSON.");
    
    return prompt.toString();
}
```

Respuesta esperada:

```json
{
  "analisis": "Comida equilibrada con buen aporte proteico...",
  "puntuacion": 7,
  "recomendaciones": [
    "Aumentar fibra con verduras",
    "Reducir grasas saturadas",
    "Buen balance proteina-carbos"
  ],
  "comida_perfecta": "Pechuga de pollo 150g + camote 150g + ensalada con aceite de oliva"
}
```

### Prompt 2: Generador de plan semanal

```java
public String construirPromptPlanSemanal(Usuario usuario, 
                                        List<String> restricciones,
                                        List<String> preferencias) {
    StringBuilder prompt = new StringBuilder();
    
    prompt.append("Eres un nutricionista experto en planificación de comidas.\n");
    prompt.append("Genera un plan semanal personalizado.\n\n");
    
    prompt.append("Datos del usuario:\n");
    prompt.append("- TDEE: ").append(calcularTDEE(usuario)).append(" kcal/día\n");
    prompt.append("- Objetivo: ").append(usuario.getObjetivo()).append("\n");
    
    if (!restricciones.isEmpty()) {
        prompt.append("- Restricciones: ").append(String.join(", ", restricciones)).append("\n");
    }
    if (!preferencias.isEmpty()) {
        prompt.append("- Preferencias: ").append(String.join(", ", preferencias)).append("\n");
    }
    
    prompt.append("\nGenera un plan con:\n");
    prompt.append("- 3 comidas principales (desayuno, almuerzo, cena)\n");
    prompt.append("- Cantidades exactas en gramos\n");
    prompt.append("- Calorías totales por día\n");
    prompt.append("- Macronutrientes (proteína, grasas, carbos)\n");
    prompt.append("- Variedad diaria\n");
    prompt.append("\nResponde en JSON con estructura de 7 días.");
    
    return prompt.toString();
}
```

---

## Manejo de errores y timeouts

```java
// AIServiceImpl.java
private String llamarOpenRouter(String proxyUrl, String model, 
                               String apiKey, String prompt) {
    HttpPost request = new HttpPost(proxyUrl);
    request.setHeader("Authorization", "Bearer " + apiKey);
    request.setHeader("HTTP-Referer", "https://nutrifit.app");
    request.setHeader("X-Title", "NutriFit");
    
    // Timeout de 30 segundos
    RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(30_000)
        .setSocketTimeout(30_000)
        .setConnectionRequestTimeout(30_000)
        .build();
    request.setConfig(config);
    
    ObjectNode json = mapper.createObjectNode();
    json.put("model", model);
    json.putPOJO("messages", Arrays.asList(
        Map.of("role", "user", "content", prompt)
    ));
    json.put("temperature", 0.7);
    json.put("max_tokens", 1500);
    
    request.setEntity(new StringEntity(mapper.writeValueAsString(json), "UTF-8"));
    
    try (CloseableHttpResponse response = httpClient.execute(request)) {
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new AIServiceException("OpenRouter returned " + 
                response.getStatusLine().getStatusCode());
        }
        
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonNode responseJson = mapper.readTree(responseBody);
        
        return responseJson.path("choices")
            .get(0)
            .path("message")
            .path("content")
            .asText();
            
    } catch (SocketTimeoutException e) {
        throw new AIServiceException("Timeout conectando con OpenRouter");
    } catch (Exception e) {
        throw new AIServiceException("Error llamando OpenRouter: " + e.getMessage());
    }
}
```

---

## Endpoints del módulo

```http
# Obtener configuración actual del usuario
GET /api/usuario/ia-config

# Actualizar configuración (con validación de clave)
POST /api/usuario/ia-config
{
  "proxy_url": "https://api.openai.com/v1/chat/completions",
  "model": "gpt-4",
  "api_key": "sk-...",
  "is_active": true
}
→ { "validacion": "OK", "config_id": 2 }

# Evaluar una comida con IA
GET /api/comida/{id}/evaluacion-ia

# Generar plan semanal
POST /api/ia/plan-semanal
{
  "restricciones": ["sin_gluten"],
  "preferencias": ["vegetariano"]
}

# Obtener sugerencias de compra basadas en plan
GET /api/ia/sugerencias-compra?planId=1

# Test de clave personalizada (valida antes de guardar)
POST /api/usuario/ia-config/validar
{
  "proxy_url": "...",
  "model": "...",
  "api_key": "..."
}
→ { "valido": true, "mensaje": "Clave funciona correctamente" }
```

---

## Caching de respuestas

Las respuestas de IA se cachean para evitar llamadas redundantes:

```java
// AIServiceImpl.java
private static final long CACHE_DURATION_MINUTES = 60;
private final Cache<String, CachedAIResponse> responseCache = 
    CacheBuilder.newBuilder()
        .expireAfterWrite(CACHE_DURATION_MINUTES, TimeUnit.MINUTES)
        .build();

public String evaluarComida(long usuarioId, long comidaId) {
    String cacheKey = "evaluacion_comida_" + comidaId;
    
    CachedAIResponse cached = responseCache.getIfPresent(cacheKey);
    if (cached != null && cached.usuarioId == usuarioId) {
        log.debug("Usando respuesta cacheada");
        return cached.respuesta;
    }
    
    // Obtener evaluación fresca
    String respuesta = obtenerEvaluacionFresca(usuarioId, comidaId);
    
    responseCache.put(cacheKey, new CachedAIResponse(usuarioId, respuesta));
    return respuesta;
}
```

---

## Auditoría y logging

Cada llamada a IA se registra:

```java
// AIAuditService.java
public void registrarLlamada(long usuarioId, String tipo, 
                            String modelo, String resultado,
                            long durationMs) {
    // Pseudocódigo - implementar según necesidad
    /*
    INSERT INTO ia_call_audit 
    (usuario_id, tipo, modelo, resultado, duration_ms, timestamp)
    VALUES (?, ?, ?, ?, ?, NOW())
    */
    
    log.info("IA call - User: {}, Type: {}, Model: {}, Duration: {}ms", 
            usuarioId, tipo, modelo, durationMs);
}
```

---

## Seguridad

### Limitaciones conocidas

1. **Claves en plaintext**: Las `api_key` se almacenan sin encriptación (TFG limitation)
   - Solución futura: AES-256 con KMS

2. **Datos personales en prompts**: Las comidas y biometría se envían a OpenRouter/OpenAI
   - Mitigation: Usar proxies sin log (OpenRouter: sin histórico con free tier)
   - Usuario avanzado: Puede usar su propia clave (datos van a su cuenta)

3. **Inyección de prompts**: Usuario podría inyectar instrucciones en comida
   - Mitigation: Inputs validados, no se interpolan directamente

### Mejoras de seguridad futuras

- Encriptación de claves en BD
- Integration con AWS Secrets Manager / HashiCorp Vault
- Auditoría de llamadas (quién llamó, cuándo, cuánto costó)
- Rate limiting por usuario (20 calls/día)
- Notificación de costes

---

## Comportamiento de fallback en detalle

Scenario: Usuario con clave personalizada, pero falla el modelo elegido

```
Comida 1: Usuario con OpenAI gpt-4
├─ Intenta: openai/gpt-4 con sk-user-key
│  └─ ✗ Error 429 (rate limit)
├─ Fallback: servidor gemma con sk-or-server
│  └─ ✓ Success (gemma responde)
└─ Resultado: Evaluación con gemma (no gpt-4, pero usuario no ve error)
```

---

## Estado actual

✅ Tabla `usuario_ia_config` implementada
✅ Integración OpenRouter
✅ Fallback automático
✅ Evaluación de comidas
✅ Generador de planes semanales
✅ Sugerencias de compra
✅ Caching de respuestas
✅ Endpoints REST
✅ Validación de claves personalizadas
✅ Rate limiting

---

## Limitaciones actuales

- Claves sin encriptación
- No hay historial de costes
- No se registra consumo de tokens
- No hay integración con webhook de OpenRouter para notificaciones
- No hay análisis de fotos de comidas (requeriría visión)

---

## Próximos pasos previstos

1. Encriptar claves en BD
2. Registrar consumo de tokens por usuario
3. Estadísticas de coste mensual
4. Integración con visión (análisis de fotos de platos)
5. Fine-tuning de prompts según feedback de usuarios
6. Webhooks para notificaciones de límite de gasto
