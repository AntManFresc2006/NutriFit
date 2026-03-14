
## ADR 0002 - Alcance inicial del módulo de comidas

## Estado
Aceptada

## Contexto
Tras completar el CRUD de alimentos y el módulo de autenticación, el siguiente paso funcional del proyecto es empezar a registrar ingestas reales de usuarios. Para que NutriFit se acerque al comportamiento esperado de una app nutricional, no basta con almacenar alimentos de forma aislada: es necesario poder agruparlos dentro de comidas y calcular su impacto nutricional.

## Decisión
Se ha decidido implementar un primer alcance del módulo de comidas con estas capacidades:

1. crear comidas para un usuario en una fecha concreta
2. listar comidas por usuario y fecha
3. añadir alimentos a una comida indicando gramos consumidos
4. listar los items de una comida
5. enriquecer el detalle de cada item con datos nutricionales calculados mediante `JOIN`

## Diseño elegido
Se ha optado por separar el módulo en dos tablas:

- `comidas`
- `comida_alimentos`

Motivo:
- una comida puede contener varios alimentos
- un mismo alimento puede reutilizarse en muchas comidas
- el modelo es más flexible y más normalizado que guardar todo en una sola tabla

## Alternativas consideradas

### Opción 1: una sola tabla con comida y alimento mezclados
Se descartó porque:
- duplicaría información
- dificultaría la reutilización de alimentos
- complica la ampliación futura del sistema

### Opción 2: implementar directamente el resumen diario completo
Se descartó como primer paso porque antes era necesario disponer de una base de datos mínima capaz de registrar comidas y sus items.

## Consecuencias
Ventajas:
- permite registrar comidas reales
- prepara la base para el resumen diario
- permite consultas SQL más ricas con `JOIN`
- encaja con una arquitectura escalable para el MVP

Inconvenientes:
- obliga a gestionar relaciones y claves foráneas
- aumenta la complejidad frente a una tabla simple
- requiere validar la existencia previa de comida y alimento

## Decisiones técnicas relacionadas
- Se mantiene Flyway para versionar la estructura de base de datos.
- El detalle enriquecido de items se calcula desde SQL en vez de hacerlo únicamente en Java.
- Se ha añadido un seed de alimentos para facilitar las pruebas y la demostración del proyecto.
- Se ha reforzado la validación en la capa de servicio para evitar errores de integridad referencial.

## Alcance que queda fuera por ahora
En esta fase no se ha implementado todavía:
- edición de comidas
- borrado de items de una comida
- edición de gramos de un item ya existente
- resumen diario agregado por fecha
- integración de comidas en la interfaz JavaFX

## Próximo paso recomendado
Construir un endpoint de resumen diario por usuario y fecha que agregue:
- kcal totales
- proteínas totales
- grasas totales
- carbohidratos totales

Esto permitirá conectar el módulo de comidas con la lógica nutricional central de NutriFit.