# 1. Introducción

## 1.1 Contexto

El seguimiento de la alimentación es una práctica habitual en contextos de salud, deporte y gestión del peso. Registrar qué se come, cuánto y cuándo permite a una persona tener información objetiva sobre su ingesta calórica y su distribución de macronutrientes a lo largo del día. Sin esa información, cualquier ajuste dietético se basa en estimaciones subjetivas.

Existen herramientas comerciales que cubren esta necesidad —aplicaciones móviles, plataformas web—, pero su complejidad interna queda oculta al usuario y, por tanto, al desarrollador que quiera aprender de ellas. NutriFit parte de esa misma necesidad funcional y la aborda como ejercicio de ingeniería: construir desde cero un sistema multicapa que gestione alimentos, registre comidas, calcule resúmenes nutricionales diarios, registre ejercicios y calcule las calorías quemadas mediante factor MET, y estime el gasto energético del usuario, con decisiones técnicas razonadas y documentadas en cada paso.

## 1.2 Problema que aborda el proyecto

El sistema pretende dar respuesta a una necesidad concreta: que un usuario pueda registrar los alimentos que consume durante el día, organizarlos por comidas, y obtener un resumen del total calórico y de macronutrientes consumidos, junto a una estimación de cuántas calorías necesita en función de su perfil biométrico.

Este problema no es técnicamente complejo en su enunciado, pero su implementación involucra decisiones de diseño no triviales: cómo modelar la relación entre comidas e ítems, cómo calcular los valores nutricionales de forma precisa sin mover datos innecesariamente entre capas, cómo gestionar sesiones de usuario con logout real, o cómo garantizar que el esquema de base de datos es reproducible en cualquier entorno. Esas decisiones son el centro del trabajo.

## 1.3 Objetivo del TFG

El objetivo principal es diseñar, implementar y documentar una aplicación web funcional para el seguimiento nutricional diario, organizada en una arquitectura de tres capas con interfaz React, backend Spring Boot y base de datos PostgreSQL.

El objetivo no es construir una aplicación de producción lista para usuarios finales, sino demostrar la capacidad de tomar decisiones técnicas justificadas, implementarlas de forma coherente dentro de un alcance definido, y documentar el resultado con precisión. Esto incluye reconocer las limitaciones del MVP y distinguir con claridad qué está hecho, qué queda fuera del alcance y por qué.

## 1.4 Alcance del sistema

NutriFit se desarrolla en su versión completa, con dieciocho módulos funcionales íntegramente implementados:

- **Autenticación**: registro, login y logout con invalidación inmediata del token en el servidor.
- **Catálogo de alimentos**: creación, consulta, búsqueda por nombre, actualización y eliminación de alimentos con sus valores nutricionales por 100 gramos.
- **Registro de comidas**: creación de comidas asociadas a una fecha y tipo de toma, con adición y eliminación de ítems que relacionan alimentos con cantidades en gramos.
- **Resumen diario**: agregación del total de kilocalorías, proteínas, grasas e hidratos de carbono consumidos en una fecha, calculado directamente en base de datos.
- **Perfil biométrico**: almacenamiento de los datos del usuario y cálculo automático de la Tasa Metabólica Basal (TMB) y el Gasto Energético Total Diario (TDEE) mediante la fórmula de Mifflin-St Jeor.
- **Registro de ejercicios**: registro de sesiones de actividad física con cálculo automático de calorías quemadas mediante la fórmula MET × peso_kg × (duración_min / 60).
- **Historial de peso**: registro y visualización del histórico de cambios de peso del usuario.
- **Hidratación**: registro de ingesta de agua y visualización de historial diario de hidratación.
- **Plan semanal (IA)**: generación de planes nutricionales personalizados mediante integración con OpenRouter API.
- **Retos y gamificación**: sistema de desafíos puntuables para incentivar hábitos saludables.
- **Evaluación con IA**: análisis y feedback de hábitos nutricionales mediante OpenRouter API.
- **Escaneo de códigos de barras**: integración con OpenFoodFacts para identificar alimentos mediante códigos de barras.
- **Lista de compra**: gestión de lista de compras con sugerencias automatizadas mediante IA.
- **Configuración de IA**: panel de usuario para configurar parámetros de integración con IA (API key, modelo, proxy URL).
- **Tendencias**: visualización gráfica de evolución de peso, consumo calórico y otros indicadores.
- **Peso e historial**: gestión del historial completo de pesos registrados.
- **Ejercicios tipo**: catálogo completo de tipos de ejercicios con factores MET configurables.
- **Información de usuario**: acceso a datos de perfil y preferencias.

La implementación está desplegada en producción: el backend en Render y la interfaz de usuario en Vercel, ambos con HTTPS, autenticación funcional y acceso público seguro.

## 1.5 Arquitectura y tecnologías

El sistema se organiza en tres capas independientes. El cliente web, implementado con React 18 + TypeScript + Vite + Tailwind CSS, actúa como single-page application y se comunica con el backend mediante peticiones HTTP. El backend, implementado con Spring Boot 3, expone una API REST sobre PostgreSQL bajo un esquema gestionado íntegramente por Flyway (22 migraciones versionadas).

El acceso a datos se realiza con `JdbcTemplate` y `RowMapper` manuales, sin ORM. La autenticación usa tokens opacos UUID almacenados en la tabla `sesiones`, con expiración a siete días y borrado explícito en logout. Un `HandlerInterceptor` valida el token en todos los endpoints protegidos. La comunicación HTTPS está garantizada por los proveedores de hosting (Render y Vercel). La integración de IA se realiza mediante OpenRouter API con configuración por usuario (modelo, API key, proxy URL opcional). El manejo de errores está centralizado en un `@RestControllerAdvice` que garantiza una estructura de respuesta uniforme para cualquier tipo de excepción. Estas y otras decisiones de diseño se desarrollan en §4 con sus alternativas consideradas.

La arquitectura y su justificación detallada se desarrollan en §4.1. Las tecnologías concretas utilizadas, con sus versiones y el papel que desempeñan, se recogen en §2.

## 1.6 Organización de la memoria

La memoria está estructurada en ocho secciones que siguen el desarrollo del proyecto de forma progresiva:

- **§2 — Tecnologías y herramientas**: las bibliotecas, frameworks y herramientas utilizadas, con la justificación de su elección.
- **§3 — Análisis de requisitos**: los requisitos funcionales y no funcionales del sistema, organizados por módulo, con la especificación completa de los dieciocho módulos implementados.
- **§4 — Diseño**: la arquitectura del sistema (§4.1), el esquema de base de datos (§4.2) y las decisiones de diseño más relevantes.
- **§5 — Implementación**: descripción módulo a módulo de la implementación del backend y del cliente frontend, incluyendo autenticación, alimentos, comidas, resumen diario, perfil, ejercicios, hidratación, IA, gamificación y el resto de módulos.
- **§6 — Pruebas**: la estrategia de pruebas, tests unitarios del backend y la verificación manual de los endpoints mediante archivos `.http` y Swagger UI.
- **§7 — Seguridad**: las medidas implementadas —hashing BCrypt, token opaco UUID, normalización de email, interceptor de autenticación— y las garantías en producción.
- **§8 — Conclusiones**: valoración del resultado, aprendizajes técnicos de arquitectura web, despliegue en producción y líneas de trabajo futuras.

Los anexos incluyen la guía de puesta en marcha del proyecto (anexo A), la referencia completa de endpoints de la API (anexo B) y la especificación de configuración para consumir OpenRouter API (anexo C).
