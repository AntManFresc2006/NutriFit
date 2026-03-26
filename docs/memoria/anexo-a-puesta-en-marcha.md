# Anexo A — Guía de puesta en marcha

Este anexo describe los pasos necesarios para arrancar NutriFit en un entorno local desde cero. El objetivo es que cualquier persona con acceso al repositorio pueda reproducir el entorno de desarrollo sin información adicional fuera de este documento.

---

## A.1 Requisitos previos

Antes de ejecutar el proyecto deben estar instalados los siguientes componentes:

| Herramienta | Versión mínima | Uso |
|-------------|---------------|-----|
| Java (JDK)  | 17            | Compilación y ejecución de backend y cliente |
| Maven       | 3.8           | Gestión de dependencias y arranque de módulos |
| MariaDB     | 10.6          | Base de datos relacional del backend |
| Git         | cualquiera    | Clonación del repositorio |

Maven descarga automáticamente todas las dependencias declaradas en los ficheros `pom.xml` en el primer arranque. No es necesario instalar Spring Boot, JavaFX ni ninguna otra librería de forma manual.

Para verificar que Java y Maven están disponibles antes de continuar:

```bash
java -version
mvn -version
```

---

## A.2 Estructura del repositorio

El repositorio es un proyecto Maven multi-módulo con el siguiente árbol de primer nivel:

```
NutriFit/
├── pom.xml          ← POM padre (nutrifit-parent)
├── backend/         ← módulo Spring Boot
├── client/          ← módulo JavaFX
└── docs/            ← documentación, ADRs, archivos HTTP, diagramas
```

El POM padre declara `backend` y `client` como módulos hijos. Cada uno tiene su propio `pom.xml` y puede compilarse de forma independiente. El backend expone la API REST; el cliente JavaFX la consume.

---

## A.3 Preparación de la base de datos

### A.3.1 Crear base de datos y usuario

El backend espera una base de datos llamada `nutrifit` y un usuario con permisos sobre ella. Ejecutar en MariaDB como administrador:

```sql
CREATE DATABASE nutrifit;
CREATE USER 'nutrifit'@'localhost' IDENTIFIED BY 'nutrifit123';
GRANT ALL PRIVILEGES ON nutrifit.* TO 'nutrifit'@'localhost';
FLUSH PRIVILEGES;
```

### A.3.2 Migraciones automáticas con Flyway

No es necesario crear las tablas manualmente. El backend incluye cinco scripts de migración en `backend/src/main/resources/db/migration/` que Flyway aplica de forma automática al arrancar:

| Migración | Contenido |
|-----------|-----------|
| `V1__usuarios_alimentos.sql` | Tablas `usuarios` y `alimentos` |
| `V2__create_core_tables.sql` | Tablas del núcleo de datos |
| `V3__auth_sessions.sql`      | Tabla `sesiones` para autenticación |
| `V4__seed_alimentos.sql`     | Datos iniciales de alimentos |
| `V5__comidas.sql`            | Tablas de comidas e ítems |

Flyway registra cada migración aplicada en la tabla `flyway_schema_history`. Si el proyecto ya arrancó antes, Flyway valida que los scripts no hayan cambiado y no los vuelve a aplicar.

---

## A.4 Configuración del backend

El archivo `backend/src/main/resources/application-local.properties` contiene la configuración de conexión para el entorno local y ya está presente en el repositorio con los valores por defecto:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/nutrifit
spring.datasource.username=nutrifit
spring.datasource.password=nutrifit123
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

server.port=8080
```

Si se usaron credenciales distintas en el paso A.3.1, hay que actualizar `username` y `password` en este archivo antes de arrancar.

> **Nota:** El archivo `application.properties` principal contiene la configuración compartida del proyecto. El perfil `local` concentra la configuración específica del entorno de desarrollo, incluida la conexión a base de datos.

---

## A.5 Arranque del backend

Desde el directorio `backend/`, ejecutar:

**Linux / macOS**
```bash
cd ~/ruta/al/proyecto/NutriFit/backend
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Windows**
```bash
cd C:\ruta\al\proyecto\NutriFit\backend
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

El flag `-DskipTests` evita ejecutar los 29 tests unitarios en cada arranque, que aunque pasan correctamente, ralentizan el proceso de puesta en marcha.

### Señales de arranque correcto

El backend está listo cuando el log muestra las siguientes líneas (en este orden):

1. Flyway valida o aplica las migraciones pendientes:
   ```
   Successfully applied N migrations to schema 'nutrifit'
   ```
   o, si ya estaban aplicadas:
   ```
   Schema 'nutrifit' is up to date. No migrations necessary.
   ```

2. Tomcat queda escuchando:
   ```
   Tomcat started on port 8080 (http)
   ```

3. Spring Boot confirma el arranque:
   ```
   Started BackendApplication in X.XXX seconds
   ```

---

## A.6 Verificación del backend

Con el servidor activo, hay dos formas de verificar que la API responde correctamente.

### A.6.1 Swagger UI

Abrir en el navegador:

```
http://localhost:8080/swagger-ui.html
```

Swagger UI muestra todos los endpoints agrupados por módulo (auth, alimentos, comidas, resumen-diario, perfil) y permite ejecutar peticiones directamente desde el navegador sin herramienta adicional.

### A.6.2 Archivos HTTP

El directorio `docs/api/` contiene cinco archivos `.http` compatibles con IntelliJ IDEA y con la extensión REST Client de VS Code:

| Archivo               | Módulo                          |
|-----------------------|---------------------------------|
| `auth.http`           | Registro, login, logout         |
| `alimentos.http`      | CRUD completo de alimentos      |
| `comidas.http`        | Comidas e ítems por fecha       |
| `resumen-diario.http` | Resumen calórico por día        |
| `perfil.http`         | Consulta y actualización de perfil |

Para verificar el arranque basta con ejecutar el endpoint de login de `auth.http` con credenciales de un usuario registrado y comprobar que la respuesta es HTTP 200 con un token en el cuerpo.

---

## A.7 Arranque del cliente JavaFX

El cliente debe arrancarse después del backend, ya que realiza peticiones HTTP al iniciar sesión.

Desde el directorio `client/`, ejecutar:

**Linux / macOS**
```bash
cd ~/ruta/al/proyecto/NutriFit/client
mvn javafx:run
```

**Windows**
```bash
cd C:\ruta\al\proyecto\NutriFit\client
mvn javafx:run
```

### Señales de arranque correcto

- Se abre la ventana de NutriFit con la pantalla de acceso (login o registro).
- Tras iniciar sesión, la interfaz muestra los módulos disponibles: alimentos, comidas del día, resumen diario y perfil de usuario.

---

## A.8 Orden de arranque y apagado

### Arranque

El orden correcto es:

1. Arrancar MariaDB (si no está ya activo como servicio del sistema).
2. Arrancar el backend desde `backend/`.
3. Arrancar el cliente desde `client/`.

El cliente no puede completar el login si el backend no está activo, pero la ventana sí se abre y muestra la pantalla de acceso.

### Apagado

- **Cliente:** cerrar la ventana o usar el botón de logout seguido del cierre. Al cerrar la ventana, `SessionManager.clear()` no se invoca, pero el token en memoria desaparece al terminar el proceso.
- **Backend:** interrumpir el proceso Maven con `Ctrl+C`. Spring Boot ejecuta el shutdown graceful de Tomcat.
- **MariaDB:** si se arrancó manualmente, detenerlo con el gestor de servicios del sistema.

---

## A.9 Problemas frecuentes

### `Access denied for user` al arrancar el backend

**Causa:** las credenciales en `application-local.properties` no coinciden con el usuario creado en MariaDB, o el usuario no tiene permisos sobre la base de datos `nutrifit`.

**Solución:** verificar usuario, contraseña y privilegios con los comandos del paso A.3.1. Comprobar que el archivo `application-local.properties` existe y tiene los valores correctos.

---

### Error de parsing en `server.port`

**Causa:** comentario escrito en la misma línea que la propiedad.

```properties
# Incorrecto
server.port=8080# comentario en la misma línea
```

```properties
# Correcto
# comentario en línea propia
server.port=8080
```

---

### Flyway advierte sobre versión de MariaDB no testeada

**Causa:** la versión de MariaDB instalada es más reciente que la oficialmente certificada por Flyway.

**Impacto:** advertencia en el log, no un error. Las migraciones se aplican correctamente. No requiere acción.

---

### Puerto 8080 ya está en uso

**Causa:** hay otra instancia del backend en ejecución, u otra aplicación ocupa ese puerto.

**Solución:** terminar el proceso anterior o cambiar `server.port` en `application-local.properties` a otro puerto libre (por ejemplo, `8081`) y asegurarse de que el cliente apunta al mismo puerto.

---

### El cliente JavaFX no arranca en Linux

**Causa habitual:** versión de Java activa en el sistema distinta de la usada para compilar, o dependencias gráficas del sistema incompletas.

**Solución:** verificar con `java -version` que la versión activa de Java es compatible con el proyecto, y ejecutar `mvn javafx:run` desde el directorio `client/`, revisando el mensaje exacto del error en la salida de Maven.
