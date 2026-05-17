import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'
import { getEjercicios, getRegistros, registrarEjercicio, deleteRegistro, buscarExternoEjercicios, createEjercicio } from '../api/ejercicios'
import type { Ejercicio, EjercicioExterno, RegistroEjercicio } from '../types'

function today() {
  return new Date().toISOString().split('T')[0]
}

export default function Ejercicios() {
  const { user } = useAuth()
  const [fecha, setFecha] = useState(today())
  const [registros, setRegistros] = useState<RegistroEjercicio[]>([])
  const [loading, setLoading] = useState(true)
  const [showSearch, setShowSearch] = useState(false)
  const [ejercicios, setEjercicios] = useState<Ejercicio[]>([])
  const [ejerciciosExterno, setEjerciciosExterno] = useState<EjercicioExterno[]>([])
  const [loadingExterno, setLoadingExterno] = useState(false)
  const [query, setQuery] = useState('')
  const [selected, setSelected] = useState<Ejercicio | null>(null)
  const [duracion, setDuracion] = useState(30)
  const [saving, setSaving] = useState(false)
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null)

  const loadRegistros = () => {
    if (!user) return
    setLoading(true)
    getRegistros(user.usuarioId, fecha)
      .then(setRegistros)
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadRegistros() }, [user, fecha])

  useEffect(() => {
    const t = setTimeout(() => {
      if (showSearch) {
        getEjercicios(query || undefined).then(setEjercicios)
        if (query.trim().length >= 2) {
          setLoadingExterno(true)
          buscarExternoEjercicios(query.trim())
            .then(setEjerciciosExterno)
            .finally(() => setLoadingExterno(false))
        } else {
          setEjerciciosExterno([])
        }
      }
    }, 400)
    return () => clearTimeout(t)
  }, [query, showSearch])

  const handleSelectExterno = async (ext: EjercicioExterno) => {
    const saved = await createEjercicio(ext)
    setSelected(saved)
  }

  const handleAdd = async () => {
    if (!selected || !user) return
    setSaving(true)
    try {
      const r = await registrarEjercicio(user.usuarioId, selected.id, fecha, duracion)
      setRegistros((prev) => [...prev, r])
      setShowSearch(false)
      setSelected(null)
      setQuery('')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id: number) => {
    setConfirmDeleteId(id)
  }

  const confirmDeleteEjercicio = async (id: number) => {
    if (!user) return
    await deleteRegistro(id, user.usuarioId)
    setRegistros((prev) => prev.filter((r) => r.id !== id))
    setConfirmDeleteId(null)
  }

  const totalKcal = registros.reduce((s, r) => s + r.kcalQuemadas, 0)
  const totalMin = registros.reduce((s, r) => s + r.duracionMin, 0)

  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="p-6 max-w-4xl mx-auto"
    >
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1, duration: 0.3 }}
        className="flex items-center justify-between mb-6 flex-wrap gap-3"
      >
        <div>
          <h1 className="text-2xl font-bold gradient-text">🏃 Ejercicios</h1>
          <p className="text-slate-400 text-sm mt-0.5">Actividad física registrada</p>
        </div>
        <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} className="input w-auto" />
      </motion.div>

      <AnimatePresence>
        {registros.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ delay: 0.15, duration: 0.3 }}
            className="grid grid-cols-2 gap-4 mb-6"
          >
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.2, type: 'spring', stiffness: 120 }}
              className="card py-4"
            >
              <p className="text-xs text-slate-500 uppercase tracking-wide">Kcal quemadas</p>
              <p className="text-2xl font-bold gradient-text mt-1">{Math.round(totalKcal)}</p>
            </motion.div>
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.25, type: 'spring', stiffness: 120 }}
              className="card py-4"
            >
              <p className="text-xs text-slate-500 uppercase tracking-wide">Tiempo total</p>
              <p className="text-2xl font-bold gradient-text mt-1">{totalMin} min</p>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.2, duration: 0.3 }}
        className="flex justify-end mb-4"
      >
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={() => { setShowSearch(!showSearch); setSelected(null); setQuery(''); getEjercicios().then(setEjercicios) }}
          className="btn-primary"
        >
          {showSearch ? '✕ Cancelar' : '+ Registrar ejercicio'}
        </motion.button>
      </motion.div>

      <AnimatePresence>
        {showSearch && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
            className="card mb-6 space-y-3"
          >
            <h2 className="text-sm font-semibold gradient-text">Buscar ejercicio</h2>
            <input
              type="text"
              className="input"
              placeholder="🔍 Buscar por nombre..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              autoFocus
            />
            {!selected && (ejercicios.length > 0 || ejerciciosExterno.length > 0) && (
              <div className="max-h-64 overflow-y-auto space-y-1">
                {ejercicios.map((e) => (
                  <motion.button
                    key={e.id}
                    whileHover={{ x: 4, backgroundColor: 'rgba(255, 255, 255, 0.05)' }}
                    onClick={() => setSelected(e)}
                    className="w-full text-left px-3 py-2.5 rounded-lg transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-slate-200 text-sm font-medium">{e.nombre}</span>
                      <span className="text-xs text-slate-500">{Math.round(e.met * 70)} kcal/h</span>
                    </div>
                    <span className="text-xs text-slate-500">{e.categoria}</span>
                  </motion.button>
                ))}
                {loadingExterno && (
                  <p className="text-xs text-slate-500 px-3 py-1">Buscando en wger.de…</p>
                )}
                {!loadingExterno && ejerciciosExterno.length > 0 && (
                  <>
                    <p className="text-xs text-slate-500 px-3 pt-2 pb-1 border-t border-slate-700 mt-1">
                      🌐 wger.de
                    </p>
                    {ejerciciosExterno.map((ext, i) => (
                      <motion.button
                        key={i}
                        whileHover={{ x: 4, backgroundColor: 'rgba(255, 255, 255, 0.05)' }}
                        onClick={() => handleSelectExterno(ext)}
                        className="w-full text-left px-3 py-2.5 rounded-lg transition-colors"
                      >
                        <div className="flex items-center justify-between">
                          <span className="text-slate-300 text-sm font-medium">{ext.nombre}</span>
                          <span className="text-xs text-slate-500">{Math.round(ext.met * 70)} kcal/h</span>
                        </div>
                        <span className="text-xs text-slate-500">{ext.categoria}</span>
                      </motion.button>
                    ))}
                  </>
                )}
              </div>
            )}
            <AnimatePresence>
              {selected && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  className="bg-white/8 border border-white/10 rounded-xl p-4 space-y-3"
                >
                  <div className="flex items-center gap-2">
                    <span className="gradient-text font-medium">🏃 {selected.nombre}</span>
                    <span className="text-slate-500 text-xs">({Math.round(selected.met * 70)} kcal/h)</span>
                    <motion.button
                      whileHover={{ scale: 1.1 }}
                      onClick={() => setSelected(null)}
                      className="text-slate-500 text-xs hover:text-slate-300 ml-auto"
                    >
                      Cambiar
                    </motion.button>
                  </div>
                  <div className="flex items-center gap-3">
                    <label htmlFor="duracion" className="label mb-0 shrink-0">Duración (min):</label>
                    <input
                      id="duracion"
                      type="number"
                      className="input w-28"
                      value={duracion}
                      min={1}
                      max={999}
                      onChange={(e) => setDuracion(Number.parseInt(e.target.value) || 0)}
                    />
                    <span className="text-slate-400 text-sm">
                      ≈ {Math.round((selected.met * 70 / 60) * duracion)} kcal
                    </span>
                  </div>
                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={handleAdd}
                    className="btn-primary w-full"
                    disabled={saving}
                  >
                    {saving ? 'Guardando...' : 'Registrar'}
                  </motion.button>
                </motion.div>
              )}
            </AnimatePresence>
          </motion.div>
        )}
      </AnimatePresence>

      {loading ? (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex justify-center py-16"
        >
          <div className="animate-spin w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full" />
        </motion.div>
      ) : registros.length === 0 ? (
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.3 }}
          className="card text-center py-16 text-slate-400"
        >
          <p className="text-4xl mb-3">🏃</p>
          <p>Sin ejercicios registrados para este día.</p>
        </motion.div>
      ) : (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15, duration: 0.3 }}
          className="card overflow-hidden p-0"
        >
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-700">
                  <th className="px-4 py-3 text-slate-400 font-medium text-left">Ejercicio</th>
                  <th className="px-4 py-3 text-slate-400 font-medium text-right">Duración</th>
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
                      exit={{ opacity: 0, x: 20, height: 0 }}
                      transition={{ delay: idx * 0.05, type: 'spring', stiffness: 300, damping: 30 }}
                      className="border-b border-slate-700/50 hover:bg-white/5 transition-colors"
                    >
                      <td className="px-4 py-3 font-medium text-slate-100">{r.nombreEjercicio}</td>
                      <td className="px-4 py-3 text-right text-slate-300">{r.duracionMin} min</td>
                      <td className="px-4 py-3 text-right gradient-text font-semibold">{Math.round(r.kcalQuemadas)}</td>
                      <td className="px-4 py-3 text-right">
                        <AnimatePresence>
                          {confirmDeleteId === r.id ? (
                            <motion.div
                              initial={{ opacity: 0 }}
                              animate={{ opacity: 1 }}
                              exit={{ opacity: 0 }}
                              className="flex gap-1 justify-end"
                            >
                              <motion.button
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                                onClick={() => confirmDeleteEjercicio(r.id)}
                                className="btn-danger text-xs py-1 px-2"
                              >
                                Sí
                              </motion.button>
                              <motion.button
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                                onClick={() => setConfirmDeleteId(null)}
                                className="btn-secondary text-xs py-1 px-2"
                              >
                                No
                              </motion.button>
                            </motion.div>
                          ) : (
                            <motion.button
                              whileHover={{ scale: 1.1 }}
                              whileTap={{ scale: 0.9 }}
                              onClick={() => handleDelete(r.id)}
                              className="btn-danger"
                            >
                              🗑
                            </motion.button>
                          )}
                        </AnimatePresence>
                      </td>
                    </motion.tr>
                  ))}
                </AnimatePresence>
              </tbody>
            </table>
          </div>
        </motion.div>
      )}
    </motion.div>
  )
}
