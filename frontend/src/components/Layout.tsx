import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { logout as apiLogout } from '../api/auth'
import { onServerReady } from '../api/client'
import { useEffect, useState } from 'react'

const navItems = [
  { to: '/dashboard', icon: '📊', label: 'Dashboard' },
  { to: '/comidas', icon: '🍽️', label: 'Comidas' },
  { to: '/alimentos', icon: '🥗', label: 'Alimentos' },
  { to: '/escaner', icon: '📷', label: 'Escáner' },
  { to: '/ejercicios', icon: '🏃', label: 'Ejercicios' },
  { to: '/retos', icon: '🏆', label: 'Retos' },
  { to: '/hidratacion', icon: '💧', label: 'Hidratación' },
  { to: '/lista-compra', icon: '🛒', label: 'Lista Compra' },
  { to: '/plan-semanal', icon: '🗓️', label: 'Plan Semanal' },
  { to: '/tendencias', icon: '📈', label: 'Tendencias' },
  { to: '/perfil', icon: '👤', label: 'Perfil' },
]

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [warming, setWarming] = useState(true)

  useEffect(() => {
    const timer = setTimeout(() => setWarming(false), 65000)
    onServerReady(() => setWarming(false))
    return () => clearTimeout(timer)
  }, [])

  const handleLogout = async () => {
    try {
      if (user) await apiLogout(user.token)
    } catch {}
    logout()
    navigate('/login')
  }

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <aside className="w-16 lg:w-60 bg-slate-800 border-r border-slate-700 flex flex-col shrink-0">
        <div className="p-4 border-b border-slate-700 flex items-center gap-3">
          <span className="text-2xl">🥦</span>
          <span className="hidden lg:block text-lg font-bold text-green-400">NutriFit</span>
        </div>

        <nav className="flex-1 p-2 space-y-1">
          {navItems.map(({ to, icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-xl transition-colors text-sm font-medium ${
                  isActive
                    ? 'bg-green-500/20 text-green-400'
                    : 'text-slate-400 hover:bg-slate-700 hover:text-slate-100'
                }`
              }
            >
              <span className="text-xl shrink-0">{icon}</span>
              <span className="hidden lg:block">{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="p-3 border-t border-slate-700">
          <div className="hidden lg:block text-xs text-slate-500 mb-2 truncate px-1">{user?.nombre}</div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 px-3 py-2 rounded-xl text-slate-400 hover:bg-slate-700 hover:text-red-400 transition-colors w-full text-sm"
          >
            <span className="text-xl">🚪</span>
            <span className="hidden lg:block">Cerrar sesión</span>
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto flex flex-col">
        {warming && (
          <div className="bg-amber-500/10 border-b border-amber-500/30 px-4 py-2 flex items-center gap-2 text-sm text-amber-400 shrink-0">
            <div className="animate-spin w-3.5 h-3.5 border-2 border-amber-400 border-t-transparent rounded-full shrink-0" />
            Servidor iniciándose... La primera carga puede tardar hasta 60 segundos.
          </div>
        )}
        <div className="flex-1">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
