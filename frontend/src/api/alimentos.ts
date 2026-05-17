import client from './client'
import type { Alimento, AlimentoExterno, AlimentoRequest } from '../types'

export const getAlimentos = (q?: string) =>
  client.get<Alimento[]>('/api/alimentos', { params: q ? { q } : {} }).then((r) => r.data)

export const createAlimento = (data: AlimentoRequest) =>
  client.post<Alimento>('/api/alimentos', data).then((r) => r.data)

export const updateAlimento = (id: number, data: AlimentoRequest) =>
  client.put<Alimento>(`/api/alimentos/${id}`, data).then((r) => r.data)

export const deleteAlimento = (id: number) =>
  client.delete(`/api/alimentos/${id}`)

export const buscarExternoAlimentos = (q: string) =>
  client.get<AlimentoExterno[]>('/api/alimentos/externo', { params: { q } }).then(r => r.data)

export interface FotoScanResult {
  nombre: string
  kcalPor100g: number
  proteinas: number
  grasas: number
  carbos: number
  porcion: number
}

export const escanearFoto = (imagenBase64: string, mimeType: string) =>
  client.post<FotoScanResult>('/api/alimentos/escanear-foto', {
    imagenBase64,
    mimeType
  }).then(r => r.data)
