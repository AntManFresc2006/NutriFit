import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Comidas from '../../pages/Comidas'
import { AuthProvider } from '../../contexts/AuthContext'
import * as comidasApi from '../../api/comidas'
import * as alimentosApi from '../../api/alimentos'

vi.mock('../../api/comidas')
vi.mock('../../api/alimentos')

const mockUser = { usuarioId: 1, nombre: 'Antonio', email: 'a@a.com' }

const mockComidas = [
  { id: 1, usuarioId: 1, fecha: '2026-05-20', tipo: 'DESAYUNO' },
  { id: 2, usuarioId: 1, fecha: '2026-05-20', tipo: 'COMIDA' },
]

const renderComidas = () => {
  localStorage.setItem('nf_user', JSON.stringify(mockUser))
  return render(
    <BrowserRouter>
      <AuthProvider>
        <Comidas />
      </AuthProvider>
    </BrowserRouter>
  )
}

describe('Comidas', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    vi.mocked(comidasApi.getComidas).mockResolvedValue(mockComidas)
    vi.mocked(comidasApi.getComidaItems).mockResolvedValue([])
    vi.mocked(alimentosApi.getAlimentos).mockResolvedValue([])
  })

  it('renders page title', () => {
    renderComidas()
    expect(screen.getByText('Comidas')).toBeInTheDocument()
  })

  it('shows registered meal types after loading', async () => {
    renderComidas()
    await waitFor(() => {
      expect(screen.getByText('DESAYUNO')).toBeInTheDocument()
      expect(screen.getByText('COMIDA')).toBeInTheDocument()
    })
  })

  it('shows available tipo buttons for unregistered meals', async () => {
    renderComidas()
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /MERIENDA/i })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /CENA/i })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /SNACK/i })).toBeInTheDocument()
    })
  })

  it('calls getComidas with the authenticated user id', async () => {
    renderComidas()
    await waitFor(() => {
      expect(comidasApi.getComidas).toHaveBeenCalledWith(1, expect.any(String))
    })
  })
})
