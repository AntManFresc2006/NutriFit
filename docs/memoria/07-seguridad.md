# 7. Seguridad

Este capítulo describe las medidas de seguridad implementadas en NutriFit, los mecanismos concretos que las sustentan y las limitaciones que permanecen fuera del alcance del MVP actual. El objetivo no es presentar un modelo de seguridad exhaustivo, sino documentar con honestidad lo que está hecho, cómo funciona y qué queda pendiente.

---

## 7.1 Gestión de contraseñas

Las contraseñas de los usuarios nunca se almacenan en texto plano. En el momento del registro, el servicio de autenticación delega el hash en `PasswordService`, que usa `BCryptPasswordEncoder` de Spring Security:

```java
// PasswordService.java
private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

public String hash(String rawPassword) {
    return encoder.encode(rawPassword);
}

public boolean matches(String rawPassword, String passwordHash) {
    return encoder.matches(rawPassword, passwordHash);
}
```

BCrypt aplica una función de derivación de clave con sal aleatoria incorporada y un factor de coste configurable. Esto implica que dos hashes del mismo texto producen valores distintos, lo que protege frente a ataques de diccionario precomputados. El valor almacenado en la columna `password_hash` de la tabla `usuarios` es el resultado de `encode()`, nunca la contraseña original.

Durante el login, `matches()` compara la contraseña recibida contra el hash almacenado. Si la verificación falla, el servicio lanza la misma excepción con el mismo mensaje que cuando el email no existe:

```java
// AuthServiceImpl.java
Usuario usuario = usuarioRepository.findByEmail(email)
        .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

boolean passwordOk = passwordService.matches(request.getPassword(), usuario.getPasswordHash());
if (!passwordOk) {
    throw new UnauthorizedException("Credenciales inválidas");
}
```

Devolver un mensaje idéntico en ambos casos es una decisión deliberada: impide que un atacante deduzca si un email está registrado en el sistema probando credenciales erróneas. Esta propiedad está verificada por dos tests unitarios en `AuthServiceImplTest` que comprueban explícitamente que el mensaje de error es igual en los dos caminos de fallo.

### Normalización del email

Antes de cualquier consulta al repositorio, el servicio aplica `trim()` y `toLowerCase()` al email recibido:

```java
String email = request.getEmail().trim().toLowerCase();
```

Esto garantiza que `Ana@Ejemplo.COM` y `ana@ejemplo.com` se traten como la misma identidad, eliminando duplicados silenciosos por diferencias de capitalización.

### Validación de entrada

El DTO de registro `RegisterRequest` declara las restricciones mínimas mediante anotaciones de Bean Validation:

```java
@NotBlank(message = "El email es obligatorio")
@Email(message = "El email no tiene un formato válido")
private String email;

@NotBlank(message = "La contraseña es obligatoria")
@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
private String password;
```

Spring rechaza la petición con HTTP 400 antes de que llegue a la capa de servicio si alguna restricción no se cumple.

---

## 7.2 Autenticación basada en token

### Diseño del mecanismo

NutriFit utiliza un token opaco — un UUID aleatorio — almacenado en la tabla `sesiones` de PostgreSQL. La decisión de no usar JWT se recoge en el ADR [0005 — Autenticación con token opaco en base de datos](../decisions/0005-token-auth.md); el razonamiento central es que el logout real con JWT requeriría una lista negra en base de datos, que es exactamente lo que ya se tiene con este enfoque.

El token se genera con `UUID.randomUUID()`, que internamente usa `SecureRandom`:

```java
// TokenService.java
public String generateToken() {
    return UUID.randomUUID().toString();
}
```

La tabla que lo almacena tiene la siguiente estructura:

```sql
CREATE TABLE sesiones (
    id         BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
```

La columna `token` tiene restricción `UNIQUE`, lo que impide colisiones en base de datos aunque sean extremadamente improbables con UUID v4. La clave foránea con `ON DELETE CASCADE` garantiza que las sesiones huérfanas se eliminen automáticamente si se borra un usuario.

### Ciclo de vida del token

**Creación.** Tanto el registro como el login generan un nuevo token y lo persisten con una expiración fija de siete días:

```java
// AuthServiceImpl.java
sesion.setExpiresAt(LocalDateTime.now().plusDays(7));
sesionRepository.save(sesion);
```

El token se devuelve al cliente en el cuerpo de la respuesta como parte de `AuthResponse`.

**Uso.** El cliente incluye el token en cada petición protegida mediante la cabecera `Authorization: Bearer <token>`. Cada `ApiClient` del cliente JavaFX lee el token de `SessionManager.getToken()` y lo añade a la cabecera antes de enviar la petición. En el backend, `AuthInterceptor` —un `HandlerInterceptor` registrado en `WebMvcConfig`— intercepta todas las peticiones a `/api/**` excepto `/api/auth/login` y `/api/auth/register`. Para cada petición interceptada extrae el token de la cabecera, consulta `SesionRepository.findByToken()` y verifica que la sesión existe y que `expiresAt` es posterior al momento actual. Si alguna comprobación falla, lanza `UnauthorizedException`, que `GlobalExceptionHandler` convierte en HTTP 401. El controlador de logout extrae el valor de esa cabecera antes de pasarlo al servicio:

```java
// AuthController.java
public void logout(@RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "").trim();
    authService.logout(token);
}
```

**Invalidación.** Al hacer logout, el servicio elimina la fila correspondiente de `sesiones`:

```java
// AuthServiceImpl.java
public void logout(String token) {
    if (token == null || token.isBlank()) {
        throw new BadRequestException("El token es obligatorio para cerrar sesión");
    }
    sesionRepository.deleteByToken(token);
}
```

Tras esa llamada, el token deja de existir en la base de datos. Cualquier petición posterior que lo presente no encontrará ninguna fila válida. La expiración a siete días actúa como mecanismo secundario de limpieza para sesiones que no se cerraron de forma explícita.

### Almacenamiento en el cliente

El cliente JavaFX guarda el token en `SessionManager`, una clase estática con estado exclusivamente en memoria:

```java
// SessionManager.java
public class SessionManager {
    private static String token;

    public static void setSession(Long usuarioId, String nombre, String email, String token) {
        SessionManager.token = token;
        // ...
    }

    public static void clear() {
        token = null;
        // ...
    }
}
```

El token no se persiste en disco, no se escribe en ningún fichero de configuración y no sobrevive al cierre de la aplicación. Al llamar a `clear()` — que ocurre en el flujo de logout — todos los campos se ponen a `null`. `isLoggedIn()` verifica que el token no sea nulo ni en blanco antes de considerar la sesión activa.

---

## 7.3 Cifrado en tránsito y CORS

### HTTPS en producción

El backend está desplegado en Render, que proporciona HTTPS automático con certificados válidos. El frontend en Vercel también usa HTTPS por defecto. En el desarrollo local con HTTP, no hay vector de ataque relevante porque ambos procesos corren en la misma máquina.

### Configuración CORS

Spring Boot está configurado para aceptar peticiones desde el dominio del frontend (Vercel en producción, `localhost:5173` en desarrollo). La configuración se encuentra en `WebMvcConfig`:

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:5173", "https://nutrifit.vercel.app")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
}
```

Los preflight (`OPTIONS`) no requieren token de autenticación. Todos los otros métodos sí.

---

## 7.4 Almacenamiento y exposición de claves API de IA

Las claves API personalizadas para configuración de IA se almacenan en texto plano en la tabla `ia_config`. Esta es una limitación conocida del MVP: en producción, deberían cifrarse con una clave derivada del usuario o almacenarse en un servicio externo de gestión de secretos (ej., AWS Secrets Manager).

Para mitigar la exposición en tránsito, el endpoint `GET /api/ia-config` nunca devuelve la clave completa. El getter de `UsuarioIaConfigResponse` enmascara el valor mostrando solo los últimos cuatro caracteres:

```java
public String getApiKey() {
    if (apiKey == null || apiKey.length() <= 4) return apiKey;
    return "••••" + apiKey.substring(apiKey.length() - 4);
}
```

De este modo, aunque la respuesta sea interceptada por un proxy o quede registrada en logs de red, la clave real no queda expuesta. El frontend muestra el valor enmascarado y permite al usuario reemplazarlo escribiendo uno nuevo.

---

## 7.5 Cabeceras de seguridad HTTP

`SecurityHeadersFilter`, un `OncePerRequestFilter` de Spring, añade en cada respuesta un conjunto de cabeceras que el navegador usa para reforzar el aislamiento de la aplicación:

```java
response.setHeader("X-Content-Type-Options", "nosniff");
response.setHeader("X-Frame-Options", "DENY");
response.setHeader("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'");
response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
```

| Cabecera | Protección |
|----------|-----------|
| `X-Content-Type-Options: nosniff` | Impide que el navegador infiera el MIME type, bloqueando ataques de content sniffing |
| `X-Frame-Options: DENY` | Impide que la aplicación sea embebida en un `<iframe>`, bloqueando clickjacking |
| `Content-Security-Policy` | Restringe los orígenes de scripts, estilos y recursos; `frame-ancestors 'none'` duplica la protección anti-clickjacking |
| `Strict-Transport-Security` | Fuerza HTTPS durante un año e incluye subdominos en la preload list (evita SSL stripping en la primera visita) |
| `Referrer-Policy` | Limita la información de referencia enviada a otros dominios |
| `Permissions-Policy` | Declara explícitamente que la app no usa cámara, micrófono ni geolocalización |

El frontend también incluye una meta CSP en `index.html` que restringe los orígenes desde los que el navegador puede cargar recursos:

```html
<meta http-equiv="Content-Security-Policy"
  content="default-src 'self';
           connect-src 'self' https://nutrifit-backend-ndoj.onrender.com https://world.openfoodfacts.org;
           img-src 'self' data: https:;
           style-src 'self' 'unsafe-inline';
           font-src 'self';" />
```

---

## 7.6 Protección contra SSRF

El módulo de configuración de IA permite al usuario especificar una URL de proxy arbitraria. Sin validación, un atacante podría suministrar URLs internas (`http://localhost:8080/admin`, `http://169.254.169.254/`) para que el backend acceda a recursos de red interna — ataque conocido como SSRF (Server-Side Request Forgery).

La protección se implementa en dos capas:

**Capa 1 — Bean Validation en DTO:** `@Pattern` en el campo `proxyUrl` de `UsuarioIaConfigRequest` garantiza que la URL tenga el formato `https://dominio/...` antes de llegar al servicio:

```java
@NotBlank
@Pattern(
    regexp = "^https://[a-zA-Z0-9][a-zA-Z0-9\\-.]+(:\\d+)?(/.*)?$",
    message = "proxyUrl debe ser una URL HTTPS con dominio público"
)
private String proxyUrl;
```

**Capa 2 — Validación programática en servicio:** `validateProxyUrl()` en `UsuarioIaConfigServiceImpl` verifica el esquema y bloquea todos los rangos de IP privada conocidos:

```java
private static final Set<String> BLOCKED_HOST_PREFIXES = Set.of(
        "localhost", "127.", "10.", "172.16.", ..., "192.168.", "169.254.", "::1", "0."
);

private void validateProxyUrl(String rawUrl) {
    URI uri = URI.create(rawUrl.trim());
    if (!"https".equalsIgnoreCase(uri.getScheme()))
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proxyUrl debe usar HTTPS");
    String host = uri.getHost().toLowerCase();
    if (BLOCKED_HOST_PREFIXES.stream().anyMatch(host::startsWith))
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proxyUrl apunta a una dirección no permitida");
}
```

Este método se invoca tanto en `saveConfig()` como en `testConfig()`, por lo que ningún path puede eludirlo.

---

## 7.7 Rate limiting

NutriFit implementa dos mecanismos de rate limiting independientes:

**`LoginRateLimiter`** — por dirección IP. Protege los endpoints de autenticación (`/api/auth/login`, `/api/auth/register`) limitando a 10 peticiones por minuto. Impide ataques de fuerza bruta sobre credenciales.

**`IaRateLimiter`** — por usuario autenticado. Limita a 5 peticiones por minuto a todos los endpoints que generan llamadas a IA. Se aplica en:
- `POST /api/resumen/evaluacion-ia` — evaluación nutricional
- `POST /api/plan-semanal` — generación de plan semanal
- `POST /api/alimentos/escanear-foto` — análisis IA de foto de producto
- `POST /api/ia-config/test` — prueba de configuración de proxy IA
- `POST /api/detective` — análisis forense nutricional

Sin este límite, un usuario podría generar miles de llamadas a APIs externas de pago en segundos.

---

## 7.8 Validación de entrada en uploads

El endpoint `POST /api/alimentos/escanear-foto` acepta una imagen codificada en Base64. Sin validación, un atacante podría enviar archivos de tipo arbitrario o de tamaño excesivo para causar DoS de memoria.

`EscanearFotoRequest` declara dos restricciones mediante Bean Validation:

```java
@NotBlank
@Size(max = 7_340_032, message = "La imagen no puede superar 5 MB")
private String imagenBase64;

@NotBlank
@Pattern(regexp = "^image/(jpeg|png|webp|gif)$",
         message = "Tipo de imagen no permitido. Use JPEG, PNG, WebP o GIF")
private String mimeType;
```

El límite de 7 340 032 caracteres corresponde a 5 MB en Base64 (factor ×1,33). La whitelist de MIME types impide enviar PDFs, ejecutables u otros formatos no esperados.

---

## 7.9 Manejo seguro de errores

Un manejo de errores descuidado puede filtrar al cliente detalles de implementación interna: rutas de clase Java, nombres de tablas, stack traces, versiones de librerías. NutriFit aplica tres defensas:

**`GlobalExceptionHandler`** (`@RestControllerAdvice`) intercepta todas las excepciones antes de que Spring genere la respuesta. El handler genérico devuelve siempre el mismo mensaje neutro:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
    return ResponseEntity.status(500).body(
        buildError(INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno en el servidor", ...)
    );
}
```

Para `HttpMessageNotReadableException` e `IllegalArgumentException` (que pueden contener detalles de parsing interno), se devuelve igualmente un mensaje genérico: `"Solicitud inválida"`. `ConstraintViolationException` devuelve solo el mensaje de la violación, sin el path interno `método.parámetro.campo`.

**`application.properties`** deshabilita cualquier mecanismo residual de Spring Boot que pudiera incluir stack traces en respuestas:

```properties
server.error.include-stacktrace=never
server.error.include-message=never
server.error.include-binding-errors=never
```

**Puntos concretos corregidos:** cuatro lugares del código original exponían `e.getMessage()` directamente al cliente (`ResumenIaController`, `AlimentoController`, `EscanerServiceImpl`, `GlobalExceptionHandler`). Todos fueron reemplazados por mensajes genéricos.

---

---

## 7.10 Privacidad y RGPD

NutriFit almacena datos de salud de sus usuarios: peso corporal, objetivo de peso, ingesta calórica diaria, registros de ejercicio e hidratación. Bajo el Reglamento General de Protección de Datos (RGPD, Reglamento UE 2016/679), los datos relativos a la salud son una categoría especial que requiere base jurídica explícita y medidas técnicas reforzadas (artículo 9).

### Datos almacenados y finalidad

| Dato | Tabla | Finalidad |
|---|---|---|
| Email y contraseña | `usuarios` | Autenticación e identificación |
| Nombre | `usuarios` | Personalización de la interfaz |
| Peso, altura, sexo, fecha de nacimiento | `perfil` | Cálculo de TMB/TDEE |
| Historial de peso | `peso_historial` | Seguimiento de progreso |
| Comidas y alimentos consumidos | `comidas`, `comida_items` | Cálculo nutricional diario |
| Registros de ejercicio | `ejercicios_registro` | Cálculo de calorías quemadas |
| Hidratación diaria | `hidratacion` | Seguimiento de ingesta hídrica |
| Configuración IA | `ia_config` | Personalización de evaluaciones de IA |
| Token de sesión | `sesiones` | Autenticación sin estado de servidor |

Ningún dato se comparte con terceros a excepción de las evaluaciones nutricionales enviadas a OpenRouter para generar respuestas de IA. En esas peticiones se incluye el resumen diario del usuario (calorías, macros, TDEE) sin datos identificativos como nombre o email.

### Medidas técnicas implementadas

**Minimización de contraseñas.** Las contraseñas se almacenan como hash BCrypt; el texto original nunca se persiste (véase sección 7.1).

**Eliminación en cascada.** La tabla `usuarios` tiene `ON DELETE CASCADE` en todas las tablas que referencian `usuario_id`. Eliminar un usuario borra automáticamente todo su historial de peso, comidas, ejercicios, sesiones e hidratación:

```sql
-- Ejemplo: peso_historial
FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
```

**Caducidad de sesiones.** Los tokens expiran a los siete días. Una sesión abandonada sin logout explícito deja de ser válida pasado ese plazo.

**HTTPS en producción.** Todos los datos se transmiten cifrados en tránsito (véase sección 7.3).

### Derechos del usuario

Con las herramientas disponibles en la aplicación, el usuario puede ejercer los siguientes derechos reconocidos por el RGPD:

- **Acceso** (art. 15): los endpoints `/api/perfil`, `/api/peso-historial`, `/api/comidas`, `/api/ejercicios-registro` e `/api/hidratacion` devuelven todos los datos almacenados del usuario autenticado.
- **Rectificación** (art. 16): `/api/perfil` (PUT) permite actualizar los datos del perfil.
- **Supresión** (art. 17): no existe un endpoint de borrado de cuenta en el MVP actual. Un usuario que desee eliminar su cuenta debe solicitarlo directamente al administrador de la base de datos.

### Limitaciones en el alcance del TFG

Como proyecto académico, NutriFit no implementa todos los requisitos que exigiría un despliegue en producción real:

- No existe una política de privacidad visible en la interfaz ni un mecanismo de consentimiento explícito al registro.
- No se ha firmado un Acuerdo de Encargado de Tratamiento (DPA) con OpenRouter.
- No hay endpoint de borrado de cuenta para que el usuario pueda ejercer el derecho de supresión de forma autónoma.
- Las claves API de IA se almacenan sin cifrar en base de datos (véase sección 7.4).

Estas carencias son conocidas y forman parte de las limitaciones documentadas en la sección 8.4 de las conclusiones.

---

## Cierre de la sección

NutriFit cubre los vectores de ataque más relevantes del OWASP Top 10 para su arquitectura:

- **Contraseñas**: BCrypt con sal aleatoria, mínimo 8 caracteres; el volcado de la BD no expone credenciales.
- **Autenticación**: token opaco UUID con expiración de 7 días, logout real por eliminación de la fila en BD, mismo mensaje de error para email inexistente y contraseña incorrecta (anti-enumeración).
- **Autorización**: todos los endpoints protegidos verifican que el recurso pertenece al usuario autenticado (IDOR mitigado); `AuthInterceptor` aplica esta comprobación globalmente.
- **Inyección SQL**: 100% de consultas parametrizadas mediante `JdbcTemplate`; sin concatenación de cadenas en SQL.
- **SSRF**: validación en dos capas (Bean Validation + blocklist de IPs privadas) en el proxy de IA.
- **Cabeceras de seguridad**: HSTS, CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy, Permissions-Policy.
- **Rate limiting**: por IP en auth (10 req/min), por usuario en todos los endpoints IA (5 req/min).
- **Validación de uploads**: whitelist de MIME types y límite de 5 MB en imágenes.
- **Exposición de errores**: `GlobalExceptionHandler` centraliza todas las excepciones con mensajes genéricos; `server.error.include-stacktrace=never` como defensa en profundidad.
- **API key en respuesta**: enmascarada (solo últimos 4 caracteres) en `GET /api/ia-config`.
- **CSP en frontend**: meta tag en `index.html` restringe orígenes permitidos en el navegador.

La sección 7.10 documenta el tratamiento de datos personales de salud conforme al RGPD.
