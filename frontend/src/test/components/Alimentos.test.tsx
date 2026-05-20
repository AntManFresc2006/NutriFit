import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import Alimentos from '../../pages/Alimentos'
import * as alimentosApi from '../../api/alimentos'

vi.mock('../../api/alimentos')

const mockAlimentos = [
  { id: 1, nombre: 'Pollo a la plancha', porcionG: 100, kcalPor100g: 165, proteinasG: 31, grasasG: 3.6, carbosG: 0, fuente: 'manual' },
  { id: 2, nombre: 'Arroz blanco cocido', porcionG: 100, kcalPor100g: 130, proteinasG: 2.7, grasasG: 0.3, carbosG: 28, fuente: 'manual' },
]

const renderAlimentos = () =>
  render(
    <BrowserRouter>
      <Alimentos />
    </BrowserRouter>
  )

describe('Alimentos', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(alimentosApi.getAlimentos).mockResolvedValue(mockAlimentos)
  })

  it('renders page title', () => {
    renderAlimentos()
    expect(screen.getByText('Alimentos')).toBeInTheDocument()
  })

  it('renders search input', () => {
    renderAlimentos()
    expect(screen.getByPlaceholderText(/Buscar alimentos/i)).toBeInTheDocument()
  })

  it('shows food names after loading', async () => {
    renderAlimentos()
    await waitFor(() => {
      expect(screen.getByText('Pollo a la plancha')).toBeInTheDocument()
      expect(screen.getByText('Arroz blanco cocido')).toBeInTheDocument()
    })
  })

  it('shows empty state when no foods found', async () => {
    vi.mocked(alimentosApi.getAlimentos).mockResolvedValue([])
    renderAlimentos()
    await waitFor(() => {
      expect(screen.getByText(/No se encontraron alimentos/i)).toBeInTheDocument()
    })
  })

  it('shows create form when "+ Nuevo alimento" is clicked', async () => {
    const user = userEvent.setup()
    renderAlimentos()
    await user.click(screen.getByRole('button', { name: /Nuevo alimento/i }))
    expect(screen.getByText('Añadir alimento')).toBeInTheDocument()
  })

  it('shows error message when food creation fails', async () => {
    const user = userEvent.setup()
    vi.mocked(alimentosApi.createAlimento).mockRejectedValue(new Error('server error'))
    renderAlimentos()

    await user.click(screen.getByRole('button', { name: /Nuevo alimento/i }))
    await waitFor(() => screen.getByText('Añadir alimento'))

    await user.type(screen.getByLabelText(/Nombre/i), 'Test alimento')
    await user.click(screen.getByRole('button', { name: /^Guardar$/i }))

    await waitFor(() => {
      expect(screen.getByText(/Error al crear el alimento/i)).toBeInTheDocument()
    })
  })
})
