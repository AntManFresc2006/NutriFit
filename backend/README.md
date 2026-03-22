# NutriFit

NutriFit es una aplicación orientada al seguimiento nutricional. El proyecto está desarrollado con una arquitectura cliente-servidor y se compone de un backend en Spring Boot, un cliente de escritorio en JavaFX y una base de datos MariaDB.

## Estructura del proyecto

El repositorio está organizado como un proyecto Maven multi-módulo:

- `backend/`: API REST, lógica de negocio, acceso a datos y migraciones Flyway
- `client/`: aplicación cliente en JavaFX
- `docs/`: documentación técnica, pruebas manuales, decisiones de diseño y capturas

## Tecnologías principales

- Java 17
- Spring Boot
- JavaFX
- Maven
- MariaDB
- Flyway
- JDBC
- Jackson
- GitHub Actions

## Funcionalidades implementadas

Actualmente el proyecto incluye:

- Registro e inicio de sesión de usuarios
- Gestión de alimentos con operaciones CRUD
- Búsqueda de alimentos por nombre
- Gestión inicial de comidas
- Asociación de alimentos a comidas con gramos consumidos
- Cálculo nutricional enriquecido por ítem de comida
- Resumen diario con kcal, proteínas, grasas y carbohidratos
- Cliente JavaFX conectado al backend
- Navegación entre pantallas principales
- Documentación técnica y pruebas manuales

## Requisitos

Para ejecutar el proyecto en local se necesita:

- Java 17 o compatible
- Maven
- MariaDB
- Una base de datos llamada `nutrifit`
- Archivo `application-local.properties` en el backend

## Ejecución rápida

### 1. Levantar la base de datos

Asegúrate de que MariaDB está ejecutándose y de que existe la base de datos `nutrifit`.

### 2. Configurar backend

Crear el archivo:

```
backend/src/main/resources/application-local.properties
```

con un contenido similar a este:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/nutrifit
spring.datasource.username=nutrifit
spring.datasource.password=nutrifit123
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

server.port=8080
```

### 3. Ejecutar backend

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

### 4. Ejecutar cliente JavaFX

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

## Base de datos y migraciones

El proyecto utiliza Flyway para versionar la estructura de base de datos. Las migraciones se encuentran en:

```
backend/src/main/resources/db/migration
```

Actualmente cubren:

- Tablas principales de usuarios y alimentos
- Sesiones de autenticación
- Seed de alimentos
- Módulo inicial de comidas

## Documentación adicional

Consultar la carpeta `docs/` para:

- Decisiones técnicas
- Pruebas manuales HTTP
- Notas de base de datos
- Capturas de funcionamiento
- Runbook# NutriFit

NutriFit es una aplicación orientada al seguimiento nutricional. El proyecto está desarrollado con una arquitectura cliente-servidor y se compone de un backend en Spring Boot, un cliente de escritorio en JavaFX y una base de datos MariaDB.

## Estructura del proyecto

El repositorio está organizado como un proyecto Maven multi-módulo:

- `backend/`: API REST, lógica de negocio, acceso a datos y migraciones Flyway
- `client/`: aplicación cliente en JavaFX
- `docs/`: documentación técnica, pruebas manuales, decisiones de diseño y capturas

## Tecnologías principales

- Java 17
- Spring Boot
- JavaFX
- Maven
- MariaDB
- Flyway
- JDBC
- Jackson
- GitHub Actions

## Funcionalidades implementadas

Actualmente el proyecto incluye:

- registro e inicio de sesión de usuarios
- gestión de alimentos con operaciones CRUD
- búsqueda de alimentos por nombre
- gestión inicial de comidas
- asociación de alimentos a comidas con gramos consumidos
- cálculo nutricional enriquecido por item de comida
- resumen diario con kcal, proteínas, grasas y carbohidratos
- cliente JavaFX conectado al backend
- navegación entre pantallas principales
- documentación técnica y pruebas manuales

## Requisitos

Para ejecutar el proyecto en local se necesita:

- Java 17 o compatible
- Maven
- MariaDB
- una base de datos llamada `nutrifit`
- archivo `application-local.properties` en el backend

## Ejecución rápida

### 1. Levantar la base de datos
Asegúrate de que MariaDB está ejecutándose y de que existe la base de datos `nutrifit`.

### 2. Configurar backend
Crear el archivo:

`backend/src/main/resources/application-local.properties`

con un contenido similar a este:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/nutrifit
spring.datasource.username=nutrifit
spring.datasource.password=nutrifit123
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

server.port=8080 de arranque y mantenimiento

## Integración continua

El repositorio incluye CI con GitHub Actions para compilar:

- `backend`
- `client`

## Estado actual

El proyecto se encuentra en una fase funcional avanzada del segundo trimestre. Ya existe una base sólida de backend, base de datos y cliente JavaFX, y actualmente se está trabajando en el refinado de la navegación, la documentación, las pruebas y futuras mejoras del flujo nutricional.

## Autor

Antonio Manuel Fresco Gómez