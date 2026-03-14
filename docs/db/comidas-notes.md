# Notas de base de datos del módulo de comidas

## Objetivo
El módulo de comidas permite registrar ingestas de un usuario en una fecha concreta y asociar varios alimentos a cada comida con una cantidad determinada en gramos.

## Tablas añadidas

### `comidas`
Representa una comida registrada por un usuario en un día concreto.

Campos principales:
- `id`: identificador único de la comida
- `usuario_id`: usuario propietario de la comida
- `fecha`: fecha de la comida
- `tipo`: tipo de comida (`DESAYUNO`, `ALMUERZO`, `CENA`, etc.)
- `created_at`: fecha y hora de creación del registro

Relación:
- una comida pertenece a un usuario
- un usuario puede tener muchas comidas

Clave foránea:
- `fk_comidas_usuario` → `usuarios(id)`

---

### `comida_alimentos`
Representa los alimentos concretos que forman parte de una comida.

Campos principales:
- `id`: identificador único del item
- `comida_id`: comida a la que pertenece el alimento
- `alimento_id`: alimento referenciado
- `gramos`: cantidad consumida de ese alimento

Relaciones:
- una comida puede tener muchos items
- un alimento puede aparecer en muchas comidas

Claves foráneas:
- `fk_comida_alimentos_comida` → `comidas(id)`
- `fk_comida_alimentos_alimento` → `alimentos(id)`

## Modelo relacional
La estructura sigue una relación de tipo:
- `usuarios` 1:N `comidas`
- `comidas` 1:N `comida_alimentos`
- `alimentos` 1:N `comida_alimentos`

Esto evita duplicar información nutricional dentro de la tabla de comidas y permite reutilizar alimentos ya existentes.

## Motivo de esta separación
Se ha optado por separar `comidas` y `comida_alimentos` porque una comida no contiene un único alimento, sino varios. Este diseño permite:
- registrar comidas compuestas
- reutilizar alimentos existentes
- calcular resúmenes nutricionales con consultas SQL y `JOIN`
- mantener una base de datos más normalizada y mantenible

## Consultas importantes del módulo
Actualmente el módulo ya utiliza:
- inserción de comidas
- inserción de alimentos dentro de una comida
- consulta de comidas por usuario y fecha
- consulta enriquecida de items de una comida mediante `JOIN` con `alimentos`

## Detalle nutricional enriquecido
El endpoint de items de comida ya devuelve información calculada a partir de los gramos consumidos y los valores por 100 g del alimento:
- `kcalEstimadas`
- `proteinasEstimadas`
- `grasasEstimadas`
- `carbosEstimados`

Estas cantidades se calculan con expresiones SQL del tipo:

```sql
(valor_por_100g * gramos) / 100
```

## Observaciones actuales
- La tabla comida_alimentos depende de que existan previamente la comida y el alimento referenciado.
- Si se intenta insertar un alimento_id inexistente, la base de datos bloquea la operación por integridad referencial.
- En la capa de servicio se ha mejorado la validación para comprobar que la comida y el alimento existan antes de insertar.

## Proximo paso previsto
El siguiente desarrollo natural del módulo es construir un resumen diario por usuario y fecha, agregando:

- kcal totales

- proteínas totales

- grasas totales

- carbohidratos totales