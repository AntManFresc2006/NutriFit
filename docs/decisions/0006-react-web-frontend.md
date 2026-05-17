# 0006 — Frontend web con React en lugar de cliente de escritorio JavaFX

## Decisión

La interfaz de usuario de NutriFit se implementa con **React 18 + TypeScript + Vite + Tailwind CSS + Framer Motion**
en lugar de un cliente de escritorio JavaFX.

---

## Contexto

NutriFit comenzó como proyecto académico con la intención de una interfaz de escritorio JavaFX.
Sin embargo, durante el desarrollo surgió la necesidad de:

1. **Facilitar el despliegue y demo**: un cliente de escritorio JavaFX requiere instalación local en cada máquina
2. **Acceso multiplataforma real**: web funciona en cualquier navegador sin dependencias del SO
3. **Hosting gratuito**: Vercel ofrece despliegue automático desde Git sin configuración
4. **Experiencia moderna de usuario**: Framer Motion permite animaciones suaves y retroalimentación visual
5. **Prototipado rápido**: React + TypeScript + Tailwind acelera la iteración frente a JavaFX

---

## Motivo de la elección

### 1. Despliegue y distribución sin fricción

Con JavaFX:
- El usuario debe descargar un JAR o instalador
- Requiere JRE instalado en el sistema
- Distinto proceso en Windows, Mac, Linux
- Demo del TFG requiere máquinas con Java preconfigurado

Con React:
- URL única, funciona en cualquier navegador moderno
- Vercel auto-deploya desde main branch sin configuración
- Demo accesible desde cualquier dispositivo con navegador
- HTTPS automático

```bash
# Despliegue actual en Vercel
git push origin main
# → Vercel auto-deploya en ~2 minutos
```

### 2. Desarrollo más ágil para un proyecto académico

React + TypeScript + Vite ofrecen:
- Hot Module Replacement (HMR) para feedback inmediato
- Ecosistema npm maduro con componentes reutilizables
- TypeScript para type safety en frontend
- Tailwind CSS para estilos sin escribir CSS puro
- Testing más directo que JavaFX

### 3. Framer Motion para UX moderna

Framer Motion permite:
- Animaciones de transición entre vistas
- Retroalimentación visual en interacciones
- Efecto de "drawer" para navegación
- Animaciones de carga y errores
- Experiencia más pulida que JavaFX + animaciones manuales

### 4. Arquitectura separada frontend/backend

El cambio a React refuerza una separación clara:

```
Frontend (React + Vite)    Backend (Spring Boot)
- Vercel (eu-west-1)      - Render (us-north-1)
- SPA, enrutamiento React  - API REST, datos
- localStorage para token  - PostgreSQL, sesiones
```

Esta separación es estándar en industria y facilita:
- Escalado independiente
- Testing frontend/backend por separado
- Colaboración con especialistas en cada área

---

## Alternativas consideradas

### Opción 1: Mantener JavaFX como cliente de escritorio
**Rechazada porque:**
- Requiere instalación JRE para usuarios finales
- No es accesible vía web
- Despliegue y demo más complicados
- Ecosistema UI más limitado que web moderno

### Opción 2: Framework alternativo (Vue, Svelte, Angular)
**Rechazada porque:**
- React es el más maduro y con mayor ecosistema
- TypeScript + React es estándar en industria
- Vercel (de los creadores de Next.js) tiene soporte first-class para React

### Opción 3: Generador estático (Next.js con SSG)
**Rechazada en MVP porque:**
- Overkill para una SPA sin SSR requirements
- Vite es más rápido para desarrollo y build
- Se puede evaluar Next.js en versión futura

---

## Diseño elegido

**Stack frontend:**
- React 18: componentes funcionales con hooks
- TypeScript: type safety sin perder productividad
- Vite: bundler rápido (~100ms cold start)
- Tailwind CSS: utility-first, responsive por defecto
- Framer Motion: animaciones declarativas
- React Router: enrutamiento de SPA
- axios/fetch: consumo de API REST

**Despliegue:**
- Vercel: auto-deploy desde main branch
- Dominio: dominio HTTPS automático
- Variables de entorno: VITE_BACKEND_URL para cambiar servidor según entorno

**Arquitectura:**
- Componentes en `/src/components`
- Hooks personalizados en `/src/hooks`
- Servicios API en `/src/services`
- Context API para estado global (auth, usuario)
- localStorage para persistencia de token

---

## Consecuencias asumidas

### Ventajas

1. **Despliegue trivial**: Vercel auto-deploya sin configuración
2. **Demo web**: compartir URL es más fácil que configurar máquinas
3. **Experiencia moderna**: Framer Motion, transiciones suaves
4. **TypeScript + React**: mejor tooling y feedback en desarrollo
5. **Escalado horizontal**: frontend y backend desacoplados
6. **Acceso desde cualquier dispositivo**: sin necesidad de SO específico

### Inconvenientes

1. **Token en localStorage**: exposición a XSS (mitigado con CSP)
   - Mitigation: validación de entrada, sanitizado de datos
   - Alternativa futura: cookies httpOnly con CSRF tokens

2. **CORS necesario**: backend debe permitir origen Vercel
   - Configurado en `CorsConfig.java` con `VERCEL_DOMAIN`

3. **Cold start Render (no es culpa de React)**:
   - Vite es extremadamente rápido (no es factor limitante)

4. **Sin acceso a APIs del SO**: no se puede abrir archivos del sistema
   - No aplica a NutriFit (no requiere acceso local)

5. **Debugging en navegador en lugar de IDE**:
   - React DevTools y sourcemaps mitigan bien este problema

---

## Decisiones técnicas relacionadas

- **CORS Configuration**: `CorsConfig.java` permite `http://localhost:5173` (dev) y dominio Vercel (prod)
- **Token en localStorage**: verificación en cada petición, sin refresh automático (7 días fijos)
- **API Base URL**: configurable vía `VITE_BACKEND_URL` en `vite.config.ts`
- **Build para producción**: `npm run build` genera `/dist` listo para Vercel

---

## Alcance implementado

✅ Enrutamiento React con navegación entre vistas
✅ Integración con API REST del backend
✅ Almacenamiento de token en localStorage
✅ Autenticación básica (login/logout)
✅ Listado de alimentos
✅ Escaneo de códigos de barras con cámara
✅ Registro de comidas y items
✅ Resumen diario nutricional
✅ Perfil de usuario y cálculos TMB/TDEE
✅ Registro de ejercicios
✅ Generador de planes semanales con IA
✅ Sugerencias de compra con IA

---

## Alcance que queda fuera

- Generador de reportes PDF
- Estadísticas históricas avanzadas (gráficos)
- Sincronización offline (Progressive Web App)
- Integración con apps nativas (React Native para iOS/Android)

---

## Próximo paso recomendado

Mantener la configuración de CORS sincronizada entre entornos de desarrollo
y producción. Considerar adoptar httpOnly cookies + CSRF para token en versión futura
si se requiere mayor seguridad.
