import client from './client'
import type { Perfil } from '../types'

export const getPerfil = (id: number) =>
  client.get<Perfil>(`/api/perfil/${id}`).then((r) => r.data)

export const updatePerfil = (id: number, data: Partial<Perfil>) =>
  client.put<Perfil>(`/api/perfil/${id}`, data).then((r) => r.data)
