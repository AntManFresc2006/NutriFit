import { createContext, useContext, useState, type ReactNode } from 'react'
import type { AuthResponse } from '../types'

interface AuthUser {
  usuarioId: number
  nombre: string
  email: string
}

interface AuthContextType {
  user: AuthUser | null
  login: (data: AuthResponse) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextType | null>(null)

function loadUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem('nf_user')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadUser)

  const login = (data: AuthResponse) => {
    const u: AuthUser = { usuarioId: data.usuarioId, nombre: data.nombre, email: data.email }
    localStorage.setItem('nf_user', JSON.stringify(u))
    setUser(u)
  }

  const logout = () => {
    localStorage.removeItem('nf_user')
    setUser(null)
  }

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
