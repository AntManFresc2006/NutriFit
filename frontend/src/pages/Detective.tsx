import { useEffect, useRef, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Search, AlertTriangle, CheckCircle, XCircle, TrendingDown, Calendar, Dumbbell, RefreshCw } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { iniciarAnalisis, getAnalisis, type DetectiveAnalisis, type DetectiveStats, type Hallazgo } from '../api/detective'

const POLL_INTERVAL_MS = 5000
const PERIODOS = [7, 14, 30] as const

const container = { hidden: {}, show: { transition: { staggerChildren: 0.08 } } }
const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } }

export default function Detective() {
  const { user } = useAuth()
  const [dias, setDias] = useState<number>(30)
  const [analisis, setAnalisis] = useState<DetectiveAnalisis | null>(null)
  const [estado, setEstado] = useState<'idle' | 'analizando' | 'listo' | 'error'>('idle')
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null)

  useEffect(() => {
    cargarAnalisisPrevio()
    return () => detenerPolling()
  }, [])

  const cargarAnalisisPrevio = async () => {
    if (!user) return
    const data = await getAnalisis(user.usuarioId).catch(() => null)
    if (!data) return
    aplicarRespuesta(data)
  }

  const aplicarRespuesta = (data: DetectiveAnalisis) => {
    setAnalisis(data)
    if (data.estado === 'ANALIZANDO') {
      setEstado('analizando')
      iniciarPolling()
    } else if (data.estado === 'ERROR') {
      setEstado('error')
      setErrorMsg(data.errorMsg ?? 'Error desconocido al generar el análisis.')
    } else {
      setEstado('listo')
    }
  }

  const iniciarPolling = () => {
    detenerPolling()
    pollRef.current = setInterval(async () => {
      if (!user) return
      const data = await getAnalisis(user.usuarioId).catch(() => null)
      if (!data || data.estado === 'ANALIZANDO') return
      detenerPolling()
      aplicarRespuesta(data)
    }, POLL_INTERVAL_MS)
  }

  const detenerPolling = () => {
    if (pollRef.current) {
      clearInterval(pollRef.current)
      pollRef.current = null
    }
  }

  const handleIniciar = async () => {
    if (!user) return
    setEstado('analizando')
    setErrorMsg(null)

    const data = await iniciarAnalisis(user.usuarioId, dias).catch(() => null)
    if (!data) {
      setEstado('error')
      setErrorMsg('No se pudo iniciar el análisis. Inténtalo de nuevo.')
      return
    }
    setAnalisis(data)
    if (data.estado === 'ANALIZANDO') {
      iniciarPolling()
    } else {
      aplicarRespuesta(data)
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="flex-1 overflow-auto p-6"
    >
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <div className="flex items-center gap-3 mb-2">
            <Search className="w-8 h-8 text-emerald-400" />
            <h1 className="gradient-text text-3xl font-bold">Detective Nutricional IA</h1>
          </div>
          <p className="text-white/50 text-sm">
            Análisis forense de tu historial nutricional — descubre exactamente por qué no alcanzas tus objetivos
          </p>
        </div>

        {/* Controles */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="card flex flex-wrap items-center justify-between gap-4 mb-6"
        >
          <div className="flex items-center gap-2">
            <span className="text-white/50 text-sm">Período:</span>
            <div className="flex gap-1">
              {PERIODOS.map((p) => (
                <motion.button
                  key={p}
                  onClick={() => setDias(p)}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  className={`px-4 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                    dias === p
                      ? 'bg-emerald-500 text-white'
                      : 'bg-white/5 border border-white/10 text-white/70 hover:bg-white/10'
                  }`}
                >
                  {p} días
                </motion.button>
              ))}
            </div>
          </div>

          <motion.button
            onClick={handleIniciar}
            disabled={estado === 'analizando'}
            whileHover={{ scale: estado === 'analizando' ? 1 : 1.04 }}
            whileTap={{ scale: estado === 'analizando' ? 1 : 0.96 }}
            className={`flex items-center gap-2 px-6 py-2.5 rounded-lg font-semibold transition-colors ${
              estado === 'analizando'
                ? 'bg-white/10 text-white/40 cursor-not-allowed'
                : estado === 'listo'
                ? 'bg-white/10 border border-white/20 text-white hover:bg-white/15'
                : 'btn-primary'
            }`}
          >
            {estado === 'listo' ? (
              <><RefreshCw className="w-4 h-4" /> Nueva investigación</>
            ) : (
              <><Search className="w-4 h-4" /> Iniciar investigación</>
            )}
          </motion.button>
        </motion.div>

        <AnimatePresence mode="wait">
          {/* Idle */}
          {estado === 'idle' && (
            <motion.div
              key="idle"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="flex flex-col items-center justify-center py-24 card text-center"
            >
              <Search className="w-16 h-16 mx-auto mb-6 text-white/20" />
              <h2 className="text-2xl font-semibold text-white mb-2">El caso está abierto</h2>
              <p className="text-white/50 mb-8 max-w-md">
                Selecciona el período a analizar y pulsa "Iniciar investigación". La IA examinará tu historial y encontrará exactamente qué está fallando.
              </p>
            </motion.div>
          )}

          {/* Analizando — stats inmediatos + spinner IA */}
          {(estado === 'analizando' || estado === 'listo') && analisis && (
            <motion.div key="resultados" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
              {analisis.estadisticas && (
                <EstadisticasGrid stats={analisis.estadisticas} tdee={0} />
              )}

              {analisis.hallazgos && analisis.hallazgos.length > 0 && (
                <HallazgosSection hallazgos={analisis.hallazgos} />
              )}

              <InformeSection estado={estado} analisisIa={analisis.analisisIa} />
            </motion.div>
          )}

          {/* Error */}
          {estado === 'error' && (
            <motion.div
              key="error"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
              className="bg-red-500/10 border border-red-500/30 rounded-lg p-4 flex items-center justify-between"
            >
              <div>
                <h3 className="text-red-400 font-semibold">Error al generar el análisis</h3>
                <p className="text-red-300 text-sm mt-1">{errorMsg}</p>
              </div>
              <motion.button
                onClick={handleIniciar}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg text-sm transition-colors"
              >
                Reintentar
              </motion.button>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </motion.div>
  )
}

function EstadisticasGrid({ stats }: { stats: DetectiveStats; tdee: number }) {
  const tarjetas = [
    {
      label: 'Registro',
      valor: stats.tasaRegistro + '%',
      sub: stats.diasConRegistro + '/' + stats.diasAnalizados + ' días',
      color: stats.tasaRegistro >= 80 ? 'text-emerald-400' : stats.tasaRegistro >= 60 ? 'text-amber-400' : 'text-red-400',
      icon: <Calendar className="w-5 h-5" />,
    },
    {
      label: 'Sin registrar',
      valor: stats.diasSinRegistro + ' días',
      sub: 'potencialmente ocultos',
      color: stats.diasSinRegistro === 0 ? 'text-emerald-400' : stats.diasSinRegistro <= 5 ? 'text-amber-400' : 'text-red-400',
      icon: <XCircle className="w-5 h-5" />,
    },
    {
      label: stats.deficitRealMedio < 0 ? 'Déficit real' : 'Superávit real',
      valor: (stats.deficitRealMedio < 0 ? '' : '+') + Math.round(stats.deficitRealMedio) + ' kcal',
      sub: 'media diaria (días registrados)',
      color: stats.deficitRealMedio < -100 ? 'text-emerald-400' : stats.deficitRealMedio < 0 ? 'text-amber-400' : 'text-red-400',
      icon: <TrendingDown className="w-5 h-5" />,
    },
    {
      label: 'Proteína',
      valor: Math.round(stats.proteinaCumplimiento) + '%',
      sub: Math.round(stats.proteinaMediaDiaria) + 'g vs ' + Math.round(stats.proteinaObjetivo) + 'g objetivo',
      color: stats.proteinaCumplimiento >= 70 ? 'text-emerald-400' : stats.proteinaCumplimiento >= 40 ? 'text-amber-400' : 'text-red-400',
      icon: <Dumbbell className="w-5 h-5" />,
    },
  ]

  return (
    <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-2 lg:grid-cols-4 gap-3 mb-6">
      {tarjetas.map((t) => (
        <motion.div key={t.label} variants={item} className="card">
          <div className={`flex items-center gap-2 mb-2 ${t.color}`}>
            {t.icon}
            <span className="text-xs font-medium text-white/60">{t.label}</span>
          </div>
          <div className={`text-2xl font-bold ${t.color}`}>{t.valor}</div>
          <div className="text-xs text-white/40 mt-1">{t.sub}</div>
        </motion.div>
      ))}
    </motion.div>
  )
}

function HallazgosSection({ hallazgos }: { hallazgos: Hallazgo[] }) {
  const config = {
    CRITICO:     { bg: 'bg-red-500/10',     border: 'border-red-500/30',    text: 'text-red-400',    icon: <XCircle className="w-5 h-5 shrink-0 mt-0.5" />, label: 'CRÍTICO' },
    ADVERTENCIA: { bg: 'bg-amber-500/10',   border: 'border-amber-500/30',  text: 'text-amber-400',  icon: <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />, label: 'ADVERTENCIA' },
    POSITIVO:    { bg: 'bg-emerald-500/10', border: 'border-emerald-500/30', text: 'text-emerald-400', icon: <CheckCircle className="w-5 h-5 shrink-0 mt-0.5" />, label: 'POSITIVO' },
  }

  return (
    <motion.div variants={container} initial="hidden" animate="show" className="mb-6 space-y-3">
      <h2 className="text-white font-semibold text-lg mb-3">Hallazgos</h2>
      {hallazgos.map((h, i) => {
        const c = config[h.severidad]
        return (
          <motion.div
            key={i}
            variants={item}
            className={`${c.bg} border ${c.border} rounded-lg p-4 flex gap-3`}
          >
            <span className={c.text}>{c.icon}</span>
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className={`text-xs font-bold ${c.text} uppercase tracking-wide`}>{c.label}</span>
                <span className="text-white font-semibold text-sm">{h.titulo}</span>
              </div>
              <p className="text-white/60 text-sm">{h.descripcion}</p>
            </div>
          </motion.div>
        )
      })}
    </motion.div>
  )
}

function InformeSection({ estado, analisisIa }: { estado: string; analisisIa: string | null }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: 0.2 }}
      className="card"
    >
      <div className="flex items-center gap-3 mb-4">
        <Search className="w-5 h-5 text-emerald-400" />
        <h2 className="text-white font-semibold text-lg">Informe del Detective</h2>
      </div>

      {estado === 'analizando' && !analisisIa ? (
        <div className="flex flex-col items-center py-12 text-center">
          <div className="animate-spin w-10 h-10 border-4 border-emerald-500 border-t-transparent rounded-full mb-4" />
          <p className="text-white/70">La IA está analizando el caso...</p>
          <p className="text-white/40 text-sm mt-1">Puedes navegar a otros módulos, el informe estará listo al volver</p>
        </div>
      ) : analisisIa ? (
        <div className="prose prose-invert max-w-none">
          {analisisIa.split('\n\n').map((parrafo, i) => (
            <motion.p
              key={i}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.1 }}
              className="text-white/80 leading-relaxed mb-4 last:mb-0"
            >
              {parrafo}
            </motion.p>
          ))}
        </div>
      ) : null}
    </motion.div>
  )
}
