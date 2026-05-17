# 2. Tecnologías y herramientas

## 2.1 Visión de conjunto

NutriFit está compuesto por dos módulos independientes: un backend basado en Maven y un frontend basado en Node.js + npm. El backend expone una API REST que el frontend (SPA React) consume como cliente web. Esta separación condiciona directamente la elección tecnológica: cada módulo selecciona sus dependencias en función de su responsabilidad, sin compartir nada salvo el protocolo HTTPS y el formato JSON que los une. El sistema está desplegado en producción con el backend en Render y el frontend en Vercel.

La siguiente tabla recoge las tecnologías principales con su versión y el papel que desempeñan en el proyecto:

| Tecnología | Versión | Módulo | Papel en el proyecto |
|---|---|---|---|
| Java | 17 | Backend | Lenguaje base del backend; target de compilación |
| Spring Boot | 3.x | Backend | Marco de aplicación; gestiona el ciclo de vida, inyección de dependencias y servidor web embebido |
| Spring JDBC (`JdbcTemplate`) | gestionada por el BOM | Backend | Acceso a datos con SQL directo y `RowMapper` manuales, sin ORM |
| Spring Security Crypto | gestionada por el BOM | Backend | Hashing de contraseñas con BCrypt |
| PostgreSQL | 12+ | Base de datos | Motor relacional que almacena el estado del sistema; reemplaza MariaDB |
| `postgresql-driver` | gestionada por el BOM | Backend | Driver JDBC para PostgreSQL |
| Flyway | gestionada por el BOM | Backend | Migraciones de esquema versionadas (22 versiones: V1 a V22) |
| springdoc-openapi | 2.x | Backend | Generación de Swagger UI y especificación OpenAPI 3 |
| React | 18.x | Frontend | Framework para interfaz de usuario web (SPA) |
| TypeScript | 5.x | Frontend | Lenguaje con tipado estático compilado a JavaScript |
| Vite | 5.x | Frontend | Empaquetador y servidor de desarrollo de ultra-alto rendimiento |
| Tailwind CSS | 3.x | Frontend | Framework de CSS de utilidad para estilos responsivos |
| Framer Motion | 10.x | Frontend | Librería de animaciones declarativas para React |
| Axios o Fetch API | — | Frontend | Cliente HTTP para comunicación con el backend |
| OpenRouter API | — | Backend + Config | Integración de modelos de IA (configurable por usuario) |
| OpenFoodFacts API | — | Backend | Base de datos pública de alimentos para escaneo de códigos de barras |
| JUnit 5 | gestionada por el BOM | Backend | Marco de tests unitarios |
| Mockito | gestionada por el BOM | Backend | Sustitución de dependencias en tests unitarios |
| AssertJ | gestionada por el BOM | Backend | Aserciones fluidas en los tests |
| Maven | 3.8+ | Backend | Construcción, gestión de dependencias y empaquetado |
| npm | 8.x+ | Frontend | Gestor de paquetes de Node.js para el frontend |
| Git + GitHub | — | Control de versiones | Control de versiones y alojamiento del código fuente |
| Render | — | Despliegue | Hosting del backend Spring Boot (HTTPS, CI/CD) |
| Vercel | — | Despliegue | Hosting del frontend React SPA (HTTPS, optimizaciones automáticas) |

Las secciones siguientes describen cada grupo de tecnologías con más detalle, explicando qué función concreta cumplen en NutriFit y por qué encajan en el alcance del proyecto.

---

## 2.2 Backend

### Java 17

El proyecto compila con Java 17, la versión LTS vigente en el momento de inicio del desarrollo. Ambos módulos declaran `<maven.compiler.release>17</maven.compiler.release>`, lo que garantiza compatibilidad de bytecode y uso uniforme del lenguaje en los dos artefactos. Java 17 incorpora características relevantes que se usan en el proyecto: bloques de texto (`"""`) para las consultas SQL multilínea en los repositorios, registros de decisión y la API `java.time` para el manejo de fechas en el dominio.

### Spring Boot 3.2.3

Spring Boot es el marco que sustenta el backend. Sus starters concentran la configuración necesaria para levantar un servidor web embebido (Tomcat), habilitar la inyección de dependencias, validar los cuerpos de las peticiones con Bean Validation y gestionar las conexiones JDBC, todo sin XML. El backend usa tres starters directamente:

- `spring-boot-starter-web`: habilita el servidor REST con Jackson integrado para serialización JSON automática.
- `spring-boot-starter-validation`: activa la validación declarativa con `@Valid` y las anotaciones de Bean Validation (`@NotNull`, `@NotBlank`, `@DecimalMin`).
- `spring-boot-starter-jdbc`: proporciona `JdbcTemplate` y la infraestructura de gestión de conexiones mediante el pool de HikariCP incluido.

Spring Boot gestiona las versiones de todas sus dependencias transitivas a través de su BOM (*Bill of Materials*), lo que simplifica el mantenimiento del `pom.xml` y garantiza la compatibilidad entre componentes.

### Spring JDBC y `JdbcTemplate`

El acceso a datos se implementa íntegramente con `JdbcTemplate` y `RowMapper` manuales. No se usa JPA, Spring Data ni Hibernate. Cada módulo declara una interfaz `XxxRepository` y una implementación `JdbcXxxRepository` con SQL explícito. Esto hace que cada consulta sea visible y trazable en una línea concreta del código, sin intermediación de ningún generador de SQL. La justificación detallada de esta elección frente a JPA se desarrolla en §4.5.1.

### PostgreSQL y el driver JDBC

PostgreSQL actúa como motor de persistencia relacional, reemplazando a MariaDB del diseño original. El driver `postgresql-driver` (gestionado por el BOM de Spring Boot) gestiona la conectividad entre `JdbcTemplate` y la base de datos. La versión mínima requerida del servidor es PostgreSQL 12, que soporta todas las características del esquema: restricciones de clave foránea, tipos `NUMERIC` y `TIMESTAMP`, y borrado en cascada.

### Flyway

Flyway controla el ciclo de vida del esquema de la base de datos. Al arrancar el backend aplica automáticamente los scripts de migración almacenados en `backend/src/main/resources/db/migration/`, numerados de `V1` a `V22`. Si las migraciones ya están aplicadas, Flyway valida la consistencia del esquema y no ejecuta nada. Esto garantiza que cualquier entorno —local, CI, evaluación del tribunal— arranca con exactamente el mismo esquema sin intervención manual. Las dependencias `flyway-core` se gestionan a través del BOM de Spring Boot.

Las veintidós migraciones incluyen:
- V1–V5: Núcleo (usuarios, alimentos, sesiones, comidas, ítems de comidas)
- V6–V7: Ejercicios (tabla de tipos, datos semilla)
- V8–V9: Stored procedure para resumen diario
- V10–V13: Índices, constraints, historial de peso, hidratación
- V14–V15: Planes semanales, retos y gamificación
- V16–V19: Lista de compra, ejercicios adicionales
- V20–V22: Configuración de intensidad, ajustes de constraints y configuración de IA

### Spring Security Crypto

El módulo `spring-security-crypto` se incluye exclusivamente por `BCryptPasswordEncoder`, que gestiona el hash y la verificación de contraseñas. No se usa ningún otro componente de Spring Security. La autenticación en cambio se implementa manualmente mediante un `HandlerInterceptor` que valida el token UUID opaco en todas las peticiones protegidas.

---

## 2.3 Frontend: React 18 + TypeScript

### React 18 y TypeScript

La interfaz de usuario es una single-page application (SPA) implementada con React 18 y TypeScript 5.x. React proporciona los componentes reutilizables, la gestión de estado local mediante hooks, y el renderizado declarativo de la interfaz. TypeScript añade tipado estático completo, lo que previene errores en tiempo de compilación y mejora la experiencia de desarrollo.

El frontend implementa dieciocho módulos funcionales completos: autenticación, alimentos, comidas, resumen diario, perfil, ejercicios, hidratación, plan semanal, retos, evaluación con IA, escaneo de códigos de barras, lista de compra, configuración de IA, tendencias, peso e historial, tipos de ejercicios e información de usuario.

### Vite 5.x

Vite es el empaquetador (bundler) y servidor de desarrollo. Ofrece:
- Compilación ultrarrápida gracias a su uso de módulos ES nativos durante el desarrollo
- Servidor de desarrollo con HMR (Hot Module Replacement) para cambios instantáneos
- Optimización automática para producción con code splitting
- Soporte nativo para TypeScript, CSS y archivos estáticos

### Tailwind CSS 3.x

Tailwind CSS proporciona un framework de utilidades para estilos responsivos. Se usa con la configuración por defecto para diseño de componentes, spacing, colores y responsiveness en todos los tamaños de pantalla (mobile, tablet, desktop).

### Framer Motion 10.x

Framer Motion es la librería de animaciones. Se usa para transiciones suaves entre vistas, animaciones de entrada/salida de modales, y feedback visual en interacciones del usuario.

### Cliente HTTP

La comunicación con el backend se realiza usando Fetch API (nativa del navegador) o mediante una librería como Axios. Las peticiones incluyen el token de autenticación en la cabecera `Authorization: Bearer <token>`. El interceptor de errores maneja respuestas 4xx y 5xx, mostrando mensajes al usuario y redirigiendo a login en caso de token expirado.

### Despliegue en Vercel

El frontend está desplegado en Vercel, que proporciona:
- Hosting global con CDN
- HTTPS automático
- CI/CD integrado: cada push a main genera un build automático
- Optimizaciones automáticas de imágenes y assets
- Precompilación de páginas para mejor rendimiento

---

## 2.4 Integración con APIs externas

### OpenRouter API

OpenRouter es un proxy que unifica el acceso a múltiples modelos de lenguaje (OpenAI, Anthropic Claude, Google Gemini, Ollama, etc.). NutriFit lo usa para:
- **Plan semanal (IA)**: generación de planes nutricionales personalizados basados en los datos del usuario
- **Evaluación con IA**: análisis y feedback de hábitos nutricionales
- **Sugerencias en lista de compra**: recomendaciones automáticas de alimentos

La configuración es completamente flexible por usuario a través del módulo `usuario_ia_config`:
- URL del proxy (puede ser OpenRouter público o instancia privada)
- Modelo de IA (ej: `gpt-3.5-turbo`, `claude-3-sonnet`, etc.)
- API key personal del usuario para la llamada

El backend valida la presencia de configuración antes de invocar IA; si el usuario no ha configurado OpenRouter, las funciones que requieren IA devuelven un error controlado.

### OpenFoodFacts API

OpenFoodFacts es una base de datos pública y colaborativa de información nutricional de alimentos. NutriFit la integra en el módulo de escaneo (`escaner`) para:
- Consultar información nutricional por código de barras EAN
- Obtener automáticamente nombre, calorías, macronutrientes y foto del alimento
- Permitir al usuario registrar alimentos sin escribir manualmente sus valores nutricionales

La API de OpenFoodFacts es pública y no requiere autenticación ni API key.

---

## 2.5 Documentación y verificación de la API

### Swagger UI / springdoc-openapi 2.3.0

La dependencia `springdoc-openapi-starter-webmvc-ui` genera automáticamente la especificación OpenAPI 3 del backend a partir de las anotaciones de los controladores y los DTOs. Con el backend activo, la interfaz interactiva queda disponible en `http://localhost:8080/swagger-ui.html`. La metadata de la API —título, descripción y versión— se configura mediante un bean `OpenAPI` en `OpenApiConfig`:

```java
// OpenApiConfig.java
return new OpenAPI()
    .info(new Info()
        .title("NutriFit API")
        .description("API REST para gestión de alimentos, comidas y resumen nutricional diario")
        .version("0.1.0"));
```

Swagger UI cumple una función concreta durante el desarrollo: permite ejecutar peticiones reales contra el backend desde el navegador sin herramientas externas, con los esquemas de entrada y salida documentados. No se usa para generar código cliente ni como fuente primaria de documentación.

### Archivos `.http`

El directorio `docs/api/` contiene cinco archivos `.http` —uno por módulo: `auth.http`, `alimentos.http`, `comidas.http`, `resumen-diario.http` y `perfil.http`— con las peticiones representativas de cada endpoint. Estos archivos pueden ejecutarse directamente desde IDEs como IntelliJ IDEA con el plugin HTTP Client. Complementan a Swagger UI cubriendo casos de prueba específicos con datos concretos y cabeceras de autenticación predefinidas. Su uso en el proceso de verificación se describe en §6.

---

## 2.6 Calidad y pruebas

### JUnit 5, Mockito y AssertJ

Las tres bibliotecas de prueba se incluyen a través de `spring-boot-starter-test` con scope `test`. La exclusión explícita de `junit-vintage-engine` en el `pom.xml` del backend confirma que se usa exclusivamente el modelo de JUnit 5 Jupiter; no hay tests de JUnit 4 en el proyecto.

- **JUnit 5 Jupiter** proporciona el ciclo de vida de los tests (`@Test`, `@BeforeEach`) y las clases anidadas con `@Nested` que organizan los casos por método probado.
- **Mockito** se usa para sustituir los repositorios y servicios por dobles en los tests unitarios. Los controladores de pantalla del cliente no se prueban: todos los tests son del backend.
- **AssertJ** ofrece las aserciones con su API fluida (`assertThat(...).isEqualTo(...)`, `assertThatThrownBy(...)`), que resulta más legible que las aserciones estáticas de JUnit para los casos con excepciones y objetos compuestos.

Los tests no requieren base de datos activa ni contexto de Spring. Las cuatro clases de test —`AlimentoServiceImplTest`, `AuthServiceImplTest`, `ResumenDiarioServiceImplTest` y `PerfilServiceImplTest`— prueban los servicios en aislamiento usando mocks de sus repositorios. La batería completa y su cobertura se describe en §6.

---

## 2.7 Despliegue en producción

### Render (Backend)

El backend Spring Boot está desplegado en Render, una plataforma de hosting en la nube que proporciona:
- Servidor web Linux con Java 17 preinstalado
- Variables de entorno para credenciales de PostgreSQL
- HTTPS automático con certificados Let's Encrypt
- CI/CD automático: cada push a main triggerea un redeploy
- Logs en tiempo real y monitoreo básico

### Vercel (Frontend)

El frontend React está desplegado en Vercel, especializada en SPA estáticas:
- Hospedaje global en CDN
- HTTPS automático
- CI/CD integrado con GitHub
- Optimizaciones automáticas de assets
- Precompilación de Next.js (si aplica) o servicio de SPA estático

### Comunicación entre capas

El frontend en Vercel y el backend en Render se comunican mediante HTTPS. El frontend hace peticiones a la URL pública del backend API en Render. CORS está configurado en el backend para permitir peticiones desde el dominio del frontend en Vercel (y localhost:5173 en desarrollo).

---

## 2.8 Construcción y control de versiones

### Maven

El proyecto usa Maven con una estructura multi-módulo: el artefacto raíz `nutrifit-parent` declara los módulos `backend` y `client`. Cada módulo se construye de forma independiente; el artefacto padre no contiene código propio. Esta organización permite compilar, testear y empaquetar el backend y el cliente por separado con `mvn package -pl backend` o `mvn package -pl client`, o construir ambos con `mvn package` desde la raíz.

### Git y GitHub

El código fuente está bajo control de versiones con Git y alojado en GitHub. El repositorio contiene tanto el código como la documentación —incluyendo los archivos de memoria en `docs/memoria/` y los scripts de migración en `backend/src/main/resources/db/migration/`—. El historial de commits refleja el avance incremental del proyecto módulo a módulo.
