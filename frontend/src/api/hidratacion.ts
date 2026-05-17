import client from './client'

export interface AguaRegistro {
  id: number
  fecha: string
  cantidadMl: number
  hora: string
}

export interface HidratacionDiaria {
  fecha: string
  totalMl: number
  objetivoMl: number
  porcentaje: number
  registros: AguaRegistro[]
}

export const getHidratacionDiaria = (usuarioId: number, fecha: string) =>
  client.get<HidratacionDiaria>('/api/hidratacion', { params: { usuarioId, fecha } }).then(r => r.data)

export const registrarAgua = (usuarioId: number, data: { fecha: string; cantidadMl: number }) =>
  client.post<AguaRegistro>('/api/hidratacion', data, { params: { usuarioId } }).then(r => r.data)

export const eliminarAgua = (usuarioId: number, id: number) =>
  client.delete(`/api/hidratacion/${id}`, { params: { usuarioId } })
