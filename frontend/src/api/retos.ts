import client from './client'

export interface Reto {
  id: number
  titulo: string
  descripcion: string
  tipo: string
  metaValor: number
  duracionDias: number
  puntos: number
  icono: string
  usuarioRetoId: number | null
  progreso: number | null
  aceptado: boolean
  completado: boolean
  fechaFin: string | null
}

export const getRetos = (usuarioId: number) =>
  client.get<Reto[]>('/api/retos', { params: { usuarioId } }).then(r => r.data)

export const aceptarReto = (usuarioId: number, retoId: number) =>
  client.post<Reto>('/api/retos/aceptar', { retoId }, { params: { usuarioId } }).then(r => r.data)

export const sincronizarProgreso = (usuarioId: number, fecha: string) =>
  client.post<Reto[]>('/api/retos/sincronizar', null, { params: { usuarioId, fecha } }).then(r => r.data)

export const abandonarReto = (usuarioId: number, usuarioRetoId: number) =>
  client.delete(`/api/retos/${usuarioRetoId}`, { params: { usuarioId } })
