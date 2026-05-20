# 0010 — Comprobación de propietario en endpoints protegidos

## Decisión

Cada endpoint que opera sobre recursos de un usuario concreto verifica que el
`usuarioId` autenticado (extraído del atributo de request `authenticatedUserId`,
establecido por `AuthInterceptor`) coincide con el propietario del recurso antes
de ejecutar la operación.

---

## Contexto

El `AuthInterceptor` garantiza que cualquier petición a `/api/**` lleva un token
válido y almacena el id del usuario autenticado en `request.setAttribute("authenticatedUserId", ...)`.
Esto resuelve **autenticación** (¿quién eres?) pero no **autorización** (¿puedes
acceder a este recurso?).

Sin comprobación de propietario, un usuario autenticado podría leer o modificar
datos de otro usuario simplemente cambiando el parámetro `usuarioId` o el
path variable `{id}` en la petición — un IDOR (*Insecure Direct Object Reference*).

---

## Patrón aplicado

```java
Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
if (!usuarioId.equals(authId)) {
    throw new UnauthorizedException("Acceso denegado");
}
```

Para recursos identificados por su propio id (p.ej. una comida), la comprobación
se delega al servicio, que carga el recurso y verifica el campo `usuarioId`:

```java
Comida comida = comidaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(...));
if (!comida.getUsuarioId().equals(usuarioId)) {
    throw new UnauthorizedException("Acceso denegado");
}
```

---

## Endpoints que aplican este patrón

| Controlador | Operaciones protegidas |
|---|---|
| `ComidaController` | GET, POST, DELETE `/api/comidas` |
| `UsuarioIaConfigController` | GET, PUT, DELETE `/api/ia-config` |
| `ResumenIaController` | POST `/api/resumen/evaluacion-ia` (usa `authId` para queries, no el body) |
| `PerfilController` | GET, PUT `/api/perfil` |
| `EjercicioController` | GET, POST, DELETE `/api/ejercicios` |

---

## Consecuencias

**`UnauthorizedException` devuelve HTTP 403.** El `GlobalExceptionHandler` mapea
esta excepción a `403 Forbidden`, que es semánticamente correcto: el usuario está
autenticado pero no autorizado para ese recurso.

**La capa de servicio recibe el `usuarioId` autenticado.** Cuando la autorización
requiere cargar el recurso (p.ej. para comprobar el campo `usuarioId` de una entidad),
el controlador pasa el id autenticado al servicio como parámetro adicional, en lugar
de confiar en el parámetro de la petición.
