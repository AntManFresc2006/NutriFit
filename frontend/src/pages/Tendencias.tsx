import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Activity, Star, TrendingDown, Flame, Utensils, Scale, Dumbbell } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useFechaPersistente } from '../hooks/useFechaPersistente'
import { getTendencias, type TendenciasData } from '../api/tendencias'
import PesoChart from '../components/PesoChart'
import NutriScoreChart from '../components/NutriScoreChart'
import MacrosChart from '../components/MacrosChart'
import EjercicioHeatmap from '../components/EjercicioHeatmap'

const container = { hidden: {}, show: { transition: { staggerChildren: 0.1 } } }
const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } }

const MODULE_MAP = {
  comidas:    { label: 'Comidas',    route: '/comidas',    storageKey: 'nutrifit_fecha_comidas',    color: 'text-emerald-400', border: 'border-emerald-500/40', bg: 'bg-emerald-500/10' },
  pesajes:    { label: 'Pesajes',    route: '/perfil',     storageKey: 'nutrifit_fecha_perfil',     color: 'text-blue-400',    border: 'border-blue-500/40',    bg: 'bg-blue-500/10'    },
  ejercicios: { label: 'Ejercicios', route: '/ejercicios', storageKey: 'nutrifit_fecha_ejercicios', color: 'text-orange-400',  border: 'border-orange-500/40',  bg: 'bg-orange-500/10'  },
} as const

type ModuleKey = keyof typeof MODULE_MAP

function dotClass(complete: boolean, isSelected: boolean): string {
  if (complete) return 'bg-emerald-500 shadow-lg shadow-emerald-500/30 cursor-default'
  if (isSelected) return 'bg-red-500 ring-2 ring-white/70 shadow-lg shadow-red-500/40 cursor-pointer'
  return 'bg-red-500/60 hover:bg-red-500 cursor-pointer'
}

export default function Tendencias() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [data, setData] = useState<TendenciasData | null>(null)
  const [loading, setLoading] = useState(true)
  const [dias, setDias] = useState(30)
  const [selectedDay, setSelectedDay] = useState<string | null>(null)
  const [fechaFin, setFechaFin] = useFechaPersistente('nutrifit_fecha_tendencias')

  useEffect(() => {
    if (!user) return
    setLoading(true)
    setSelectedDay(null)
    getTendencias(user.usuarioId, dias, fechaFin)
      .then(setData)
      .catch(() => setData(null))
      .finally(() => setLoading(false))
  }, [user, dias, fechaFin])

  const dateRange = Array.from({ length: dias }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (dias - 1 - i))
    return d.toISOString().split('T')[0]
  })

  const nutriScoreDates = new Set(data?.nutriScore.map(n => n.fecha.split('T')[0]) ?? [])
  const pesoDates       = new Set(data?.peso.map(p => p.fecha.split('T')[0]) ?? [])
  const ejercicioDates  = new Set(data?.ejercicio.map(e => e.fecha.split('T')[0]) ?? [])

  const totalComidas   = nutriScoreDates.size
  const totalPeso      = pesoDates.size
  const totalEjercicio = ejercicioDates.size

  const allComplete = totalComidas === dias && totalPeso === dias && totalEjercicio === dias

  const getMissing = (date: string): ModuleKey[] => {
    const missing: ModuleKey[] = []
    if (!nutriScoreDates.has(date)) missing.push('comidas')
    if (!pesoDates.has(date))       missing.push('pesajes')
    if (!ejercicioDates.has(date))  missing.push('ejercicios')
    return missing
  }

  const handleNavigate = (module: ModuleKey, date: string) => {
    localStorage.setItem(MODULE_MAP[module].storageKey, date)
    navigate(MODULE_MAP[module].route)
  }

  // Pre-compute stats for full analysis view
  const nutriScoreAvg = data && data.nutriScore.length > 0
    ? Math.round(data.nutriScore.reduce((acc, p) => acc + p.score, 0) / data.nutriScore.length)
    : null
  const pesoChange = data && data.peso.length > 1
    ? data.peso.at(-1)!.pesoKg - data.peso[0].pesoKg
    : null
  let pesoDiffDisplay: string
  if (pesoChange === null) {
    pesoDiffDisplay = '—'
  } else {
    const sign = pesoChange > 0 ? '+' : ''
    pesoDiffDisplay = `${sign}${pesoChange.toFixed(1)} kg`
  }

  const periodButtons = (
    <div className="flex items-center gap-3 flex-wrap">
      <div className="flex gap-2 flex-wrap">
        {[2, 3, 5, 30, 60, 90].map(d => (
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
      <input
        type="date"
        value={fechaFin}
        onChange={e => setFechaFin(e.target.value)}
        className="input w-auto py-1.5 text-sm"
        title="Fecha de fin del análisis"
      />
    </div>
  )

  if (loading) {
    return (
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="flex-1 overflow-auto p-6">
        <div className="max-w-7xl mx-auto">
          <div className="mb-6">
            <h1 className="text-2xl font-bold text-slate-100 mb-4">Análisis de Tendencias</h1>
            <div className="flex gap-2">
              {[2, 3, 5, 30, 60, 90].map(d => (
                <button key={d} onClick={() => setDias(d)} className={`px-4 py-2 rounded-lg font-medium transition-colors ${dias === d ? 'bg-green-500/20 text-green-400' : 'bg-slate-700 text-slate-300 hover:bg-slate-600'}`}>{d} días</button>
              ))}
            </div>
          </div>
          <div className="grid grid-cols-3 gap-4 mb-6">
            {[1, 2, 3].map(i => <div key={i} className="card h-28 animate-pulse bg-slate-700 rounded-2xl" />)}
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {[1, 2, 3, 4].map(i => <div key={i} className="card h-80 animate-pulse bg-slate-700 rounded-2xl" />)}
          </div>
        </div>
      </motion.div>
    )
  }

  // Main content extracted to avoid nested ternaries
  let mainContent: ReactNode

  if (data === null) {
    mainContent = (
      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="card py-10 px-6">
        <p className="text-white/70 font-medium text-center mb-6">Para ver tu análisis necesitas tener datos en al menos una de estas secciones:</p>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-white/5 border border-white/10 rounded-xl p-5 flex flex-col items-center gap-3">
            <Utensils className="w-8 h-8 text-emerald-400" />
            <p className="text-white font-semibold">Comidas</p>
            <p className="text-white/50 text-sm text-center">Registra lo que comes cada día desde la sección <span className="text-emerald-400 font-medium">Comidas</span>. Se usará para calcular tus macros y NutriScore.</p>
          </div>
          <div className="bg-white/5 border border-white/10 rounded-xl p-5 flex flex-col items-center gap-3">
            <Scale className="w-8 h-8 text-blue-400" />
            <p className="text-white font-semibold">Pesajes</p>
            <p className="text-white/50 text-sm text-center">Registra tu peso desde la sección <span className="text-blue-400 font-medium">Perfil</span>. Se mostrará tu evolución y progreso hacia tu objetivo.</p>
          </div>
          <div className="bg-white/5 border border-white/10 rounded-xl p-5 flex flex-col items-center gap-3">
            <Dumbbell className="w-8 h-8 text-orange-400" />
            <p className="text-white font-semibold">Ejercicios</p>
            <p className="text-white/50 text-sm text-center">Registra tus entrenamientos en <span className="text-orange-400 font-medium">Ejercicios</span>. Verás un mapa de calor con tu actividad diaria.</p>
          </div>
        </div>
      </motion.div>
    )
  } else if (allComplete) {
    mainContent = (
      <>
        <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <motion.div variants={item}>
            <StatsCard label="Días con ejercicio" value={`${data.ejercicio.filter(e => e.tuvoEjercicio).length}/${data.ejercicio.length}`} icon={<Activity className="w-8 h-8 text-blue-400" />} color="text-blue-400" />
          </motion.div>
          <motion.div variants={item}>
            <StatsCard label="NutriScore promedio" value={nutriScoreAvg ?? '—'} icon={<Star className="w-8 h-8 text-yellow-400" />} color="text-yellow-400" />
          </motion.div>
          <motion.div variants={item}>
            <StatsCard label="Peso perdido/ganado" value={pesoDiffDisplay} icon={<TrendingDown className="w-8 h-8 text-green-400" />} color="text-green-400" />
          </motion.div>
          <motion.div variants={item}>
            <StatsCard label="Racha actual" value={calcularRacha(data.nutriScore) + ' días'} icon={<Flame className="w-8 h-8 text-orange-400" />} color="text-orange-400" />
          </motion.div>
        </motion.div>

        <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <motion.div variants={item}><PesoChart data={data.peso} pesoObjetivo={data.pesoObjetivo} /></motion.div>
          <motion.div variants={item}><NutriScoreChart data={data.nutriScore} /></motion.div>
          <motion.div variants={item}><MacrosChart data={data.macros} /></motion.div>
          <motion.div variants={item}><EjercicioHeatmap data={data.ejercicio} /></motion.div>
        </motion.div>
      </>
    )
  } else {
    mainContent = (
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }} className="card">
        <h2 className="text-sm font-semibold text-white/50 uppercase tracking-wide mb-1">Días completados</h2>
        <p className="text-white/40 text-xs mb-4">Verde = todo registrado · Rojo = faltan datos · Haz click en rojo para ver qué falta</p>

        <div className="flex flex-wrap gap-2">
          {dateRange.map(date => {
            const missing = getMissing(date)
            const complete = missing.length === 0
            const isSelected = selectedDay === date
            const cls = dotClass(complete, isSelected)
            return (
              <motion.button
                key={date}
                whileHover={{ scale: complete ? 1 : 1.25 }}
                whileTap={{ scale: complete ? 1 : 0.9 }}
                onClick={() => { if (!complete) setSelectedDay(isSelected ? null : date) }}
                title={complete ? `✓ ${date}` : `${date} — falta: ${missing.map(k => MODULE_MAP[k].label).join(', ')}`}
                className={`w-8 h-8 rounded-full transition-all duration-200 ${cls}`}
              />
            )
          })}
        </div>

        <AnimatePresence>
          {selectedDay && (
            <motion.div
              key={selectedDay}
              initial={{ opacity: 0, height: 0, marginTop: 0 }}
              animate={{ opacity: 1, height: 'auto', marginTop: 16 }}
              exit={{ opacity: 0, height: 0, marginTop: 0 }}
              className="overflow-hidden"
            >
              <div className="bg-white/5 border border-white/10 rounded-xl p-4">
                <p className="text-white/60 text-sm mb-3">
                  El <span className="text-white font-semibold">{selectedDay}</span> te falta registrar:
                </p>
                <div className="flex flex-col gap-2">
                  {getMissing(selectedDay).map(moduleKey => {
                    const mod = MODULE_MAP[moduleKey]
                    return (
                      <div key={moduleKey} className={`flex items-center justify-between ${mod.bg} border ${mod.border} rounded-xl px-4 py-3`}>
                        <span className={`font-semibold text-sm ${mod.color}`}>{mod.label}</span>
                        <motion.button
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                          onClick={() => handleNavigate(moduleKey, selectedDay)}
                          className="btn-primary text-xs py-1.5 px-3"
                        >
                          Haz click aquí para cambiarlo
                        </motion.button>
                      </div>
                    )
                  })}
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    )
  }

  return (
    <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="flex-1 overflow-auto p-6">
      <div className="max-w-7xl mx-auto">

        {/* Header */}
        <div className="mb-6">
          <h1 className="gradient-text text-3xl font-bold mb-2">Análisis de Tendencias</h1>
          <p className="text-white/50 text-sm mb-4">Visualiza tu progreso en los últimos días</p>
          {periodButtons}
        </div>

        {/* Counter cards — always visible */}
        <motion.div variants={container} initial="hidden" animate="show" className="grid grid-cols-3 gap-4 mb-6">
          <motion.div variants={item}>
            <CounterCard label="Comidas"    icon={<Utensils className="w-6 h-6" />}  count={totalComidas}   total={dias} iconColor="text-emerald-400" />
          </motion.div>
          <motion.div variants={item}>
            <CounterCard label="Pesajes"    icon={<Scale className="w-6 h-6" />}     count={totalPeso}      total={dias} iconColor="text-blue-400"    />
          </motion.div>
          <motion.div variants={item}>
            <CounterCard label="Ejercicios" icon={<Dumbbell className="w-6 h-6" />}  count={totalEjercicio} total={dias} iconColor="text-orange-400"  />
          </motion.div>
        </motion.div>

        {mainContent}
      </div>
    </motion.div>
  )
}

function CounterCard({ label, icon, count, total, iconColor }: {
  readonly label: string
  readonly icon: ReactNode
  readonly count: number
  readonly total: number
  readonly iconColor: string
}) {
  const complete = count === total
  return (
    <motion.div
      whileHover={{ scale: 1.04, y: -3 }}
      transition={{ type: 'spring', stiffness: 300 }}
      className="card py-5 flex flex-col items-center text-center gap-1"
    >
      <div className={iconColor}>{icon}</div>
      <p className={`text-4xl font-bold mt-1 tabular-nums ${complete ? 'text-emerald-400' : 'text-red-400'}`}>{count}</p>
      <p className="text-white/40 text-xs">/ {total} días</p>
      <p className="text-white/60 text-sm font-medium mt-1">{label}</p>
    </motion.div>
  )
}

function StatsCard({ label, value, icon, color }: {
  readonly label: string
  readonly value: string | number
  readonly icon: ReactNode
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
      <div className="mt-2">{icon}</div>
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
    if (diffDays === 1) racha++
    else break
  }
  return racha
}
