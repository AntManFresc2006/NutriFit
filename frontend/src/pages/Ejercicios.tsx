import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Wind, Dumbbell, Trash2, X } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import {
  getEjercicios,
  getRegistros,
  registrarEjercicioAerobico,
  registrarEjercicioAnaerobico,
  deleteRegistro,
} from '../api/ejercicios'
import type { Ejercicio, RegistroEjercicio } from '../types'

type TipoFlujo = 'AEROBICO' | 'ANAEROBICO'
type Intensidad = 'BAJA' | 'MEDIA' | 'ALTA'
type Paso = 'tipo' | 'ejercicio' | 'detalle'

function today() {
  return new Date().toISOString().split('T')[0]
}

const INTENSIDADES: { value: Intensidad; label: string; desc: string; color: string }[] = [
  { value: 'BAJA',  label: 'Baja',  desc: 'Muchas reps · poco peso',    color: 'bg-green-500' },
  { value: 'MEDIA', label: 'Media', desc: 'Reps normales · peso moderado', color: 'bg-yellow-400' },
  { value: 'ALTA',  label: 'Alta',  desc: 'Pocas reps · mucho peso',     color: 'bg-red-500' },
]

export default function Ejercicios() {
  const { user } = useAuth()
  const [fecha, setFecha] = useState(today())
  const [registros, setRegistros] = useState<RegistroEjercicio[]>([])
  const [loading, setLoading] = useState(true)
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null)

  // flujo de registro
  const [paso, setPaso] = useState<Paso | null>(null)
  const [tipoFlujo, setTipoFlujo] = useState<TipoFlujo | null>(null)
  const [ejercicios, setEjercicios] = useState<Ejercicio[]>([])
  const [query, setQuery] = useState('')
  const [selected, setSelected] = useState<Ejercicio | null>(null)
  const [duracion, setDuracion] = useState(30)
  const [intensidad, setIntensidad] = useState<Intensidad>('MEDIA')
  const [numSeries, setNumSeries] = useState(3)
  const [saving, setSaving] = useState(false)

  const loadRegistros = () => {
    if (!user) return
    setLoading(true)
    getRegistros(user.usuarioId, fecha)
      .then(setRegistros)
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadRegistros() }, [user, fecha])

  useEffect(() => {
    if (paso !== 'ejercicio' || !tipoFlujo) return
    const t = setTimeout(() => {
      getEjercicios(query.trim() ? { q: query, tipo: tipoFlujo } : { tipo: tipoFlujo })
        .then(setEjercicios)
    }, 300)
    return () => clearTimeout(t)
  }, [query, paso, tipoFlujo])

  const iniciarFlujo = (tipo: TipoFlujo) => {
    setTipoFlujo(tipo)
    setSelected(null)
    setQuery('')
    setDuracion(30)
    setIntensidad('MEDIA')
    setNumSeries(3)
    setPaso('ejercicio')
  }

  const seleccionarEjercicio = (e: Ejercicio) => {
    setSelected(e)
    setPaso('detalle')
  }

  const handleRegistrar = async () => {
    if (!selected || !user) return
    setSaving(true)
    try {
      let r: RegistroEjercicio
      if (tipoFlujo === 'AEROBICO') {
        r = await registrarEjercicioAerobico(user.usuarioId, selected.id, fecha, duracion)
      } else {
        r = await registrarEjercicioAnaerobico(user.usuarioId, selected.id, fecha, intensidad, numSeries)
      }
      setRegistros((prev) => [...prev, r])
      setPaso(null)
      setTipoFlujo(null)
      setSelected(null)
    } finally {
      setSaving(false)
    }
  }

  const cancelar = () => {
    setPaso(null)
    setTipoFlujo(null)
    setSelected(null)
    setQuery('')
  }

  const confirmDeleteEjercicio = async (id: number) => {
    if (!user) return
    await deleteRegistro(id, user.usuarioId)
    setRegistros((prev) => prev.filter((r) => r.id !== id))
    setConfirmDeleteId(null)
  }

  const totalKcal = registros.reduce((s, r) => s + r.kcalQuemadas, 0)

  const kcalPreview = selected
    ? tipoFlujo === 'AEROBICO'
      ? Math.round((selected.met * 70 / 60) * duracion)
      : Math.round(selected.met * ({ BAJA: 2.0, MEDIA: 3.5, ALTA: 5.5 }[intensidad]) * numSeries)
    : 0

  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="p-6 max-w-4xl mx-auto"
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold gradient-text">Ejercicios</h1>
          <p className="text-slate-400 text-sm mt-0.5">Actividad física registrada</p>
        </div>
        <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} className="input w-auto" />
      </div>

      {/* Resumen kcal */}
      <AnimatePresence>
        {registros.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0 }}
            className="card py-4 mb-6 flex items-center gap-6"
          >
            <div>
              <p className="text-xs text-slate-500 uppercase tracking-wide">Kcal quemadas</p>
              <p className="text-2xl font-bold gradient-text mt-0.5">{Math.round(totalKcal)}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500 uppercase tracking-wide">Ejercicios</p>
              <p className="text-2xl font-bold gradient-text mt-0.5">{registros.length}</p>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Botón registrar */}
      {paso === null && (
        <div className="flex justify-end mb-4">
          <button onClick={() => setPaso('tipo')} className="btn-primary">
            + Registrar ejercicio
          </button>
        </div>
      )}

      {/* ── FLUJO PASO A PASO ── */}
      <AnimatePresence mode="wait">

        {/* PASO 1: elegir tipo */}
        {paso === 'tipo' && (
          <motion.div
            key="paso-tipo"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -12 }}
            className="card mb-6 space-y-4"
          >
            <div className="flex items-center justify-between">
              <h2 className="font-semibold text-white">¿Qué tipo de ejercicio?</h2>
              <button onClick={cancelar} className="text-slate-500 hover:text-white text-sm">Cancelar</button>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <button
                onClick={() => iniciarFlujo('AEROBICO')}
                className="flex flex-col items-center gap-2 p-5 rounded-xl border border-white/10 hover:border-emerald-500/50 hover:bg-emerald-500/5 transition-all"
              >
                <Wind className="w-10 h-10 text-emerald-400" />
                <span className="font-semibold text-white">Aeróbico</span>
                <span className="text-xs text-slate-400 text-center">Cardio · quema por tiempo</span>
              </button>
              <button
                onClick={() => iniciarFlujo('ANAEROBICO')}
                className="flex flex-col items-center gap-2 p-5 rounded-xl border border-white/10 hover:border-cyan-500/50 hover:bg-cyan-500/5 transition-all"
              >
                <Dumbbell className="w-10 h-10 text-cyan-400" />
                <span className="font-semibold text-white">Anaeróbico</span>
                <span className="text-xs text-slate-400 text-center">Fuerza · series e intensidad</span>
              </button>
            </div>
          </motion.div>
        )}

        {/* PASO 2: elegir ejercicio */}
        {paso === 'ejercicio' && (
          <motion.div
            key="paso-ejercicio"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -12 }}
            className="card mb-6 space-y-3"
          >
            <div className="flex items-center gap-2">
              <button onClick={() => setPaso('tipo')} className="text-slate-500 hover:text-white text-sm">← Volver</button>
              <span className="text-white font-semibold flex-1">
                {tipoFlujo === 'AEROBICO' ? 'Aeróbico' : 'Anaeróbico'} · Elige ejercicio
              </span>
              <button onClick={cancelar} className="text-slate-500 hover:text-white"><X className="w-4 h-4" /></button>
            </div>
            <input
              type="text"
              className="input"
              placeholder="Buscar..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              autoFocus
            />
            <div className="max-h-72 overflow-y-auto space-y-0.5">
              {ejercicios.map((e) => (
                <button
                  key={e.id}
                  onClick={() => seleccionarEjercicio(e)}
                  className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-white/5 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-slate-200 text-sm font-medium">{e.nombre}</span>
                    <span className="text-xs text-slate-500">{e.categoria}</span>
                  </div>
                </button>
              ))}
              {ejercicios.length === 0 && (
                <p className="text-slate-500 text-sm px-3 py-4 text-center">Sin resultados</p>
              )}
            </div>
          </motion.div>
        )}

        {/* PASO 3: detalle */}
        {paso === 'detalle' && selected && (
          <motion.div
            key="paso-detalle"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -12 }}
            className="card mb-6 space-y-4"
          >
            <div className="flex items-center gap-2">
              <button onClick={() => setPaso('ejercicio')} className="text-slate-500 hover:text-white text-sm">← Volver</button>
              <span className="text-white font-semibold flex-1">{selected.nombre}</span>
              <button onClick={cancelar} className="text-slate-500 hover:text-white"><X className="w-4 h-4" /></button>
            </div>

            {tipoFlujo === 'AEROBICO' ? (
              <div className="space-y-3">
                <label className="label" htmlFor="det-duracion">Duración (minutos)</label>
                <input
                  id="det-duracion"
                  type="number"
                  className="input w-36"
                  value={duracion}
                  min={1}
                  max={600}
                  onChange={(e) => setDuracion(Math.max(1, Number(e.target.value) || 1))}
                />
              </div>
            ) : (
              <div className="space-y-4">
                {/* Intensidad */}
                <div>
                  <p className="label mb-2">Intensidad</p>
                  <div className="grid grid-cols-3 gap-2">
                    {INTENSIDADES.map((i) => (
                      <button
                        key={i.value}
                        onClick={() => setIntensidad(i.value)}
                        className={`flex flex-col items-center gap-1 p-3 rounded-xl border transition-all ${
                          intensidad === i.value
                            ? 'border-emerald-500/60 bg-emerald-500/10 text-white'
                            : 'border-white/10 text-slate-400 hover:border-white/20'
                        }`}
                      >
                        <span className={`w-3 h-3 rounded-full ${i.color}`} />
                        <span className="text-sm font-medium">{i.label}</span>
                        <span className="text-[10px] text-center leading-tight opacity-70">{i.desc}</span>
                      </button>
                    ))}
                  </div>
                </div>
                {/* Series */}
                <div>
                  <label className="label" htmlFor="det-series">Número de series</label>
                  <input
                    id="det-series"
                    type="number"
                    className="input w-28 mt-1"
                    value={numSeries}
                    min={1}
                    max={20}
                    onChange={(e) => setNumSeries(Math.max(1, Number(e.target.value) || 1))}
                  />
                </div>
              </div>
            )}

            {/* Preview kcal */}
            <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-white/5 border border-white/8">
              <span className="text-slate-400 text-sm">Kcal estimadas:</span>
              <span className="gradient-text font-bold">≈ {kcalPreview} kcal</span>
            </div>

            <button
              onClick={handleRegistrar}
              disabled={saving}
              className="btn-primary w-full"
            >
              {saving ? 'Guardando...' : 'Registrar'}
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Lista de registros del día */}
      {loading ? (
        <div className="flex justify-center py-16">
          <div className="animate-spin w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full" />
        </div>
      ) : registros.length === 0 ? (
        <div className="card text-center py-16 text-slate-400">
          <p>Sin ejercicios registrados para este día.</p>
        </div>
      ) : (
        <div className="card overflow-hidden p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-700">
                  <th className="px-4 py-3 text-slate-400 font-medium text-left">Ejercicio</th>
                  <th className="px-4 py-3 text-slate-400 font-medium text-left">Detalle</th>
                  <th className="px-4 py-3 text-slate-400 font-medium text-right">Kcal</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody>
                <AnimatePresence>
                  {registros.map((r, idx) => (
                    <motion.tr
                      key={r.id}
                      layout
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: 20 }}
                      transition={{ delay: idx * 0.04, type: 'spring', stiffness: 300, damping: 30 }}
                      className="border-b border-slate-700/50 hover:bg-white/5 transition-colors"
                    >
                      <td className="px-4 py-3 font-medium text-slate-100">{r.nombreEjercicio}</td>
                      <td className="px-4 py-3 text-slate-400 text-xs">
                        {r.tipoEjercicio === 'ANAEROBICO'
                          ? `${r.numSeries ?? '?'} series · ${r.intensidad ?? '?'}`
                          : `${r.duracionMin} min`}
                      </td>
                      <td className="px-4 py-3 text-right gradient-text font-semibold">
                        {Math.round(r.kcalQuemadas)}
                      </td>
                      <td className="px-4 py-3 text-right">
                        <AnimatePresence>
                          {confirmDeleteId === r.id ? (
                            <motion.div
                              initial={{ opacity: 0 }}
                              animate={{ opacity: 1 }}
                              exit={{ opacity: 0 }}
                              className="flex gap-1 justify-end"
                            >
                              <button
                                onClick={() => confirmDeleteEjercicio(r.id)}
                                className="btn-danger text-xs py-1 px-2"
                              >
                                Sí
                              </button>
                              <button
                                onClick={() => setConfirmDeleteId(null)}
                                className="btn-secondary text-xs py-1 px-2"
                              >
                                No
                              </button>
                            </motion.div>
                          ) : (
                            <button
                              onClick={() => setConfirmDeleteId(r.id)}
                              className="btn-danger"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          )}
                        </AnimatePresence>
                      </td>
                    </motion.tr>
                  ))}
                </AnimatePresence>
              </tbody>
            </table>
          </div>
        </div>
      )}
    </motion.div>
  )
}
