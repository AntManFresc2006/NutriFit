export interface AuthResponse {
  usuarioId: number
  nombre: string
  email: string
  token: string
}

export interface Alimento {
  id: number
  nombre: string
  porcionG: number
  kcalPor100g: number
  proteinasG: number
  grasasG: number
  carbosG: number
  fuente: string
}

export interface AlimentoRequest {
  nombre: string
  porcionG: number
  kcalPor100g: number
  proteinasG: number
  grasasG: number
  carbosG: number
}

export interface Comida {
  id: number
  usuarioId: number
  fecha: string
  tipo: string
}

export interface ComidaItem {
  itemId: number
  comidaId: number
  alimentoId: number
  nombre: string
  gramos: number
  kcalEstimadas: number
  proteinasEstimadas: number
  grasasEstimadas: number
  carbosEstimados: number
}

export interface RegistroEjercicio {
  id: number
  usuarioId: number
  ejercicioId: number
  nombreEjercicio: string
  fecha: string
  duracionMin: number
  kcalQuemadas: number
}

export interface Ejercicio {
  id: number
  nombre: string
  categoria: string
  kcalPorHora: number
}

export interface ResumenDiario {
  usuarioId: number
  fecha: string
  kcalTotales: number
  proteinasTotales: number
  grasasTotales: number
  carbosTotales: number
  kcalQuemadasTotales: number
  balanceNeto: number
  tdee: number
  balanceReal: number
  estadoBalance: string
}

export interface Perfil {
  id: number
  nombre: string
  email: string
  sexo: string
  fechaNacimiento: string
  alturaCm: number
  pesoKgActual: number
  pesoObjetivo: number | null
  nivelActividad: string
  tmb: number
  tdee: number
}
