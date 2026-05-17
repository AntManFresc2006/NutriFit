import { useEffect, useState } from 'react'
import { motion, AnimatePresence as AnimatePresenceMotion } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'

const AnimatePresence = AnimatePresenceMotion
import { generarPlan, getPlan, regenerarPlan, type PlanSemanal } from '../api/planSemanal'

const container = { hidden: {}, show: { transition: { staggerChildren: 0.1 } } }
const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } }

export default function PlanSemanal() {
  const { user } = useAuth()
  const [semanaInicio, setSemanaInicio] = useState<string>(obtenerLunesActual())
  const [plan, setPlan] = useState<PlanSemanal | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    cargarPlan()
  }, [semanaInicio])

  const cargarPlan = async () => {
    if (!user) return
    setLoading(true)
    setError(null)
    try {
      const response = await getPlan(user.usuarioId, semanaInicio)
      if (response) {
        const parsedPlan = JSON.parse(response.planJson)
        setPlan(parsedPlan)
      } else {
        setPlan(null)
      }
    } catch (err) {
      console.error('Error cargando plan:', err)
      setPlan(null)
    } finally {
      setLoading(false)
    }
  }

  const handleGenerar = async () => {
    if (!user) return
    setLoading(true)
    setError(null)
    try {
      const response = await generarPlan(user.usuarioId, semanaInicio)
      const parsedPlan = JSON.parse(response.planJson)
      setPlan(parsedPlan)
    } catch (err) {
      setError('Error al generar el plan. Intenta nuevamente.')
      console.error('Error generando plan:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleRegenerar = async () => {
    if (!user) return
    setLoading(true)
    setError(null)
    try {
      const response = await regenerarPlan(user.usuarioId, semanaInicio)
      const parsedPlan = JSON.parse(response.planJson)
      setPlan(parsedPlan)
    } catch (err) {
      setError('Error al regenerar el plan. Intenta nuevamente.')
      console.error('Error regenerando plan:', err)
    } finally {
      setLoading(false)
    }
  }

  const irASemanaAnterior = () => {
    const fecha = new Date(semanaInicio)
    fecha.setDate(fecha.getDate() - 7)
    setSemanaInicio(fecha.toISOString().split('T')[0])
  }

  const irASemanaProxima = () => {
    const fecha = new Date(semanaInicio)
    fecha.setDate(fecha.getDate() + 7)
    setSemanaInicio(fecha.toISOString().split('T')[0])
  }

  const irASemanaActual = () => {
    setSemanaInicio(obtenerLunesActual())
  }

  const formatearFecha = (dateStr: string) => {
    const date = new Date(dateStr + 'T00:00:00')
    return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' })
  }

  const obtenerRangoSemana = () => {
    const inicio = new Date(semanaInicio + 'T00:00:00')
    const fin = new Date(inicio)
    fin.setDate(fin.getDate() + 6)
    const inicioStr = inicio.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' })
    const finStr = fin.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' })
    return `${inicioStr} al ${finStr}`
  }

  const colorComida = (tipo: string) => {
    switch (tipo) {
      case 'desayuno':
        return 'bg-amber-500/10 border-amber-500/30 text-amber-400'
      case 'almuerzo':
        return 'bg-green-500/10 border-green-500/30 text-green-400'
      case 'merienda':
        return 'bg-orange-500/10 border-orange-500/30 text-orange-400'
      case 'cena':
        return 'bg-blue-500/10 border-blue-500/30 text-blue-400'
      default:
        return 'bg-slate-500/10 border-slate-500/30 text-slate-400'
    }
  }

  const colorIconoComida = (tipo: string) => {
    switch (tipo) {
      case 'desayuno':
        return 'text-amber-400'
      case 'almuerzo':
        return 'text-green-400'
      case 'merienda':
        return 'text-orange-400'
      case 'cena':
        return 'text-blue-400'
      default:
        return 'text-slate-400'
    }
  }

  const emojiComida = (tipo: string) => {
    switch (tipo) {
      case 'desayuno':
        return '🥐'
      case 'almuerzo':
        return '🍽️'
      case 'merienda':
        return '🥤'
      case 'cena':
        return '🍲'
      default:
        return '🍴'
    }
  }

  return (
    <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="flex-1 overflow-auto p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <h1 className="gradient-text text-3xl font-bold mb-2">Plan Semanal IA</h1>
          <p className="text-white/50 text-sm mb-4">Tu plan nutricional personalizado para esta semana</p>

          {/* Navegación de semanas */}
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="flex items-center justify-between card"
          >
            <motion.button
              onClick={irASemanaAnterior}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="px-4 py-2 bg-white/5 border border-white/10 hover:bg-white/10 text-white rounded-lg transition-colors"
            >
              ← Anterior
            </motion.button>

            <div className="text-center flex-1 mx-4">
              <div className="text-white/50 text-sm mb-1">Semana del</div>
              <div className="text-lg font-semibold text-white">{obtenerRangoSemana()}</div>
            </div>

            <div className="flex gap-2">
              <motion.button
                onClick={irASemanaActual}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="px-4 py-2 bg-white/5 border border-white/10 hover:bg-white/10 text-white rounded-lg transition-colors text-sm"
              >
                Hoy
              </motion.button>
              <motion.button
                onClick={irASemanaProxima}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="px-4 py-2 bg-white/5 border border-white/10 hover:bg-white/10 text-white rounded-lg transition-colors"
              >
                Próxima →
              </motion.button>
            </div>
          </motion.div>
        </div>

        {/* Estado sin plan */}
        <AnimatePresence mode="wait">
          {!loading && !plan && !error && (
            <motion.div
              key="no-plan"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="flex flex-col items-center justify-center py-24 card"
            >
              <div className="text-7xl mb-6">📅</div>
              <h2 className="text-2xl font-semibold text-white mb-2">No tienes plan para esta semana</h2>
              <p className="text-white/50 mb-8">Genera un plan personalizado con IA para esta semana</p>
              <motion.button
                onClick={handleGenerar}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="btn-primary"
              >
                Generar Plan con IA
              </motion.button>
            </motion.div>
          )}

          {/* Cargando */}
          {loading && (
            <motion.div
              key="loading"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="flex flex-col items-center justify-center py-24 card"
            >
              <div className="animate-spin w-12 h-12 border-4 border-emerald-500 border-t-transparent rounded-full mb-6" />
              <p className="text-white text-lg">
                {plan ? 'Regenerando tu plan personalizado...' : 'Generando tu plan personalizado...'}
              </p>
              <p className="text-white/50 text-sm mt-2">(puede tardar 30 segundos)</p>
            </motion.div>
          )}

          {/* Error */}
          {error && (
            <motion.div
              key="error"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="mb-6 bg-red-500/10 border border-red-500/30 rounded-lg p-4"
            >
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-red-400 font-semibold">Error</h3>
                  <p className="text-red-300 text-sm mt-1">{error}</p>
                </div>
                <motion.button
                  onClick={handleGenerar}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-colors text-sm"
                >
                  Reintentar
                </motion.button>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Plan con 7 días */}
        {!loading && plan && !error && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <div className="flex justify-end mb-4">
              <motion.button
                onClick={handleRegenerar}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="px-6 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/50 rounded-lg transition-colors"
              >
                🔄 Regenerar plan
              </motion.button>
            </div>

            <motion.div
              variants={container}
              initial="hidden"
              animate="show"
              className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
            >
              {plan.dias.map((dia, idx) => (
                <motion.div
                  key={idx}
                  variants={item}
                  whileHover={{ scale: 1.02, y: -4 }}
                  transition={{ type: 'spring', stiffness: 300 }}
                  className="card overflow-hidden border-t-4 border-t-emerald-500"
                >
                  {/* Header del día */}
                  <div className="bg-emerald-500/10 border-b border-emerald-500/30 px-4 py-3 mb-3">
                    <div className="flex items-baseline justify-between">
                      <div>
                        <div className="font-semibold text-white">{dia.dia}</div>
                        <div className="text-xs text-white/50">{formatearFecha(dia.fecha)}</div>
                      </div>
                      <div className="text-right">
                        <div className="text-lg font-bold text-emerald-400">{dia.totalKcal}</div>
                        <div className="text-xs text-white/50">kcal</div>
                      </div>
                    </div>
                  </div>

                  {/* Comidas */}
                  <div className="space-y-2 mb-3">
                    {Object.entries(dia.comidas).map(([tipo, comida]) => (
                      <motion.div
                        key={tipo}
                        whileHover={{ scale: 1.02 }}
                        className={`border rounded-lg p-2.5 ${colorComida(tipo)}`}
                      >
                        <div className="flex items-start gap-2 mb-1.5">
                          <span className={`text-lg ${colorIconoComida(tipo)}`}>{emojiComida(tipo)}</span>
                          <div className="flex-1">
                            <div className="font-semibold text-xs capitalize text-white">{tipo}</div>
                            <div className="text-xs text-white/60 mt-0.5 line-clamp-1">
                              {comida.descripcion}
                            </div>
                          </div>
                        </div>

                        <div className="text-xs space-y-0.5 text-white/70">
                          <div className="flex justify-between">
                            <span>P:</span>
                            <span className="font-semibold">{comida.proteinas}g</span>
                          </div>
                          <div className="flex justify-between">
                            <span>C:</span>
                            <span className="font-semibold">{comida.carbos}g</span>
                          </div>
                          <div className="flex justify-between">
                            <span>G:</span>
                            <span className="font-semibold">{comida.grasas}g</span>
                          </div>
                        </div>

                        {/* Barra de progreso kcal */}
                        <div className="mt-1.5 bg-white/10 rounded h-1.5 overflow-hidden">
                          <motion.div
                            className="bg-current h-full"
                            initial={{ width: 0 }}
                            animate={{ width: `${Math.min((comida.kcal / dia.totalKcal) * 100, 100)}%` }}
                            transition={{ duration: 0.8, delay: idx * 0.05 }}
                          />
                        </div>
                        <div className="text-xs text-white/50 mt-0.5">{comida.kcal} kcal</div>
                      </motion.div>
                    ))}
                  </div>

                  {/* Totales */}
                  <div className="bg-white/5 border-t border-white/10 px-3 py-2 text-xs text-white/70 space-y-0.5">
                    <div className="flex justify-between">
                      <span>P:</span>
                      <span className="font-semibold">{dia.totalProteinas}g</span>
                    </div>
                    <div className="flex justify-between">
                      <span>C:</span>
                      <span className="font-semibold">{dia.totalCarbos}g</span>
                    </div>
                    <div className="flex justify-between">
                      <span>G:</span>
                      <span className="font-semibold">{dia.totalGrasas}g</span>
                    </div>
                  </div>
                </motion.div>
              ))}
            </motion.div>
          </motion.div>
        )}
      </div>
    </motion.div>
  )
}

function obtenerLunesActual(): string {
  const hoy = new Date()
  const dia = hoy.getDay()
  const diferencia = dia === 0 ? -6 : 1 - dia
  const lunes = new Date(hoy)
  lunes.setDate(lunes.getDate() + diferencia)
  return lunes.toISOString().split('T')[0]
}
