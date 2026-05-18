import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import ErrorBoundary from '../../components/ErrorBoundary'

const ThrowingComponent = ({ shouldThrow }: { shouldThrow: boolean }) => {
  if (shouldThrow) throw new Error('Error de prueba')
  return <div>Contenido normal</div>
}

describe('ErrorBoundary', () => {
  beforeEach(() => {
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  it('renders children when no error is thrown', () => {
    render(
      <ErrorBoundary>
        <ThrowingComponent shouldThrow={false} />
      </ErrorBoundary>
    )

    expect(screen.getByText('Contenido normal')).toBeInTheDocument()
    expect(screen.queryByText(/Algo salió mal/i)).not.toBeInTheDocument()
  })

  it('renders fallback UI when a child component throws', () => {
    render(
      <ErrorBoundary>
        <ThrowingComponent shouldThrow={true} />
      </ErrorBoundary>
    )

    expect(screen.getByText(/Algo salió mal/i)).toBeInTheDocument()
    expect(screen.queryByText('Contenido normal')).not.toBeInTheDocument()
  })

  it('displays the error message from the thrown error', () => {
    render(
      <ErrorBoundary>
        <ThrowingComponent shouldThrow={true} />
      </ErrorBoundary>
    )

    expect(screen.getByText('Error de prueba')).toBeInTheDocument()
  })

  it('renders a reload button in the fallback UI', () => {
    render(
      <ErrorBoundary>
        <ThrowingComponent shouldThrow={true} />
      </ErrorBoundary>
    )

    expect(screen.getByRole('button', { name: /Recargar página/i })).toBeInTheDocument()
  })
})
