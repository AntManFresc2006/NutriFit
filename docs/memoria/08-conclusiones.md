# 8. Conclusiones

## 8.1 Valoración del proyecto

NutriFit es una aplicación de escritorio funcional que permite registrar alimentos, crear comidas, agregar su contenido nutricional por día y calcular el gasto energético estimado a partir del perfil biométrico del usuario. Todo ello se sostiene sobre una arquitectura de tres capas —cliente JavaFX, backend Spring Boot y base de datos MariaDB— en la que cada componente tiene responsabilidades delimitadas y se comunica con el siguiente a través de una interfaz bien definida.

El sistema no es un prototipo de interfaz ni una simulación: los seis módulos del backend están implementados, exponen endpoints REST verificados y responden a las peticiones del cliente con datos reales almacenados en base de datos. El esquema está gestionado por Flyway y es reproducible desde cero en cualquier entorno. Los 60 tests unitarios del backend cubren la lógica de negocio relevante y pasan sin base de datos ni contexto de Spring activo.

Dicho esto, el MVP tiene limitaciones conocidas y documentadas, recogidas en §3.4 y §7.3.

## 8.2 Decisiones técnicas clave

NutriFit se sostiene en decisiones concretas de arquitectura:

- **JDBC directo sin ORM**: las consultas SQL están a la vista, son trazables y predecibles.
- **Token opaco en BD**: el logout es inmediato. Con cada petición se valida el token en la BD, no con secretos en memoria.
- **Agregación en SQL**: el resumen diario se calcula en una sola consulta, no trayendo datos a Java.
- **GlobalExceptionHandler**: todas las excepciones devuelven la misma estructura JSON, sin sorpresas.

Cada decisión aparece en un ADR o en su sección correspondiente, con sus tradeoffs. No son convenciones por defecto; son elecciones razonadas.

El backend es independiente del cliente. Puede arrancarse, probarse y depurarse solo. El cliente es intercambiable; cualquier consumidor HTTP que respete el contrato obtendrá el mismo comportamiento.

## 8.3 Aprendizajes

**Flyway y migraciones.** Pensar el esquema como explícito, versionado, no como efecto secundario del ORM. Cada cambio es un script numerado. El estado de la BD es reproducible.

**Tests sin infraestructura.** Interfaces de repositorio permiten mocks. Mockito testea la lógica sin BD ni Spring. Fue una decisión consciente para testear.

**Concurrencia en JavaFX.** El hilo UI nunca debe bloquearse esperando red. `Task` corre en background; solo `setOnSucceeded` y `setOnFailed` tocan la UI. No es opcional.

**Tradeoffs visibles.** JDBC directo es más repetitivo que JPA. Token opaco cuesta una query por request. HandlerInterceptor propio es más manual que Spring Security. Documentar los costos, no solo beneficios.

## 8.4 Limitaciones conocidas

- **HTTP plano, sin HTTPS.** Funciona en local. No funciona en red.
- **Catálogo compartido.** Todos los usuarios comparten alimentos. Cualquiera puede borrar lo que creó otro.

Se documentan dónde corresponde. No son sorpresas ocultas; son límites del MVP.

## 8.5 Próximos pasos

**HTTPS.** Spring Boot soporta keystores JKS o PKCS12. Necesario antes de sacar la app de local.

Lo que no está en esta lista: móvil, integración con APIs externas grandes, análisis complejos. Excedería el alcance.

## 8.6 Resumen

NutriFit funciona. Sus decisiones están documentadas, sus límites están claros. Los tests pasan. No es perfecto, pero es razonable.

El punto no era construir una app de producción, sino demostrar decisiones técnicas coherentes y documentarlas sin adornos. Eso es lo que aquí aparece.
