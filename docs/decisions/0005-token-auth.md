# 0005 — Autenticación con token opaco en base de datos en lugar de JWT

## Decisión

La autenticación se implementa con un **token UUID almacenado en la tabla `sesiones`**,
con fecha de expiración y borrado explícito al hacer logout.
No se usa JWT ni ninguna librería de tokens firmados.

---

## Contexto

NutriFit necesita identificar al usuario entre peticiones y permitir cerrar sesión de
forma explícita. Surgieron dos alternativas estándar en aplicaciones Spring Boot:

| Enfoque | Mecanismo |
|---|---|
| Token opaco en BD | UUID en tabla `sesiones` con `expires_at` |
| Token firmado (JWT) | Token autofirmado, sin estado servidor |

---

## Motivo de la elección

### 1. El logout es real

Con JWT, un token válido sigue siendo válido aunque el usuario haga logout,
hasta que expire por sí solo. Para invalidarlo habría que mantener una lista negra,
que es exactamente una tabla en base de datos — la misma infraestructura que usamos,
pero más compleja.

Con token en BD, el logout borra la fila:

```java
// AuthServiceImpl.java
public void logout(String token) {
    sesionRepository.deleteByToken(token);
}
```

Después de esa llamada, el token no existe en `sesiones` y cualquier petición
que lo presente falla la validación.

### 2. Sin gestión de claves criptográficas

JWT requiere una clave secreta (HMAC) o un par de claves (RSA/EC).
Esa clave debe almacenarse de forma segura, rotarse cuando se compromete
y configurarse correctamente en cada entorno.

En una aplicación de escritorio local eso es complejidad sin beneficio real.
El token UUID se genera con `UUID.randomUUID()`, que usa `SecureRandom`
internamente — suficiente para el alcance de este proyecto.

```java
// TokenService.java
public String generateToken() {
    return UUID.randomUUID().toString();
}
```

### 3. El modelo mental es transparente

La tabla `sesiones` tiene exactamente lo que parece:

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

Para saber si una sesión es válida: `SELECT * FROM sesiones WHERE token = ? AND expires_at > NOW()`.
Para cerrarla: `DELETE FROM sesiones WHERE token = ?`.
No hay decodificación, no hay verificación de firma, no hay claims.

### 4. La expiración es configurable sin reemitir tokens

En JWT, cambiar el tiempo de expiración requiere reemitir el token.
Aquí, `expires_at` es un campo de la fila — puede actualizarse, prolongarse
o acortarse sin tocar el token del cliente.
La implementación actual fija 7 días:

```java
// AuthServiceImpl.java
sesion.setExpiresAt(LocalDateTime.now().plusDays(7));
```

---

## Consecuencias asumidas

**Una consulta a BD por petición autenticada.** El backend consulta `sesiones`
para validar el token en cada llamada protegida. Con JWT el backend podría verificar
el token en memoria sin tocar BD. Para una aplicación de escritorio de un único usuario
concurrente, este coste es irrelevante.

**Escala menos que JWT.** Si NutriFit se convirtiera en un servicio web con miles
de usuarios concurrentes, la consulta a `sesiones` en cada petición sería un cuello de botella.
En el alcance actual — aplicación local de escritorio — no aplica.

**El token no contiene información.** A diferencia de un JWT, el token UUID no puede
decodificarse para extraer el `usuarioId` u otros datos sin consultar la BD.
Esto es intencional: el cliente ya tiene el `usuarioId` del login y lo pasa como
parámetro cuando lo necesita.

---

## Alcance de esta decisión

La infraestructura de token está completamente implementada:
generación en registro y login, almacenamiento en `sesiones`, expiración a 7 días
y borrado en logout.

La validación del token en cada endpoint protegido es el siguiente paso natural
fuera del alcance del MVP actual.
