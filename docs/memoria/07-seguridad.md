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

NutriFit utiliza un token opaco — un UUID aleatorio — almacenado en la tabla `sesiones` de MariaDB. La decisión de no usar JWT se recoge en el ADR [0005 — Autenticación con token opaco en base de datos](../decisions/0005-token-auth.md); el razonamiento central es que el logout real con JWT requeriría una lista negra en base de datos, que es exactamente lo que ya se tiene con este enfoque.

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
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
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

**Uso.** El cliente incluye el token en cada petición mediante la cabecera `Authorization: Bearer <token>`. El controlador de logout extrae el valor de esa cabecera antes de pasarlo al servicio:

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

## 7.3 Limitaciones del MVP

Las medidas descritas en las secciones anteriores cubren los aspectos más críticos para una aplicación de escritorio local. Sin embargo, existen limitaciones conocidas que no se han abordado en el alcance del MVP y que deberían resolverse antes de cualquier despliegue en entorno compartido o con acceso de red.

### Sin cifrado en tránsito

El backend no tiene HTTPS configurado. Las comunicaciones entre el cliente JavaFX y la API REST se realizan sobre HTTP plano. En el contexto actual — ambos procesos en la misma máquina local — esto no supone un vector de ataque real, ya que el tráfico no sale de la interfaz de loopback. En cualquier escenario donde el backend fuera accesible por red, los tokens y las contraseñas viajarían en claro.

### Validación del token no aplicada globalmente

La infraestructura de token está completa: generación, almacenamiento, expiración y borrado en logout. Lo que no está implementado es la validación del token como requisito previo en los endpoints protegidos. En el MVP actual, un endpoint como `GET /api/alimentos` responde aunque la petición no incluya un token válido o no incluya ninguno. El único endpoint que extrae y procesa el token en la cabecera es `/api/auth/logout`.

Esto significa que, en la práctica, la autenticación protege el acceso lógico desde el cliente, pero no impide peticiones directas a la API sin credenciales. La protección es suficiente para el contexto de una aplicación de escritorio local donde el servidor solo escucha en `localhost`, pero no sería aceptable en un entorno expuesto.

El paso natural para resolverlo sería implementar un filtro de Spring que intercepte todas las peticiones a `/api/**` (excepto `/api/auth/login` y `/api/auth/register`), consulte la tabla `sesiones` con el token recibido y rechace las peticiones sin token válido con HTTP 401.

### Contraseña mínima de seis caracteres

La restricción `@Size(min = 6)` en `RegisterRequest` establece el umbral mínimo de longitud. Seis caracteres es un límite bajo para una contraseña; lo habitual en aplicaciones en producción es exigir al menos ocho, con requisitos adicionales de complejidad. La limitación es conocida y deliberada para simplificar las pruebas durante el desarrollo.

---

## Cierre de la sección

NutriFit implementa las dos medidas de seguridad más relevantes para su alcance: las contraseñas se almacenan con BCrypt, de modo que un volcado de la base de datos no expone credenciales en texto plano; y el mecanismo de sesión con token opaco permite un logout real e inmediato, a diferencia de soluciones basadas en tokens firmados sin estado servidor. El cliente no persiste el token fuera de la memoria de proceso.

Las limitaciones documentadas — ausencia de HTTPS, validación de token no aplicada a todos los endpoints, umbral de contraseña bajo — son consecuencia directa del alcance del MVP y no de decisiones de diseño irreversibles. La arquitectura del backend está preparada para incorporar esas mejoras sin cambios estructurales.
