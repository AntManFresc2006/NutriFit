import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'
import { getHidratacionDiaria, registrarAgua, eliminarAgua, type HidratacionDiaria } from '../api/hidratacion'

export default function Hidratacion() {
  const { user } = useAuth()
  const [fecha, setFecha] = useState(new Date().toISOString().split('T')[0])
  const [diario, setDiario] = useState<HidratacionDiaria | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [personalizado, setPersonalizado] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!user) return
    loadDiario()
  }, [user, fecha])

  const loadDiario = async () => {
    if (!user) return
    setLoading(true)
    setError(null)
    try {
      const data = await getHidratacionDiaria(user.usuarioId, fecha)
      setDiario(data)
    } catch {
      setError('No se pudo cargar los datos de hidratación')
    } finally {
      setLoading(false)
    }
  }

  const handleRegistrar = async (cantidad: number) => {
    if (!user || !diario) return
    setSaving(true)
    try {
      const nuevoRegistro = await registrarAgua(user.usuarioId, { fecha, cantidadMl: cantidad })
      setDiario(prev => {
        if (!prev) return prev
        const newTotal = prev.totalMl + cantidad
        const newPorcentaje = Math.min(100, (newTotal * 100) / prev.objetivoMl)
        return {
          ...prev,
          totalMl: newTotal,
          porcentaje: newPorcentaje,
          registros: [nuevoRegistro, ...prev.registros]
        }
      })
      setPersonalizado('')
    } catch {
      setError('No se pudo registrar el agua')
    } finally {
      setSaving(false)
    }
  }

  const handleEliminar = async (id: number) => {
    if (!user || !diario) return
    try {
      await eliminarAgua(user.usuarioId, id)
      setDiario(prev => {
        if (!prev) return prev
        const registroEliminado = prev.registros.find(r => r.id === id)
        if (!registroEliminado) return prev
        const newTotal = prev.totalMl - registroEliminado.cantidadMl
        const newPorcentaje = Math.min(100, (newTotal * 100) / prev.objetivoMl)
        return {
          ...prev,
          totalMl: newTotal,
          porcentaje: newPorcentaje,
          registros: prev.registros.filter(r => r.id !== id)
        }
      })
    } catch {
      setError('No se pudo eliminar el registro')
    }
  }

  if (!user || loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin w-8 h-8 border-2 border-cyan-500 border-t-transparent rounded-full" />
      </div>
    )
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="p-6 max-w-2xl mx-auto"
    >
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1, duration: 0.3 }}
        className="flex items-center justify-between mb-8 flex-wrap gap-4"
      >
        <div>
          <h1 className="text-3xl font-bold gradient-text">💧 Hidratación</h1>
          <p className="text-slate-400 text-sm mt-0.5">Mantente hidratado durante el día</p>
        </div>
        <input
          type="date"
          value={fecha}
          onChange={e => setFecha(e.target.value)}
          className="input w-auto"
        />
      </motion.div>

      <AnimatePresence>
        {error && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="bg-red-500/10 border border-red-500/30 rounded-xl p-3 mb-6 text-red-400 text-sm"
          >
            {error}
          </motion.div>
        )}
      </AnimatePresence>

      {diario && (
        <>
          {/* Círculo de progreso */}
          <motion.div
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ delay: 0.15, type: 'spring', stiffness: 120 }}
            className="card flex justify-center mb-8 shadow-xl"
          >
            <div className="relative w-48 h-48">
              {diario.porcentaje >= 100 && (
                <motion.div
                  className="absolute inset-0 rounded-full border-4 border-emerald-400/30"
                  animate={{ scale: [1, 1.08, 1], opacity: [0.4, 0.1, 0.4] }}
                  transition={{ duration: 2, repeat: Infinity }}
                />
              )}
              <svg viewBox="0 0 200 200" className="w-full h-full transform -rotate-90">
                <circle cx="100" cy="100" r="90" fill="none" stroke="#334155" strokeWidth="8" />
                <circle
                  cx="100"
                  cy="100"
                  r="90"
                  fill="none"
                  stroke={diario.porcentaje >= 100 ? '#22c55e' : '#06b6d4'}
                  strokeWidth="8"
                  strokeDasharray={`${(diario.porcentaje / 100) * 565.48} 565.48`}
                  strokeLinecap="round"
                  style={{ transition: 'stroke-dasharray 0.3s ease' }}
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <div className="text-center">
                  <motion.div
                    key={diario.totalMl}
                    initial={{ scale: 1.2, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    transition={{ duration: 0.2 }}
                    className="text-3xl font-bold text-slate-100"
                  >
                    {diario.totalMl}
                  </motion.div>
                  <div className="text-sm text-slate-400">/ {diario.objetivoMl} ml</div>
                  <div className="text-xl font-semibold mt-2 gradient-text">{diario.porcentaje}%</div>
                </div>
              </div>
            </div>
          </motion.div>

          {/* Botones rápidos */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.25, duration: 0.3 }}
            className="grid grid-cols-2 gap-3 mb-8"
          >
            {[
              { label: 'Vaso pequeño', ml: 150 },
              { label: 'Vaso normal', ml: 250 },
              { label: 'Botella pequeña', ml: 330 },
              { label: 'Botella grande', ml: 500 }
            ].map(({ label, ml }, idx) => (
              <motion.button
                key={ml}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.25 + idx * 0.05 }}
                whileHover={{ scale: 1.04 }}
                whileTap={{ scale: 0.96 }}
                onClick={() => handleRegistrar(ml)}
                disabled={saving}
                className="relative overflow-hidden bg-white/8 hover:bg-cyan-500/15 border border-white/10 hover:border-cyan-500/30 text-white font-medium py-4 px-4 rounded-2xl transition-all duration-200 text-sm text-center group disabled:opacity-50"
              >
                <div className="text-2xl mb-1">💧</div>
                <div className="font-semibold">{label}</div>
                <div className="text-xs text-white/50">{ml} ml</div>
              </motion.button>
            ))}
          </motion.div>

          {/* Input personalizado */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.35, duration: 0.3 }}
            className="card mb-8"
          >
            <label className="label mb-3">Cantidad personalizada (ml)</label>
            <div className="flex gap-3">
              <input
                type="number"
                min="1"
                max="5000"
                value={personalizado}
                onChange={e => setPersonalizado(e.target.value)}
                placeholder="Ej: 200"
                className="input flex-1"
              />
              <motion.button
                whileHover={{ scale: 1.04 }}
                whileTap={{ scale: 0.96 }}
                onClick={() => {
                  if (personalizado && parseInt(personalizado) > 0) {
                    handleRegistrar(parseInt(personalizado))
                  }
                }}
                disabled={saving || !personalizado}
                className="btn-primary disabled:opacity-50"
              >
                Añadir
              </motion.button>
            </div>
          </motion.div>

          {/* Lista de registros */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.45, duration: 0.3 }}
            className="card"
          >
            <h2 className="section-title mb-4">Registros del día</h2>
            {diario.registros.length === 0 ? (
              <p className="text-slate-400 text-center py-8">No hay registros de agua hoy</p>
            ) : (
              <AnimatePresence>
                <div className="space-y-2">
                  {diario.registros.map(registro => (
                    <motion.div
                      key={registro.id}
                      layout
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: 20, height: 0 }}
                      transition={{ type: 'spring', stiffness: 300, damping: 30 }}
                      className="flex items-center justify-between bg-white/5 border border-white/10 hover:border-cyan-500/30 rounded-xl p-3 transition-colors"
                    >
                      <div className="flex items-center gap-3">
                        <span className="text-2xl">💧</span>
                        <div>
                          <div className="text-slate-100 font-medium">{registro.cantidadMl} ml</div>
                          <div className="text-xs text-slate-400">
                            {new Date(`${registro.fecha}T${registro.hora}`).toLocaleTimeString('es-ES', {
                              hour: '2-digit',
                              minute: '2-digit'
                            })}
                          </div>
                        </div>
                      </div>
                      <motion.button
                        whileHover={{ scale: 1.1 }}
                        whileTap={{ scale: 0.9 }}
                        onClick={() => handleEliminar(registro.id)}
                        className="text-red-400 hover:text-red-300 hover:bg-red-900/30 p-2 rounded-lg transition-colors text-lg"
                      >
                        🗑️
                      </motion.button>
                    </motion.div>
                  ))}
                </div>
              </AnimatePresence>
            )}
          </motion.div>
        </>
      )}
    </motion.div>
  )
}
