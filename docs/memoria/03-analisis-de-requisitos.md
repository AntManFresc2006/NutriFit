# 3. Análisis de requisitos

## 3.1 Introducción

Esta sección recoge los requisitos que delimitan el alcance de NutriFit en su versión completa de dieciocho módulos funcionales implementados. Los requisitos no se formularon en una fase de análisis previa al desarrollo, sino que emergieron de forma iterativa a medida que los módulos se definían e implementaban y se integraban con APIs externas (OpenRouter para IA, OpenFoodFacts para escaneo). El objetivo de documentarlos aquí es hacer explícito qué hace el sistema, qué restricciones de calidad cumple y las características de despliegue en producción.

Los requisitos funcionales se agrupan por módulo para facilitar su lectura junto a las secciones §5, donde se describe la implementación de cada uno. Los requisitos no funcionales recogen propiedades transversales que afectan al sistema en su conjunto: validación, HTTPS en Render y Vercel, interceptor de autenticación, y gestión de errores uniforme.

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
| RF-EJ-01 | El sistema mantiene un catálogo de ejercicios disponibles con sus factores MET. Cualquier usuario autenticado puede consultarlo completo o filtrar por nombre. |
| RF-EJ-02 | Un usuario puede registrar una sesión de ejercicio indicando el tipo de ejercicio, la fecha y la duración en minutos. |
| RF-EJ-03 | El sistema calcula automáticamente las calorías quemadas en cada sesión aplicando la fórmula MET × peso_kg × (duración_min / 60), redondeando el resultado a dos decimales. |
| RF-EJ-04 | El peso utilizado en el cálculo se toma del perfil del usuario en el momento del registro y se persiste junto al resultado. Si el usuario no tiene perfil configurado, el registro es rechazado. |
| RF-EJ-05 | Un usuario puede eliminar un registro de ejercicio propio. El sistema verifica que el registro pertenece al usuario antes de borrarlo. |
| RF-EJ-06 | El sistema permite registrar sesiones de ejercicio con diferentes valores de intensidad anaeróbica, opcionalmente. |

### Módulo de historial de peso

| Código | Requisito |
|--------|-----------|
| RF-HP-01 | Un usuario puede registrar un pesaje en una fecha concreta con el peso actual en kilogramos. |
| RF-HP-02 | El sistema mantiene un historial completo de los pesos registrados por cada usuario, ordenado por fecha. |
| RF-HP-03 | Un usuario puede consultar el histórico de pesos con la fecha y el peso registrado en cada ocasión. |
| RF-HP-04 | Un usuario puede eliminar un registro de peso. El sistema verifica que el registro pertenece al usuario antes de borrarlo. |

### Módulo de hidratación

| Código | Requisito |
|--------|-----------|
| RF-HID-01 | Un usuario puede registrar el consumo de agua indicando el volumen en mililitros y la fecha/hora. |
| RF-HID-02 | El sistema mantiene el registro completo de consumo de agua por usuario y fecha. |
| RF-HID-03 | Un usuario puede consultar el resumen diario de hidratación: total de ml consumidos y número de registros. |
| RF-HID-04 | Un usuario puede eliminar un registro de hidratación individual. |

### Módulo de plan semanal (IA)

| Código | Requisito |
|--------|-----------|
| RF-PS-01 | Si el usuario ha configurado OpenRouter, puede solicitar un plan nutricional semanal. El sistema invoca OpenRouter con los datos del usuario (edad, peso, altura, actividad, objetivos). |
| RF-PS-02 | El plan semanal generado por IA se almacena en la base de datos con la fecha de generación. Un usuario puede tener múltiples planes históricos. |
| RF-PS-03 | Un usuario puede consultar el plan semanal más reciente o cualquiera de sus planes anteriores. |
| RF-PS-04 | Si la configuración de IA no está disponible, el sistema devuelve un error controlado indicando que debe configurarse OpenRouter. |

### Módulo de retos y gamificación

| Código | Requisito |
|--------|-----------|
| RF-RETO-01 | El sistema mantiene un catálogo de retos disponibles, cada uno con un nombre, descripción y puntos asociados. |
| RF-RETO-02 | Un usuario autenticado puede asignarse un reto activo. El sistema registra la fecha de asignación. |
| RF-RETO-03 | Un usuario puede marcar un reto como completado. El sistema registra la fecha de completación y suma los puntos al score total del usuario. |
| RF-RETO-04 | Un usuario puede consultar su lista de retos activos, completados y sus puntos acumulados. |
| RF-RETO-05 | Un usuario puede abandonar un reto activo sin penalización. |

### Módulo de lista de compra

| Código | Requisito |
|--------|-----------|
| RF-LC-01 | Un usuario puede crear una lista de compra y añadirle ítems especificando alimento y cantidad recomendada. |
| RF-LC-02 | Si el usuario ha configurado OpenRouter, puede solicitar sugerencias automáticas de compra basadas en sus planes y comidas. |
| RF-LC-03 | Un usuario puede marcar un ítem como comprado sin eliminarlo de la lista. |
| RF-LC-04 | Un usuario puede eliminar un ítem de la lista de compra. |
| RF-LC-05 | Un usuario puede consultar su lista actual, ordenada por estado (pendiente, comprado). |

### Módulo de escaneo (códigos de barras)

| Código | Requisito |
|--------|-----------|
| RF-ESC-01 | El cliente frontend permite capturar un código de barras (EAN) mediante la cámara del dispositivo. |
| RF-ESC-02 | El backend consulta OpenFoodFacts con el EAN para obtener información nutricional. Si el producto existe, devuelve nombre, kcal, proteínas, grasas y carbohidratos. |
| RF-ESC-03 | Si el producto no se encuentra en OpenFoodFacts, el sistema devuelve un error indicando que no hay información disponible. |
| RF-ESC-04 | El usuario puede registrar el alimento escaneado directamente en su diario. |

### Módulo de configuración de IA

| Código | Requisito |
|--------|-----------|
| RF-IA-CFG-01 | Un usuario puede configurar su integración personal con OpenRouter: URL del proxy, modelo de IA, y API key. |
| RF-IA-CFG-02 | La configuración se almacena en la tabla `usuario_ia_config` cifrada (si procede) o en texto plano según requisitos de seguridad. |
| RF-IA-CFG-03 | Un usuario puede actualizar su configuración de IA en cualquier momento. |
| RF-IA-CFG-04 | Un usuario puede consultar su configuración actual (sin mostrar el API key completo por seguridad). |
| RF-IA-CFG-05 | Si un usuario no ha configurado IA, las operaciones que la requieren (plan semanal, sugerencias) devuelven error con instrucciones para configurar OpenRouter. |

### Módulo de evaluación con IA

| Código | Requisito |
|--------|-----------|
| RF-EVAL-IA-01 | Si el usuario ha configurado OpenRouter, puede solicitar una evaluación de sus hábitos nutricionales en un período (últimos 7 días, 30 días, etc.). |
| RF-EVAL-IA-02 | El backend invoca OpenRouter con el resumen de comidas, ejercicios y métricas del período, solicitando análisis y feedback personalizado. |
| RF-EVAL-IA-03 | La evaluación se devuelve como texto libre generado por el modelo de IA. |
| RF-EVAL-IA-04 | El usuario puede solicitar múltiples evaluaciones en diferentes períodos. |

### Módulo de tendencias

| Código | Requisito |
|--------|-----------|
| RF-TEND-01 | El sistema calcula y visualiza gráficamente la evolución del peso del usuario a lo largo del tiempo. |
| RF-TEND-02 | El sistema visualiza la evolución del consumo calórico promedio (últimos 7, 30, 90 días). |
| RF-TEND-03 | El sistema visualiza la evolución del ejercicio (calorías quemadas totales por semana). |
| RF-TEND-04 | Todos los gráficos son responsivos y se adaptan a mobile, tablet y desktop. |

### Módulo de información de usuario

| Código | Requisito |
|--------|-----------|
| RF-INFO-01 | Un usuario puede consultar sus datos personales (nombre, email, fecha de registro). |
| RF-INFO-02 | Un usuario puede actualizar su nombre y preferencias. |
| RF-INFO-03 | Un usuario puede ver su score total de puntos de retos. |

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

### Frontend responsivo y no bloqueante

| Código | Requisito |
|--------|-----------|
| RNF-08 | El frontend React es una single-page application responsiva que funciona en mobile, tablet y desktop. |
| RNF-08b | Todas las operaciones de red del frontend se ejecutan de forma asincrónica mediante Promises o async/await. La interfaz gráfica nunca queda bloqueada esperando respuesta del backend. |
| RNF-08c | El frontend muestra indicadores de carga durante las peticiones de red (spinners, skeletons). |
| RNF-08d | Los errores de red o de validación se muestran al usuario en mensajes claros sin romper el flujo de la interfaz. |

### Pruebas y HTTPS en producción

| Código | Requisito |
|--------|-----------|
| RNF-09 | Los tests unitarios del backend se ejecutan sin base de datos activa y sin levantar el contexto de Spring. Los repositorios se sustituyen por mocks de Mockito, lo que permite probar la lógica de servicio de forma aislada y con tiempos de ejecución reducidos. |
| RNF-10 | La lógica de negocio de cada módulo —validaciones de dominio, transformaciones, cálculos— está cubierta por tests unitarios. Los repositorios, al ser envolturas directas sobre SQL, se verifican mediante pruebas manuales de la API. |
| RNF-11 | El backend en Render y el frontend en Vercel cuentan con HTTPS automático mediante certificados Let's Encrypt. La comunicación entre capas y la transmisión de credenciales está protegida por TLS/SSL. |
| RNF-12 | CORS está configurado en el backend para permitir peticiones desde el dominio del frontend en Vercel (y desde localhost:5173 en desarrollo local). |
| RNF-13 | La sesión HTTP es sin estado del lado del backend: el servidor no mantiene sesiones. La autenticación se realiza únicamente mediante el token UUID enviado en el header `Authorization`. |

---

## 3.4 Características de seguridad en producción

La versión completa de NutriFit implementada y desplegada en Render (backend) y Vercel (frontend) incluye:

**HTTPS en ambas capas.** El backend en Render y el frontend en Vercel cuentan con HTTPS automático mediante certificados Let's Encrypt. La comunicación entre capas se realiza siempre de forma cifrada.

**Autenticación por token opaco UUID.** El token de sesión es un UUID v4 generado con `SecureRandom`, almacenado en la tabla `sesiones` con expiración a siete días. Un `HandlerInterceptor` valida el token en cada petición protegida.

**Catálogo de alimentos compartido.** El catálogo es un recurso compartido entre todos los usuarios. Se asume que es un recurso de confianza; cualquier usuario puede crear, modificar o eliminar alimentos.

**Control de acceso a datos por usuario.** Los datos personales (comidas, ejercicios, perfil, peso histórico, etc.) están protegidos: cada usuario solo puede acceder a sus propios datos. El backend valida la pertenencia del recurso al usuario autenticado antes de devolver o modificar datos.

**Integración segura con IA.** La API key de OpenRouter se configura a nivel de usuario y se envía de forma segura (en POST body o header personalizado según configuración). El proxy opcional es configurable para permitir instancias privadas de OpenRouter.

## 3.5 Exclusiones deliberadas

**Control de acceso por roles.** No existe diferenciación de roles entre usuarios (ej: admin, moderador, usuario). Todos los usuarios autenticados tienen el mismo nivel de acceso a operaciones sobre sus propios datos.

**Control granular de permisos sobre el catálogo de alimentos.** El catálogo es completamente abierto: cualquier usuario autenticado puede modificarlo o eliminarlo. En un entorno multi-tenant de producción, esto requerería capas adicionales de autorización.

**Validación avanzada de datos nutricionales.** No hay validación en tiempo de inserción que verifique la plausibilidad de los valores nutricionales (ej: detectar valores imposibles). Se confía en que los usuarios ingresan datos razonables.
