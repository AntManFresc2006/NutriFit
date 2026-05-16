import { useEffect, useState } from 'react'
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
    if (!user || !confirm('¿Eliminar este registro?')) return
    await deleteRegistro(id, user.usuarioId)
    setRegistros((prev) => prev.filter((r) => r.id !== id))
  }

  const totalKcal = registros.reduce((s, r) => s + r.kcalQuemadas, 0)
  const totalMin = registros.reduce((s, r) => s + r.duracionMin, 0)

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Ejercicios</h1>
          <p className="text-slate-400 text-sm mt-0.5">Actividad física registrada</p>
        </div>
        <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} className="input w-auto" />
      </div>

      {registros.length > 0 && (
        <div className="grid grid-cols-2 gap-4 mb-6">
          <div className="card py-4">
            <p className="text-xs text-slate-500 uppercase tracking-wide">Kcal quemadas</p>
            <p className="text-2xl font-bold text-green-400 mt-1">{Math.round(totalKcal)}</p>
          </div>
          <div className="card py-4">
            <p className="text-xs text-slate-500 uppercase tracking-wide">Tiempo total</p>
            <p className="text-2xl font-bold text-blue-400 mt-1">{totalMin} min</p>
          </div>
        </div>
      )}

      <div className="flex justify-end mb-4">
        <button onClick={() => { setShowSearch(!showSearch); setSelected(null); setQuery(''); getEjercicios().then(setEjercicios) }} className="btn-primary">
          {showSearch ? '✕ Cancelar' : '+ Registrar ejercicio'}
        </button>
      </div>

      {showSearch && (
        <div className="card mb-6 space-y-3">
          <h2 className="text-sm font-semibold text-slate-300">Buscar ejercicio</h2>
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
                <button
                  key={e.id}
                  onClick={() => setSelected(e)}
                  className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-slate-700 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-slate-200 text-sm font-medium">{e.nombre}</span>
                    <span className="text-xs text-slate-500">{Math.round(e.met * 70)} kcal/h</span>
                  </div>
                  <span className="text-xs text-slate-500">{e.categoria}</span>
                </button>
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
                    <button
                      key={i}
                      onClick={() => handleSelectExterno(ext)}
                      className="w-full text-left px-3 py-2.5 rounded-lg hover:bg-slate-700 transition-colors"
                    >
                      <div className="flex items-center justify-between">
                        <span className="text-slate-300 text-sm font-medium">{ext.nombre}</span>
                        <span className="text-xs text-slate-500">{Math.round(ext.met * 70)} kcal/h</span>
                      </div>
                      <span className="text-xs text-slate-500">{ext.categoria}</span>
                    </button>
                  ))}
                </>
              )}
            </div>
          )}
          {selected && (
            <div className="bg-slate-700/40 rounded-xl p-4 space-y-3">
              <div className="flex items-center gap-2">
                <span className="text-green-400 font-medium">🏃 {selected.nombre}</span>
                <span className="text-slate-500 text-xs">({Math.round(selected.met * 70)} kcal/h)</span>
                <button onClick={() => setSelected(null)} className="text-slate-500 text-xs hover:text-slate-300 ml-auto">Cambiar</button>
              </div>
              <div className="flex items-center gap-3">
                <label className="label mb-0 shrink-0">Duración (min):</label>
                <input
                  type="number"
                  className="input w-28"
                  value={duracion}
                  min={1}
                  max={999}
                  onChange={(e) => setDuracion(parseInt(e.target.value) || 0)}
                />
                <span className="text-slate-400 text-sm">
                  ≈ {Math.round((selected.met * 70 / 60) * duracion)} kcal
                </span>
              </div>
              <button onClick={handleAdd} className="btn-primary" disabled={saving}>
                {saving ? 'Guardando...' : 'Registrar'}
              </button>
            </div>
          )}
        </div>
      )}

      {loading ? (
        <div className="flex justify-center py-16">
          <div className="animate-spin w-8 h-8 border-2 border-green-500 border-t-transparent rounded-full" />
        </div>
      ) : registros.length === 0 ? (
        <div className="card text-center py-16 text-slate-400">
          <p className="text-4xl mb-3">🏃</p>
          <p>Sin ejercicios registrados para este día.</p>
        </div>
      ) : (
        <div className="card overflow-hidden p-0">
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
                {registros.map((r) => (
                  <tr key={r.id} className="border-b border-slate-700/50 hover:bg-slate-700/30 transition-colors">
                    <td className="px-4 py-3 font-medium text-slate-100">{r.nombreEjercicio}</td>
                    <td className="px-4 py-3 text-right text-slate-300">{r.duracionMin} min</td>
                    <td className="px-4 py-3 text-right text-green-400 font-semibold">{Math.round(r.kcalQuemadas)}</td>
                    <td className="px-4 py-3 text-right">
                      <button onClick={() => handleDelete(r.id)} className="btn-danger">🗑</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
