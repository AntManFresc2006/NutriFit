# 5.3 Comidas

## 5.3.1 Problema que resuelve el módulo

El catálogo de alimentos —descrito en §5.2— proporciona los valores nutricionales de referencia, pero no permite modelar qué ha comido un usuario en un día concreto. El módulo de comidas cubre ese hueco: permite registrar las tomas del día, asociar a cada una los alimentos consumidos con sus gramos exactos, y exponer al módulo de resumen diario (§5.4) los datos necesarios para calcular el balance nutricional.

---

## 5.3.2 Comida e ítem de comida: dos conceptos distintos

El modelo distingue dos entidades:

- **Comida** representa una toma del día —desayuno, almuerzo, cena o cualquier etiqueta que el usuario asigne—. Se identifica por usuario, fecha y tipo. No contiene información nutricional por sí misma.
- **Ítem de comida** (`ComidaAlimento`) es la relación entre una comida y un alimento del catálogo, con los gramos consumidos. Es la unidad mínima que aporta valor calórico y macronutricional.

La separación en dos tablas permite que una comida contenga múltiples alimentos y que los ítems se añadan y borren de forma independiente sin afectar a la comida que los agrupa.

---

## 5.3.3 API del módulo

`ComidaController` expone seis endpoints bajo el prefijo `/api/comidas`:

| Método | Ruta                                     | Código de éxito | Descripción                                          |
|--------|------------------------------------------|-----------------|------------------------------------------------------|
| GET    | `/api/comidas`                           | 200 OK          | Lista las comidas de un usuario en una fecha         |
| POST   | `/api/comidas`                           | 201 Created     | Crea una nueva comida                                |
| DELETE | `/api/comidas/{id}`                      | 204 No Content  | Elimina una comida y todos sus ítems                 |
| GET    | `/api/comidas/{comidaId}/items`          | 200 OK          | Lista los ítems de una comida con valores calculados |
| POST   | `/api/comidas/{comidaId}/items`          | 201 Created     | Añade un ítem (alimento + gramos) a una comida       |
| DELETE | `/api/comidas/{comidaId}/items/{itemId}` | 204 No Content  | Elimina un ítem concreto de una comida               |

Las operaciones de escritura reciben un `ComidaRequest` o `ComidaAlimentoRequest` anotados con `@Valid`.

---

## 5.3.4 Creación de una comida

`POST /api/comidas` recibe `usuarioId` como parámetro de query y `fecha` y `tipo` en el cuerpo. En `ComidaServiceImpl` el tipo se normaliza a mayúsculas antes de persistir:

```java
// ComidaServiceImpl.java — save()
comida.setTipo(request.getTipo().trim().toUpperCase());
```

El sistema no valida el tipo contra una lista cerrada: admite cualquier cadena no vacía dentro del límite de 30 caracteres de la columna. La inserción usa `GeneratedKeyHolder` para devolver el id asignado por la base de datos.

---

## 5.3.5 Adición de ítems a una comida

`POST /api/comidas/{comidaId}/items` recibe `alimentoId` y `gramos` en el cuerpo. Antes de insertar, el servicio verifica que tanto la comida como el alimento existan:

```java
// ComidaServiceImpl.java — addAlimentoToComida()
comidaRepository.findById(comidaId)
        .orElseThrow(() -> new ResourceNotFoundException(COMIDA_NO_ENCONTRADA + comidaId));

alimentoRepository.findById(request.getAlimentoId())
        .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + request.getAlimentoId()));

comidaRepository.addAlimentoToComida(comidaId, request.getAlimentoId(), request.getGramos());
```

Si cualquiera de los dos ids no existe, se lanza `ResourceNotFoundException` y no se ejecuta el `INSERT`. No hay restricción que impida añadir el mismo alimento varias veces a la misma comida: cada inserción genera una fila independiente.

---

## 5.3.6 Consulta de comidas e ítems

**Comidas por fecha.** `GET /api/comidas` filtra por `usuarioId` y `fecha` y devuelve los resultados ordenados por id ascendente. Si no hay comidas para esa fecha devuelve una lista vacía.

```sql
-- JdbcComidaRepository.java — findByUsuarioAndFecha()
SELECT id, usuario_id, fecha, tipo
FROM comidas
WHERE usuario_id = ? AND fecha = ?
ORDER BY id ASC
```

**Ítems de una comida.** `GET /api/comidas/{comidaId}/items` valida primero que la comida existe y luego ejecuta un JOIN con `alimentos` para calcular en SQL los valores nutricionales proporcionales a los gramos registrados:

```sql
-- JdbcComidaRepository.java — findDetalleItemsByComidaId()
SELECT
    ca.id                                           AS item_id,
    ca.comida_id,
    ca.alimento_id,
    a.nombre,
    ca.gramos,
    ROUND((a.kcal_por_100g  * ca.gramos) / 100, 2) AS kcal_estimadas,
    ROUND((a.proteinas_g    * ca.gramos) / 100, 2) AS proteinas_estimadas,
    ROUND((a.grasas_g       * ca.gramos) / 100, 2) AS grasas_estimadas,
    ROUND((a.carbos_g       * ca.gramos) / 100, 2) AS carbos_estimados
FROM comida_alimentos ca
INNER JOIN alimentos a ON a.id = ca.alimento_id
WHERE ca.comida_id = ?
ORDER BY ca.id ASC
```

El redondeo a dos decimales se aplica directamente en base de datos. `ComidaItemDetalleResponse` devuelve los campos calculados junto con el nombre del alimento, de modo que el cliente no necesita realizar ningún cálculo adicional.

---

## 5.3.7 Borrado de comida e ítem

**Borrado de comida.** `DELETE /api/comidas/{id}` verifica que la comida existe y la elimina. La clave foránea con `ON DELETE CASCADE` en `comida_alimentos` propaga el borrado a todos sus ítems.

**Borrado de ítem.** `DELETE /api/comidas/{comidaId}/items/{itemId}` aplica una comprobación adicional: verifica que el ítem pertenece a la comida indicada en la ruta antes de borrarlo:

```java
// ComidaServiceImpl.java — deleteItem()
ComidaAlimento item = comidaRepository.findItemById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("No existe un item con id " + itemId));

if (!item.getComidaId().equals(comidaId)) {
    throw new ResourceNotFoundException("El item " + itemId + " no pertenece a la comida " + comidaId);
}

comidaRepository.deleteItemById(itemId);
```

Esta verificación impide eliminar un ítem usando el id de una comida ajena.

---

## 5.3.8 Relación con alimentos y resumen diario

Las comidas actúan como contenedor intermedio entre el catálogo de alimentos y el resumen diario. El flujo es: `alimentos` almacena los valores por 100 g → `comida_alimentos` registra los gramos consumidos en cada toma → el módulo de resumen diario (§5.4) agrega todos los ítems de un día mediante un `LEFT JOIN` sobre las tres tablas, sumando las kilocalorías y macronutrientes ponderados por los gramos de cada ítem.

El `LEFT JOIN` garantiza que una comida sin ítems no produzca error en la agregación. Si no existe ninguna comida registrada para la fecha consultada, la consulta no devuelve filas: en ese caso `JdbcResumenDiarioRepository` devuelve directamente un `ResumenDiarioResponse` con todos los totales a cero, sin depender de `COALESCE` para cubrir ese escenario.

---

## 5.3.9 Validaciones

```java
// ComidaRequest.java
@NotNull(message = "La fecha es obligatoria")
private LocalDate fecha;

@NotBlank(message = "El tipo de comida es obligatorio")
private String tipo;
```

```java
// ComidaAlimentoRequest.java
@NotNull(message = "El id del alimento es obligatorio")
private Long alimentoId;

@DecimalMin(value = "0.01", message = "Los gramos deben ser mayores que 0")
private double gramos;
```

---

## 5.3.10 `ComidaRepository` y `JdbcComidaRepository`

`ComidaRepository` declara las operaciones de acceso a datos del módulo:

```java
public interface ComidaRepository {
    List<Comida> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);
    Optional<Comida> findById(Long id);
    Comida save(Comida comida);
    boolean deleteById(Long id);
    void addAlimentoToComida(Long comidaId, Long alimentoId, double gramos);
    List<ComidaItemDetalleResponse> findDetalleItemsByComidaId(Long comidaId);
    Optional<ComidaAlimento> findItemById(Long itemId);
    boolean deleteItemById(Long itemId);
}
```

`JdbcComidaRepository` implementa esta interfaz con `JdbcTemplate` sin ORM. A diferencia del módulo de alimentos —que centraliza el mapeo en un `RowMapper` separado—, aquí el mapeo de `Comida`, `ComidaAlimento` y `ComidaItemDetalleResponse` se resuelve con expresiones lambda directamente en cada consulta. Las búsquedas de un único registro devuelven `Optional<T>` mediante `stream().findFirst()`.

---

## 5.3.11 Integración con el cliente JavaFX

En la versión actual del cliente no existe una pantalla dedicada a la gestión de comidas. Los endpoints del módulo están implementados y son funcionales en el backend, pero el cliente no los consume directamente.

Lo que el cliente sí muestra es el resultado agregado: `DiarioController` llama a `ResumenDiarioApiClient`, que obtiene los totales del día desde `/api/resumen-diario` y los presenta junto al TDEE del usuario recuperado de `SessionManager`. Para que el resumen muestre datos, las comidas deben haberse registrado previamente a través del backend —por ejemplo, mediante una herramienta de pruebas HTTP—. La pantalla de gestión de comidas es una funcionalidad pendiente en el cliente.

---

## 5.3.12 Manejo de errores

| Situación                                             | Excepción / respuesta          | Código HTTP |
|-------------------------------------------------------|--------------------------------|-------------|
| Comida no encontrada (GET items, DELETE comida)       | `ResourceNotFoundException`    | 404         |
| Alimento no encontrado al añadir ítem                 | `ResourceNotFoundException`    | 404         |
| Ítem no encontrado al borrar                          | `ResourceNotFoundException`    | 404         |
| Ítem no pertenece a la comida indicada en la ruta     | `ResourceNotFoundException`    | 404         |
| Campo obligatorio ausente o `gramos` ≤ 0             | Bean Validation (via `@Valid`) | 400         |

---

## 5.3.13 Tests

El módulo de comidas no dispone de tests automatizados. No existe ninguna clase `ComidaServiceImplTest` ni test de integración sobre el controlador o el repositorio. La cobertura de pruebas del proyecto se describe en §6.
