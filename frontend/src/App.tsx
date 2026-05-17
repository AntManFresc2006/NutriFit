import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import ErrorBoundary from './components/ErrorBoundary'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Alimentos from './pages/Alimentos'
import Comidas from './pages/Comidas'
import Ejercicios from './pages/Ejercicios'
import Hidratacion from './pages/Hidratacion'
import Perfil from './pages/Perfil'
import Escaner from './pages/Escaner'
import PlanSemanal from './pages/PlanSemanal'
import ListaCompra from './pages/ListaCompra'
import Retos from './pages/Retos'
import Tendencias from './pages/Tendencias'
import OpcionesIA from './pages/OpcionesIA'

export default function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/comidas" element={<Comidas />} />
            <Route path="/alimentos" element={<Alimentos />} />
            <Route path="/escaner" element={<Escaner />} />
            <Route path="/ejercicios" element={<Ejercicios />} />
            <Route path="/hidratacion" element={<Hidratacion />} />
            <Route path="/lista-compra" element={<ListaCompra />} />
            <Route path="/retos" element={<Retos />} />
            <Route path="/plan-semanal" element={<PlanSemanal />} />
            <Route path="/perfil" element={<Perfil />} />
            <Route path="/opciones-ia" element={<OpcionesIA />} />
            <Route path="/tendencias" element={<Tendencias />} />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  )
}
