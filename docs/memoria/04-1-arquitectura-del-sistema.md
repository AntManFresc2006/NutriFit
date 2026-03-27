# 4.1 Arquitectura del sistema

NutriFit sigue una arquitectura de tres capas con separación física entre presentación, lógica de aplicación y persistencia. Los tres componentes son procesos independientes que se comunican a través de interfaces bien definidas: el cliente JavaFX habla con el backend a través de una API REST, y el backend accede a MariaDB mediante JDBC. Cada capa interactúa con la siguiente a través de contratos bien definidos y sin depender de sus detalles de implementación.

> **Figura 4.1** — Diagrama de arquitectura del sistema.
> *(ver `docs/diagrams/arquitectura.puml`)*

---

## 4.1.1 Visión general

El sistema se organiza en tres capas con responsabilidades diferenciadas:

| Capa | Tecnología | Responsabilidad principal |
|------|-----------|--------------------------|
| Presentación | JavaFX 21 | Interfaz gráfica, navegación, llamadas HTTP |
| Lógica de aplicación | Spring Boot 3.2 (puerto 8080) | API REST, reglas de negocio, acceso a datos |
| Persistencia | MariaDB (`nutrifit`) | Almacenamiento relacional del estado del sistema |

El cliente y el backend son módulos Maven independientes dentro del mismo repositorio. Pueden arrancarse, construirse y probarse por separado. El backend no sabe nada del cliente; el cliente no sabe nada de MariaDB. Ambas dependencias son unidireccionales y cruzar la capa de comunicación definida es la única vía de interacción.

---

## 4.1.2 Capa de presentación: el cliente JavaFX

El cliente es una aplicación de escritorio implementada con JavaFX 21. Su punto de entrada es `NutriFitClientApplication`, que extiende `javafx.application.Application` y carga la primera pantalla al arrancar:

```java
// NutriFitClientApplication.java
FXMLLoader fxmlLoader = new FXMLLoader(
    NutriFitClientApplication.class.getResource("/com/nutrifit/client/login-view.fxml")
);
Scene scene = new Scene(fxmlLoader.load(), 1000, 620);
stage.setTitle("NutriFit - Acceso");
stage.setScene(scene);
```

La interfaz está compuesta por cuatro pantallas definidas en FXML: acceso (login/registro), gestión de alimentos, diario nutricional y perfil de usuario. Cada pantalla tiene su propio controlador JavaFX que gestiona la interacción del usuario y delega las operaciones de datos en los clientes HTTP.

Dentro del cliente, los componentes se distribuyen en tres grupos:

- **Pantallas FXML y controladores.** Gestionan el ciclo de vida de la vista, responden a eventos del usuario y actualizan los controles. No contienen lógica de negocio ni SQL.

- **Clientes HTTP (`ApiClients`).** Una clase por módulo (`AlimentoApiClient`, `AuthApiClient`, `PerfilApiClient`, etc.) que construye las peticiones HTTP usando `java.net.http.HttpClient`, las envía al backend y deserializa la respuesta JSON con Jackson `ObjectMapper`. La comunicación con el backend está completamente encapsulada en estas clases; los controladores de pantalla no construyen URLs ni manejan JSON directamente.

- **`SessionManager`.** Clase estática que mantiene en memoria los datos de sesión activa: identificador del usuario, nombre, email, token de autenticación y TDEE. Es el único punto de estado compartido entre pantallas. No persiste nada en disco; todo desaparece al cerrar la aplicación o al llamar a `clear()`.

Las operaciones de red se ejecutan en hilos de fondo con `javafx.concurrent.Task` para no bloquear el hilo de la interfaz gráfica. El resultado se devuelve al hilo de JavaFX a través de los callbacks `setOnSucceeded` y `setOnFailed`.

---

## 4.1.3 Capa de lógica de aplicación: el backend Spring Boot

El backend es una aplicación Spring Boot 3.2 que arranca con el perfil `local` y queda escuchando en `http://localhost:8080`. Expone cinco grupos de endpoints REST:

| Prefijo              | Módulo                        |
|----------------------|-------------------------------|
| `/api/auth`          | Registro, login, logout       |
| `/api/alimentos`     | CRUD del catálogo de alimentos|
| `/api/comidas`       | Registro e ítems de comidas   |
| `/api/resumen-diario`| Agregación nutricional por día|
| `/api/perfil`        | Consulta y actualización de perfil |
| `/api/ejercicios`    | Catálogo de ejercicios (GET, POST) |
| `/api/ejercicios-registro` | Registro de ejercicios por usuario y fecha (GET, POST, DELETE) |

Internamente, todos los módulos siguen la misma estructura en capas verticales:

```
Controller → Service → Repository → JdbcTemplate → MariaDB
```

**Controladores.** Reciben la petición HTTP, validan el cuerpo con `@Valid` cuando aplica, delegan la operación en el servicio correspondiente y devuelven la respuesta. No contienen lógica de negocio. Cada controlador está anotado con `@RestController` y produce JSON de forma automática.

**Servicios.** Contienen las reglas de negocio: validaciones de dominio, transformaciones, cálculos (la fórmula de Mifflin-St Jeor para TMB y TDEE vive aquí), y coordinación entre repositorios cuando una operación afecta a más de una tabla. Son la única capa que puede lanzar excepciones de dominio (`ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`).

**Repositorios.** Cada módulo declara una interfaz `XxxRepository` y una implementación `JdbcXxxRepository` que usa `JdbcTemplate` con SQL directo y un `RowMapper` propio. No hay ORM ni gestión automática de esquema desde el código Java. La decisión de usar JDBC en lugar de JPA se documenta en el ADR 0004 y se desarrolla en §4.5.1.

**`GlobalExceptionHandler`.** Anotado con `@RestControllerAdvice`, intercepta todas las excepciones no capturadas y las convierte en una respuesta JSON con estructura uniforme (`ApiError`). El cliente siempre recibe el mismo formato de error, con independencia del tipo de excepción que se haya producido:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "La altura mínima es 100 cm",
  "path": "/api/perfil/1"
}
```

**Servicios de seguridad.** `PasswordService` encapsula el uso de `BCryptPasswordEncoder`; `TokenService` genera tokens UUID v4 mediante `SecureRandom`. Ambos están inyectados únicamente en `AuthServiceImpl`. El mecanismo de autenticación completo se describe en §7.

---

## 4.1.4 Capa de persistencia: MariaDB y Flyway

MariaDB almacena el estado del sistema en una base de datos llamada `nutrifit`. El esquema está compuesto por cinco tablas: `usuarios`, `alimentos`, `sesiones`, `comidas` y `comida_alimentos`. Su estructura y relaciones se describen en detalle en §4.2.

El esquema no se define ni se modifica manualmente. Flyway aplica las migraciones automáticamente al arrancar el backend, leyendo los scripts del directorio `backend/src/main/resources/db/migration/`. Si todas las migraciones ya están aplicadas, Flyway valida la consistencia y no ejecuta nada. Si hay migraciones nuevas, las aplica en orden. Esto garantiza que cualquier entorno que arranque desde cero queda con el mismo esquema exacto que el entorno de desarrollo.

La compatibilidad entre Flyway y el acceso JDBC directo es completa: no hay conflicto de gestión de esquema porque el código Java nunca intenta crear ni modificar tablas. Flyway es la única autoridad sobre la estructura de la base de datos.

---

## 4.1.5 Comunicación entre cliente y backend

La comunicación se realiza exclusivamente mediante HTTP sobre JSON. No hay ningún mecanismo de comunicación directo entre el cliente y la base de datos; el cliente no conoce las credenciales de MariaDB.

Cada petición HTTP sigue el mismo patrón desde el cliente:

1. El controlador de pantalla invoca un método del `ApiClient` correspondiente.
2. El `ApiClient` construye la petición con `HttpRequest.newBuilder()`, serializa el cuerpo con Jackson si aplica, y envía la petición con `httpClient.send()`.
3. El `ApiClient` verifica el código de estado HTTP. Si no es 2xx, lanza una excepción con el mensaje del cuerpo de error.
4. Si es 2xx, deserializa el cuerpo JSON en el DTO correspondiente y lo devuelve al controlador.

En los flujos que requieren autenticación, el token se incluye en la cabecera `Authorization: Bearer <token>`. El token se obtiene de `SessionManager.getToken()`, que lo recibió del backend en el login o el registro.

La ausencia de HTTPS en el MVP implica que las peticiones viajan en texto plano. Como se documenta en §7.3, esto es aceptable en el contexto de una aplicación de escritorio local donde cliente y servidor se ejecutan en la misma máquina, pero sería una limitación relevante en cualquier despliegue en red.

---

## 4.1.6 Swagger UI como apoyo al desarrollo

La dependencia `springdoc-openapi-starter-webmvc-ui` genera automáticamente la documentación interactiva de la API a partir de las anotaciones de los controladores y los DTOs. Con el backend activo, la interfaz está disponible en:

```
http://localhost:8080/swagger-ui.html
```

La metadata de la API se configura en `OpenApiConfig`:

```java
// OpenApiConfig.java
return new OpenAPI()
    .info(new Info()
        .title("NutriFit API")
        .description("API REST para gestión de alimentos, comidas y resumen nutricional diario")
        .version("0.1.0"));
```

Swagger UI cumple dos funciones durante el desarrollo. La primera es la verificación rápida de endpoints sin necesidad de herramientas externas: permite ejecutar peticiones directamente desde el navegador con los esquemas de entrada y salida documentados. La segunda es complementar las pruebas manuales recogidas en los archivos `.http` de `docs/api/`, que se describen en §6.3.

---

## 4.1.7 Ventajas del desacoplamiento

La separación entre las tres capas tiene consecuencias prácticas sobre el desarrollo y la verificabilidad del proyecto.

**El backend es verificable de forma independiente.** Los 60 tests unitarios del backend no requieren el cliente JavaFX ni la base de datos activa. Las pruebas manuales de la API con los archivos `.http` y con Swagger UI tampoco. El backend puede desarrollarse, probarse y depurarse sin arrancar ningún otro componente.

**El cliente es sustituible.** El backend no sabe que el cliente es JavaFX. Podría sustituirse por una interfaz web, una aplicación móvil o un cliente de línea de comandos sin modificar una sola línea del backend, siempre que se respete el contrato de la API REST.

**El esquema de base de datos está bajo control explícito.** Flyway asegura que el estado de la base de datos es reproducible y versionado. No hay estado implícito que dependa del entorno donde se arranca el proyecto.

**El comportamiento de error es predecible.** El `GlobalExceptionHandler` garantiza que cualquier error que el backend produzca llegue al cliente con la misma estructura JSON. El cliente no necesita tratar distintos formatos de error según el tipo de excepción o el módulo que la originó.

---

## 4.1.8 Relación con otras secciones de la memoria

Esta sección describe la arquitectura a nivel de componentes y responsabilidades. Las secciones siguientes desarrollan los elementos introducidos aquí:

- **§4.2** — Diseño detallado del esquema de base de datos y las decisiones sobre tablas y relaciones.
- **§4.5** — Decisiones de diseño que justifican las elecciones arquitectónicas: JDBC frente a JPA, token opaco frente a JWT, y otras.
- **§5** — Implementación módulo por módulo: autenticación, alimentos, comidas, resumen diario, perfil y ejercicios y MET.
- **§6** — Estrategia de pruebas: tests unitarios del backend y pruebas manuales de la API.
- **§7** — Seguridad: gestión de contraseñas y mecanismo de autenticación basado en token.
