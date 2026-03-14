# Plan de pruebas - CRUD de alimentos

## Objetivo
Verificar que el módulo de alimentos funciona correctamente en operaciones CRUD, validación y manejo de errores.

## Casos de prueba iniciales

### TC01 - Crear alimento válido
**Entrada**
- nombre correcto
- porción válida
- kcal/macros válidas

**Resultado esperado**
- respuesta exitosa
- alimento guardado en base de datos

---

### TC02 - Crear alimento con nombre vacío
**Entrada**
- nombre vacío o en blanco

**Resultado esperado**
- error de validación
- mensaje claro indicando que el nombre es obligatorio

---

### TC03 - Crear alimento con porción no válida
**Entrada**
- porción igual a 0 o negativa

**Resultado esperado**
- error de validación
- mensaje claro indicando que la porción debe ser mayor que 0

---

### TC04 - Crear alimento con macros negativas
**Entrada**
- proteínas, grasas, carbohidratos o kcal con valor negativo

**Resultado esperado**
- error de validación
- mensaje claro

---

### TC05 - Listar alimentos
**Entrada**
- petición GET general

**Resultado esperado**
- lista de alimentos devuelta correctamente

---

### TC06 - Buscar alimentos por nombre
**Entrada**
- consulta por texto parcial, por ejemplo `pollo`

**Resultado esperado**
- lista filtrada correctamente

---

### TC07 - Obtener alimento existente por ID
**Entrada**
- ID existente

**Resultado esperado**
- alimento devuelto correctamente

---

### TC08 - Obtener alimento inexistente por ID
**Entrada**
- ID no existente

**Resultado esperado**
- error controlado
- mensaje claro indicando que el recurso no existe

---

### TC09 - Editar alimento existente
**Entrada**
- ID existente con datos válidos

**Resultado esperado**
- alimento actualizado correctamente

---

### TC10 - Eliminar alimento existente
**Entrada**
- ID existente

**Resultado esperado**
- alimento eliminado correctamente

## Observación
Estas pruebas se realizarán primero de forma manual mediante peticiones HTTP, y más adelante se ampliarán con tests automatizados en backend.

## Ejecución manual realizada

### Resultado
Se ha probado correctamente el CRUD completo del módulo de alimentos mediante peticiones HTTP.

#### Operaciones verificadas
- GET /api/alimentos → 200 OK
- POST /api/alimentos → 201 Created
- GET /api/alimentos/{id} → 200 OK
- GET /api/alimentos?q=... → 200 OK
- PUT /api/alimentos/{id} → 200 OK
- DELETE /api/alimentos/{id} → 204 No Content

#### Validaciones verificadas
- ID inexistente → 404 Not Found con mensaje controlado
- nombre vacío → 400 Bad Request con mensaje de validación

#### Observación
El módulo backend de alimentos queda funcional y preparado para conectarse con la futura interfaz JavaFX.