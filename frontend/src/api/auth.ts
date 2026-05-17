import client from './client'
import type { AuthResponse } from '../types'

export const login = (email: string, password: string) =>
  client.post<AuthResponse>('/api/auth/login', { email, password }).then((r) => r.data)

export const register = (nombre: string, email: string, password: string) =>
  client.post<AuthResponse>('/api/auth/register', { nombre, email, password }).then((r) => r.data)

export const logout = () =>
  client.post('/api/auth/logout')
