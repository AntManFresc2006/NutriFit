# 4.1 Arquitectura del sistema

NutriFit sigue una arquitectura de tres capas con separación física entre presentación, lógica de aplicación y persistencia. Los tres componentes son procesos independientes que se comunican a través de interfaces bien definidas: el frontend React actúa como single-page application servida desde Vercel, se comunica con el backend Spring Boot en Render mediante una API REST sobre HTTPS, y el backend accede a PostgreSQL mediante JDBC. Cada capa interactúa con la siguiente a través de contratos bien definidos y sin depender de sus detalles de implementación.

> **Figura 4.1** — Diagrama de arquitectura del sistema.
> *(ver `docs/diagrams/arquitectura.puml`)*

---

## 4.1.1 Visión general

El sistema se organiza en tres capas con responsabilidades diferenciadas:

| Capa | Tecnología | Responsabilidad principal | Despliegue |
|------|-----------|--------------------------|-----------|
| Presentación | React 18 + TypeScript | Interfaz web responsiva, navegación SPA, peticiones HTTP | Vercel (CDN global) |
| Lógica de aplicación | Spring Boot 3 (puerto 8080 interno, 443 HTTPS público) | API REST, reglas de negocio, acceso a datos, integración con IA | Render (Linux + HTTPS) |
| Persistencia | PostgreSQL (`nutrifit`) | Almacenamiento relacional del estado del sistema | Render PostgreSQL (externa) o cloud database |

El frontend y el backend son módulos completamente independientes dentro del mismo repositorio Git. Pueden arrancarse, construirse y probarse por separado. El backend no sabe nada del cliente; el frontend no sabe nada de PostgreSQL. Ambas dependencias son unidireccionales y cruzar la capa de comunicación definida es la única vía de interacción. El despliegue en producción es automatizado mediante CI/CD en ambas plataformas (Vercel y Render).

---

## 4.1.2 Capa de presentación: el frontend React

El frontend es una single-page application (SPA) implementada con React 18, TypeScript, Vite como bundler y Tailwind CSS para estilos. Su punto de entrada es la función `App.tsx`, que establece el router principal y carga la primera pantalla al abrir la aplicación:

```typescript
// App.tsx (estructura simplificada)
function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/perfil" element={<Perfil />} />
        {/* ... más rutas */}
      </Routes>
    </BrowserRouter>
  );
}
```

La interfaz está compuesta por dieciocho módulos funcionales completos, cada uno con sus propias pantallas, componentes, y servicios HTTP.

Dentro del frontend, los componentes se distribuyen así:

- **Componentes React y páginas.** Gestionan el ciclo de vida de la vista, responden a eventos del usuario mediante hooks (`useState`, `useEffect`) y delegan las operaciones de datos en los servicios HTTP. No contienen lógica de negocio ni SQL.

- **Servicios HTTP.** Una clase o módulo por cada funcionalidad principal (`authService`, `alimentosService`, `comidasService`, etc.) que construye las peticiones HTTP usando Fetch API o Axios, las envía al backend con el token en el header `Authorization`, y deserializa la respuesta JSON. La comunicación con el backend está completamente encapsulada en estos servicios; los componentes no construyen URLs ni manejan JSON directamente.

- **Context o state management.** Estado compartido entre componentes (sesión actual, usuario autenticado, token, datos de sesión). En la mayoría de implementaciones de React modernas se usa Context API o una librería ligera como Zustand o Redux.

- **Estilos e animaciones.** Tailwind CSS proporciona las utilidades de diseño responsivo. Framer Motion añade animaciones de transición suave entre vistas y feedback visual en interacciones.

La aplicación es responsiva: se adapta automáticamente a mobile (375px+), tablet (768px+) y desktop (1024px+). Los componentes usan grid y flexbox de Tailwind para layouts flexibles. Framer Motion proporciona transiciones fluidas en cambios de pantalla sin necesidad de cargar HTML nuevo del servidor.

---

## 4.1.3 Capa de lógica de aplicación: el backend Spring Boot

El backend es una aplicación Spring Boot 3 que arranca en Render con una variable de entorno `DATABASE_URL` para la conexión a PostgreSQL. En desarrollo local, se ejecuta con `mvn spring-boot:run` y queda escuchando en `http://localhost:8080`. En producción (Render), está disponible en `https://api.nutrifit.render.com` (ejemplo hipotético) con HTTPS automático.

El backend expone dieciocho grupos de endpoints REST organizados por módulo:

| Prefijo | Módulo |
|---------|--------|
| `/api/auth` | Registro, login, logout, validación de token |
| `/api/alimentos` | CRUD del catálogo de alimentos |
| `/api/comidas` | Registro e ítems de comidas |
| `/api/resumen-diario` | Agregación nutricional por día |
| `/api/perfil` | Consulta y actualización de perfil |
| `/api/ejercicios` | Catálogo de tipos de ejercicios |
| `/api/ejercicios-registro` | Registro de sesiones de ejercicio |
| `/api/peso-historial` | Historial de pesajes |
| `/api/hidratacion` | Registro de consumo de agua |
| `/api/plan-semanal` | Planes nutricionales generados por IA |
| `/api/retos` | Catálogo de retos y progreso del usuario |
| `/api/lista-compra` | Lista de compra del usuario |
| `/api/escaner` | Consulta a OpenFoodFacts por código de barras |
| `/api/resumen/evaluacion-ia` | Evaluación de hábitos nutricionales con IA |
| `/api/tendencias` | Gráficos de evolución de peso, calorías, ejercicio |
| `/api/ia-config` | Configuración personal de OpenRouter |
| `/api/usuario` | Información del usuario y preferencias |
| `/api/gamificacion` | Puntos y logros del usuario |

Internamente, todos los módulos siguen la misma estructura en capas verticales:

```
Controller → Service → Repository → JdbcTemplate → PostgreSQL
```

**Controladores.** Anotados con `@RestController`, reciben la petición HTTP, validan el cuerpo con `@Valid` cuando aplica, delegan la operación en el servicio correspondiente y devuelven la respuesta JSON serializada automáticamente por Spring. No contienen lógica de negocio. La mayoría de endpoints protegidos requieren autenticación validada por un `HandlerInterceptor`.

**Servicios.** Contienen las reglas de negocio: validaciones de dominio, transformaciones, cálculos (la fórmula de Mifflin-St Jeor para TMB y TDEE, el cálculo MET para ejercicios), coordinación entre repositorios cuando una operación afecta a más de una tabla, e integración con APIs externas (OpenRouter para IA, OpenFoodFacts para escaneo). Son la única capa que puede lanzar excepciones de dominio (`ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`).

**Repositorios.** Cada módulo declara una interfaz `XxxRepository` y una implementación `JdbcXxxRepository` que usa `JdbcTemplate` con SQL directo y un `RowMapper` propio. No hay ORM ni gestión automática de esquema desde el código Java. La decisión de usar JDBC en lugar de JPA se documenta en decisiones de diseño anteriores.

**`GlobalExceptionHandler`.** Anotado con `@RestControllerAdvice`, intercepta todas las excepciones no capturadas y las convierte en una respuesta JSON con estructura uniforme (`ApiError`). El cliente siempre recibe el mismo formato de error, con independencia del tipo de excepción que se haya producido:

```json
{
  "timestamp": "2026-05-17T10:30:45Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La altura mínima es 100 cm",
  "path": "/api/perfil"
}
```

**`HandlerInterceptor` de autenticación.** Intercepta todas las peticiones HTTP antes de llegar al controlador. Extrae el token del header `Authorization: Bearer <token>`, lo busca en la tabla `sesiones`, valida que no ha expirado, y si es válido, injacta el `usuarioId` en la petición para que el controlador y el servicio sepan quién está haciendo la operación. Si el token es inválido o está expirado, devuelve un 401 Unauthorized.

**Servicios de seguridad.** `PasswordService` encapsula el uso de `BCryptPasswordEncoder` para hash y verificación; `TokenService` genera tokens UUID v4 mediante `SecureRandom` y los persiste en `sesiones` con expiración a siete días. Ambos están inyectados únicamente en `AuthServiceImpl`.

**Integración con OpenRouter.** Cuando un usuario solicita un plan semanal, una evaluación de IA o sugerencias de compra, el backend invoca OpenRouter de forma segura:
1. Obtiene la configuración de IA del usuario desde `usuario_ia_config`
2. Verifica que tiene model, API key y URL configurados
3. Construye el prompt con los datos del usuario y la petición específica
4. Invoca OpenRouter de forma asincrónica (sin bloquear la respuesta HTTP)
5. Almacena el resultado en la base de datos para referencia futura

**Integración con OpenFoodFacts.** El módulo `escaner` consulta la API pública de OpenFoodFacts sin autenticación:
1. El frontend captura un código de barras EAN
2. Envía el EAN al backend
3. El backend consulta OpenFoodFacts con el EAN
4. Si encuentra el producto, devuelve información nutricional
5. Si no lo encuentra, devuelve error 404 con mensaje controlado

---

## 4.1.4 Capa de persistencia: PostgreSQL y Flyway

PostgreSQL almacena el estado del sistema en una base de datos llamada `nutrifit`. El esquema está compuesto por veintidós migraciones versionadas (V1 a V22) que cubren:

- **Núcleo (V1–V5):** Tablas `usuarios`, `alimentos`, `sesiones`, `comidas`, `comida_alimentos`
- **Ejercicios (V6–V7):** Tabla `ejercicios_tipo` (tipos disponibles) y tabla `ejercicios_registro` (sesiones del usuario)
- **Complementarios (V8–V22):** Procedimientos almacenados, historial de peso, hidratación, plan semanal, retos, lista de compra, configuración de IA, índices y constraints

El esquema no se define ni se modifica manualmente. Flyway aplica las migraciones automáticamente al arrancar el backend, leyendo los scripts del directorio `backend/src/main/resources/db/migration/`. Registra cada ejecución en la tabla interna `flyway_schema_history`. Si todas las migraciones ya están aplicadas, Flyway valida la consistencia y no ejecuta nada.

Esto garantiza que cualquier entorno que arranque desde cero queda con el mismo esquema exacto que el entorno de desarrollo. La compatibilidad entre Flyway y el acceso JDBC directo es completa: no hay conflicto de gestión de esquema porque el código Java nunca intenta crear ni modificar tablas.

En producción (Render), PostgreSQL puede estar alojado en un proveedor externo (ej: AWS RDS, Railway, Supabase) con acceso mediante URL de conexión almacenada en variables de entorno.

---

## 4.1.5 Comunicación entre capas: HTTPS y CORS

La comunicación se realiza exclusivamente mediante HTTPS sobre JSON. No hay ningún mecanismo de comunicación directo entre el frontend y PostgreSQL; el frontend no conoce las credenciales de la base de datos.

Cada petición HTTP sigue el mismo patrón desde el frontend:

1. El componente React invoca un método del servicio HTTP correspondiente (ej: `alimentosService.obtenerAlimentos()`)
2. El servicio construye la petición con Fetch API o Axios, serializa el cuerpo con JSON si aplica, e incluye el token en el header `Authorization: Bearer <token>` si la operación requiere autenticación.
3. El servicio envía la petición al backend (ej: `https://api.nutrifit.render.com/api/alimentos`) con `credentials: 'include'` si se requieren cookies.
4. El backend recibe la petición, el `HandlerInterceptor` valida el token, el controlador procesa la lógica, y devuelve una respuesta JSON.
5. Si la respuesta es 2xx, el servicio deserializa el cuerpo JSON en un objeto TypeScript y lo devuelve al componente.
6. Si es 4xx o 5xx, el servicio lanza una excepción. El componente la captura y muestra un mensaje de error al usuario.

**CORS (Cross-Origin Resource Sharing).** El backend está configurado con CORS para permitir peticiones desde:
- `https://nutrifit.vercel.app` (producción)
- `https://*.vercel.app` (previews de Vercel)
- `http://localhost:5173` (desarrollo local, puerto por defecto de Vite)

Las peticiones OPTIONS (preflight) son permitidas sin autenticación. Las peticiones POST, PUT, DELETE requieren el token válido.

**HTTPS y certificados.** Tanto Render como Vercel proporcionan HTTPS automático:
- El backend en Render obtiene certificados Let's Encrypt renovados automáticamente.
- El frontend en Vercel es servido desde un CDN HTTPS.
- La comunicación frontend-backend siempre usa HTTPS; no hay fallback a HTTP.

---

## 4.1.6 Swagger UI como apoyo al desarrollo

La dependencia `springdoc-openapi-starter-webmvc-ui` genera automáticamente la documentación interactiva de la API a partir de las anotaciones de los controladores y los DTOs. En desarrollo local (localhost:8080), la interfaz está disponible en:

```
http://localhost:8080/swagger-ui.html
```

En producción (Render), está disponible en:

```
https://api.nutrifit.render.com/swagger-ui.html
```

La metadata de la API se configura en `OpenApiConfig`:

```java
// OpenApiConfig.java
return new OpenAPI()
    .info(new Info()
        .title("NutriFit API")
        .description("API REST para gestión nutricional, ejercicios e IA personalizada")
        .version("1.0.0"));
```

Swagger UI cumple una función única durante el desarrollo: verificación rápida de endpoints sin necesidad de herramientas externas. Permite ejecutar peticiones directamente desde el navegador con los esquemas de entrada y salida documentados. Los tests manuales de integración se complementan con archivos `.http` en `docs/api/` que pueden ejecutarse desde IDEs como IntelliJ IDEA o VS Code.

---

## 4.1.7 Ventajas del desacoplamiento

La separación entre las tres capas tiene consecuencias prácticas sobre el desarrollo y la verificabilidad del proyecto.

**El backend es verificable de forma independiente.** Los tests unitarios del backend no requieren el frontend React ni la base de datos activa. Las pruebas manuales de la API con Swagger UI tampoco. El backend puede desarrollarse, probarse y depurarse sin arrancar ningún otro componente. En CI/CD, los tests se ejecutan en paralelo con los builds sin dependencias de entorno.

**El frontend es sustituible.** El backend no sabe que el cliente es React. Podría sustituirse por una interfaz web vanilla, una aplicación móvil nativa, una aplicación de escritorio Electron, o un cliente de línea de comandos sin modificar una sola línea del backend, siempre que se respete el contrato de la API REST y el protocolo de autenticación por token.

**El despliegue es independiente.** El frontend en Vercel y el backend en Render pueden actualizarse de forma completamente desacoplada. Una nueva versión del frontend puede existir mientras el backend no está actualizado, y viceversa, siempre que ambas versiones sean compatibles en el API.

**El esquema de base de datos está bajo control explícito.** Flyway asegura que el estado de la base de datos es reproducible y versionado. No hay estado implícito que dependa del entorno donde se arranca el proyecto. Las migraciones son reversibles y pueden auditarse en Git.

**El comportamiento de error es predecible.** El `GlobalExceptionHandler` garantiza que cualquier error que el backend produzca llegue al cliente con la misma estructura JSON. El cliente no necesita tratar distintos formatos de error según el tipo de excepción o el módulo que la originó.

**HTTPS en ambas capas.** La comunicación de extremo a extremo está cifrada. No hay tokens transmitidos en texto plano, no hay credenciales expuestas en las URLs, no hay intermediarios MitM posibles sin certificados válidos.

---

## 4.1.8 Relación con otras secciones de la memoria

Esta sección describe la arquitectura a nivel de componentes y responsabilidades. Las secciones siguientes desarrollan los elementos introducidos aquí:

- **§4.2** — Diseño detallado del esquema de base de datos (22 migraciones) y las decisiones sobre tablas y relaciones.
- **§5** — Implementación módulo por módulo: autenticación, alimentos, comidas, resumen diario, perfil, ejercicios, hidratación, IA, gamificación, y el resto de 18 módulos.
- **§6** — Estrategia de pruebas: tests unitarios del backend y pruebas manuales de la API.
- **§7** — Seguridad: gestión de contraseñas, mecanismo de autenticación por token opaco UUID, interceptor de validación, HTTPS en producción.
