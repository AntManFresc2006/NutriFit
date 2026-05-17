import client from './client'

export interface ComidaPlan {
  descripcion: string
  kcal: number
  proteinas: number
  carbos: number
  grasas: number
}

export interface DiaPlan {
  dia: string
  fecha: string
  comidas: {
    desayuno: ComidaPlan
    almuerzo: ComidaPlan
    merienda: ComidaPlan
    cena: ComidaPlan
  }
  totalKcal: number
  totalProteinas: number
  totalCarbos: number
  totalGrasas: number
}

export interface PlanSemanal {
  dias: DiaPlan[]
}

export interface PlanSemanalResponse {
  id: number
  semanaInicio: string
  planJson: string
  createdAt: string
}

export const generarPlan = (usuarioId: number, semanaInicio: string) =>
  client
    .post<PlanSemanalResponse>(
      '/api/plan-semanal',
      { semanaInicio },
      { params: { usuarioId } }
    )
    .then((r) => r.data)

export const getPlan = (usuarioId: number, semanaInicio: string) =>
  client
    .get<PlanSemanalResponse>('/api/plan-semanal', {
      params: { usuarioId, semanaInicio },
    })
    .then((r) => r.data)

export const regenerarPlan = async (usuarioId: number, semanaInicio: string) => {
  await client.delete('/api/plan-semanal', { params: { usuarioId, semanaInicio } })
  return generarPlan(usuarioId, semanaInicio)
}
