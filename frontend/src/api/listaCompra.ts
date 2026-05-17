import client from './client'

export interface ListaItem {
  id: number
  nombre: string
  cantidad: string | null
  categoria: string
  completado: boolean
  createdAt: string
}

export type ListaAgrupada = Record<string, ListaItem[]>

export const getItems = (usuarioId: number) =>
  client.get<ListaAgrupada>('/api/lista-compra', { params: { usuarioId } }).then(r => r.data)

export const addItem = (usuarioId: number, data: { nombre: string; cantidad?: string; categoria?: string }) =>
  client.post<ListaItem>('/api/lista-compra', data, { params: { usuarioId } }).then(r => r.data)

export const toggleItem = (usuarioId: number, id: number) =>
  client.patch<ListaItem>(`/api/lista-compra/${id}/toggle`, null, { params: { usuarioId } }).then(r => r.data)

export const deleteItem = (usuarioId: number, id: number) =>
  client.delete(`/api/lista-compra/${id}`, { params: { usuarioId } })

export const clearCompletados = (usuarioId: number) =>
  client.delete('/api/lista-compra/completados', { params: { usuarioId } })

export const getSugerencias = (usuarioId: number) =>
  client.get<{ sugerencias: string[] }>('/api/lista-compra/sugerencias', { params: { usuarioId } }).then(r => r.data)
