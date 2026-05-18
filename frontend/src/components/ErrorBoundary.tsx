import { Component, type ReactNode, type ErrorInfo } from 'react'
import { AlertTriangle } from 'lucide-react'

interface Props { children: ReactNode }
interface State { hasError: boolean; error?: Error }

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Error capturado:', error, info)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex flex-col items-center justify-center h-screen bg-slate-900 text-slate-100 gap-4">
          <AlertTriangle className="w-10 h-10 text-amber-400" />
          <h2 className="text-xl font-bold">Algo salió mal</h2>
          <p className="text-slate-400 text-sm">{this.state.error?.message}</p>
          <button
            className="btn-primary"
            onClick={() => window.location.reload()}
          >
            Recargar página
          </button>
        </div>
      )
    }
    return this.props.children
  }
}
