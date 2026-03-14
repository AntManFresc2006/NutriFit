# 0001 - Food CRUD Scope

## Decisión
El primer slice funcional de NutriFit será el CRUD completo de alimentos.

## Motivo
Este módulo permite demostrar de forma temprana y clara:
- programación orientada a objetos
- arquitectura por capas
- acceso a base de datos con JDBC/Spring JDBC
- API REST
- validación de datos
- base para la futura interfaz gráfica en JavaFX

Además, es un bloque de alto valor para la rúbrica porque conecta base de datos, backend y futura GUI en una funcionalidad real del proyecto.

## Alcance inicial
En esta primera fase se implementarán los endpoints:
- GET /api/alimentos
- GET /api/alimentos/{id}
- GET /api/alimentos?q=...
- POST /api/alimentos
- PUT /api/alimentos/{id}
- DELETE /api/alimentos/{id}

## Reglas funcionales iniciales
- El nombre del alimento será obligatorio.
- La porción en gramos deberá ser mayor que 0.
- Las kcal por 100g no podrán ser negativas.
- Las proteínas no podrán ser negativas.
- Las grasas no podrán ser negativas.
- Los carbohidratos no podrán ser negativos.

## Fuera de alcance por ahora
En esta fase no se desarrollarán todavía:
- autenticación y sesiones
- comidas del día
- ejercicios
- resumen diario
- estadísticas
- recomendaciones IA

## Decisión técnica relacionada
Primero se cerrará el backend del CRUD de alimentos y después se construirá la primera pantalla JavaFX conectada a este módulo.