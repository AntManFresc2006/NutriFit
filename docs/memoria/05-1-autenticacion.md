# 5.1 Autenticación

## 5.1.1 Problema que resuelve el módulo

NutriFit almacena datos personales vinculados a cada usuario: perfil biométrico, historial de comidas y registro nutricional. Para que un usuario pueda acceder únicamente a sus propios datos, el sistema necesita saber quién realiza cada petición.

El módulo de autenticación cubre esa necesidad. Permite registrar nuevos usuarios, verificar la identidad de usuarios existentes y establecer una sesión activa representada por un token. También permite cerrar esa sesión de forma explícita, invalidando el token en el servidor.

---

## 5.1.2 API del módulo

El módulo expone tres endpoints bajo el prefijo `/api/auth`, implementados en `AuthController`:

| Método | Ruta                  | Código de éxito | Descripción                                    |
|--------|-----------------------|-----------------|------------------------------------------------|
| POST   | `/api/auth/register`  | 201 Created     | Registra un nuevo usuario y abre sesión        |
| POST   | `/api/auth/login`     | 200 OK          | Autentica un usuario existente y abre sesión   |
| POST   | `/api/auth/logout`    | 204 No Content  | Invalida el token de sesión activo             |

Los dos primeros endpoints aceptan un cuerpo JSON y devuelven un `AuthResponse` con `usuarioId`, `nombre`, `email` y `token`. El tercero no devuelve cuerpo; lee el token de la cabecera `Authorization`. Los DTOs de entrada están anotados con `@Valid`, de modo que Spring valida el cuerpo antes de que llegue al servicio.

---

## 5.1.3 Flujo de registro

El registro espera `nombre`, `email` y `password`. `RegisterRequest` exige que el nombre y el email no sean vacíos, que el email tenga formato válido y que la contraseña tenga al menos seis caracteres.

`AuthServiceImpl.register()` ejecuta los pasos siguientes en orden:

1. **Normalización del email.** Se aplica `trim()` y `toLowerCase()` antes de cualquier consulta, de modo que `Ana@Ejemplo.COM` y `ana@ejemplo.com` se traten como la misma identidad.
2. **Comprobación de unicidad.** Si ya existe un usuario con ese email en la tabla `usuarios`, se lanza `BadRequestException` y no se persiste nada.
3. **Hash de la contraseña.** La contraseña en claro se pasa a `PasswordService.hash()`, que devuelve el hash BCrypt. El valor en claro nunca se almacena.
4. **Persistencia del usuario.** Se crea y persiste el `Usuario` con nombre, email normalizado y hash.
5. **Generación y persistencia del token.** `TokenService.generateToken()` produce un UUID v4. Se crea una `Sesion` con el `usuarioId`, el token y una expiración de siete días, que se persiste en la tabla `sesiones`.
6. **Respuesta.** Se devuelve el `AuthResponse` con los datos del usuario y el token.

El registro abre sesión de forma automática: el usuario no necesita hacer login tras registrarse.

---

## 5.1.4 Flujo de login

El login espera `email` y `password`. `AuthServiceImpl.login()` sigue estos pasos:

1. **Normalización del email.** Mismo `trim()` y `toLowerCase()`.
2. **Búsqueda del usuario.** Se consulta la tabla `usuarios` por email. Si no existe, se lanza `UnauthorizedException("Credenciales inválidas")`.
3. **Verificación de la contraseña.** `PasswordService.matches()` compara la contraseña recibida contra el hash almacenado. Si no coinciden, se lanza la misma excepción con el mismo mensaje.
4. **Generación y persistencia del token.** Igual que en el registro: UUID v4 y sesión con expiración a siete días.
5. **Respuesta.** Se devuelve `AuthResponse` con los datos del usuario y el nuevo token.

> **Figura 5.1** — Diagrama de secuencia del flujo de login.
> *(ver `docs/diagrams/secuencia-login.puml`)*

El diagrama muestra la secuencia completa desde el cliente hasta MariaDB: búsqueda del usuario, verificación BCrypt, generación del token, inserción en `sesiones` y respuesta.

---

## 5.1.5 Flujo de logout

El cliente envía el token en la cabecera `Authorization: Bearer <token>`. El controlador lo extrae y lo pasa al servicio:

```java
// AuthController.java
String token = authorizationHeader.replace("Bearer ", "").trim();
authService.logout(token);
```

El servicio valida que el token no sea nulo ni vacío y delega la eliminación en `JdbcSesionRepository`:

```sql
DELETE FROM sesiones WHERE token = ?
```

Si el token no existe en la tabla —porque ya expiró o fue borrado previamente— la sentencia no afecta a ninguna fila y el cliente recibe igualmente HTTP 204.

---

## 5.1.6 Mecanismo de seguridad

Las contraseñas se almacenan exclusivamente como hash BCrypt mediante `PasswordService`. El algoritmo incorpora una sal aleatoria por hash, por lo que el valor almacenado en `password_hash` nunca puede revertirse a la contraseña original. Los detalles del algoritmo y la justificación de la elección se desarrollan en §7.1.

El token de sesión es un UUID v4 generado por `UUID.randomUUID()`, que usa `SecureRandom` internamente. Se persiste en la tabla `sesiones` junto con `usuario_id` y `expires_at`. La invalidación en el logout consiste en eliminar esa fila. La justificación de usar un token opaco en lugar de JWT se recoge en el ADR 0005 y se desarrolla en §7.2.

La validación del token en cada petición protegida se implementa mediante `AuthInterceptor`, un `HandlerInterceptor` Spring MVC registrado en `WebMvcConfig` para todas las rutas `/api/**` excepto `/api/auth/login` y `/api/auth/register`. El interceptor extrae el token de la cabecera `Authorization: Bearer <token>`, consulta `sesionRepository.findByToken()` —método añadido a `SesionRepository` y a `JdbcSesionRepository`— y comprueba que la sesión existe y que `expiresAt` es posterior al momento actual. Si alguna comprobación falla, lanza `UnauthorizedException`, que `GlobalExceptionHandler` convierte en HTTP 401 con la estructura `ApiError` estándar.

---

## 5.1.7 Integración con el cliente JavaFX

`AuthApiClient` centraliza la comunicación con los tres endpoints. Usa `java.net.http.HttpClient` para las peticiones y `ObjectMapper` para serializar y deserializar el cuerpo JSON. Para el logout incluye el token en la cabecera `Authorization`.

`LoginController` gestiona la pantalla de acceso, que contiene el formulario de login y el de registro en una misma vista. Las llamadas a `AuthApiClient` se ejecutan en hilos de fondo con `javafx.concurrent.Task`:

```java
// LoginController.java — onLogin()
Task<Map<String, Object>> task = new Task<>() {
    @Override
    protected Map<String, Object> call() throws Exception {
        Map<String, Object> resp = authApiClient.login(email, password);
        cargarTdeeEnSesion(resp);
        return resp;
    }
};
task.setOnSucceeded(event -> {
    Map<String, Object> response = task.getValue();
    SessionManager.setSession(
            ((Number) response.get("usuarioId")).longValue(),
            (String) response.get("nombre"),
            (String) response.get("email"),
            (String) response.get("token")
    );
    abrirPantallaAlimentos();
});
```

El flujo de registro sigue la misma estructura con `authApiClient.register()`.

Tras autenticarse, el token y los datos del usuario se almacenan en `SessionManager`, una clase con campos estáticos que actúa como estado de sesión compartido entre pantallas. No persiste nada en disco; todo desaparece al cerrar la aplicación o al llamar a `SessionManager.clear()`.

Después del login o el registro, el controlador intenta cargar el TDEE del usuario en `SessionManager` mediante una llamada a `GET /api/perfil/{id}`. Si la llamada falla —por ejemplo, porque el usuario recién registrado todavía no tiene perfil configurado— el error se ignora y la sesión queda establecida igualmente. El TDEE se cargará cuando el usuario visite la pantalla de perfil.

---

## 5.1.8 Manejo de errores

| Situación                                  | Excepción                           | Código HTTP |
|--------------------------------------------|-------------------------------------|-------------|
| Email ya registrado                        | `BadRequestException`               | 400         |
| Email o contraseña incorrectos en login    | `UnauthorizedException`             | 401         |
| Token nulo o en blanco en logout           | `BadRequestException`               | 400         |
| Campo obligatorio ausente o mal formateado | Bean Validation (via `@Valid`)      | 400         |

El mensaje de error en los dos casos de fallo del login es deliberadamente el mismo: `"Credenciales inválidas"`. Devolver mensajes distintos para «email no encontrado» y «contraseña incorrecta» permitiría a un atacante enumerar qué emails están registrados en el sistema.

En el cliente, si el backend devuelve un código fuera del rango 2xx, `AuthApiClient` lanza una `IOException` con el código y el cuerpo del error. `LoginController` la captura en `task.setOnFailed()` y muestra el mensaje en la pantalla.

---

## 5.1.9 Tests de `AuthServiceImplTest`

`AuthServiceImplTest` contiene nueve tests unitarios en tres clases anidadas (`Register`, `Login`, `Logout`). Los cuatro colaboradores de `AuthServiceImpl` se sustituyen por mocks de Mockito; no se requiere base de datos ni contexto de Spring.

Los casos cubiertos son: email duplicado en registro, registro exitoso con verificación de token y expiración, normalización del email con mayúsculas y espacios, email inexistente en login, contraseña incorrecta sin creación de sesión, login exitoso, token nulo en logout, token en blanco en logout y eliminación correcta de sesión con token válido. La descripción completa de la batería de pruebas del backend se recoge en §6.
