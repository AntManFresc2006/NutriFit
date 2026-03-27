# NutriFit

NutriFit es una aplicación de seguimiento nutricional y deportivo inspirada en MyFitnessPal.

## Arquitectura
- `client/` → cliente de escritorio JavaFX
- `backend/` → API REST con Spring Boot
- `MariaDB` → base de datos relacional
- `Flyway` → migraciones de esquema

## Requisitos
- Java 17
- Maven
- MariaDB

## Estructura del repositorio
- `backend/` → servidor REST, lógica de negocio y acceso a datos
- `client/` → interfaz gráfica JavaFX
- `docs/` → documentación técnica, pruebas manuales, decisiones de diseño y capturas

## Base de datos
Crear una base de datos llamada:

```sql
CREATE DATABASE nutrifit;
```

## Docker

Levantar la base de datos y el backend con un solo comando (construye la imagen si no existe):

```bash
docker-compose up --build
```

Parar y eliminar contenedores, red y volumen de datos:

```bash
docker-compose down -v
```

> El cliente JavaFX **no se incluye en Docker**; se lanza localmente apuntando a `localhost:8080`.
> Asegúrate de que la aplicación de escritorio tenga configurada la URL base `http://localhost:8080`.