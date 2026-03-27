# 5.6 Ejercicios y MET

## 5.6.1 Problema que resuelve el módulo

El resumen diario (§5.4) y el perfil (§5.5) cubren la dimensión de ingesta y gasto basal, pero ninguno registra la actividad física realizada por el usuario ni cuantifica las calorías quemadas durante esa actividad. El módulo de ejercicios cubre ese hueco.

El módulo tiene dos responsabilidades diferenciadas. La primera es mantener un catálogo de tipos de ejercicio con su valor MET asociado, que actúa como referencia compartida para todos los usuarios. La segunda es permitir que cada usuario registre sus sesiones de ejercicio diarias, calculando automáticamente las calorías quemadas a partir del tipo de ejercicio, la duración y el peso corporal del usuario.

Las dos responsabilidades se corresponden con dos tablas en base de datos: `ejercicios`, que almacena el catálogo, y `ejercicios_registro`, que almacena los registros individuales vinculados a cada usuario.

---

## 5.6.2 Modelo de datos

**Tabla `ejercicios`** — catálogo de tipos de ejercicio:

| Columna     | Tipo SQL         | Descripción                                      |
|-------------|------------------|--------------------------------------------------|
| `id`        | `BIGINT PK`      | Clave primaria, asignada por la base de datos    |
| `nombre`    | `VARCHAR(150)`   | Nombre del ejercicio («Correr», «Yoga», etc.)    |
| `met`       | `DECIMAL(4,2)`   | Factor MET del ejercicio                         |
| `categoria` | `VARCHAR(50)`    | Categoría opcional («CARDIO», «FUERZA», etc.)    |
| `created_at`| `TIMESTAMP`      | Fecha de inserción, gestionada por la base de datos |

La tabla tiene un índice sobre `nombre` para acelerar las búsquedas por texto. Los registros del catálogo se precargan con la migración `V7__seed_ejercicios.sql`.

**Tabla `ejercicios_registro`** — sesiones de ejercicio por usuario:

| Columna        | Tipo SQL          | Descripción                                           |
|----------------|-------------------|-------------------------------------------------------|
| `id`           | `BIGINT PK`       | Clave primaria, asignada por la base de datos         |
| `usuario_id`   | `BIGINT FK`       | Usuario que realizó el ejercicio                      |
| `ejercicio_id` | `BIGINT FK`       | Ejercicio del catálogo                                |
| `fecha`        | `DATE`            | Fecha de la sesión                                    |
| `duracion_min` | `SMALLINT`        | Duración en minutos                                   |
| `kcal_quemadas`| `DECIMAL(7,2)`    | Calorías quemadas, calculadas y persistidas en el registro |
| `created_at`   | `TIMESTAMP`       | Fecha de inserción                                    |

La clave foránea `usuario_id` tiene `ON DELETE CASCADE`: si se elimina un usuario, sus registros de ejercicio desaparecen con él. La clave foránea `ejercicio_id` tiene `ON DELETE RESTRICT`: no se puede eliminar un ejercicio del catálogo mientras haya registros que lo referencien. Existe un índice compuesto sobre `(usuario_id, fecha)` para acelerar la consulta más frecuente del módulo.

`kcal_quemadas` se calcula en el momento del registro y se persiste como un valor concreto en la fila. Esto garantiza que el historial no cambie si el usuario actualiza posteriormente su peso en el perfil: el registro refleja las calorías quemadas con el peso que tenía en ese momento, no con el peso actual.

---

## 5.6.3 Fórmula MET

Las calorías quemadas en una sesión de ejercicio se calculan con la fórmula estándar basada en el equivalente metabólico:

```
kcal = MET × peso_kg × (duración_min / 60.0)
```

Cada valor proviene de una fuente diferente. El MET se toma del catálogo: el ejercicio recuperado de `ejercicios` por su `id` lleva el factor MET almacenado como atributo de la entidad `Ejercicio`. El peso se toma del perfil del usuario en el momento del registro: el servicio consulta `PerfilRepository.findById(usuarioId)` y extrae `pesoKgActual`. La duración la envía el cliente en el campo `duracionMin` del cuerpo de la petición.

El cálculo se implementa en `RegistroEjercicioServiceImpl` como un método estático con visibilidad de paquete:

```java
// RegistroEjercicioServiceImpl.java
static double calcularKcal(double met, double pesoKg, int duracionMin) {
    double resultado = met * pesoKg * (duracionMin / 60.0);
    return BigDecimal.valueOf(resultado)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
}
```

El redondeo a dos decimales con `HALF_UP` evita que valores como `116.6666...` se almacenen con precisión arbitraria. Un ejemplo concreto: correr (MET = 8.0), 75 kg, 45 minutos produce `8.0 × 75 × 0.75 = 450.00 kcal`. Otro: ciclismo moderado (MET = 5.0), 70 kg, 20 minutos produce `5.0 × 70 × 0.333... = 116.67 kcal`.

---

## 5.6.4 API del módulo

El módulo expone dos controladores. `EjercicioController` gestiona el catálogo bajo `/api/ejercicios`:

| Método | Ruta                  | Código de éxito | Descripción                                        |
|--------|-----------------------|-----------------|----------------------------------------------------|
| GET    | `/api/ejercicios`     | 200 OK          | Devuelve el catálogo completo o filtra por `?q=`   |
| GET    | `/api/ejercicios/{id}`| 200 OK          | Devuelve un ejercicio concreto por id              |
| POST   | `/api/ejercicios`     | 201 Created     | Crea un nuevo ejercicio en el catálogo             |

`RegistroEjercicioController` gestiona los registros de usuario bajo `/api/ejercicios-registro`:

| Método | Ruta                          | Código de éxito | Descripción                                             |
|--------|-------------------------------|-----------------|-------------------------------------------------------- |
| GET    | `/api/ejercicios-registro`    | 200 OK          | Lista los registros de un usuario en una fecha          |
| POST   | `/api/ejercicios-registro`    | 201 Created     | Registra una sesión y devuelve las kcal calculadas      |
| DELETE | `/api/ejercicios-registro/{id}`| 204 No Content | Elimina un registro del usuario autenticado             |

`GET /api/ejercicios` y `GET /api/ejercicios-registro` aceptan `usuarioId` y `fecha` como parámetros de query. Los endpoints `POST` y `DELETE` de registros reciben `usuarioId` como parámetro de query; el cuerpo de `POST` contiene `ejercicioId`, `fecha` y `duracionMin`.

---

## 5.6.5 Estructura interna

El módulo sigue la misma estructura en capas verticales que el resto del backend:

```
Controller → Service → Repository → JdbcTemplate → MariaDB
```

La separación en dos controladores responde a que las operaciones sobre el catálogo y las operaciones sobre los registros de usuario tienen colaboradores distintos y contratos de entrada y salida diferentes. `EjercicioController` delega en `EjercicioService`, que a su vez usa `EjercicioRepository`. `RegistroEjercicioController` delega en `RegistroEjercicioService`, que coordina tres repositorios: `RegistroEjercicioRepository`, `EjercicioRepository` y `PerfilRepository`.

Ambos servicios se declaran como interfaces. Las implementaciones son `EjercicioServiceImpl` y `RegistroEjercicioServiceImpl`, anotadas con `@Service`. Los repositorios siguen el mismo patrón: interfaz `XxxRepository` más implementación `JdbcXxxRepository` con `JdbcTemplate` y SQL directo.

---

## 5.6.6 Flujo de registro

`POST /api/ejercicios-registro` recibe `usuarioId` como parámetro de query y `ejercicioId`, `fecha` y `duracionMin` en el cuerpo. `RegistroEjercicioServiceImpl.registrar()` ejecuta los pasos siguientes en orden:

1. **Verificación del ejercicio.** Se consulta `ejercicioRepository.findById(ejercicioId)`. Si no existe, se lanza `ResourceNotFoundException` antes de consultar el perfil.
2. **Verificación del perfil.** Se consulta `perfilRepository.findById(usuarioId)`. Si el usuario no tiene perfil configurado, se lanza `ResourceNotFoundException`. Esta verificación es la que hace disponible el peso para el cálculo; sin perfil el registro no puede completarse.
3. **Cálculo de calorías.** Se invoca `calcularKcal` con el MET del ejercicio, el peso del perfil y la duración recibida.
4. **Construcción y persistencia del registro.** Se crea un `RegistroEjercicio` con todos los campos, incluyendo `kcalQuemadas` ya calculadas, y se persiste con `registroRepository.save()`.
5. **Respuesta.** Se devuelve un `RegistroEjercicioResponse` con `id`, `usuarioId`, `ejercicioId`, `nombreEjercicio`, `fecha`, `duracionMin` y `kcalQuemadas`.

```java
// RegistroEjercicioServiceImpl.java — registrar()
Ejercicio ejercicio = ejercicioRepository.findById(request.getEjercicioId())
        .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un ejercicio con id " + request.getEjercicioId()));

Perfil perfil = perfilRepository.findById(usuarioId)
        .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un usuario con id " + usuarioId));

double kcalQuemadas = calcularKcal(ejercicio.getMet(), perfil.getPesoKgActual(), request.getDuracionMin());
```

La validación es fail-fast: si el ejercicio no existe, no se consulta el perfil ni se llama a `save`.

---

## 5.6.7 Consulta y borrado de registros

**Consulta por fecha.** `findByUsuarioAndFecha` delega directamente en el repositorio sin transformación adicional. La consulta es un `INNER JOIN` entre `ejercicios_registro` y `ejercicios` para devolver `nombre_ejercicio` en la misma respuesta:

```sql
-- JdbcRegistroEjercicioRepository.java — findByUsuarioAndFecha()
SELECT
    er.id,
    er.usuario_id,
    er.ejercicio_id,
    e.nombre AS nombre_ejercicio,
    er.fecha,
    er.duracion_min,
    er.kcal_quemadas
FROM ejercicios_registro er
INNER JOIN ejercicios e ON e.id = er.ejercicio_id
WHERE er.usuario_id = ? AND er.fecha = ?
ORDER BY er.id ASC
```

El cliente recibe directamente el nombre del ejercicio sin necesidad de una segunda petición al catálogo.

**Borrado con verificación de pertenencia.** `deleteById` verifica que el registro existe y que pertenece al usuario indicado antes de borrarlo:

```java
// RegistroEjercicioServiceImpl.java — deleteById()
RegistroEjercicio registro = registroRepository.findById(registroId)
        .orElseThrow(() -> new ResourceNotFoundException(
                "No existe un registro de ejercicio con id " + registroId));

if (!registro.getUsuarioId().equals(usuarioId)) {
    throw new ResourceNotFoundException(
            "El registro " + registroId + " no pertenece al usuario " + usuarioId);
}

registroRepository.deleteById(registroId);
```

El mensaje de error incluye ambos identificadores para facilitar el diagnóstico. Esta verificación impide que un usuario borre registros ajenos conociendo únicamente el id del registro.

---

## 5.6.8 Decisión de diseño: el cálculo ocurre en el backend

El cliente envía únicamente `ejercicioId`, `fecha` y `duracionMin`. No envía el MET del ejercicio ni el peso del usuario. El backend recupera ambos valores de sus fuentes autoritativas —el catálogo y el perfil— y realiza el cálculo antes de persistir.

Esta decisión tiene dos consecuencias concretas. La primera es que el cliente no necesita conocer el valor MET de cada ejercicio para registrar una sesión: basta con que identifique el ejercicio por su id. La segunda es que el cliente no necesita acceder al peso del usuario en el momento del registro: ese dato vive en el perfil del backend y el servicio lo extrae directamente. El cálculo y los datos que necesita para realizarlo están en el mismo lugar.

El patrón es análogo al del módulo de perfil, donde el cliente envía los datos biométricos brutos y el backend calcula TMB y TDEE (§4.5.7). Centralizar la lógica de cálculo en el backend garantiza que cualquier consumidor futuro de la API —una aplicación web, un cliente móvil— obtenga los mismos resultados sin duplicar la fórmula.

---

## 5.6.9 `EjercicioRepository` y `RegistroEjercicioRepository`

`EjercicioRepository` declara cuatro operaciones sobre el catálogo:

```java
public interface EjercicioRepository {
    List<Ejercicio> findAll();
    List<Ejercicio> searchByNombre(String query);
    Optional<Ejercicio> findById(Long id);
    Ejercicio save(Ejercicio ejercicio);
}
```

`searchByNombre` realiza la misma búsqueda parcial insensible a mayúsculas que `AlimentoRepository`: `LOWER(nombre) LIKE LOWER(?)` con el parámetro envuelto en `%`.

`RegistroEjercicioRepository` declara las operaciones sobre los registros de usuario:

```java
public interface RegistroEjercicioRepository {
    List<RegistroEjercicioResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);
    Optional<RegistroEjercicio> findById(Long id);
    RegistroEjercicio save(RegistroEjercicio registro);
    boolean deleteById(Long id);
}
```

`findByUsuarioAndFecha` devuelve directamente `List<RegistroEjercicioResponse>` en lugar de entidades de dominio, porque la consulta incluye el `INNER JOIN` con `ejercicios` y proyecta campos de las dos tablas. Construir una entidad intermedia y luego transformarla en DTO no añadiría ningún valor. `JdbcRegistroEjercicioRepository` usa `GeneratedKeyHolder` en `save` para recuperar el id asignado por MariaDB tras el `INSERT` y asignarlo al objeto devuelto.

---

## 5.6.10 Integración con el cliente JavaFX

`EjercicioController` (cliente) gestiona la pantalla `ejercicio-view.fxml`. La pantalla se articula en dos zonas. La zona superior contiene el formulario de registro: un `ComboBox<EjercicioDto>` cargado con el catálogo completo al arrancar la pantalla, un `DatePicker` con la fecha de hoy como valor por defecto, un campo de texto para la duración en minutos y un botón «Registrar». La zona inferior muestra una `TableView<RegistroEjercicioDto>` con tres columnas —ejercicio, duración y kcal quemadas— que se actualiza al cambiar la fecha o tras cada registro exitoso.

Las columnas de la tabla se configuran con lambdas en `initialize()` en lugar de `PropertyValueFactory`, dado que `RegistroEjercicioDto` no expone propiedades JavaFX observables:

```java
// EjercicioController.java — initialize()
colEjercicio.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreEjercicio()));
colDuracion.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getDuracionMin()));
colKcal.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getKcalQuemadas()));
```

La validación del formulario es previa a la llamada a la API: el controlador verifica que hay un ejercicio seleccionado en el `ComboBox`, que el campo de duración no está vacío, que el valor es un entero positivo y que no supera 999 minutos. Si alguna comprobación falla, se muestra el mensaje en el `statusLabel` y no se realiza ninguna petición.

El botón «Eliminar» está deshabilitado hasta que el usuario selecciona una fila en la tabla. Al pulsarlo se muestra un `Alert.AlertType.CONFIRMATION` con el nombre del ejercicio seleccionado antes de ejecutar el borrado. Todas las operaciones de red se ejecutan en hilos de fondo con `javafx.concurrent.Task`; el hilo de la interfaz nunca queda bloqueado.

La comunicación con los dos endpoints se delega en `EjercicioApiClient` y `RegistroEjercicioApiClient`, que incluyen el token de sesión en la cabecera `Authorization: Bearer <token>` obtenido de `SessionManager`.

---

## 5.6.11 Manejo de errores

| Situación                                             | Excepción / respuesta          | Código HTTP |
|-------------------------------------------------------|--------------------------------|-------------|
| Ejercicio no encontrado al registrar                  | `ResourceNotFoundException`    | 404         |
| Perfil de usuario no encontrado al registrar          | `ResourceNotFoundException`    | 404         |
| Registro no encontrado al borrar                      | `ResourceNotFoundException`    | 404         |
| Registro no pertenece al usuario indicado             | `ResourceNotFoundException`    | 404         |
| Ejercicio no encontrado por id (catálogo)             | `ResourceNotFoundException`    | 404         |
| Campo obligatorio ausente o `duracionMin` ≤ 0        | Bean Validation (via `@Valid`) | 400         |

---

## 5.6.12 Tests de `RegistroEjercicioServiceImplTest`

`RegistroEjercicioServiceImplTest` contiene catorce tests unitarios en cuatro clases anidadas: `CalcularKcal`, `Registrar`, `FindByUsuarioAndFecha` y `DeleteById`. Los tres colaboradores de `RegistroEjercicioServiceImpl` —`RegistroEjercicioRepository`, `EjercicioRepository` y `PerfilRepository`— se sustituyen por mocks de Mockito. No se requiere base de datos ni contexto de Spring.

`calcularKcal` tiene visibilidad de paquete (`static` sin modificador de acceso público), lo que permite que los tests de `CalcularKcal` lo invoquen directamente sin pasar por el flujo completo de `registrar`. Los cuatro casos de esa clase verifican resultados exactos calculados analíticamente: 450.00 kcal para correr con MET 8.0 a 75 kg durante 45 minutos, 140.00 kcal para caminar con MET 3.5 a 80 kg durante 30 minutos, 150.00 kcal para yoga con MET 2.5 a 60 kg durante 60 minutos, y 116.67 kcal para un caso con resultado periódico que verifica el redondeo `HALF_UP`.

`Registrar` cubre cinco casos: registro exitoso con verificación completa del DTO devuelto, verificación de que `nombreEjercicio` proviene de la entidad recuperada del catálogo y no del request, verificación mediante `ArgumentCaptor` de que `kcalQuemadas` se calcula correctamente antes de persistir, ejercicio inexistente con comprobación de que ni el perfil ni el repositorio se consultan, y usuario sin perfil con comprobación de que `save` no se invoca.

`FindByUsuarioAndFecha` verifica que el servicio delega directamente en el repositorio y que una lista vacía se devuelve sin error. `DeleteById` cubre registro existente y propio, registro inexistente y registro que pertenece a otro usuario, verificando en los dos últimos casos que `deleteById` no llega a invocarse. La descripción completa de la batería de pruebas del backend se recoge en §6.
