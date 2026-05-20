import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Dashboard from '../../pages/Dashboard'
import { AuthProvider } from '../../contexts/AuthContext'
import * as resumenApi from '../../api/resumen'
import * as ejerciciosApi from '../../api/ejercicios'

vi.mock('../../api/resumen')
vi.mock('../../api/ejercicios')

const mockUser = { usuarioId: 1, nombre: 'Antonio', email: 'a@a.com' }

const mockResumen = {
  usuarioId: 1,
  fecha: '2026-05-20',
  kcalTotales: 1800,
  proteinasTotales: 100,
  grasasTotales: 70,
  carbosTotales: 180,
  kcalQuemadasTotales: 250,
  balanceNeto: -450,
  tdee: 2000,
  balanceReal: -450,
  estadoBalance: 'DEFICIT',
  diasParaObjetivo: null,
  fechaObjetivo: null,
}

const mockGamificacion = {
  racha: 3,
  nutriScore: 80,
  nutriGrade: 'B',
  cumpleProteina: true,
  cumpleBalance: true,
  cumpleEjercicio: false,
  cumpleVariedad: true,
}

const mockRecuperacion = {
  tieneEjercicioIntensivo: false,
  ejercicioNombre: null,
  met: null,
  sugerenciaProteinaG: null,
  sugerenciaCarbosG: null,
}

const renderDashboard = () => {
  localStorage.setItem('nf_user', JSON.stringify(mockUser))
  return render(
    <BrowserRouter>
      <AuthProvider>
        <Dashboard />
      </AuthProvider>
    </BrowserRouter>
  )
}

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    vi.mocked(resumenApi.getResumenDiario).mockResolvedValue(mockResumen)
    vi.mocked(resumenApi.getGamificacion).mockResolvedValue(mockGamificacion)
    vi.mocked(resumenApi.getEvaluacionIA).mockResolvedValue({ evaluacion: 'Test eval' })
    vi.mocked(ejerciciosApi.getRecuperacion).mockResolvedValue(mockRecuperacion)
  })

  it('renders greeting with the authenticated user name', async () => {
    renderDashboard()
    await waitFor(() => {
      expect(screen.getByText('Antonio')).toBeInTheDocument()
    })
  })

  it('shows empty state when API returns no data', async () => {
    vi.mocked(resumenApi.getResumenDiario).mockRejectedValue(new Error('sin datos'))
    renderDashboard()
    await waitFor(() => {
      expect(screen.getByText(/Sin datos para este día/i)).toBeInTheDocument()
    })
  })

  it('shows nutritional stat labels after data loads', async () => {
    renderDashboard()
    await waitFor(() => {
      expect(screen.getByText('Kcal consumidas')).toBeInTheDocument()
      expect(screen.getByText('Kcal quemadas')).toBeInTheDocument()
      expect(screen.getByText('TDEE')).toBeInTheDocument()
    })
  })

  it('calls getResumenDiario with the authenticated user id', async () => {
    renderDashboard()
    await waitFor(() => {
      expect(resumenApi.getResumenDiario).toHaveBeenCalledWith(1, expect.any(String))
    })
  })
})
