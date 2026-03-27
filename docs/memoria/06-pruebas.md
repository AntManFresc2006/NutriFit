# 6. Pruebas

## 6.1 Estrategia de pruebas

La estrategia de pruebas de NutriFit combina dos enfoques complementarios: pruebas unitarias automatizadas sobre la capa de servicio del backend, y pruebas manuales sobre la API REST mediante peticiones HTTP reales contra el servidor en ejecución.

La cobertura automatizada se centra en la capa de servicio porque es ahí donde reside la lógica de negocio: validación de reglas de dominio, manejo de casos de error, transformación de datos y, en el módulo de perfil, el cálculo de valores nutricionales. Los repositorios son envolturas directas sobre SQL y su comportamiento queda cubierto por las pruebas manuales de la API.

### Herramientas

Las pruebas unitarias se implementan con tres bibliotecas del ecosistema Java:

- **JUnit 5** como marco de ejecución. Se usa `@Nested` para agrupar los tests por operación dentro de cada clase, y `@DisplayName` para asignar descripciones legibles que sirven de documentación del comportamiento esperado.
- **Mockito** para sustituir los colaboradores reales por objetos simulados. Esto permite probar cada servicio de forma aislada, sin base de datos ni ningún componente externo.
- **AssertJ** para las aserciones. Su API fluida produce mensajes de error claros y permite expresar las comprobaciones de forma natural: `assertThat(resultado.getNombre()).isEqualTo("Pollo a la plancha")`.

### Aislamiento del contexto de Spring

Ningún test levanta el contexto de aplicación de Spring. En lugar de `@SpringBootTest`, que arranca el contenedor IoC y requiere una base de datos activa, se emplea `@ExtendWith(MockitoExtension.class)`:

```java
@ExtendWith(MockitoExtension.class)
class AlimentoServiceImplTest {

    @Mock
    private AlimentoRepository alimentoRepository;

    @InjectMocks
    private AlimentoServiceImpl service;
}
```

Mockito instancia el servicio inyectando el repositorio simulado, sin intervención de Spring. El resultado es una suite que se ejecuta en menos de tres segundos y no requiere ningún servicio externo activo.

---

## 6.2 Pruebas unitarias del backend

La suite está compuesta por 46 tests distribuidos en cinco clases, una por cada servicio con lógica de negocio relevante.

**Tabla 6.1 — Distribución de la suite de pruebas unitarias**

| Clase de test                    | Tests | Operaciones cubiertas               |
|----------------------------------|-------|-------------------------------------|
| `AlimentoServiceImplTest`        | 12    | findAll, findById, save, update, deleteById |
| `AuthServiceImplTest`            | 9     | register, login, logout             |
| `ComidaServiceImplTest`          | 17    | save, findByUsuarioAndFecha, deleteById, addAlimentoToComida, findDetalleItemsByComidaId, deleteItem |
| `ResumenDiarioServiceImplTest`   | 3     | obtenerResumenDiario                |
| `PerfilServiceImplTest`          | 5     | getPerfil, updatePerfil             |
| **Total**                        | **46**|                                     |

> **Figura 6.1** — Salida de Maven tras la ejecución completa:
> `Tests run: 46, Failures: 0, Errors: 0, Skipped: 0`
>
> *(insertar captura de terminal)*

### AlimentoServiceImpl — 12 tests

Esta clase cubre el servicio con mayor superficie de operaciones. Los tests se organizan en cinco clases anidadas: `FindAll`, `FindById`, `Save`, `Update` y `DeleteById`.

**Enrutamiento en la búsqueda.** El servicio distingue entre tres situaciones al recibir una petición de listado: parámetro ausente, parámetro vacío o en blanco, y texto real. Los dos primeros invocan `findAll()`; el tercero delega en `searchByNombre()` con el texto recortado. Los tests verifican no solo el camino tomado, sino también que el alternativo no se invoca en ningún caso:

```java
verify(alimentoRepository, never()).searchByNombre(anyString());
```

**Sanitización del nombre.** Un test verifica que el servicio elimina los espacios del nombre antes de persistir, aunque lleguen en el DTO de entrada. Para ello se usa un *argument matcher* en línea:

```java
// el request contiene "  Pollo a la plancha  "
verify(alimentoRepository).save(
    argThat(a -> "Pollo a la plancha".equals(a.getNombre()))
);
```

Este test sitúa la responsabilidad de normalizar la entrada en el servicio, no en el controlador.

**Protección ante borrado sin existencia previa.** Cuando el id no existe, el test verifica que se lanza `ResourceNotFoundException` antes de que el repositorio sea invocado:

```java
verify(alimentoRepository, never()).deleteById(anyLong());
```

### AuthServiceImpl — 9 tests

Este servicio coordina cuatro colaboradores: `UsuarioRepository`, `SesionRepository`, `PasswordService` y `TokenService`. Todos se reemplazan por mocks, lo que permite controlar exactamente qué devuelve cada uno y verificar las interacciones.

**Registro con email duplicado.** Si el repositorio ya tiene un usuario con ese email, se lanza `BadRequestException` sin que se llegue a invocar `save` en ninguno de los dos repositorios. El test lo comprueba con `verify(..., never())` en ambos.

**Verificación de la sesión creada.** Tras un registro o login exitoso, se usa `ArgumentCaptor` para capturar el objeto `Sesion` que se pasa al repositorio y comprobar sus campos:

```java
ArgumentCaptor<Sesion> sesionCaptor = ArgumentCaptor.forClass(Sesion.class);
verify(sesionRepository).save(sesionCaptor.capture());

assertThat(sesionCaptor.getValue().getToken()).isEqualTo("token-abc-123");
assertThat(sesionCaptor.getValue().getExpiresAt()).isAfter(LocalDateTime.now());
```

Esta comprobación confirma que la sesión se persiste con el token correcto y con una fecha de expiración futura, sin que `SesionRepository` esté implementado.

**Mensajes de error indiferenciados en login.** Por razones de seguridad, el servicio devuelve el mismo mensaje —«Credenciales inválidas»— tanto si el email no existe como si la contraseña es incorrecta. Dos tests cubren ambos casos y verifican explícitamente que el mensaje es idéntico, evitando que un atacante pueda deducir si un email está registrado.

**Normalización del email.** El test `emailConMayusculas_seNormaliza` envía `"  ANA@EJEMPLO.COM  "` y verifica, mediante `ArgumentCaptor<Usuario>`, que el valor persistido es `"ana@ejemplo.com"`. La normalización se aplica en el servicio antes de cualquier consulta al repositorio.

**Validación de token en logout.** Los tests sobre token nulo y token en blanco verifican que el servicio rechaza la operación antes de consultar la base de datos. En ambos casos, `deleteByToken` no llega a invocarse.

### ResumenDiarioServiceImpl — 3 tests

Este servicio delega directamente en el repositorio sin transformar el resultado. La suite refleja ese comportamiento: tres tests son suficientes porque no existe lógica propia que justifique más. Añadir tests adicionales sería cobertura sobre código que no hace nada.

Los tres casos cubren: delegación correcta con verificación del resultado devuelto, propagación de un resumen con todos los valores a cero (día sin comidas), y paso sin modificación de valores con decimales.

### ComidaServiceImpl — 17 tests

Los tests se organizan en seis clases anidadas: `Save`, `FindByUsuarioAndFecha`, `DeleteById`, `AddAlimentoToComida`, `FindDetalleItemsByComidaId` y `DeleteItem`. Los dos colaboradores del servicio —`ComidaRepository` y `AlimentoRepository`— se sustituyen por mocks.

**Normalización del tipo.** `ArgumentCaptor<Comida>` verifica que el servicio aplica `trim().toUpperCase()` al tipo antes de persistir, de forma análoga a la sanitización del nombre en `AlimentoServiceImpl`.

**Validación fail-fast.** Los tests de `addAlimentoToComida` y `deleteItem` comprueban con `verify(..., never())` que las operaciones destructivas no se invocan cuando la validación previa falla: si la comida no existe, el servicio no consulta el alimento; si el ítem no pertenece a la comida indicada, no se llama a `deleteItemById`.

**Pertenencia del ítem.** El test `itemDeOtraComida_lanzaExcepcionSinBorrar` verifica que el mensaje de error incluye tanto el id del ítem como el de la comida, lo que facilita el diagnóstico en el cliente.

### PerfilServiceImpl — 5 tests

Este es el servicio con mayor contenido de lógica de negocio propia, centrada en el cálculo de TMB y TDEE mediante la fórmula de Mifflin-St Jeor.

**Verificación de la fórmula por sexo.** Dos tests cubren los dos caminos del cálculo: la constante `+5` para hombres y `−161` para mujeres. Los valores esperados se calculan analíticamente antes de escribir el test:

*Hombre, 30 años, 70 kg, 170 cm, nivel SEDENTARIO (factor 1,2):*

```
base = 10 × 70 + 6,25 × 170 − 5 × 30 = 1612,5
TMB  = 1612,5 + 5  = 1617,5
TDEE = 1617,5 × 1,2 = 1941,0
```

*Mujer, 30 años, 60 kg, 160 cm, nivel MODERADO (factor 1,55):*

```
base = 10 × 60 + 6,25 × 160 − 5 × 30 = 1450,0
TMB  = 1450,0 − 161  = 1289,0
TDEE = 1289,0 × 1,55 = 1997,95
```

Las aserciones comprueban los valores exactos resultantes:

```java
assertThat(resultado.getTmb()).isEqualTo(1617.5);
assertThat(resultado.getTdee()).isEqualTo(1941.0);
```

**Estabilidad temporal.** La fórmula calcula la edad con `LocalDate.now()`, lo que haría que una fecha de nacimiento fija produjera resultados distintos según el día de ejecución. Para evitarlo, la fecha se construye como `LocalDate.now().minusYears(30)`, de modo que `Period.between` devuelve siempre 30 años exactos con independencia de cuándo se ejecuten los tests.

**Actualización del perfil.** El test `aplicaCambiosYPersistePorRepositorio` verifica que `updatePerfil` llama a `repository.update()` con el id correcto y que la respuesta refleja los valores del perfil devuelto por el mock. Se comprueba explícitamente el sexo, la altura, el peso, el peso objetivo y el nivel de actividad.

---

## 6.3 Pruebas manuales de la API

Todos los endpoints han sido verificados mediante peticiones HTTP reales contra el servidor en ejecución, siguiendo el plan de pruebas recogido en `docs/tests/food-crud-test-plan.md`.

### Archivos de peticiones HTTP

El directorio `docs/api/` contiene cinco archivos `.http`, uno por módulo, compatibles con el cliente HTTP de IntelliJ IDEA y con la extensión REST Client de VS Code. Cubren los flujos principales y los casos de error más relevantes de cada módulo:

| Archivo               | Endpoints cubiertos                                              |
|-----------------------|------------------------------------------------------------------|
| `auth.http`           | registro, login, logout                                          |
| `alimentos.http`      | GET all, GET por id, GET búsqueda, POST, PUT, DELETE             |
| `comidas.http`        | crear comida, añadir ítem, GET por usuario/fecha, DELETE comida, DELETE ítem |
| `resumen-diario.http` | GET resumen por usuario y fecha                                  |
| `perfil.http`         | GET perfil, PUT actualización válida e inválida, GET id inexistente |

Además de los flujos exitosos, cada archivo incluye al menos un caso de error: id inexistente (→ 404), datos inválidos (→ 400) y, en autenticación, credenciales erróneas (→ 401).

### Swagger UI

La dependencia `springdoc-openapi-starter-webmvc-ui` genera automáticamente la documentación interactiva de la API, accesible en `http://localhost:8080/swagger-ui.html` con el servidor activo. Permite explorar los endpoints del sistema, consultar los esquemas de entrada y salida de cada operación y ejecutar peticiones directamente desde el navegador.

> **Figura 6.2** — Captura de Swagger UI con los módulos y endpoints desplegados.
>
> *(insertar captura)*

### Comportamiento ante errores de validación

Las pruebas manuales verificaron la respuesta del `GlobalExceptionHandler` ante entradas inválidas. La estructura de error es uniforme en todos los módulos:

```json
{
  "timestamp": "2026-03-14T02:47:21",
  "status": 400,
  "error": "Bad Request",
  "message": "La porción debe ser mayor que 0",
  "path": "/api/alimentos"
}
```

El campo `message` corresponde al mensaje definido en la anotación de validación del DTO, lo que permite al cliente mostrar el error directamente al usuario sin necesidad de interpretar el código de estado.

---

## Cierre de la sección

La combinación de pruebas unitarias automatizadas y pruebas manuales de la API cubre las dos dimensiones más importantes del backend: la corrección de la lógica de negocio aislada de infraestructura, y el comportamiento real del sistema ante peticiones HTTP. Los 46 tests unitarios se ejecutan sin base de datos ni contexto de Spring, lo que los hace rápidos y reproducibles en cualquier entorno. Las pruebas manuales, respaldadas por los archivos `.http` y por Swagger UI, complementan esa cobertura verificando el comportamiento extremo a extremo, incluyendo la validación de entrada, el manejo de errores y los flujos de autenticación.
