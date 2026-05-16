import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { logout as apiLogout } from '../api/auth'

const navItems = [
  { to: '/dashboard', icon: '📊', label: 'Dashboard' },
  { to: '/comidas', icon: '🍽️', label: 'Comidas' },
  { to: '/alimentos', icon: '🥗', label: 'Alimentos' },
  { to: '/ejercicios', icon: '🏃', label: 'Ejercicios' },
  { to: '/perfil', icon: '👤', label: 'Perfil' },
]

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

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
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}
