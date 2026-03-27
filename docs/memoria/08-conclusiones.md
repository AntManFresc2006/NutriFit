# 8. Conclusiones

## 8.1 Valoración del proyecto

NutriFit es una aplicación de escritorio funcional que permite registrar alimentos, crear comidas, agregar su contenido nutricional por día y calcular el gasto energético estimado a partir del perfil biométrico del usuario. Todo ello se sostiene sobre una arquitectura de tres capas —cliente JavaFX, backend Spring Boot y base de datos MariaDB— en la que cada componente tiene responsabilidades delimitadas y se comunica con el siguiente a través de una interfaz bien definida.

El sistema no es un prototipo de interfaz ni una simulación: los cinco módulos del backend están implementados, exponen endpoints REST verificados y responden a las peticiones del cliente con datos reales almacenados en base de datos. El esquema está gestionado por Flyway y es reproducible desde cero en cualquier entorno. Los 46 tests unitarios del backend cubren la lógica de negocio relevante y pasan sin base de datos ni contexto de Spring activo.

Dicho esto, el MVP tiene limitaciones conocidas y documentadas, recogidas en §3.4 y §7.3.

## 8.2 Aportación del sistema

Dentro del alcance de un TFG, NutriFit demuestra que es posible construir un sistema multicapa cohesionado partiendo de decisiones técnicas justificadas. Las principales son: acceso a datos con `JdbcTemplate` sin ORM, lo que mantiene las consultas SQL visibles y trazables; autenticación con token opaco en base de datos, que hace el logout inmediato y real; cálculo del resumen diario mediante una única consulta SQL con agregación, en lugar de traer los datos a memoria y procesarlos en Java; y manejo de errores centralizado que garantiza una estructura de respuesta uniforme para cualquier tipo de excepción.

Cada una de estas decisiones tiene un ADR de referencia o una sección dedicada en la memoria. No se tomaron como convenciones por defecto, sino como elecciones razonadas frente a alternativas concretas.

La separación entre cliente y backend tiene también una consecuencia verificable: el backend puede arrancarse, probarse y depurarse de forma completamente independiente del cliente. El cliente es intercambiable desde el punto de vista del servidor; cualquier otro consumidor que respete el contrato HTTP obtendrá el mismo comportamiento.

## 8.3 Aprendizajes técnicos

El desarrollo de NutriFit ha implicado trabajar con tecnologías y patrones que en muchos casos no se estudian en detalle durante el grado:

**Gestión del esquema con migraciones versionadas.** El uso de Flyway obliga a pensar el esquema como algo explícito y versionado, no como un efecto secundario del modelo de datos. Cada cambio requiere un script numerado; el estado de la base de datos en cualquier entorno es predecible y reproducible.

**Tests unitarios sin infraestructura.** Diseñar los servicios con interfaces de repositorio, en lugar de clases concretas acopladas a `JdbcTemplate`, fue una decisión que se tomó en parte para facilitar las pruebas. Los tests con Mockito son el resultado de esa elección, no su punto de partida.

**Concurrencia en JavaFX.** La regla de que el hilo de la UI no puede bloquearse esperando una respuesta de red no es opcional en JavaFX: viola internamente las garantías del toolkit. Usar `Task` correctamente —delegando la operación al hilo de fondo y actualizando la interfaz solo en los callbacks `setOnSucceeded` y `setOnFailed`— requirió entender cómo funciona el modelo de hilos de JavaFX, no solo aplicar un patrón.

**Decisiones con consecuencias.** Trabajar con `JdbcTemplate` implica escribir más código repetitivo que con JPA. Usar un token opaco implica una consulta a base de datos por cada petición autenticada. No usar Spring Security completo simplifica la configuración; el control de acceso se cubre con un `HandlerInterceptor` propio que valida el token en todos los endpoints protegidos. Documentar estas consecuencias, y no solo las ventajas, es parte del aprendizaje.

## 8.4 Limitaciones del MVP

Las limitaciones del sistema se recogen con detalle en los capítulos correspondientes. Las más relevantes desde el punto de vista de la completitud del sistema son:

- **Ausencia de HTTPS.** La comunicación entre cliente y backend se realiza sobre HTTP plano. Esto es aceptable mientras ambos procesos corren en la misma máquina, pero no en ningún despliegue en red (§3.4).
- **Catálogo de alimentos sin control de acceso por usuario.** Cualquier usuario puede crear, modificar o eliminar cualquier alimento del catálogo compartido (§3.4).

Estas limitaciones son consecuencia del alcance definido para el MVP, no de errores de diseño. La arquitectura no las oculta: se documentan en los capítulos que las originan.

## 8.5 Líneas futuras de trabajo

Las extensiones más directas del proyecto son aquellas que completan funcionalidades ya parcialmente presentes:

**Configurar HTTPS en el backend.** Spring Boot soporta HTTPS mediante un keystore JKS o PKCS12. Esta mejora sería necesaria antes de cualquier despliegue accesible desde otra máquina.

No se incluyen aquí funcionalidades que supondrían un cambio de alcance significativo —aplicación móvil, integración con bases de datos de alimentos externas, análisis de tendencias— porque exceden lo que la arquitectura actual tiene implementado como base.

## 8.6 Cierre

NutriFit es un sistema funcional dentro de su alcance definido. Sus decisiones de diseño están documentadas, sus limitaciones están identificadas y sus pruebas cubren la lógica de negocio relevante del backend. No es un sistema terminado en todos sus aspectos, pero sí es un sistema del que se puede razonar con precisión: qué hace, qué no hace y por qué.

El objetivo del TFG no era construir una aplicación de producción, sino demostrar la capacidad de tomar decisiones técnicas razonadas, implementarlas de forma coherente y documentar el resultado con honestidad. Eso es lo que esta memoria pretende reflejar.
