import client from './client'
import type { ResumenDiario } from '../types'

export const getResumenDiario = (usuarioId: number, fecha: string) =>
  client.get<ResumenDiario>('/api/resumen-diario', { params: { usuarioId, fecha } }).then((r) => r.data)

export const getEvaluacionIA = (data: {
  usuarioId: number
  fecha: string
  kcalConsumidas: number
  kcalQuemadas: number
  proteinasTotales: number
  grasasTotales: number
  carbosTotales: number
  tdee: number
  balanceReal: number
}) => client.post<{ evaluacion: string }>('/api/resumen/evaluacion-ia', data).then((r) => r.data)
