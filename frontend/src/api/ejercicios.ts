import client from './client'
import type { Ejercicio, RegistroEjercicio } from '../types'

export const getEjercicios = (q?: string) =>
  client.get<Ejercicio[]>('/api/ejercicios', { params: q ? { q } : {} }).then((r) => r.data)

export const getRegistros = (usuarioId: number, fecha: string) =>
  client.get<RegistroEjercicio[]>('/api/ejercicios-registro', { params: { usuarioId, fecha } }).then((r) => r.data)

export const registrarEjercicio = (usuarioId: number, ejercicioId: number, fecha: string, duracionMin: number) =>
  client
    .post<RegistroEjercicio>('/api/ejercicios-registro', { ejercicioId, fecha, duracionMin }, { params: { usuarioId } })
    .then((r) => r.data)

export const deleteRegistro = (id: number, usuarioId: number) =>
  client.delete(`/api/ejercicios-registro/${id}`, { params: { usuarioId } })
