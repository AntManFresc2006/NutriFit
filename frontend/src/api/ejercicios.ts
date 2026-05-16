import client from './client'
import type { Ejercicio, EjercicioExterno, RegistroEjercicio } from '../types'

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

export const createEjercicio = (data: EjercicioExterno) =>
  client.post<Ejercicio>('/api/ejercicios', data).then(r => r.data)

export const buscarExternoEjercicios = (q: string) =>
  client.get<EjercicioExterno[]>('/api/ejercicios/externo', { params: { q } }).then(r => r.data)

export interface RecuperacionData {
  tieneEjercicioIntensivo: boolean
  ejercicioNombre: string | null
  met: number | null
  sugerenciaProteinaG: number | null
  sugerenciaCarbosG: number | null
}

export const getRecuperacion = (usuarioId: number, fecha: string) =>
  client.get<RecuperacionData>('/api/ejercicios-registro/recuperacion', { params: { usuarioId, fecha } }).then(r => r.data)
