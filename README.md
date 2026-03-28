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

## Capturas de pantalla

### Autenticación

![Pantalla de acceso: inicio de sesión y registro de usuario](docs/screenshots/Captura%20de%20pantalla_20260327_171704.png)

Pantalla de acceso con formulario de inicio de sesión (izquierda) y registro de nueva cuenta (derecha).

---

### Gestión de alimentos

![Vista inicial vacía del módulo de gestión de alimentos](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20024721.png)

Vista inicial del módulo: tabla vacía y formulario en blanco listos para operar.

![Lista de alimentos cargada desde la API](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20025435.png)

Lista de alimentos cargada correctamente desde el backend.

![Formulario preparado para crear un nuevo alimento](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20025731.png)

Formulario relleno con los datos de un nuevo alimento antes de guardarlo.

![Nuevo alimento creado correctamente](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20031733.png)

El alimento "espaguetis a la carbonara" aparece en la tabla tras crearse correctamente.

![Alimento seleccionado en tabla con datos en formulario](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20145557.png)

Al seleccionar una fila, el formulario se rellena automáticamente con los datos del alimento.

![Alimento eliminado correctamente](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20145612.png)

Confirmación en barra de estado tras eliminar un alimento; la tabla se actualiza.

![Búsqueda filtrada por nombre](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20145629.png)

Filtrado de alimentos por texto: búsqueda de "arroz" devuelve solo los resultados coincidentes.

![Búsqueda sin resultados](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20145711.png)

Búsqueda sin resultados: la tabla queda vacía cuando ningún alimento coincide con el término.

![Alimento seleccionado para actualizar](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20150828.png)

Alimento seleccionado ("sopa de mariscos") listo para editar su porción u otros campos.

![Porción actualizada y guardada](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20150910.png)

Porción modificada y guardada; la tabla refleja el valor actualizado.

![Validación: nombre del alimento obligatorio](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20153526.png)

Mensaje de validación cuando se intenta guardar un alimento sin nombre.

![Validación: porción debe ser mayor que cero](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20153546.png)

Mensaje de validación cuando la porción introducida es cero o negativa.

![Validación: kcal deben ser un número válido](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20153606.png)

Mensaje de validación cuando el campo kcal contiene texto no numérico.

![Validación: proteínas no pueden ser negativas](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20153726.png)

Mensaje de validación cuando se introduce un valor negativo en el campo de proteínas.

![Diálogo de confirmación antes de eliminar](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20163104.png)

Diálogo de confirmación que aparece antes de borrar definitivamente un alimento.

![Eliminación cancelada por el usuario](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20163126.png)

Lista sin cambios tras cancelar la eliminación desde el diálogo de confirmación.

![Búsqueda por nombre con resultado filtrado](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20163221.png)

Búsqueda de "pollo" con el resultado filtrado y el alimento cargado en el formulario.

![Lista de alimentos con muchos registros](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20210514.png)

Lista de alimentos con múltiples registros cargados desde la base de datos.

![Gestión de alimentos con navegación completa](docs/screenshots/Captura%20de%20pantalla_20260327_225804.png)

Versión final del módulo con barra de navegación (Diario, Comidas, Ejercicios, Perfil).

![Lista de alimentos ampliada con más registros](docs/screenshots/Captura%20de%20pantalla_20260327_152508.png)

Lista de alimentos con la base de datos ya poblada con más productos.

---

### Perfil de usuario

![Pantalla de perfil con TMB y TDEE calculados](docs/screenshots/Captura%20de%20pantalla_20260326_231119.png)

Pantalla de perfil con datos personales y valores de TMB y TDEE calculados automáticamente.

---

### Módulo de comidas

![Módulo de comidas: vista inicial vacía](docs/screenshots/Captura%20de%20pantalla_20260327_145808.png)

Vista inicial del módulo de comidas: sin comidas registradas para el día seleccionado.

![Primera comida del día creada](docs/screenshots/Captura%20de%20pantalla_20260327_150152.png)

Primera comida creada (DESAYUNO) y visible en la lista de comidas del día.

![Tres comidas registradas en el día](docs/screenshots/Captura%20de%20pantalla_20260327_150616.png)

Tres comidas registradas (DESAYUNO, ALMUERZO, MERIENDA) con sus tipos y macros.

![Alimentos de la comida seleccionada en tabla de macros](docs/screenshots/Captura%20de%20pantalla_20260327_150648.png)

Tabla de macros (kcal, gramos, proteínas, grasas, carbos) de los alimentos de una comida.

![Añadir alimento a una comida mediante buscador](docs/screenshots/Captura%20de%20pantalla_20260327_150637.png)

Buscador para añadir alimentos a la comida seleccionada, con campo de cantidad.

![Comida eliminada con diálogo de confirmación](docs/screenshots/Captura%20de%20pantalla_20260327_152338.png)

Diálogo de confirmación antes de eliminar una comida y todos sus alimentos asociados.

![Comidas del día con alimentos cargados](docs/screenshots/Captura%20de%20pantalla_20260327_171810.png)

Módulo de comidas con las comidas del día cargadas y una comida seleccionada.

![Alimentos de comida cargados en tabla](docs/screenshots/Captura%20de%20pantalla_20260327_171823.png)

Detalle de los alimentos de una comida con macros individuales por alimento.

---

### Resumen diario

![Resumen diario nutricional (versión inicial)](docs/screenshots/Captura%20de%20pantalla%202026-03-14%20204055.png)

Primera versión del resumen diario: kcal, proteínas, grasas y carbohidratos totales del día.

![Resumen diario con TDEE integrado](docs/screenshots/Captura%20de%20pantalla_20260326_231242.png)

Versión mejorada con TDEE del perfil: muestra las kcal consumidas frente al objetivo diario.

![Resumen diario con datos reales del día](docs/screenshots/Captura%20de%20pantalla_20260327_152319.png)

Resumen del día con datos reales tras registrar alimentos en el módulo de comidas.

![Resumen diario con unidades etiquetadas](docs/screenshots/Captura%20de%20pantalla_20260327_171743.png)

Versión final del resumen diario con unidades (kcal / g) etiquetadas junto a cada valor.

---

### Módulo de ejercicios

![Módulo de ejercicios: sin ejercicios registrados](docs/screenshots/Captura%20de%20pantalla_20260327_225850.png)

Vista del módulo de ejercicios con la lista vacía y el formulario de registro a la derecha.

![Módulo de ejercicios: ejercicio registrado](docs/screenshots/Captura%20de%20pantalla_20260327_225830.png)

Ejercicio registrado en la lista del día con el MET y las kcal quemadas calculadas.

![Módulo de ejercicios: confirmación de eliminación](docs/screenshots/Captura%20de%20pantalla_20260327_225839.png)

Diálogo de confirmación antes de eliminar un registro de ejercicio del día.

---

### Diagramas técnicos

![Diagrama de clases del módulo perfil](docs/screenshots/Captura%20de%20pantalla_20260327_000314.png)

Diagrama de clases del módulo de perfil: controlador, servicio, repositorio y DTOs.

![Diagrama de arquitectura cliente-backend](docs/screenshots/Captura%20de%20pantalla_20260327_000403.png)

Arquitectura general: cliente JavaFX, API Spring Boot, base de datos MariaDB y Flyway.

![Diagrama entidad-relación de la base de datos](docs/screenshots/Captura%20de%20pantalla_20260327_000426.png)

Modelo entidad-relación con todas las tablas y sus relaciones.

![Diagrama de secuencia del flujo de autenticación](docs/screenshots/Captura%20de%20pantalla_20260327_000452.png)

Secuencia de registro e inicio de sesión con generación y validación de token JWT.

![Diagrama de secuencia del ciclo de vida del JWT](docs/screenshots/Captura%20de%20pantalla_20260327_000505.png)

Ciclo de vida del JWT: emisión, validación en cada petición y caducidad.

---

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