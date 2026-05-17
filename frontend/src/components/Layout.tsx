import { NavLink, Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { logout as apiLogout } from '../api/auth'
import { onServerReady } from '../api/client'
import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'

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
  const location = useLocation()
  const [warming, setWarming] = useState(true)

  useEffect(() => {
    const timer = setTimeout(() => setWarming(false), 65000)
    onServerReady(() => setWarming(false))
    return () => clearTimeout(timer)
  }, [])

  const handleLogout = async () => {
    try {
      await apiLogout()
    } catch {}
    logout()
    navigate('/login')
  }

  const navVariants = {
    hidden: {},
    show: { transition: { staggerChildren: 0.05, delayChildren: 0.1 } }
  }

  const itemVariants = {
    hidden: { opacity: 0, x: -16 },
    show: { opacity: 1, x: 0, transition: { duration: 0.3 } }
  }

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <aside className="w-16 lg:w-60 bg-[#080c15] border-r border-white/8 flex flex-col shrink-0 backdrop-blur-xl">
        {/* Logo */}
        <motion.div
          initial={{ opacity: 0, y: -8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.05 }}
          className="flex items-center gap-3 px-4 py-5 border-b border-white/8"
        >
          <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-emerald-500 to-cyan-500 flex items-center justify-center text-xl shadow-lg shadow-emerald-500/20 shrink-0">
            🥗
          </div>
          <span className="font-bold text-white lg:block hidden text-lg tracking-tight">NutriFit</span>
        </motion.div>

        {/* Navigation */}
        <motion.nav
          className="flex-1 p-2 space-y-1"
          variants={navVariants}
          initial="hidden"
          animate="show"
        >
          {navItems.map(({ to, icon, label }) => (
            <motion.div key={to} variants={itemVariants}>
              <NavLink
                to={to}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-xl mx-2 transition-all duration-200 text-sm font-medium ${
                    isActive
                      ? 'bg-gradient-to-r from-emerald-500/20 to-cyan-500/10 border border-emerald-500/20 text-emerald-400 shadow-lg shadow-emerald-500/10'
                      : 'text-white/50 hover:text-white hover:bg-white/5'
                  }`
                }
              >
                <span className="text-lg shrink-0">{icon}</span>
                <span className="hidden lg:block">{label}</span>
              </NavLink>
            </motion.div>
          ))}
        </motion.nav>

        {/* User section */}
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.3 }}
          className="mx-2 mb-3 p-3 rounded-xl bg-white/5 border border-white/8 flex items-center gap-3"
        >
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-emerald-500 to-cyan-500 flex items-center justify-center text-sm font-bold text-white shrink-0">
            {user?.nombre?.[0]?.toUpperCase() || 'U'}
          </div>
          <div className="flex-1 min-w-0 lg:block hidden">
            <p className="text-white text-xs font-medium truncate">{user?.nombre}</p>
            <p className="text-white/40 text-xs truncate">{user?.email}</p>
          </div>
        </motion.div>

        {/* Logout button */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.4, delay: 0.35 }}
          className="p-3 border-t border-white/8"
        >
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 px-3 py-2 rounded-xl text-white/50 hover:text-red-400 hover:bg-red-500/10 transition-all duration-200 w-full text-sm font-medium"
          >
            <span className="text-lg">🚪</span>
            <span className="hidden lg:block">Cerrar sesión</span>
          </button>
        </motion.div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto flex flex-col">
        {warming && (
          <motion.div
            initial={{ opacity: 0, y: -4 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-amber-500/10 border-b border-amber-500/30 px-4 py-2 flex items-center gap-2 text-sm text-amber-400 shrink-0"
          >
            <div className="animate-spin w-3.5 h-3.5 border-2 border-amber-400 border-t-transparent rounded-full shrink-0" />
            Servidor iniciándose... La primera carga puede tardar hasta 60 segundos.
          </motion.div>
        )}
        <div className="flex-1">
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -12 }}
              transition={{ duration: 0.25 }}
              className="flex-1 overflow-auto"
            >
              <Outlet />
            </motion.div>
          </AnimatePresence>
        </div>
      </main>
    </div>
  )
}
