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
@Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
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

## 7.4 Almacenamiento de claves API de IA

Las claves API personalizadas para configuración de IA se almacenan en texto plano en la tabla `ia_config`. Esta es una limitación conocida del MVP: en producción, deberían cifrarse con una clave derivada del usuario o almacenarse en un servicio externo de gestión de secretos (ej., AWS Secrets Manager).

---

## 7.5 Contraseña mínima de seis caracteres

La restricción `@Size(min = 6)` en `RegisterRequest` establece el umbral mínimo de longitud. Seis caracteres es un límite bajo para una contraseña; lo habitual en aplicaciones en producción es exigir al menos ocho, con requisitos adicionales de complejidad. La limitación es conocida y deliberada para simplificar las pruebas durante el desarrollo.

---

---

## 7.6 Privacidad y RGPD

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

NutriFit implementa las medidas de seguridad más relevantes para su alcance actual: las contraseñas se almacenan con BCrypt, de modo que un volcado de la base de datos no expone credenciales en texto plano; el mecanismo de sesión con token opaco permite un logout real e inmediato; todos los endpoints protegidos exigen un token válido y no expirado mediante `AuthInterceptor`; HTTPS está habilitado en producción; CORS está configurado para permitir únicamente orígenes autorizados; y el cliente incluye el token en cada llamada autenticada.

La sección 7.6 documenta el tratamiento de datos personales de salud conforme al RGPD, identificando las medidas técnicas implementadas —cifrado en tránsito, eliminación en cascada, caducidad de sesiones— y las limitaciones del MVP que deberían resolverse antes de un despliegue en producción real.
