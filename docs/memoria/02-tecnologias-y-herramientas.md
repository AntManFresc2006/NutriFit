# 2. Tecnologías y herramientas

## 2.1 Visión de conjunto

NutriFit está compuesto por dos módulos Maven independientes —`backend` y `client`— bajo un proyecto padre común. El backend expone una API REST y el cliente la consume como aplicación de escritorio. Esta separación condiciona directamente la elección tecnológica: cada módulo selecciona sus dependencias en función de su responsabilidad, sin compartir nada salvo el protocolo HTTP y el formato JSON que los une.

La siguiente tabla recoge las tecnologías principales con su versión y el papel que desempeñan en el proyecto:

| Tecnología | Versión | Módulo | Papel en el proyecto |
|---|---|---|---|
| Java | 17 | Backend + cliente | Lenguaje base; target de compilación en ambos módulos |
| Spring Boot | 3.2.3 | Backend | Marco de aplicación; gestiona el ciclo de vida, la inyección de dependencias y el servidor web embebido |
| Spring JDBC (`JdbcTemplate`) | gestionada por el BOM | Backend | Acceso a datos con SQL directo y `RowMapper` manuales |
| Spring Security Crypto | gestionada por el BOM | Backend | Codificación de contraseñas con BCrypt |
| MariaDB | 10.6+ | Base de datos | Motor relacional que almacena el estado del sistema |
| `mariadb-java-client` | 3.1.4 | Backend | Driver JDBC para MariaDB |
| Flyway | gestionada por el BOM | Backend | Migraciones de esquema versionadas y reproducibles |
| springdoc-openapi | 2.3.0 | Backend | Generación de Swagger UI y especificación OpenAPI 3 |
| JavaFX | 21.0.2 | Cliente | Marco de interfaz gráfica de escritorio |
| `java.net.http.HttpClient` | Java 17 (nativo) | Cliente | Comunicación HTTP con el backend |
| Jackson Databind | 2.17.0 | Cliente | Serialización y deserialización JSON |
| JUnit 5 | gestionada por el BOM | Backend | Marco de tests unitarios |
| Mockito | gestionada por el BOM | Backend | Sustitución de dependencias en tests unitarios |
| AssertJ | gestionada por el BOM | Backend | Aserciones fluidas en los tests |
| Maven | 3.8+ | Backend + cliente | Construcción, gestión de dependencias y empaquetado |
| Git + GitHub | — | Repositorio | Control de versiones y alojamiento del código fuente |

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

### MariaDB y el driver JDBC

MariaDB actúa como motor de persistencia relacional. El driver `mariadb-java-client` 3.1.4 gestiona la conectividad entre `JdbcTemplate` y la base de datos. La versión mínima requerida del servidor es 10.6, que soporta las restricciones de clave foránea y el tipo `DECIMAL` utilizados en el esquema.

### Flyway

Flyway controla el ciclo de vida del esquema de la base de datos. Al arrancar el backend aplica automáticamente los scripts de migración almacenados en `backend/src/main/resources/db/migration/`, numerados de `V1` a `V5`. Si las migraciones ya están aplicadas, Flyway valida la consistencia del esquema y no ejecuta nada. Esto garantiza que cualquier entorno —local, CI, evaluación del tribunal— arranca con exactamente el mismo esquema sin intervención manual. Las dependencias `flyway-core` y `flyway-mysql` se gestionan a través del BOM de Spring Boot.

### Spring Security Crypto

El módulo `spring-security-crypto` se incluye exclusivamente por `BCryptPasswordEncoder`, que gestiona el hash y la verificación de contraseñas. No se usa ningún otro componente de Spring Security: no hay filtros de seguridad HTTP, no hay contexto de seguridad ni interceptores. Esta elección evita la complejidad de configurar el marco de seguridad completo para un caso de uso que solo necesita una única clase de utilidad.

---

## 2.3 Cliente JavaFX

### JavaFX 21.0.2

La interfaz de usuario es una aplicación de escritorio implementada con JavaFX 21.0.2. Se usan los módulos `javafx-controls` —para controles de UI como `TableView`, `DatePicker`, `Label` y `TextField`— y `javafx-fxml` —para definir las pantallas en archivos `.fxml` y vincularlas con los controladores Java mediante `FXMLLoader`—. El cliente tiene cuatro pantallas: acceso, gestión de alimentos, diario nutricional y perfil de usuario.

JavaFX 21 es la LTS activa del proyecto OpenJFX en el momento de desarrollo y forma la primera versión con soporte a largo plazo publicada por separado del JDK. Su uso como módulo Maven externo (a través de `javafx-maven-plugin 0.0.8`) permite compilar y ejecutar el cliente con un JDK estándar sin JFX incluido.

### `java.net.http.HttpClient`

La comunicación HTTP entre el cliente y el backend se realiza con el cliente HTTP nativo de Java, disponible desde Java 11 a través del paquete `java.net.http`. No se añade ninguna librería de red externa (OkHttp, Retrofit, Apache HttpClient). Cada módulo del cliente encapsula sus llamadas en una clase de servicio dedicada (`AlimentoApiClient`, `AuthApiClient`, `PerfilApiClient`, `ResumenDiarioApiClient`) que construye las peticiones, verifica el código de estado HTTP y propaga los errores como `IOException`. La justificación de esta decisión se desarrolla en §4.5.6.

### Jackson Databind 2.17.0

Jackson es la única dependencia externa del cliente para la comunicación con la API. Se usa `ObjectMapper` para serializar los objetos de petición a JSON y deserializar las respuestas del backend en DTOs (`AlimentoDto`, `PerfilDto`, `ResumenDiarioDto`, etc.). Jackson es la misma librería que usa Spring Boot internamente en el backend, lo que garantiza la compatibilidad de representación JSON entre ambos extremos.

---

## 2.4 Documentación y verificación de la API

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

## 2.5 Calidad y pruebas

### JUnit 5, Mockito y AssertJ

Las tres bibliotecas de prueba se incluyen a través de `spring-boot-starter-test` con scope `test`. La exclusión explícita de `junit-vintage-engine` en el `pom.xml` del backend confirma que se usa exclusivamente el modelo de JUnit 5 Jupiter; no hay tests de JUnit 4 en el proyecto.

- **JUnit 5 Jupiter** proporciona el ciclo de vida de los tests (`@Test`, `@BeforeEach`) y las clases anidadas con `@Nested` que organizan los casos por método probado.
- **Mockito** se usa para sustituir los repositorios y servicios por dobles en los tests unitarios. Los controladores de pantalla del cliente no se prueban: todos los tests son del backend.
- **AssertJ** ofrece las aserciones con su API fluida (`assertThat(...).isEqualTo(...)`, `assertThatThrownBy(...)`), que resulta más legible que las aserciones estáticas de JUnit para los casos con excepciones y objetos compuestos.

Los tests no requieren base de datos activa ni contexto de Spring. Las cuatro clases de test —`AlimentoServiceImplTest`, `AuthServiceImplTest`, `ResumenDiarioServiceImplTest` y `PerfilServiceImplTest`— prueban los servicios en aislamiento usando mocks de sus repositorios. La batería completa y su cobertura se describe en §6.

---

## 2.6 Construcción y control de versiones

### Maven

El proyecto usa Maven con una estructura multi-módulo: el artefacto raíz `nutrifit-parent` declara los módulos `backend` y `client`. Cada módulo se construye de forma independiente; el artefacto padre no contiene código propio. Esta organización permite compilar, testear y empaquetar el backend y el cliente por separado con `mvn package -pl backend` o `mvn package -pl client`, o construir ambos con `mvn package` desde la raíz.

### Git y GitHub

El código fuente está bajo control de versiones con Git y alojado en GitHub. El repositorio contiene tanto el código como la documentación —incluyendo los archivos de memoria en `docs/memoria/` y los scripts de migración en `backend/src/main/resources/db/migration/`—. El historial de commits refleja el avance incremental del proyecto módulo a módulo.
