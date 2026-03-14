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

## Mejoras previstas
En siguientes migraciones se valorará añadir:
- índice por `nombre`
- constraints adicionales si faltan
- datos semilla para facilitar pruebas y demo

## Observación
La tabla de alimentos es una de las piezas fundamentales del MVP, por lo que se prioriza su estabilidad antes de construir módulos dependientes.