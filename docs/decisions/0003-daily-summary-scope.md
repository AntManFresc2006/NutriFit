# ADR 0003 - Implementación del resumen diario nutricional

## Estado
Aceptada

## Contexto
Tras implementar alimentos, comidas e items de comida enriquecidos con datos nutricionales, el siguiente paso funcional era ofrecer una vista más cercana al objetivo real de una aplicación como NutriFit: conocer el total nutricional consumido en un día.

## Decisión
Se ha decidido implementar un endpoint específico de resumen diario que agregue, por usuario y fecha:
- kcal totales
- proteínas totales
- grasas totales
- carbohidratos totales

Además, se ha creado una primera pantalla JavaFX para mostrar esta información en la interfaz del cliente.

## Motivo
Esta funcionalidad conecta varias partes del sistema en una misma vista útil:
- comidas
- alimentos
- relaciones entre ambas
- cálculos nutricionales
- consumo de API desde JavaFX

## Alternativas consideradas

### Opción 1: calcular el resumen en Java recorriendo listas
Se descartó como primera opción porque:
- obliga a traer más datos al backend
- complica la lógica en la capa de servicio
- desaprovecha la capacidad de agregación de SQL

### Opción 2: guardar resúmenes diarios ya calculados
Se descartó por ahora porque:
- añade complejidad de sincronización
- requiere recalcular ante cada cambio en comidas o alimentos
- no es necesaria todavía en esta fase del proyecto

## Diseño elegido
Se ha optado por:
- una consulta SQL agregada
- `LEFT JOIN` entre `comidas`, `comida_alimentos` y `alimentos`
- uso de `SUM`, `ROUND` y `COALESCE`
- un endpoint REST dedicado
- una vista JavaFX específica para mostrar los resultados

## Consecuencias
### Ventajas
- el cálculo se realiza de forma eficiente
- se aprovecha mejor la base de datos
- la funcionalidad tiene valor visible inmediato en la aplicación
- refuerza el carácter práctico del proyecto

### Inconvenientes
- requiere que las tablas previas estén bien relacionadas
- depende de que las comidas e items se registren correctamente
- aún no incluye objetivos, ejercicio ni balance neto

## Decisiones técnicas relacionadas
- Se mantiene Flyway para versionar la estructura de base de datos.
- El detalle enriquecido de items de comida se calcula con SQL y `JOIN`.
- Se ha añadido un seed de alimentos para facilitar las pruebas y la demostración del proyecto.
- Se ha creado una pantalla JavaFX específica para visualizar el resumen diario.
- La navegación completa entre pantallas todavía se encuentra en evolución.

## Alcance que queda fuera por ahora
En esta fase no se ha implementado todavía:
- objetivos calóricos personalizados
- balance neto con ejercicio
- comparativa entre consumo y meta
- gráficos o estadísticas históricas
- navegación completa y definitiva entre todas las pantallas del cliente

## Próximo paso recomendado
Integrar esta pantalla de resumen diario en la navegación real del cliente JavaFX y, más adelante, ampliar el módulo con:
- objetivos calóricos y de macronutrientes
- balance energético neto
- histórico diario
- visualizaciones más completas para el usuario
