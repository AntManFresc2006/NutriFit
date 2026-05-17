import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getResumenDiario, getEvaluacionIA, getGamificacion } from '../api/resumen'
import { getRecuperacion } from '../api/ejercicios'
import MacroRing from '../components/MacroRing'
import { motion, AnimatePresence } from 'framer-motion'
import type { ResumenDiario } from '../types'
import type { Gamificacion } from '../api/resumen'
import type { RecuperacionData } from '../api/ejercicios'

function today() {
  return new Date().toISOString().split('T')[0]
}

function formatDate(date: string) {
  const d = new Date(date + 'T00:00:00')
  return d.toLocaleDateString('es-ES', { weekday: 'long', month: 'short', day: 'numeric' })
}

function balanceBadge(estado: string) {
  const map: Record<string, string> = {
    DEFICIT: 'badge bg-blue-500/20 text-blue-400',
    EQUILIBRADO: 'badge bg-green-500/20 text-green-400',
    SUPERAVIT: 'badge bg-amber-500/20 text-amber-400',
  }
  return map[estado] ?? 'badge bg-slate-700 text-slate-400'
}

export default function Dashboard() {
  const { user } = useAuth()
  const [resumen, setResumen] = useState<ResumenDiario | null>(null)
  const [loadingResumen, setLoadingResumen] = useState(true)
  const [ia, setIa] = useState('')
  const [loadingIa, setLoadingIa] = useState(false)
  const [fecha, setFecha] = useState(today())
  const [gamificacion, setGamificacion] = useState<Gamificacion | null>(null)
  const [recuperacion, setRecuperacion] = useState<RecuperacionData | null>(null)

  useEffect(() => {
    if (!user) return
    setLoadingResumen(true)
    setIa('')
    getResumenDiario(user.usuarioId, fecha)
      .then(setResumen)
      .catch(() => setResumen(null))
      .finally(() => setLoadingResumen(false))
    getGamificacion(user.usuarioId, fecha)
      .then(setGamificacion)
      .catch(() => setGamificacion(null))
    getRecuperacion(user.usuarioId, fecha)
      .then(setRecuperacion)
      .catch(() => setRecuperacion(null))
  }, [user, fecha])

  const handleIa = async () => {
    if (!resumen || !user) return
    setLoadingIa(true)
    setIa('')
    try {
      const res = await getEvaluacionIA({
        usuarioId: user.usuarioId,
        fecha,
        kcalConsumidas: resumen.kcalTotales,
        kcalQuemadas: resumen.kcalQuemadasTotales,
        proteinasTotales: resumen.proteinasTotales,
        grasasTotales: resumen.grasasTotales,
        carbosTotales: resumen.carbosTotales,
        tdee: resumen.tdee,
        balanceReal: resumen.balanceReal,
      })
      setIa(res.evaluacion)
    } catch {
      setIa('No se pudo obtener la evaluación. Inténtalo de nuevo.')
    } finally {
      setLoadingIa(false)
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.4 }}
      className="flex-1 overflow-auto p-6 min-h-screen"
    >
      <div className="max-w-5xl mx-auto">
        <div className="flex items-center justify-between mb-8 flex-wrap gap-3">
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
          >
            <h1 className="text-3xl font-bold text-white">
              Hola, <span className="gradient-text">{user?.nombre}</span> 👋
            </h1>
            <p className="text-white/40 text-sm mt-1">{formatDate(fecha)}</p>
          </motion.div>
          <motion.input
            initial={{ opacity: 0, x: 10 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.15 }}
            type="date"
            value={fecha}
            onChange={(e) => setFecha(e.target.value)}
            className="input w-auto"
          />
        </div>

        {loadingResumen ? (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="flex items-center justify-center h-64"
          >
            <div className="animate-spin w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full" />
          </motion.div>
        ) : !resumen ? (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            className="card text-center py-16 text-white/40"
          >
            <p className="text-4xl mb-3">📋</p>
            <p>Sin datos para este día. Registra comidas y ejercicios primero.</p>
          </motion.div>
        ) : (
          <>
            <AnimatePresence>
              {recuperacion?.tieneEjercicioIntensivo && (
                <motion.div
                  initial={{ opacity: 0, y: -8 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -8 }}
                  className="card border border-blue-500/30 bg-blue-500/5 mb-4"
                >
                  <div className="flex items-start gap-3">
                    <span className="text-2xl">💪</span>
                    <div>
                      <p className="font-semibold text-blue-300 text-sm">Ventana de recuperación activa</p>
                      <p className="text-white/70 text-sm mt-0.5">
                        Registraste <strong>{recuperacion.ejercicioNombre}</strong> hoy (MET {recuperacion.met?.toFixed(1)}).
                        {' '}Prioriza: <strong>{recuperacion.sugerenciaProteinaG}g proteína</strong> + <strong>{recuperacion.sugerenciaCarbosG}g carbohidratos</strong> para recuperación muscular.
                      </p>
                      <p className="text-white/40 text-xs mt-1">Opciones: atún + arroz · yogur griego + plátano · pollo + pasta.</p>
                    </div>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Macro ring */}
              <motion.div
                initial={{ opacity: 0, scale: 0.95, y: 12 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                transition={{ delay: 0.15 }}
                className="card lg:col-span-1 flex flex-col items-center"
              >
                <h2 className="text-sm font-semibold text-white/60 uppercase tracking-wide mb-4 self-start section-title">
                  Calorías y macros
                </h2>
                <MacroRing
                  kcal={resumen.kcalTotales}
                  tdee={resumen.tdee}
                  proteinas={resumen.proteinasTotales}
                  grasas={resumen.grasasTotales}
                  carbos={resumen.carbosTotales}
                />
              </motion.div>

              {/* Stats */}
              <div className="lg:col-span-2 space-y-4">
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ staggerChildren: 0.05, delayChildren: 0.15 }}
                  className="grid grid-cols-2 gap-4"
                >
                  <StatCard icon="🔥" label="Kcal consumidas" value={`${Math.round(resumen.kcalTotales)} kcal`} color="text-amber-400" index={0} />
                  <StatCard icon="💪" label="Kcal quemadas" value={`${Math.round(resumen.kcalQuemadasTotales)} kcal`} color="text-blue-400" index={1} />
                  <StatCard icon="⚖️" label="TDEE" value={`${Math.round(resumen.tdee)} kcal`} color="text-slate-300" index={2} />
                  <StatCard
                    icon="📉"
                    label="Balance real"
                    value={`${resumen.balanceReal > 0 ? '+' : ''}${Math.round(resumen.balanceReal)} kcal`}
                    color={resumen.balanceReal > 0 ? 'text-amber-400' : 'text-blue-400'}
                    extra={<span className={balanceBadge(resumen.estadoBalance)}>{resumen.estadoBalance}</span>}
                    index={3}
                  />
                  {resumen.fechaObjetivo && (
                    <StatCard
                      icon="🎯"
                      label="Fecha estimada objetivo"
                      value={resumen.fechaObjetivo}
                      color="text-emerald-400"
                      extra={<span className="text-xs text-white/40">{resumen.diasParaObjetivo} días desde hoy</span>}
                      index={4}
                    />
                  )}
                </motion.div>

                {/* Racha y NutriScore */}
                {gamificacion && (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ staggerChildren: 0.08, delayChildren: 0.3 }}
                    className="grid grid-cols-2 gap-4"
                  >
                    <motion.div
                      variants={{
                        hidden: { opacity: 0, y: 8 },
                        show: { opacity: 1, y: 0 }
                      }}
                      initial="hidden"
                      animate="show"
                      className="card py-4"
                    >
                      <p className="text-xs text-white/50 uppercase tracking-wide">Racha de registro</p>
                      <p className="text-xl font-bold mt-1 gradient-text">
                        🔥 {gamificacion.racha} {gamificacion.racha === 1 ? 'día' : 'días'}
                      </p>
                      <p className="text-xs text-white/40 mt-1">días consecutivos</p>
                    </motion.div>
                    <motion.div
                      variants={{
                        hidden: { opacity: 0, y: 8 },
                        show: { opacity: 1, y: 0 }
                      }}
                      initial="hidden"
                      animate="show"
                      className="card py-4"
                    >
                      <p className="text-xs text-white/50 uppercase tracking-wide">NutriScore hoy</p>
                      <p className={`text-xl font-bold mt-1 ${
                        ['A','B'].includes(gamificacion.nutriGrade) ? 'text-emerald-400' :
                        gamificacion.nutriGrade === 'C' ? 'text-yellow-400' : 'text-red-400'
                      }`}>
                        {gamificacion.nutriGrade}
                        <span className="text-sm font-normal text-white/40 ml-1">({gamificacion.nutriScore}/100)</span>
                      </p>
                      <div className="mt-1 flex gap-2 text-xs text-white/40 flex-wrap">
                        <span title="Proteína">{gamificacion.cumpleProteina ? '✅' : '❌'} Prot.</span>
                        <span title="Balance">{gamificacion.cumpleBalance ? '✅' : '❌'} Bal.</span>
                        <span title="Ejercicio">{gamificacion.cumpleEjercicio ? '✅' : '❌'} Ej.</span>
                        <span title="Variedad">{gamificacion.cumpleVariedad ? '✅' : '❌'} Var.</span>
                      </div>
                    </motion.div>
                  </motion.div>
                )}

                {/* IA evaluation */}
                <motion.div
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.4 }}
                  className="card"
                >
                  <div className="flex items-center justify-between mb-3">
                    <h2 className="text-sm font-semibold text-white/60 uppercase tracking-wide section-title">Evaluación IA</h2>
                    <motion.button
                      whileHover={{ scale: 1.02 }}
                      whileTap={{ scale: 0.97 }}
                      onClick={handleIa}
                      className="btn-primary text-sm py-1.5"
                      disabled={loadingIa}
                    >
                      {loadingIa ? (
                        <span className="flex items-center gap-2">
                          <span className="animate-spin w-3.5 h-3.5 border border-white border-t-transparent rounded-full" />
                          Analizando...
                        </span>
                      ) : (
                        '✨ Analizar día'
                      )}
                    </motion.button>
                  </div>
                  {ia ? (
                    <motion.p
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                      transition={{ delay: 0.1 }}
                      className="text-white/70 text-sm leading-relaxed whitespace-pre-wrap"
                    >
                      {ia}
                    </motion.p>
                  ) : (
                    <p className="text-white/40 text-sm">Pulsa el botón para obtener un análisis personalizado de tu día.</p>
                  )}
                </motion.div>
              </div>
            </div>
          </>
        )}
      </div>
    </motion.div>
  )
}

function StatCard({ icon, label, value, color, extra, index }: { readonly icon: string; readonly label: string; readonly value: string; readonly color: string; readonly extra?: React.ReactNode; readonly index?: number }) {
  return (
    <motion.div
      variants={{
        hidden: { opacity: 0, y: 8 },
        show: { opacity: 1, y: 0 }
      }}
      initial="hidden"
      animate="show"
      whileHover={{ scale: 1.02, transition: { duration: 0.2 } }}
      className="card py-4 cursor-default"
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs text-white/50 uppercase tracking-wide">{label}</p>
          <p className={`text-xl font-bold mt-1 ${color}`}>{value}</p>
          {extra && <div className="mt-1">{extra}</div>}
        </div>
        <span className="text-2xl">{icon}</span>
      </div>
    </motion.div>
  )
}
