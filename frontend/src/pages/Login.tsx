import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { login as apiLogin } from '../api/auth'
import { useAuth } from '../contexts/AuthContext'
import { Leaf } from 'lucide-react'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await apiLogin(email, password)
      login(data)
      navigate('/dashboard')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? 'Email o contraseña incorrectos')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#05080f] flex items-center justify-center relative overflow-hidden p-4">
      {/* Aurora animated blobs */}
      <motion.div
        className="absolute w-[600px] h-[600px] rounded-full blur-[120px] opacity-20 bg-emerald-500 -top-40 -left-40"
        animate={{ x: [0, 40, 0], y: [0, -30, 0] }}
        transition={{ duration: 12, repeat: Infinity, ease: 'easeInOut' }}
      />
      <motion.div
        className="absolute w-[500px] h-[500px] rounded-full blur-[100px] opacity-15 bg-cyan-400 -bottom-20 -right-20"
        animate={{ x: [0, -30, 0], y: [0, 40, 0] }}
        transition={{ duration: 15, repeat: Infinity, ease: 'easeInOut', delay: 2 }}
      />
      <motion.div
        className="absolute w-[400px] h-[400px] rounded-full blur-[80px] opacity-10 bg-violet-500 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2"
        animate={{ x: [0, 20, -20, 0], y: [0, -20, 20, 0] }}
        transition={{ duration: 20, repeat: Infinity, ease: 'easeInOut', delay: 5 }}
      />

      {/* Glassmorphism card */}
      <motion.div
        initial={{ opacity: 0, y: 30, scale: 0.97 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{ duration: 0.5, ease: 'easeOut' }}
        className="relative z-10 w-full max-w-md bg-white/8 backdrop-blur-2xl border border-white/15 rounded-3xl p-8 shadow-2xl"
      >
        {/* Logo/brand area */}
        <div className="text-center mb-8">
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 200, delay: 0.2 }}
            className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-emerald-500 to-cyan-500 flex items-center justify-center shadow-lg shadow-emerald-500/30"
          >
            <Leaf className="w-8 h-8 text-white" />
          </motion.div>
          <motion.h1
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
            className="text-2xl font-bold text-white"
          >
            NutriFit
          </motion.h1>
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.4 }}
            className="text-white/50 text-sm mt-1"
          >
            Tu asistente de nutrición
          </motion.p>
        </div>

        {/* Form title */}
        <motion.h2
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3 }}
          className="text-xl font-semibold mb-6 text-white"
        >
          Iniciar sesión
        </motion.h2>

        {/* Error display */}
        {error && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="bg-red-500/10 border border-red-500/20 rounded-xl p-3 text-red-400 text-sm mb-4"
          >
            {error}
          </motion.div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Email field */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.35 }}
          >
            <label htmlFor="email" className="label">Email</label>
            <input
              id="email"
              type="email"
              className="input"
              placeholder="tu@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </motion.div>

          {/* Password field */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.4 }}
          >
            <label htmlFor="password" className="label">Contraseña</label>
            <input
              id="password"
              type="password"
              className="input"
              placeholder="••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </motion.div>

          {/* Submit button */}
          <motion.button
            type="submit"
            className="btn-primary w-full mt-6"
            disabled={loading}
            whileTap={{ scale: 0.97 }}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.45 }}
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <motion.span
                  animate={{ rotate: 360 }}
                  transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
                >
                  ⏳
                </motion.span>
                Entrando...
              </span>
            ) : (
              'Entrar'
            )}
          </motion.button>
        </form>

        {/* Register link */}
        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
          className="text-center text-sm text-white/50 mt-6"
        >
          ¿No tienes cuenta?{' '}
          <Link to="/register" className="text-emerald-400 hover:text-emerald-300 transition-colors font-medium">
            Regístrate
          </Link>
        </motion.p>
      </motion.div>
    </div>
  )
}
