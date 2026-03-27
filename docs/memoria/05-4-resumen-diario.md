# 5.4 Resumen diario

## 5.4.1 Problema que resuelve el módulo

El diario nutricional de NutriFit permite registrar comidas e ítems a lo largo del día, pero esos registros por sí solos no dan al usuario una imagen de conjunto. Para valorar su ingesta diaria necesita ver los totales: cuántas kilocalorías ha consumido, cuántos gramos de proteínas, grasas e hidratos.

El módulo de resumen diario cubre esa necesidad. Dado un usuario y una fecha, agrega los valores nutricionales de todos los ítems registrados en esa jornada y devuelve un único objeto con los cuatro totales. La pantalla de diario usa ese objeto para mostrar el resumen al usuario, opcionalmente acompañado del TDEE como valor de referencia.

---

## 5.4.2 Endpoint y respuesta

El módulo expone un único endpoint en `ResumenDiarioController`:

| Método | Ruta                   | Parámetros              | Código de éxito |
|--------|------------------------|-------------------------|-----------------|
| GET    | `/api/resumen-diario`  | `usuarioId`, `fecha`    | 200 OK          |

Ambos parámetros se pasan como query params. `fecha` se deserializa directamente a `LocalDate` gracias al soporte de Spring para tipos de fecha estándar.

La respuesta es un `ResumenDiarioResponse` con los siguientes campos:

| Campo                 | Tipo Java    | Descripción                                      |
|-----------------------|--------------|--------------------------------------------------|
| `usuarioId`           | `Long`       | Identificador del usuario consultado             |
| `fecha`               | `LocalDate`  | Fecha del resumen                                |
| `kcalTotales`         | `double`     | Total de kilocalorías consumidas en el día       |
| `proteinasTotales`    | `double`     | Total de proteínas en gramos                     |
| `grasasTotales`       | `double`     | Total de grasas en gramos                        |
| `carbosTotales`       | `double`     | Total de hidratos de carbono en gramos           |

---

## 5.4.3 Cálculo del resumen: la consulta de agregación

La lógica central del módulo reside en `JdbcResumenDiarioRepository`. El resumen no se calcula en Java: se delega completamente a MariaDB mediante una única consulta de agregación:

```sql
SELECT
    c.usuario_id,
    c.fecha,
    COALESCE(ROUND(SUM((a.kcal_por_100g * ca.gramos) / 100), 2), 0) AS kcal_totales,
    COALESCE(ROUND(SUM((a.proteinas_g * ca.gramos) / 100), 2), 0)   AS proteinas_totales,
    COALESCE(ROUND(SUM((a.grasas_g * ca.gramos) / 100), 2), 0)      AS grasas_totales,
    COALESCE(ROUND(SUM((a.carbos_g * ca.gramos) / 100), 2), 0)      AS carbos_totales
FROM comidas c
LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
LEFT JOIN alimentos a ON a.id = ca.alimento_id
WHERE c.usuario_id = ? AND c.fecha = ?
GROUP BY c.usuario_id, c.fecha
```

La consulta cruza tres tablas. `comidas` contiene las comidas del día —una fila por comida registrada—. `comida_alimentos` contiene los ítems de cada comida con el campo `gramos`. `alimentos` tiene los valores nutricionales por 100 g. Los dos `LEFT JOIN` permiten que comidas sin ítems participen en el resultado sin ser excluidas.

La fórmula de conversión por cada ítem es:

```
valor_ítem = (valor_por_100g × gramos) / 100
```

`SUM` agrega todos los ítems del día. `ROUND(..., 2)` limita el resultado a dos decimales. `COALESCE(..., 0)` garantiza que si `SUM` produce `NULL` —porque no hay ítems— el campo devuelto sea `0` en lugar de nulo.

---

## 5.4.4 Por qué el cálculo vive en la base de datos

Mover esta lógica a Java requeriría recuperar todos los ítems del día con sus gramos y los valores por 100 g de cada alimento, acumularlos en bucles y redondear el resultado. La consulta de agregación hace exactamente lo mismo en una sola operación que el motor ejecuta sobre los índices de las tablas, sin transferir datos intermedios al proceso Java.

Hay además una razón de coherencia: los valores nutricionales por 100 g viven en `alimentos`, y `SUM` opera directamente sobre ellos sin pasar por DTOs intermedios. Cualquier cambio en un alimento se refleja automáticamente en el siguiente cálculo sin necesidad de invalidar ninguna caché ni sincronizar estado en Java.

La decisión de usar JDBC con SQL directo en lugar de un ORM facilita escribir esta consulta sin restricciones: el SQL se lee exactamente como aparece en el repositorio, sin capas de traducción que oculten lo que ocurre en la base de datos.

---

## 5.4.5 Día sin comidas registradas

Cuando el usuario no tiene ninguna comida registrada en la fecha consultada, la consulta no devuelve ninguna fila —`GROUP BY` sobre un conjunto vacío produce un resultado vacío—. El repositorio trata ese caso explícitamente:

```java
// JdbcResumenDiarioRepository.java
return jdbcTemplate.query(sql, rs -> {
    if (rs.next()) {
        return new ResumenDiarioResponse(
                rs.getLong("usuario_id"),
                rs.getDate("fecha").toLocalDate(),
                rs.getDouble("kcal_totales"),
                rs.getDouble("proteinas_totales"),
                rs.getDouble("grasas_totales"),
                rs.getDouble("carbos_totales")
        );
    }
    return new ResumenDiarioResponse(usuarioId, fecha, 0, 0, 0, 0);
}, usuarioId, fecha);
```

Si el `ResultSet` no tiene filas, se construye un `ResumenDiarioResponse` con todos los totales a cero. El cliente recibe siempre una respuesta con la misma estructura, independientemente de si el día tiene datos o no. La pantalla de diario no necesita tratar el caso de respuesta vacía: los ceros se muestran directamente.

---

## 5.4.6 Integración con el cliente JavaFX

`ResumenDiarioApiClient` encapsula la petición GET. Construye la URL con los dos query params y deserializa la respuesta en un `ResumenDiarioDto`:

```java
// ResumenDiarioApiClient.java
String url = BASE_URL
        + "?usuarioId=" + URLEncoder.encode(String.valueOf(usuarioId), StandardCharsets.UTF_8)
        + "&fecha=" + URLEncoder.encode(fecha, StandardCharsets.UTF_8);
```

`DiarioController` gestiona la pantalla de diario. Al inicializarse fija la fecha al día actual y lanza la primera carga:

```java
// DiarioController.java — initialize()
fechaPicker.setValue(LocalDate.now());
fechaPicker.setOnAction(event -> cargarResumen());
cargarResumen();
```

Cada vez que el usuario cambia la fecha en el `DatePicker`, se dispara una nueva llamada al backend. La carga se ejecuta en un hilo de fondo con `javafx.concurrent.Task`:

```java
// DiarioController.java — cargarResumen()
task.setOnSucceeded(event -> {
    ResumenDiarioDto resumen = task.getValue();
    double tdee = SessionManager.getTdee();
    if (tdee > 0) {
        kcalLabel.setText(resumen.getKcalTotales() + " / " + tdee + " kcal");
    } else {
        kcalLabel.setText(String.valueOf(resumen.getKcalTotales()));
    }
    proteinasLabel.setText(String.valueOf(resumen.getProteinasTotales()));
    grasasLabel.setText(String.valueOf(resumen.getGrasasTotales()));
    carbosLabel.setText(String.valueOf(resumen.getCarbosTotales()));
});
```

Las kilocalorías se muestran con o sin referencia al TDEE según si `SessionManager.getTdee()` devuelve un valor positivo. Si el TDEE está disponible, la etiqueta muestra el formato `consumido / objetivo kcal`; si no lo está, muestra solo el total consumido. Los macronutrientes se muestran siempre como valores absolutos en gramos.

---

## 5.4.7 Tests de `ResumenDiarioServiceImplTest`

`ResumenDiarioServiceImplTest` contiene tres tests en una clase anidada `ObtenerResumenDiario`. El repositorio se sustituye por un mock de Mockito; no se requiere base de datos ni contexto de Spring.

Los tres casos cubren: que el servicio delega en el repositorio con los argumentos correctos y retorna el mismo objeto recibido; que un día sin comidas propaga un resumen con los cuatro totales a cero; y que los valores con decimales se propagan sin modificación desde el repositorio hasta la respuesta. Los tres tests verifican que `ResumenDiarioServiceImpl` no introduce transformación alguna sobre lo que devuelve el repositorio, lo cual es coherente con que todo el cálculo ocurre en la consulta SQL. La descripción completa de la batería de pruebas del backend se recoge en §6.
