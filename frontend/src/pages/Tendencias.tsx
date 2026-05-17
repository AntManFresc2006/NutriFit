import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getTendencias, type TendenciasData } from '../api/tendencias'
import PesoChart from '../components/PesoChart'
import NutriScoreChart from '../components/NutriScoreChart'
import MacrosChart from '../components/MacrosChart'
import EjercicioHeatmap from '../components/EjercicioHeatmap'

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
      <div className="p-6 max-w-7xl mx-auto">
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
    )
  }

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-100 mb-4">Análisis de Tendencias 📈</h1>
        <div className="flex gap-2 flex-wrap">
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

      {!data ? (
        <div className="card text-center py-16 text-slate-400">
          <p className="text-4xl mb-3">📊</p>
          <p>Sin datos disponibles. Registra comidas, pesajes y ejercicios primero.</p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <StatsCard
              label="Días con ejercicio"
              value={`${data.ejercicio.filter(e => e.tuvoEjercicio).length}/${data.ejercicio.length}`}
              icon="🏃"
              color="text-blue-400"
            />
            <StatsCard
              label="NutriScore promedio"
              value={data.nutriScore.length > 0
                ? Math.round(data.nutriScore.reduce((acc, p) => acc + p.score, 0) / data.nutriScore.length)
                : '—'}
              icon="⭐"
              color="text-yellow-400"
            />
            <StatsCard
              label="Peso perdido/ganado"
              value={data.peso.length > 1
                ? `${data.peso[data.peso.length - 1].pesoKg - data.peso[0].pesoKg > 0 ? '+' : ''}${(data.peso[data.peso.length - 1].pesoKg - data.peso[0].pesoKg).toFixed(1)} kg`
                : '—'}
              icon="📉"
              color="text-green-400"
            />
            <StatsCard
              label="Racha actual"
              value={calcularRacha(data.nutriScore) + ' días'}
              icon="🔥"
              color="text-orange-400"
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <PesoChart data={data.peso} pesoObjetivo={data.pesoObjetivo} />
            <NutriScoreChart data={data.nutriScore} />
            <MacrosChart data={data.macros} />
            <EjercicioHeatmap data={data.ejercicio} />
          </div>
        </>
      )}
    </div>
  )
}

function StatsCard({
  label,
  value,
  icon,
  color,
}: {
  label: string
  value: string | number
  icon: string
  color: string
}) {
  return (
    <div className="card py-4">
      <p className="text-xs text-slate-500 uppercase tracking-wide">{label}</p>
      <p className={`text-2xl font-bold mt-2 ${color}`}>{value}</p>
      <span className="text-3xl block mt-2">{icon}</span>
    </div>
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
