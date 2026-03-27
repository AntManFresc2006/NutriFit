# 5.2 Alimentos

## 5.2.1 Problema que resuelve el módulo

Para que el usuario pueda registrar lo que come necesita tener alimentos disponibles en el sistema, con sus valores nutricionales por 100 g. El módulo de alimentos cubre esa necesidad: mantiene un catálogo consultable y editable desde el que el resto de módulos —en particular el de comidas— extraen la información nutricional que necesitan.

El catálogo es compartido: cualquier usuario autenticado puede consultar, crear, modificar o eliminar alimentos. En el ámbito del MVP, no existe una separación por usuario dentro del catálogo.

---

## 5.2.2 API del módulo

El módulo expone cinco endpoints en `AlimentoController` bajo el prefijo `/api/alimentos`:

| Método | Ruta               | Código de éxito | Descripción                                                  |
|--------|--------------------|-----------------|--------------------------------------------------------------|
| GET    | `/api/alimentos`   | 200 OK          | Devuelve todos los alimentos o filtra por nombre (`?q=`)     |
| GET    | `/api/alimentos/{id}` | 200 OK       | Devuelve un alimento concreto por id                         |
| POST   | `/api/alimentos`   | 201 Created     | Crea un nuevo alimento                                       |
| PUT    | `/api/alimentos/{id}` | 200 OK       | Actualiza completamente un alimento existente                |
| DELETE | `/api/alimentos/{id}` | 204 No Content | Elimina un alimento existente                            |

Las operaciones de escritura reciben un `AlimentoRequest` anotado con `@Valid`; Spring rechaza la petición antes de que llegue al servicio si alguna restricción no se cumple.

---

## 5.2.3 Datos del alimento

Cada alimento se representa en el modelo de dominio `Alimento` con los siguientes campos:

| Campo          | Tipo Java    | Descripción                                      |
|----------------|--------------|--------------------------------------------------|
| `id`           | `Long`       | Clave primaria, asignada por la base de datos    |
| `nombre`       | `String`     | Nombre del alimento                              |
| `porcionG`     | `BigDecimal` | Porción de referencia en gramos                  |
| `kcalPor100g`  | `BigDecimal` | Kilocalorías por 100 g                           |
| `proteinasG`   | `BigDecimal` | Proteínas en gramos por 100 g                    |
| `grasasG`      | `BigDecimal` | Grasas en gramos por 100 g                       |
| `carbosG`      | `BigDecimal` | Hidratos de carbono en gramos por 100 g          |
| `fuente`       | `String`     | Fuente de los datos nutricionales (opcional)     |

Los valores nutricionales se modelan como `BigDecimal` en el dominio y en los DTOs del backend para preservar la precisión decimal exactamente como se almacena en la base de datos. El campo `fuente` es el único sin restricción de obligatoriedad: permite anotar el origen de los datos (por ejemplo, `"USDA"`) pero puede enviarse vacío o sin incluir.

Las restricciones de `AlimentoRequest` garantizan que los valores numéricos no sean negativos y que la porción sea estrictamente mayor que cero:

```java
// AlimentoRequest.java
@NotBlank(message = "El nombre es obligatorio")
private String nombre;

@NotNull(message = "La porción es obligatoria")
@DecimalMin(value = "0.01", message = "La porción debe ser mayor que 0")
private BigDecimal porcionG;

@NotNull(message = "Las kcal por 100g son obligatorias")
@DecimalMin(value = "0.0", inclusive = true, message = "Las kcal por 100g no pueden ser negativas")
private BigDecimal kcalPor100g;
```

Los macronutrientes siguen el mismo patrón que `kcalPor100g`: `@NotNull` y `@DecimalMin` con cero inclusive.

---

## 5.2.4 Búsqueda por nombre

El endpoint `GET /api/alimentos` acepta el parámetro opcional `q`. Si está ausente o en blanco, el servicio devuelve el catálogo completo. Si tiene contenido, delega en `searchByNombre` con el texto recortado:

```java
// AlimentoServiceImpl.java — findAll()
if (query == null || query.isBlank()) {
    alimentos = alimentoRepository.findAll();
} else {
    alimentos = alimentoRepository.searchByNombre(query.trim());
}
```

En el repositorio, la búsqueda usa `LIKE` con comodines y aplica `LOWER` tanto a la columna como al parámetro para que la coincidencia no distinga mayúsculas de minúsculas:

```sql
-- JdbcAlimentoRepository.java — searchByNombre()
SELECT id, nombre, porcion_g, kcal_por_100g, proteinas_g, grasas_g, carbos_g, fuente
FROM alimentos
WHERE LOWER(nombre) LIKE LOWER(?)
ORDER BY nombre ASC
```

El parámetro se pasa como `"%" + query + "%"`, de modo que la búsqueda es una coincidencia parcial: `"poll"` encuentra `"Pollo a la plancha"`. Tanto la lista completa como los resultados filtrados se ordenan alfabéticamente por nombre.

---

## 5.2.5 Operaciones de escritura: creación, actualización y eliminación

Las tres operaciones de escritura comparten el mismo patrón en `AlimentoServiceImpl`. La creación convierte el DTO a modelo de dominio, aplica `trim()` al nombre y persiste:

```java
// AlimentoServiceImpl.java — toModel()
alimento.setNombre(request.getNombre().trim());
```

La actualización y la eliminación verifican primero que el id existe en la base de datos antes de ejecutar la operación:

```java
// AlimentoServiceImpl.java — update() / deleteById()
alimentoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + id));
```

Si el id no existe, se lanza `ResourceNotFoundException` y no se ejecuta ninguna sentencia de modificación. En el repositorio, la actualización es una sustitución completa de todos los campos:

```sql
-- JdbcAlimentoRepository.java — update()
UPDATE alimentos
SET nombre = ?, porcion_g = ?, kcal_por_100g = ?, proteinas_g = ?, grasas_g = ?, carbos_g = ?, fuente = ?
WHERE id = ?
```

No existe una actualización parcial: el cliente siempre envía el alimento completo.

---

## 5.2.6 `AlimentoRowMapper` y `JdbcAlimentoRepository`

`AlimentoRowMapper` implementa `RowMapper<Alimento>` y centraliza la conversión de cada fila del `ResultSet` en un objeto de dominio:

```java
// AlimentoRowMapper.java
public Alimento mapRow(ResultSet rs, int rowNum) throws SQLException {
    Alimento alimento = new Alimento();
    alimento.setId(rs.getLong("id"));
    alimento.setNombre(rs.getString("nombre"));
    alimento.setPorcionG(rs.getBigDecimal("porcion_g"));
    alimento.setKcalPor100g(rs.getBigDecimal("kcal_por_100g"));
    alimento.setProteinasG(rs.getBigDecimal("proteinas_g"));
    alimento.setGrasasG(rs.getBigDecimal("grasas_g"));
    alimento.setCarbosG(rs.getBigDecimal("carbos_g"));
    alimento.setFuente(rs.getString("fuente"));
    return alimento;
}
```

Extraer el mapeo en una clase separada en lugar de usar una expresión lambda en cada consulta evita duplicar la correspondencia columna-campo en `findAll`, `searchByNombre` y `findById`. `JdbcAlimentoRepository` declara una única instancia del mapper y la reutiliza en las tres consultas de lectura.

La inserción usa `GeneratedKeyHolder` para recuperar el id asignado por MariaDB tras el `INSERT` y asignarlo al objeto devuelto. El `findById` consulta por clave primaria y devuelve `Optional<Alimento>` obteniendo el primer elemento de la lista resultante:

```java
// JdbcAlimentoRepository.java — findById()
List<Alimento> resultados = jdbcTemplate.query(sql, rowMapper, id);
return resultados.stream().findFirst();
```

---

## 5.2.7 Integración con el cliente JavaFX

El cliente mantiene dos representaciones del alimento. `AlimentoDto` es un POJO plano para la serialización y deserialización JSON con Jackson. `AlimentoFx` es el modelo observable de JavaFX: cada campo se declara como `XxxProperty` (`StringProperty`, `DoubleProperty`, etc.), lo que permite que `TableView` observe los cambios directamente sin código adicional de actualización.

`AlimentoApiClient` convierte entre ambas representaciones. Al recibir la lista del backend transforma cada `AlimentoDto` en `AlimentoFx` mediante `toFx()`. Al enviar datos al backend convierte el `AlimentoFx` del formulario en `AlimentoDto` mediante `toDto()`.

`FoodController` gestiona la pantalla de alimentos. Las columnas de la tabla se vinculan a las propiedades del modelo observable:

```java
// FoodController.java — initialize()
nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
kcalColumn.setCellValueFactory(new PropertyValueFactory<>("kcalPor100g"));
```

El formulario funciona en dos modos según el estado de selección de la tabla. Si no hay alimento seleccionado, el botón muestra «Crear» y `onGuardar` llama a `apiClient.create()`. Si hay uno seleccionado, muestra «Actualizar» y llama a `apiClient.update()` asignando el id del elemento seleccionado antes de enviar. La eliminación muestra un diálogo de confirmación de tipo `Alert.AlertType.CONFIRMATION` antes de ejecutar la operación.

Todas las operaciones de red se ejecutan en hilos de fondo con `javafx.concurrent.Task`. Los controles de la pantalla se deshabilitan durante la ejecución y se reactivan al completarse, independientemente de si la tarea tuvo éxito o falló.

La búsqueda desde el cliente envía el texto del campo `searchField` como parámetro `q`. Si el campo está vacío, se llama a `getAll()` en lugar de `search()`, reproduciendo en el cliente la misma lógica de bifurcación que aplica el servicio en el backend.

---

## 5.2.8 Manejo de errores

| Situación                                  | Excepción / respuesta               | Código HTTP |
|--------------------------------------------|-------------------------------------|-------------|
| Id no encontrado (GET, PUT, DELETE)        | `ResourceNotFoundException`         | 404         |
| Campo obligatorio ausente o inválido       | Bean Validation (via `@Valid`)      | 400         |
| Valor numérico negativo o porción cero     | Bean Validation (via `@Valid`)      | 400         |

En el cliente, `FoodController.validarFormulario()` comprueba los campos del formulario antes de enviar la petición: nombre no vacío, porción mayor que cero y macronutrientes no negativos. Si la validación falla, se muestra el error en el `statusLabel` y no se hace ninguna llamada a la API. Esta validación es redundante con la del backend pero evita peticiones innecesarias y da retroalimentación inmediata al usuario.

---

## 5.2.9 Tests de `AlimentoServiceImplTest`

`AlimentoServiceImplTest` contiene doce tests en cinco clases anidadas. El repositorio se sustituye por un mock de Mockito; no se requiere base de datos ni contexto de Spring.

Los casos cubiertos son: catálogo completo sin query, query en blanco tratado igual que ausencia de query, búsqueda con texto que delega en `searchByNombre` con el texto recortado, repositorio vacío sin error; id existente en `findById` con verificación de datos, id inexistente con `ResourceNotFoundException`; guardado con devolución del id asignado y nombre recortado; actualización de id existente e id inexistente sin llamar a `update`; eliminación de id existente e id inexistente sin llamar a `deleteById`. La descripción completa de la batería de pruebas del backend se recoge en §6.
