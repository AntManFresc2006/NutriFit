# Frontend de NutriFit

Aplicación React 18 + TypeScript con Vite, Tailwind CSS y Framer Motion.

## Estructura

```
src/
├── pages/           → pantallas principales
├── components/      → componentes reutilizables
├── api/             → clientes HTTP (axios)
├── hooks/           → hooks personalizados
├── context/         → contexto global (autenticación, usuario)
├── styles/          → Tailwind CSS
└── types/           → tipos TypeScript compartidos

dist/               → bundle de producción (Vite build)
```

## Requisitos

- Node.js 18+
- npm 9+

## Inicio rápido

### 1. Instalar dependencias

```bash
cd frontend
npm install
```

### 2. Variables de entorno

Crear `frontend/.env.local`:

```
VITE_API_URL=http://localhost:8080
```

En producción (Vercel), usar:
```
VITE_API_URL=https://nutrifit-backend.onrender.com
```

### 3. Desarrollo

```bash
npm run dev
```

Frontend arranca en `http://localhost:5173` y hot-reload habilitado.

### 4. Build

```bash
npm run build
```

Genera bundle en `dist/`.

### 5. Preview del build

```bash
npm run preview
```

Sirve la versión de producción localmente para verificar.

## Páginas principales

- `Alimentos.tsx` — gestión de catálogo de alimentos
- `Comidas.tsx` — comidas diarias + alimentos
- `Dashboard.tsx` — resumen nutricional del día
- `Ejercicios.tsx` — registro de ejercicios
- `Escaner.tsx` — escáner de códigos de barras
- `Hidratacion.tsx` — seguimiento de hidratación
- `ListaCompra.tsx` — lista de la compra
- `Login.tsx` → entrada (login/registro)
- `OpcionesIA.tsx` — configurar modelo IA y proxy
- `Perfil.tsx` — datos personales, BMR, TDEE
- `PlanSemanal.tsx` — plan de comidas generado por IA
- `Retos.tsx` — desafíos de fitness
- `Tendencias.tsx` — gráficas de tendencias (90 días)

## Stack técnico

- **React 18** — librería UI
- **TypeScript** — tipado estático
- **Vite** — bundler de desarrollo + production
- **Tailwind CSS** — utilidades CSS
- **Framer Motion** — animaciones
- **Axios** — cliente HTTP
- **React Router** — navegación
- **ESLint** — linting (opcional, configurable)

## Desarrollo local

**Con backend en localhost:8080**:

```bash
npm run dev
```

El frontend auto-detecta `VITE_API_URL` y conecta al backend.

**Con backend en Render** (producción):

Cambiar `VITE_API_URL` en `.env.local` al URL de Render, luego `npm run dev`.

## Despliegue

Frontend se despliega automáticamente en Vercel al hacer push a `main`.

Vercel lee:
- `VITE_API_URL` desde variables de entorno del proyecto
- Build command: `npm run build`
- Output dir: `dist`

## Ambiente

- **Desarrollo**: `localhost:5173`, backend en `localhost:8080`
- **Producción**: Vercel (frontend), Render (backend)

## Notas

- **Hot reload**: activado en desarrollo gracias a Vite
- **TypeScript**: tipos estrictos en `src/types/`
- **Autenticación**: token almacenado en localStorage, enviado en header `Authorization: Bearer <token>`
- **Errores HTTP**: capturados en service layer, mostrados en UI

## Autor

Antonio Manuel Fresco Gómez
