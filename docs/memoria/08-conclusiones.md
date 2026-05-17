# 8. Conclusiones

## 8.1 Valoración del proyecto

NutriFit es una aplicación web funcional que permite registrar alimentos, crear comidas, registrar ejercicio, hacer seguimiento de hidratación, recibir evaluaciones con IA, generar planes semanales, crear listas de compra, registrar peso y analizar tendencias nutricionales. Todo ello se sostiene sobre una arquitectura moderna de tres capas —frontend React, backend Spring Boot y base de datos PostgreSQL— con despliegue en la nube (Vercel + Render).

El sistema no es un prototipo: los 18 módulos del backend están implementados, exponen endpoints REST verificados y responden a las peticiones del cliente con datos reales almacenados en base de datos. El esquema está gestionado por Flyway y es reproducible desde cero en cualquier entorno. Los 60 tests unitarios del backend cubren la lógica de negocio relevante y pasan sin base de datos ni contexto de Spring activo.

La integración con IA mediante OpenRouter (configurable por usuario o con valores por defecto del servidor) permite evaluaciones personalizadas. El escáner de código de barras integra OpenFoodFacts API para búsqueda rápida de alimentos.

Dicho esto, el MVP tiene limitaciones conocidas y documentadas, recogidas en §3.4 y §7.

## 8.2 Decisiones técnicas clave

NutriFit se sostiene en decisiones concretas de arquitectura:

- **JDBC directo sin ORM**: las consultas SQL están a la vista, son trazables y predecibles.
- **Token opaco en BD**: el logout es inmediato. Con cada petición se valida el token en la BD, no con secretos en memoria.
- **Agregación en SQL**: el resumen diario se calcula en una sola consulta, no trayendo datos a Java.
- **GlobalExceptionHandler**: todas las excepciones devuelven la misma estructura JSON, sin sorpresas.
- **IA configurable por usuario**: cada usuario puede usar su propia clave API y modelo, o utilizar valores por defecto del servidor. El servicio implementa fallback robusto.
- **OpenFoodFacts para escáner**: integración directa del API público, sin autenticación requerida, para búsqueda rápida de alimentos por código de barras.

Cada decisión aparece en un ADR o en su sección correspondiente, con sus tradeoffs. No son convenciones por defecto; son elecciones razonadas.

El backend es independiente del cliente. Puede arrancarse, probarse y depurarse solo. El cliente es intercambiable; cualquier consumidor HTTP que respete el contrato obtendrá el mismo comportamiento.

## 8.3 Aprendizajes

**Flyway y migraciones.** Pensar el esquema como explícito, versionado, no como efecto secundario del ORM. Cada cambio es un script numerado. El estado de la BD es reproducible.

**Tests sin infraestructura.** Interfaces de repositorio permiten mocks. Mockito testea la lógica sin BD ni Spring. Fue una decisión consciente para testear.

**React con TypeScript y Vite.** Desarrollo rápido con hot reload. Tailwind CSS para estilos consistentes. Framer Motion para animaciones sutiles que mejoran la experiencia sin distracciones.

**Tradeoffs visibles.** JDBC directo es más repetitivo que JPA. Token opaco cuesta una query por request. HandlerInterceptor propio es más manual que Spring Security. Documentar los costos, no solo beneficios.

## 8.4 Limitaciones conocidas

- **Almacenamiento de claves API en texto plano.** Las claves API de OpenRouter se almacenan sin cifrado. En producción, deberían cifrarse o usar un servicio de gestión de secretos.
- **Catálogo compartido.** Todos los usuarios comparten alimentos y ejercicios. Cualquiera puede borrar lo que creó otro.
- **Sin autenticación en endpoints de lectura pública.** El escáner de código de barras no requiere token de autenticación (necesario para búsquedas rápidas).

Se documentan dónde corresponde. No son sorpresas ocultas; son límites del MVP.

## 8.5 Próximos pasos

**Cifrado de secretos.** Las claves API deberían cifrarse en base de datos o almacenarse en un servicio externo como AWS Secrets Manager.

**Catálogos privados.** Permitir que usuarios creen y gestionen sus propios alimentos y ejercicios, opcionalmente compartibles.

**Análisis predictivo.** Extender las tendencias con previsiones basadas en el comportamiento histórico del usuario.

Lo que no está en esta lista: aplicación móvil nativa, integración con sensores biométricos, sincronización en tiempo real. Excedería el alcance del MVP.

## 8.6 Resumen

NutriFit funciona. Implementa 18 módulos con arquitectura clara, despliegue en la nube, integración con IA, y análisis nutritivo completo. Sus decisiones están documentadas, sus límites están claros. Los 60 tests pasan sin base de datos. La interfaz es responsiva y fluida.

El punto no era construir una app de producción corporativa, sino demostrar decisiones técnicas coherentes, documentarlas sin adornos, e implementar funcionalidad real. Eso es lo que aquí aparece.
