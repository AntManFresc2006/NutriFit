import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import ProtectedRoute from '../../components/ProtectedRoute'
import { AuthProvider } from '../../contexts/AuthContext'

const TestComponent = () => <div>Protected Content</div>

const renderProtectedRoute = (children: React.ReactNode) => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        <ProtectedRoute>{children}</ProtectedRoute>
      </AuthProvider>
    </BrowserRouter>
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('should render children when user is logged in', () => {
    const user = {
      usuarioId: 1,
      nombre: 'Test User',
      email: 'test@example.com',
    }
    localStorage.setItem('nf_user', JSON.stringify(user))

    renderProtectedRoute(<TestComponent />)

    expect(screen.getByText(/Protected Content/i)).toBeInTheDocument()
  })

  it('should redirect to login when user is not logged in', () => {
    renderProtectedRoute(<TestComponent />)

    expect(screen.queryByText(/Protected Content/i)).not.toBeInTheDocument()
  })
})
