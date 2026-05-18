import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import Register from '../../pages/Register'
import { AuthProvider } from '../../contexts/AuthContext'
import * as authApi from '../../api/auth'

vi.mock('../../api/auth')
vi.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: React.HTMLAttributes<HTMLDivElement>) => <div {...props}>{children}</div>,
    h1: ({ children, ...props }: React.HTMLAttributes<HTMLHeadingElement>) => <h1 {...props}>{children}</h1>,
    h2: ({ children, ...props }: React.HTMLAttributes<HTMLHeadingElement>) => <h2 {...props}>{children}</h2>,
    p: ({ children, ...props }: React.HTMLAttributes<HTMLParagraphElement>) => <p {...props}>{children}</p>,
    button: ({ children, ...props }: React.ButtonHTMLAttributes<HTMLButtonElement>) => <button {...props}>{children}</button>,
    span: ({ children, ...props }: React.HTMLAttributes<HTMLSpanElement>) => <span {...props}>{children}</span>,
  },
  AnimatePresence: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}))

const renderRegister = () =>
  render(
    <BrowserRouter>
      <AuthProvider>
        <Register />
      </AuthProvider>
    </BrowserRouter>
  )

describe('Register page', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('renders form fields for nombre, email and password', () => {
    renderRegister()

    expect(screen.getByLabelText(/Nombre/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/Email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/Contraseña/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Crear cuenta/i })).toBeInTheDocument()
  })

  it('shows client-side error when password is shorter than 6 characters', async () => {
    const user = userEvent.setup()
    renderRegister()

    await user.type(screen.getByLabelText(/Nombre/i), 'Ana')
    await user.type(screen.getByLabelText(/Email/i), 'ana@example.com')
    await user.type(screen.getByLabelText(/Contraseña/i), '123')
    await user.click(screen.getByRole('button', { name: /Crear cuenta/i }))

    expect(screen.getByText(/al menos 6 caracteres/i)).toBeInTheDocument()
    expect(authApi.register).not.toHaveBeenCalled()
  })

  it('calls register API with correct data on valid submit', async () => {
    const user = userEvent.setup()
    vi.mocked(authApi.register).mockResolvedValueOnce({
      usuarioId: 1,
      nombre: 'Ana',
      email: 'ana@example.com',
      token: 'tok-abc',
    })
    renderRegister()

    await user.type(screen.getByLabelText(/Nombre/i), 'Ana')
    await user.type(screen.getByLabelText(/Email/i), 'ana@example.com')
    await user.type(screen.getByLabelText(/Contraseña/i), 'secreta123')
    await user.click(screen.getByRole('button', { name: /Crear cuenta/i }))

    await waitFor(() => {
      expect(authApi.register).toHaveBeenCalledWith('Ana', 'ana@example.com', 'secreta123')
    })
  })

  it('shows API error message when registration fails', async () => {
    const user = userEvent.setup()
    vi.mocked(authApi.register).mockRejectedValueOnce({
      response: { data: { message: 'El email ya está registrado' } },
    })
    renderRegister()

    await user.type(screen.getByLabelText(/Nombre/i), 'Ana')
    await user.type(screen.getByLabelText(/Email/i), 'ana@example.com')
    await user.type(screen.getByLabelText(/Contraseña/i), 'secreta123')
    await user.click(screen.getByRole('button', { name: /Crear cuenta/i }))

    await waitFor(() => {
      expect(screen.getByText(/El email ya está registrado/i)).toBeInTheDocument()
    })
  })
})
