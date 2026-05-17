# Notas de base de datos - alimentos

## Tabla actual
La tabla `alimentos` forma parte del núcleo inicial del proyecto y actualmente contiene los siguientes campos principales:

- id
- nombre
- porcion_g
- kcal_por_100g
- proteinas_g
- grasas_g
- carbos_g
- fuente
- created_at

## Intención del modelo
Esta tabla servirá como catálogo base de alimentos sobre el que después se apoyarán:
- la creación de comidas
- el cálculo de ingesta diaria
- el resumen nutricional
- futuras recomendaciones del sistema

## Reglas de validación previstas
A nivel de aplicación, se aplicarán estas reglas:
- `nombre` obligatorio
- `porcion_g > 0`
- `kcal_por_100g >= 0`
- `proteinas_g >= 0`
- `grasas_g >= 0`
- `carbos_g >= 0`

## Mejoras implementadas
✅ Índice por `nombre` para búsquedas rápidas
✅ Constraints adicionales
✅ Datos semilla para facilitar pruebas y demo

## Integración con otros módulos
- **Comidas**: cada comida referencia múltiples alimentos a través de `comida_alimentos`
- **Resumen diario**: se utiliza para calcular totales nutricionales
- **Escaneo de códigos**: importa alimentos desde OpenFoodFacts y los almacena aquí

## Observación
La tabla de alimentos es una de las piezas fundamentales del MVP, por lo que se prioriza su estabilidad. Actualmente está completamente estabilizada.