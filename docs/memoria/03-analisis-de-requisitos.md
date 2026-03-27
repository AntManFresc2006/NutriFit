# 3. Análisis de requisitos

## 3.1 Introducción

Esta sección recoge los requisitos que delimitan el alcance de NutriFit en su primera versión funcional —el MVP—. Los requisitos no se formularon en una fase de análisis previa al desarrollo: emergieron de forma iterativa a medida que los módulos se definían e implementaban. El objetivo de documentarlos aquí es hacer explícito qué hace el sistema, qué restricciones de calidad cumple y qué queda deliberadamente fuera del alcance actual.

Los requisitos funcionales se agrupan por módulo para facilitar su lectura junto a las secciones §5.1–§5.6, donde se describe la implementación de cada uno. Los requisitos no funcionales recogen propiedades transversales que afectan al sistema en su conjunto.

---

## 3.2 Requisitos funcionales

### Módulo de autenticación

| Código | Requisito |
|--------|-----------|
| RF-01 | El sistema permite registrar un nuevo usuario proporcionando nombre, dirección de correo electrónico y contraseña. El registro abre una sesión activa de forma automática, sin necesidad de login posterior. |
| RF-02 | Un usuario registrado puede autenticarse con su email y contraseña. Si las credenciales son correctas, el sistema genera un token de sesión y lo devuelve al cliente. |
| RF-03 | Un usuario puede cerrar su sesión de forma explícita. La invalidación del token es inmediata y del lado del servidor: tras el logout, el token deja de ser válido aunque no haya expirado. |
| RF-04 | El sistema trata el email como identificador único normalizado: aplica recorte de espacios y conversión a minúsculas antes de cualquier búsqueda o almacenamiento, de modo que `Ana@Ejemplo.COM` y `ana@ejemplo.com` se consideran la misma identidad. |
| RF-05 | No pueden existir dos usuarios con el mismo email. Un intento de registro con un email ya registrado es rechazado con un error antes de persistir ningún dato. |

### Módulo de alimentos

| Código | Requisito |
|--------|-----------|
| RF-06 | El sistema mantiene un catálogo de alimentos compartido. Cualquier usuario puede consultar el catálogo completo o filtrar por nombre. |
| RF-07 | La búsqueda por nombre es parcial y no distingue mayúsculas de minúsculas: el texto `"poll"` encuentra `"Pollo a la plancha"`. |
| RF-08 | Cada alimento puede consultarse individualmente por su identificador. Si el identificador no existe, el sistema devuelve un error 404. |
| RF-09 | Se puede crear un alimento nuevo especificando nombre, porción de referencia en gramos, kilocalorías por 100 g, proteínas por 100 g, grasas por 100 g e hidratos de carbono por 100 g. La fuente de los datos nutricionales es un campo opcional. |
| RF-10 | Un alimento existente puede actualizarse completamente o eliminarse del catálogo. La actualización reemplaza todos sus campos; no existe actualización parcial. |

### Módulo de comidas

| Código | Requisito |
|--------|-----------|
| RF-11 | Un usuario puede registrar una comida para una fecha concreta asignándole un tipo de toma (por ejemplo, «DESAYUNO», «ALMUERZO» o «CENA»). El tipo se almacena normalizado en mayúsculas y no está restringido a una lista cerrada. |
| RF-12 | A una comida existente se le pueden añadir ítems. Cada ítem asocia un alimento del catálogo con una cantidad en gramos. No hay restricción que impida añadir el mismo alimento varias veces a la misma comida. |
| RF-13 | Al consultar los ítems de una comida, el sistema calcula y devuelve los valores nutricionales de cada ítem ponderados por los gramos registrados: kilocalorías, proteínas, grasas e hidratos de carbono estimados. El cálculo se realiza en la base de datos, no en el cliente. |
| RF-14 | Se pueden listar todas las comidas de un usuario para una fecha concreta, ordenadas por orden de creación. Si no hay comidas registradas en esa fecha, el resultado es una lista vacía. |
| RF-15 | Se pueden listar todos los ítems de una comida concreta con sus valores nutricionales calculados. El sistema verifica que la comida existe antes de ejecutar la consulta. |
| RF-16 | Una comida puede eliminarse. Al hacerlo, todos sus ítems se eliminan automáticamente por la restricción de clave foránea con borrado en cascada definida en la base de datos. |
| RF-17 | Un ítem concreto puede eliminarse sin eliminar la comida que lo contiene. El sistema verifica que el ítem pertenece a la comida indicada en la petición antes de borrarlo. |

### Módulo de resumen diario

| Código | Requisito |
|--------|-----------|
| RF-18 | El sistema calcula y devuelve el total de kilocalorías, proteínas, grasas e hidratos de carbono consumidos por un usuario en una fecha determinada, agregando todos los ítems de todas las comidas de ese día. |
| RF-19 | Si el usuario no tiene comidas registradas en la fecha consultada, el resumen devuelve cero en todos los campos nutricionales. No se produce un error ni una respuesta vacía. |
| RF-20 | La pantalla del diario en el cliente muestra el consumo calórico del día junto al TDEE del usuario cuando este está disponible, en el formato «kilocalorías consumidas / TDEE». Si el TDEE no ha podido cargarse en la sesión actual, se muestra solo el total consumido. |

### Módulo de perfil

| Código | Requisito |
|--------|-----------|
| RF-21 | Un usuario puede configurar su perfil biométrico indicando sexo, fecha de nacimiento, altura en centímetros, peso actual en kilogramos, nivel de actividad habitual y, opcionalmente, peso objetivo. |
| RF-22 | El sistema calcula la Tasa Metabólica Basal (TMB) y el Gasto Energético Total Diario (TDEE) a partir de los datos del perfil, aplicando la fórmula de Mifflin-St Jeor. El cálculo se realiza en el backend y el cliente recibe los valores ya calculados. |
| RF-23 | Un usuario puede actualizar su perfil biométrico en cualquier momento. La actualización reemplaza todos los campos del perfil y el sistema recalcula TMB y TDEE de forma automática. |
| RF-24 | Un usuario puede consultar su perfil actual junto a los valores calculados de TMB y TDEE. Si el identificador no existe, el sistema devuelve un error 404. |

### Módulo de ejercicios

| Código | Requisito |
|--------|-----------|
| RF-EJ-01 | El sistema mantiene un catálogo de ejercicios disponibles. Cualquier usuario autenticado puede consultarlo completo o filtrar por nombre. |
| RF-EJ-02 | Un usuario puede registrar una sesión de ejercicio indicando el tipo de ejercicio, la fecha y la duración en minutos. |
| RF-EJ-03 | El sistema calcula automáticamente las calorías quemadas en cada sesión aplicando la fórmula MET × peso_kg × (duración_min / 60), redondeando el resultado a dos decimales. |
| RF-EJ-04 | El peso utilizado en el cálculo se toma del perfil del usuario en el momento del registro y se persiste junto al resultado. Si el usuario no tiene perfil configurado, el registro es rechazado. |
| RF-EJ-05 | Un usuario puede eliminar un registro de ejercicio propio. El sistema verifica que el registro pertenece al usuario antes de borrarlo. |

---

## 3.3 Requisitos no funcionales

### Validación de entrada

| Código | Requisito |
|--------|-----------|
| RNF-01 | Todos los campos obligatorios de las peticiones de escritura se validan mediante anotaciones de Bean Validation antes de que la petición llegue a la capa de servicio. Si alguna restricción no se cumple, el sistema rechaza la petición con HTTP 400 sin ejecutar ninguna operación de negocio. |
| RNF-02 | Los valores numéricos de los alimentos (kilocalorías, macronutrientes, porción) no pueden ser negativos. La cantidad en gramos de cada ítem de comida debe ser mayor que cero. La altura del perfil debe estar entre 100 y 250 cm; el peso actual, por encima de 20 kg. La fecha de nacimiento debe ser anterior a la fecha actual. |

### Estructura de errores

| Código | Requisito |
|--------|-----------|
| RNF-03 | Todos los errores que produce el backend tienen la misma estructura JSON, con independencia del módulo o el tipo de excepción. La respuesta incluye siempre `timestamp`, `status`, `error`, `message` y `path`. Un `@RestControllerAdvice` centralizado garantiza esta uniformidad. |
| RNF-04 | Los mensajes de error de credenciales inválidas durante el login son deliberadamente iguales para el caso de email no encontrado y para el de contraseña incorrecta. Esto impide deducir mediante ensayo y error si un email está registrado en el sistema. |

### Persistencia y reproducibilidad

| Código | Requisito |
|--------|-----------|
| RNF-05 | El esquema de la base de datos está gestionado exclusivamente por Flyway. El código Java nunca crea ni modifica tablas. Al arrancar el backend, Flyway aplica las migraciones pendientes o valida la consistencia del esquema existente, garantizando que cualquier entorno produce el mismo estado de base de datos. |
| RNF-06 | Las contraseñas se almacenan exclusivamente como hash BCrypt. El texto en claro nunca se persiste ni se registra. La verificación durante el login compara la contraseña recibida contra el hash almacenado mediante `BCryptPasswordEncoder.matches()`. |
| RNF-07 | Los tokens de sesión son UUIDs v4 generados con `SecureRandom`. Se almacenan en la tabla `sesiones` con una fecha de expiración de siete días. Al hacer logout, la fila correspondiente se elimina de la base de datos. |

### Cliente no bloqueante

| Código | Requisito |
|--------|-----------|
| RNF-08 | Todas las operaciones de red del cliente JavaFX se ejecutan en hilos de fondo mediante `javafx.concurrent.Task`. El hilo de la interfaz gráfica nunca queda bloqueado esperando la respuesta del backend. Los resultados y los errores se devuelven al hilo de la UI a través de los callbacks `setOnSucceeded` y `setOnFailed`. |

### Pruebas

| Código | Requisito |
|--------|-----------|
| RNF-09 | Los tests unitarios del backend se ejecutan sin base de datos activa y sin levantar el contexto de Spring. Los repositorios se sustituyen por mocks de Mockito, lo que permite probar la lógica de servicio de forma aislada y con tiempos de ejecución reducidos. |
| RNF-10 | La lógica de negocio de cada módulo —validaciones de dominio, transformaciones, cálculos— está cubierta por tests unitarios. Los repositorios, al ser envolturas directas sobre SQL, se verifican mediante pruebas manuales de la API. |

---

## 3.4 Alcance del MVP y exclusiones conocidas

El MVP cubre los módulos descritos en los requisitos anteriores: autenticación, alimentos, comidas, resumen diario, perfil y ejercicios. Las siguientes funcionalidades no están implementadas en la versión actual y se reconocen como limitaciones conocidas:

**Ausencia de HTTPS.** La comunicación entre el cliente y el backend se realiza sobre HTTP plano. En el contexto actual —ambos procesos en la misma máquina— el tráfico no sale de la interfaz de loopback. En cualquier despliegue en red, los tokens y las credenciales viajarían sin cifrar.

**Catálogo de alimentos compartido.** No existe separación entre usuarios dentro del catálogo: cualquier usuario puede crear, modificar o eliminar cualquier alimento. En el alcance del MVP se asume que el catálogo es un recurso compartido y de confianza.

**Umbral mínimo de contraseña.** La restricción `@Size(min = 6)` en `RegisterRequest` establece un límite bajo respecto a los estándares habituales en aplicaciones en producción. Se mantiene así para simplificar las pruebas durante el desarrollo.

**Control de acceso por roles.** No existe diferenciación de roles entre usuarios. Todos los usuarios autenticados tienen el mismo nivel de acceso a todas las operaciones del sistema.
