import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AuthProvider, useAuth } from '../../contexts/AuthContext'

const TestComponent = () => {
  const { user, login, logout } = useAuth()

  return (
    <div>
      {user ? (
        <>
          <div data-testid="user-name">{user.nombre}</div>
          <div data-testid="user-email">{user.email}</div>
          <button onClick={logout}>Logout</button>
        </>
      ) : (
        <>
          <div data-testid="no-user">No user logged in</div>
          <button
            onClick={() =>
              login({
                usuarioId: 123,
                nombre: 'John Doe',
                email: 'john@example.com',
                token: 'fake-token',
              })
            }
          >
            Login
          </button>
        </>
      )}
    </div>
  )
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('should start with no user when localStorage is empty', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    expect(screen.getByTestId('no-user')).toBeInTheDocument()
  })

  it('should login user and save to localStorage', async () => {
    const user = userEvent.setup()
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    const loginButton = screen.getByRole('button', { name: /Login/i })
    await user.click(loginButton)

    await waitFor(() => {
      expect(screen.getByTestId('user-name')).toHaveTextContent('John Doe')
      expect(screen.getByTestId('user-email')).toHaveTextContent('john@example.com')
    })

    const stored = localStorage.getItem('nf_user')
    expect(stored).toBeDefined()
    expect(JSON.parse(stored!).nombre).toBe('John Doe')
  })

  it('should logout user and remove from localStorage', async () => {
    const user = userEvent.setup()
    const testUser = {
      usuarioId: 123,
      nombre: 'John Doe',
      email: 'john@example.com',
    }
    localStorage.setItem('nf_user', JSON.stringify(testUser))

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    expect(screen.getByTestId('user-name')).toHaveTextContent('John Doe')

    const logoutButton = screen.getByRole('button', { name: /Logout/i })
    await user.click(logoutButton)

    await waitFor(() => {
      expect(screen.getByTestId('no-user')).toBeInTheDocument()
    })

    expect(localStorage.getItem('nf_user')).toBeNull()
  })

  it('should load user from localStorage on mount', () => {
    const testUser = {
      usuarioId: 456,
      nombre: 'Jane Smith',
      email: 'jane@example.com',
    }
    localStorage.setItem('nf_user', JSON.stringify(testUser))

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    expect(screen.getByTestId('user-name')).toHaveTextContent('Jane Smith')
    expect(screen.getByTestId('user-email')).toHaveTextContent('jane@example.com')
  })
})
