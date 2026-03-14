# Notas del resumen diario nutricional

## Objetivo
El resumen diario permite obtener, para un usuario y una fecha concreta, el total de:
- kcal consumidas
- proteínas consumidas
- grasas consumidas
- carbohidratos consumidos

## Origen de los datos
El resumen no se almacena como tabla independiente. Se calcula dinámicamente a partir de:
- `comidas`
- `comida_alimentos`
- `alimentos`

## Estrategia de cálculo
Se utiliza una consulta SQL agregada con `LEFT JOIN` entre:
- `comidas`
- `comida_alimentos`
- `alimentos`

Esto permite sumar los valores nutricionales proporcionales a los gramos consumidos en cada item de comida.

## Fórmulas utilizadas
Para cada item de comida se calcula:

```sql
(valor_por_100g * gramos) / 100
```

Se aplica a:
- kcal
- proteínas
- grasas
- carbohidratos

Después se agrupan todos los items del mismo usuario y la misma fecha con `SUM`.

## Consulta base del resumen
La implementación actual usa una consulta de este estilo:

```sql
SELECT
    c.usuario_id,
    c.fecha,
    COALESCE(ROUND(SUM((a.kcal_por_100g * ca.gramos) / 100), 2), 0) AS kcal_totales,
    COALESCE(ROUND(SUM((a.proteinas_g * ca.gramos) / 100), 2), 0) AS proteinas_totales,
    COALESCE(ROUND(SUM((a.grasas_g * ca.gramos) / 100), 2), 0) AS grasas_totales,
    COALESCE(ROUND(SUM((a.carbos_g * ca.gramos) / 100), 2), 0) AS carbos_totales
FROM comidas c
LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
LEFT JOIN alimentos a ON a.id = ca.alimento_id
WHERE c.usuario_id = ? AND c.fecha = ?
GROUP BY c.usuario_id, c.fecha;
```

## Comportamiento si no hay datos
Si el usuario no tiene comidas o items registrados para esa fecha, el sistema devuelve un resumen con valores `0` en todos los campos.

## Endpoint relacionado
```http
GET /api/resumen-diario?usuarioId=1&fecha=2026-03-14
```

## Utilidad dentro de NutriFit
Este resumen es una pieza central del proyecto porque conecta:
- alimentos
- comidas
- relaciones entre ambas
- cálculo nutricional
- interfaz gráfica JavaFX

Además, sirve como base para futuras ampliaciones:
- objetivos diarios
- comparativa entre consumo y meta
- balance neto con ejercicio
- gráficos o estadísticas

## Ventajas del enfoque actual
- no duplica datos
- aprovecha la potencia de agregación de SQL
- mantiene el modelo de base de datos más limpio
- permite recalcular el resumen automáticamente cuando cambian comidas o alimentos

## Limitaciones actuales
En esta versión:
- no se incluyen objetivos calóricos del usuario
- no se incluye ejercicio ni gasto energético
- no se calcula todavía el balance neto diario
- no se muestra aún el detalle del resumen dentro de una navegación completa de varias pantallas

## Próximo paso previsto
El siguiente desarrollo natural del módulo es integrar este resumen diario dentro de la navegación real del cliente JavaFX y, más adelante, ampliarlo con:
- objetivos personalizados
- balance calórico neto
- más métricas de seguimiento
- estadísticas históricas
