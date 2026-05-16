import client from './client'
import type { Comida, ComidaItem } from '../types'

export const getComidas = (usuarioId: number, fecha: string) =>
  client.get<Comida[]>('/api/comidas', { params: { usuarioId, fecha } }).then((r) => r.data)

export const createComida = (usuarioId: number, fecha: string, tipo: string) =>
  client.post<Comida>('/api/comidas', { fecha, tipo }, { params: { usuarioId } }).then((r) => r.data)

export const deleteComida = (id: number) => client.delete(`/api/comidas/${id}`)

export const getComidaItems = (comidaId: number) =>
  client.get<ComidaItem[]>(`/api/comidas/${comidaId}/items`).then((r) => r.data)

export const addItemToComida = (comidaId: number, alimentoId: number, gramos: number) =>
  client.post(`/api/comidas/${comidaId}/items`, { alimentoId, gramos })

export const deleteComidaItem = (comidaId: number, itemId: number) =>
  client.delete(`/api/comidas/${comidaId}/items/${itemId}`)
