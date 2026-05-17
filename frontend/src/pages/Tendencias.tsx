import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'
import { getTendencias, type TendenciasData } from '../api/tendencias'
import PesoChart from '../components/PesoChart'
import NutriScoreChart from '../components/NutriScoreChart'
import MacrosChart from '../components/MacrosChart'
import EjercicioHeatmap from '../components/EjercicioHeatmap'

const container = { hidden: {}, show: { transition: { staggerChildren: 0.1 } } }
const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } }

export default function Tendencias() {
  const { user } = useAuth()
  const [data, setData] = useState<TendenciasData | null>(null)
  const [loading, setLoading] = useState(true)
  const [dias, setDias] = useState(30)

  useEffect(() => {
    if (!user) return
    setLoading(true)
    getTendencias(user.usuarioId, dias)
      .then(setData)
      .catch(() => setData(null))
      .finally(() => setLoading(false))
  }, [user, dias])

  if (loading) {
    return (
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="flex-1 overflow-auto p-6">
        <div className="max-w-7xl mx-auto">
          <div className="mb-6">
            <h1 className="text-2xl font-bold text-slate-100 mb-4">Análisis de Tendencias 📈</h1>
            <div className="flex gap-2">
              {[30, 60, 90].map(d => (
                <button
                  key={d}
                  onClick={() => setDias(d)}
                  className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                    dias === d
                      ? 'bg-green-500/20 text-green-400'
                      : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                  }`}
                >
                  {d} días
                </button>
              ))}
            </div>
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="card h-80 animate-pulse bg-slate-700 rounded-2xl" />
            ))}
          </div>
        </div>
      </motion.div>
    )
  }

  return (
    <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="flex-1 overflow-auto p-6">
      <div className="max-w-7xl mx-auto">
        <div className="mb-6">
          <h1 className="gradient-text text-3xl font-bold mb-2">Análisis de Tendencias</h1>
          <p className="text-white/50 text-sm mb-4">Visualiza tu progreso en los últimos días</p>
          <div className="flex gap-2 flex-wrap">
            {[30, 60, 90].map(d => (
              <motion.button
                key={d}
                onClick={() => setDias(d)}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className={`px-4 py-2 rounded-lg font-medium transition-all ${
                  dias === d
                    ? 'bg-gradient-to-r from-emerald-500 to-cyan-500 text-white shadow-lg shadow-emerald-500/20'
                    : 'bg-white/5 border border-white/10 text-white/60 hover:text-white hover:bg-white/10'
                }`}
              >
                {d} días
              </motion.button>
            ))}
          </div>
        </div>

        {!data ? (
          <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="card text-center py-16 text-white/50">
            <p className="text-5xl mb-3">📊</p>
            <p>Sin datos disponibles. Registra comidas, pesajes y ejercicios primero.</p>
          </motion.div>
        ) : (
          <>
            <motion.div
              variants={container}
              initial="hidden"
              animate="show"
              className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6"
            >
              <motion.div variants={item}>
                <StatsCard
                  label="Días con ejercicio"
                  value={`${data.ejercicio.filter(e => e.tuvoEjercicio).length}/${data.ejercicio.length}`}
                  icon="🏃"
                  color="text-blue-400"
                />
              </motion.div>
              <motion.div variants={item}>
                <StatsCard
                  label="NutriScore promedio"
                  value={data.nutriScore.length > 0
                    ? Math.round(data.nutriScore.reduce((acc, p) => acc + p.score, 0) / data.nutriScore.length)
                    : '—'}
                  icon="⭐"
                  color="text-yellow-400"
                />
              </motion.div>
              <motion.div variants={item}>
                <StatsCard
                  label="Peso perdido/ganado"
                  value={data.peso.length > 1
                    ? `${data.peso[data.peso.length - 1].pesoKg - data.peso[0].pesoKg > 0 ? '+' : ''}${(data.peso[data.peso.length - 1].pesoKg - data.peso[0].pesoKg).toFixed(1)} kg`
                    : '—'}
                  icon="📉"
                  color="text-green-400"
                />
              </motion.div>
              <motion.div variants={item}>
                <StatsCard
                  label="Racha actual"
                  value={calcularRacha(data.nutriScore) + ' días'}
                  icon="🔥"
                  color="text-orange-400"
                />
              </motion.div>
            </motion.div>

            <motion.div
              variants={container}
              initial="hidden"
              animate="show"
              className="grid grid-cols-1 lg:grid-cols-2 gap-6"
            >
              <motion.div variants={item}>
                <PesoChart data={data.peso} pesoObjetivo={data.pesoObjetivo} />
              </motion.div>
              <motion.div variants={item}>
                <NutriScoreChart data={data.nutriScore} />
              </motion.div>
              <motion.div variants={item}>
                <MacrosChart data={data.macros} />
              </motion.div>
              <motion.div variants={item}>
                <EjercicioHeatmap data={data.ejercicio} />
              </motion.div>
            </motion.div>
          </>
        )}
      </div>
    </motion.div>
  )
}

function StatsCard({
  label,
  value,
  icon,
  color,
}: {
  readonly label: string
  readonly value: string | number
  readonly icon: string
  readonly color: string
}) {
  return (
    <motion.div
      whileHover={{ scale: 1.05, y: -4 }}
      transition={{ type: 'spring', stiffness: 300 }}
      className="card py-4"
    >
      <p className="text-xs text-white/50 uppercase tracking-wide">{label}</p>
      <p className={`text-2xl font-bold mt-2 ${color}`}>{value}</p>
      <span className="text-3xl block mt-2">{icon}</span>
    </motion.div>
  )
}

function calcularRacha(nutriScores: Array<{ fecha: string }>): number {
  if (!nutriScores.length) return 0
  let racha = 1
  for (let i = nutriScores.length - 1; i > 0; i--) {
    const fecha1 = new Date(nutriScores[i].fecha)
    const fecha2 = new Date(nutriScores[i - 1].fecha)
    const diffDays = (fecha1.getTime() - fecha2.getTime()) / (1000 * 60 * 60 * 24)
    if (diffDays === 1) {
      racha++
    } else {
      break
    }
  }
  return racha
}
