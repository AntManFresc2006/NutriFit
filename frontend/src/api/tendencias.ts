import client from './client'

export interface PesoPoint {
  fecha: string
  pesoKg: number
}

export interface NutriScorePoint {
  fecha: string
  score: number
  grade: string
}

export interface MacrosPoint {
  semana: string
  inicioSemana: string
  kcalPromedio: number
  proteinasPromedio: number
  carbosPromedio: number
  grasasPromedio: number
}

export interface EjercicioPoint {
  fecha: string
  tuvoEjercicio: boolean
  duracionMin: number
  kcalQuemadas: number
}

export interface TendenciasData {
  peso: PesoPoint[]
  nutriScore: NutriScorePoint[]
  macros: MacrosPoint[]
  ejercicio: EjercicioPoint[]
  pesoObjetivo: number | null
}

export const getTendencias = (usuarioId: number, dias: number = 30) =>
  client.get<TendenciasData>('/api/tendencias', { params: { usuarioId, dias } }).then(r => r.data)
