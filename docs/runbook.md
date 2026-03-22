# Runbook de NutriFit

## Objetivo

Este documento recoge los pasos básicos para poner en marcha el proyecto NutriFit en local y resolver los problemas más comunes de arranque.

---

## 1. Requisitos previos

Antes de ejecutar el proyecto se recomienda tener instalado:

- Java 17 o una versión compatible
- Maven
- MariaDB
- DBeaver o cliente SQL (opcional)
- Git

---

## 2. Estructura del proyecto

El repositorio está dividido en:

- `backend/`
- `client/`
- `docs/`

El backend expone la API REST y el cliente JavaFX consume dicha API.

---

## 3. Preparación de base de datos

### 3.1 Crear base de datos

La aplicación espera una base de datos llamada `nutrifit`:

```sql
CREATE DATABASE nutrifit;
```

### 3.2 Crear usuario de aplicación

Se recomienda crear un usuario específico para la app:

```sql
CREATE USER 'nutrifit'@'localhost' IDENTIFIED BY 'nutrifit123';
GRANT ALL PRIVILEGES ON nutrifit.* TO 'nutrifit'@'localhost';
FLUSH PRIVILEGES;
```

### 3.3 Importar dump (opcional)

Si se dispone de un dump de Windows o de otro entorno, se puede importar así:

**Linux**
```bash
mariadb -u nutrifit -p nutrifit < ~/Escritorio/nutrifit_backup.sql
```

**Windows**
```bash
mysql -u nutrifit -p nutrifit < C:\ruta\al\archivo\nutrifit_backup.sql
```

---

## 4. Configuración local del backend

Crear el archivo:

```
backend/src/main/resources/application-local.properties
```

con este contenido:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/nutrifit
spring.datasource.username=nutrifit
spring.datasource.password=nutrifit123
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

server.port=8080
```

> **Nota:** Este archivo es local y no debería subirse al repositorio con credenciales personales.

---

## 5. Arranque del backend

**Windows**
```bash
cd C:\Users\anton\Desktop\NutriFit\backend
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Linux**
```bash
cd ~/Escritorio/NutriFit/backend
mvn -DskipTests spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Qué debería ocurrir

- Spring Boot arranca correctamente
- Flyway valida o aplica migraciones
- Tomcat queda escuchando en `http://localhost:8080`

---

## 6. Arranque del cliente JavaFX

**Windows**
```bash
cd C:\Users\anton\Desktop\NutriFit\client
mvn javafx:run
```

**Linux**
```bash
cd ~/Escritorio/NutriFit/client
mvn javafx:run
```

### Qué debería ocurrir

- Se abre la ventana de NutriFit
- Aparece la pantalla de acceso
- Tras iniciar sesión se puede navegar por alimentos y resumen diario

---

## 7. Flujo recomendado de arranque

1. Arrancar MariaDB
2. Arrancar backend
3. Arrancar cliente JavaFX

---

## 8. Problemas frecuentes

### 8.1 `Access denied for user 'root'@'localhost'`

**Causa:** el backend intenta usar `root` en MariaDB con autenticación no compatible.

**Solución:**
- Usar un usuario específico como `nutrifit`
- Revisar `application-local.properties`

---

### 8.2 Error en `server.port`

**Causa:** comentario mal escrito en la misma línea de la propiedad.

❌ Incorrecto:
```properties
server.port=8080# comentario
```

✅ Correcto:
```properties
# comentario
server.port=8080
```

---

### 8.3 Flyway detecta una versión de MariaDB más nueva

**Causa:** MariaDB instalada es más reciente que la versión oficialmente testeada por Flyway.

**Impacto:** normalmente el proyecto sigue funcionando. De momento se considera una advertencia, no un bloqueo.

---

### 8.4 JavaFX no arranca en Linux

**Posibles causas:**
- Versión de Java distinta a la esperada
- Dependencias gráficas del sistema
- Configuración del entorno JavaFX

**Solución recomendada:**
- Comprobar la versión de Java
- Volver a lanzar Maven desde `client/`
- Revisar los mensajes exactos del error

---

### 8.5 Puerto 8080 ocupado

**Causa:** ya existe otra instancia del backend o de otra aplicación usando ese puerto.

**Solución:**
- Cerrar la instancia anterior
- O cambiar el puerto local de ejecución

---

## 9. Verificación rápida del entorno

| Componente | Comprobación |
|---|---|
| Backend | Responde en `http://localhost:8080` |
| Backend | Migraciones validadas |
| Backend | Conexión a BD correcta |
| Cliente | Login operativo |
| Cliente | Módulo de alimentos operativo |
| Cliente | Resumen diario operativo |

---

## 10. Archivos útiles del proyecto

```
docs/api/alimentos.http
docs/api/auth.http
docs/api/comidas.http
docs/api/resumen-diario.http
docs/db/
docs/decisions/
```

---

## 11. Mantenimiento recomendado

- No versionar `application-local.properties` con credenciales reales
- No incluir `.git/` ni carpetas `target/` en zips de entrega
- Mantener actualizado el README
- Documentar cada bloque importante antes de abrir otro frente nuevo
