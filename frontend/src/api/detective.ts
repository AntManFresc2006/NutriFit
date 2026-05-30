import client from './client'

export interface DetectiveStats {
  diasAnalizados: number
  diasConRegistro: number
  diasSinRegistro: number
  tasaRegistro: number
  kcalMediaDiaria: number
  deficitRealMedio: number
  diasSobreObjetivo: number
  proteinaMediaDiaria: number
  proteinaObjetivo: number
  diasCumpliendoProteina: number
  proteinaCumplimiento: number
  kcalPorDiaSemana: Record<string, number>
  peorDiaSemana: string | null
  mejorDiaSemana: string | null
}

export interface Hallazgo {
  severidad: 'CRITICO' | 'ADVERTENCIA' | 'POSITIVO'
  titulo: string
  descripcion: string
}

export interface DetectiveAnalisis {
  id: number
  diasAnalizados: number
  estado: 'ANALIZANDO' | 'LISTO' | 'ERROR'
  estadisticas: DetectiveStats | null
  hallazgos: Hallazgo[] | null
  analisisIa: string | null
  errorMsg: string | null
}

export const iniciarAnalisis = (usuarioId: number, dias: number) =>
  client
    .post<DetectiveAnalisis>('/api/detective', null, { params: { usuarioId, dias } })
    .then((r) => r.data)

export const getAnalisis = (usuarioId: number) =>
  client
    .get<DetectiveAnalisis>('/api/detective', { params: { usuarioId } })
    .then((r) => r.data)
