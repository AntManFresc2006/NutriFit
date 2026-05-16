import client from './client'
import type { PesoHistorial } from '../types'

export const getPesoHistorial = (usuarioId: number, limit = 30) =>
  client.get<PesoHistorial[]>('/api/peso-historial', { params: { usuarioId, limit } }).then(r => r.data)

export const registrarPeso = (usuarioId: number, fecha: string, pesoKg: number) =>
  client.post<PesoHistorial>('/api/peso-historial', { fecha, pesoKg }, { params: { usuarioId } }).then(r => r.data)

export const eliminarPeso = (usuarioId: number, fecha: string) =>
  client.delete('/api/peso-historial', { params: { usuarioId, fecha } })
