# 1. Introducción

## 1.1 Contexto

El seguimiento de la alimentación es una práctica habitual en contextos de salud, deporte y gestión del peso. Registrar qué se come, cuánto y cuándo permite a una persona tener información objetiva sobre su ingesta calórica y su distribución de macronutrientes a lo largo del día. Sin esa información, cualquier ajuste dietético se basa en estimaciones subjetivas.

Existen herramientas comerciales que cubren esta necesidad —aplicaciones móviles, plataformas web—, pero su complejidad interna queda oculta al usuario y, por tanto, al desarrollador que quiera aprender de ellas. NutriFit parte de esa misma necesidad funcional y la aborda como ejercicio de ingeniería: construir desde cero un sistema multicapa que gestione alimentos, registre comidas, calcule resúmenes nutricionales diarios y estime el gasto energético del usuario, con decisiones técnicas razonadas y documentadas en cada paso.

## 1.2 Problema que aborda el proyecto

El sistema pretende dar respuesta a una necesidad concreta: que un usuario pueda registrar los alimentos que consume durante el día, organizarlos por comidas, y obtener un resumen del total calórico y de macronutrientes consumidos, junto a una estimación de cuántas calorías necesita en función de su perfil biométrico.

Este problema no es técnicamente complejo en su enunciado, pero su implementación involucra decisiones de diseño no triviales: cómo modelar la relación entre comidas e ítems, cómo calcular los valores nutricionales de forma precisa sin mover datos innecesariamente entre capas, cómo gestionar sesiones de usuario con logout real, o cómo garantizar que el esquema de base de datos es reproducible en cualquier entorno. Esas decisiones son el centro del trabajo.

## 1.3 Objetivo del TFG

El objetivo principal es diseñar, implementar y documentar un sistema de escritorio funcional para el seguimiento nutricional diario, organizado en una arquitectura de tres capas con cliente JavaFX, backend Spring Boot y base de datos MariaDB.

El objetivo no es construir una aplicación de producción lista para usuarios finales, sino demostrar la capacidad de tomar decisiones técnicas justificadas, implementarlas de forma coherente dentro de un alcance definido, y documentar el resultado con precisión. Esto incluye reconocer las limitaciones del MVP y distinguir con claridad qué está hecho, qué queda fuera del alcance y por qué.

## 1.4 Alcance del sistema

NutriFit se desarrolla en su primera versión funcional, denominada MVP. El sistema cubre cinco áreas funcionales:

- **Autenticación**: registro, login y logout con invalidación inmediata del token en el servidor.
- **Catálogo de alimentos**: creación, consulta, búsqueda por nombre, actualización y eliminación de alimentos con sus valores nutricionales por 100 gramos.
- **Registro de comidas**: creación de comidas asociadas a una fecha y tipo de toma, con adición y eliminación de ítems que relacionan alimentos con cantidades en gramos.
- **Resumen diario**: agregación del total de kilocalorías, proteínas, grasas e hidratos de carbono consumidos en una fecha, calculado directamente en base de datos.
- **Perfil biométrico**: almacenamiento de los datos del usuario y cálculo automático de la Tasa Metabólica Basal (TMB) y el Gasto Energético Total Diario (TDEE) mediante la fórmula de Mifflin-St Jeor.

Quedan fuera del alcance del MVP varias funcionalidades que se documentan explícitamente en §3.4: la validación sistemática del token en todos los endpoints protegidos, la pantalla de gestión de comidas en el cliente, el cifrado de la comunicación mediante HTTPS y el control de acceso por roles.

## 1.5 Arquitectura y tecnologías

El sistema se organiza en tres capas independientes. El cliente JavaFX actúa como interfaz de escritorio y se comunica con el backend mediante peticiones HTTP. El backend, implementado con Spring Boot 3.2, expone una API REST y concentra toda la lógica de negocio y el acceso a datos. MariaDB almacena el estado del sistema bajo un esquema gestionado íntegramente por Flyway.

El acceso a datos se realiza con `JdbcTemplate` y `RowMapper` manuales, sin ORM. La autenticación usa tokens opacos almacenados en base de datos, con expiración y borrado explícito en logout. El manejo de errores está centralizado en un `@RestControllerAdvice` que garantiza una estructura de respuesta uniforme para cualquier tipo de excepción. Estas y otras decisiones de diseño se justifican en §4.5 con sus alternativas consideradas.

La arquitectura y su justificación detallada se desarrollan en §4.1. Las tecnologías concretas utilizadas, con sus versiones y el papel que desempeñan, se recogen en §2.

## 1.6 Organización de la memoria

La memoria está estructurada en ocho secciones que siguen el desarrollo del proyecto de forma progresiva:

- **§2 — Tecnologías y herramientas**: las bibliotecas, frameworks y herramientas utilizadas, con la justificación de su elección.
- **§3 — Análisis de requisitos**: los requisitos funcionales y no funcionales del sistema, organizados por módulo, con el alcance del MVP y sus exclusiones explícitas.
- **§4 — Diseño**: la arquitectura del sistema (§4.1), el esquema de base de datos (§4.2) y las decisiones de diseño más relevantes (§4.5).
- **§5 — Implementación**: descripción módulo a módulo de la implementación del backend y del cliente, incluyendo autenticación (§5.1), alimentos (§5.2), comidas (§5.3), resumen diario (§5.4) y perfil (§5.5).
- **§6 — Pruebas**: la estrategia de pruebas, la suite de 29 tests unitarios del backend y la verificación manual de los endpoints mediante archivos `.http` y Swagger UI.
- **§7 — Seguridad**: las medidas implementadas —hashing BCrypt, token opaco, normalización de email— y las limitaciones conocidas del MVP.
- **§8 — Conclusiones**: valoración del resultado, aprendizajes técnicos, limitaciones del MVP y líneas de trabajo futuras.

Los anexos incluyen la guía de puesta en marcha del proyecto (anexo A) y la referencia completa de endpoints de la API (anexo B).
