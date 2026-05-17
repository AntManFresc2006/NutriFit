import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getHidratacionDiaria, registrarAgua, eliminarAgua, type HidratacionDiaria, type AguaRegistro } from '../api/hidratacion'

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
        <div className="animate-spin w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full" />
      </div>
    )
  }

  return (
    <div className="p-6 max-w-2xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-8 flex-wrap gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-100">💧 Hidratación</h1>
          <p className="text-slate-400 text-sm mt-0.5">Mantente hidratado durante el día</p>
        </div>
        <input
          type="date"
          value={fecha}
          onChange={e => setFecha(e.target.value)}
          className="bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-slate-100 text-sm"
        />
      </div>

      {error && (
        <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-3 mb-6 text-red-400 text-sm">
          {error}
        </div>
      )}

      {diario && (
        <>
          {/* Círculo de progreso */}
          <div className="flex justify-center mb-8">
            <div className="relative w-48 h-48">
              <svg viewBox="0 0 200 200" className="w-full h-full transform -rotate-90">
                {/* Círculo de fondo */}
                <circle cx="100" cy="100" r="90" fill="none" stroke="#334155" strokeWidth="8" />
                {/* Círculo de progreso */}
                <circle
                  cx="100"
                  cy="100"
                  r="90"
                  fill="none"
                  stroke={diario.porcentaje >= 100 ? '#22c55e' : '#3b82f6'}
                  strokeWidth="8"
                  strokeDasharray={`${(diario.porcentaje / 100) * 565.48} 565.48`}
                  strokeLinecap="round"
                  style={{ transition: 'stroke-dasharray 0.3s ease' }}
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <div className="text-center">
                  <div className="text-3xl font-bold text-slate-100">{diario.totalMl}</div>
                  <div className="text-sm text-slate-400">/ {diario.objetivoMl} ml</div>
                  <div className="text-xl font-semibold mt-2 text-blue-400">{diario.porcentaje}%</div>
                </div>
              </div>
            </div>
          </div>

          {/* Botones rápidos */}
          <div className="grid grid-cols-2 gap-3 mb-8">
            {[
              { label: 'Vaso pequeño', ml: 150 },
              { label: 'Vaso normal', ml: 250 },
              { label: 'Botella pequeña', ml: 330 },
              { label: 'Botella grande', ml: 500 }
            ].map(({ label, ml }) => (
              <button
                key={ml}
                onClick={() => handleRegistrar(ml)}
                disabled={saving}
                className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-medium py-3 px-4 rounded-xl transition-colors text-sm text-center"
              >
                <div>{label}</div>
                <div className="text-xs opacity-90">{ml} ml</div>
              </button>
            ))}
          </div>

          {/* Input personalizado */}
          <div className="bg-slate-800 border border-slate-700 rounded-xl p-4 mb-8">
            <label className="text-sm font-semibold text-slate-300 mb-2 block">Cantidad personalizada (ml)</label>
            <div className="flex gap-3">
              <input
                type="number"
                min="1"
                max="5000"
                value={personalizado}
                onChange={e => setPersonalizado(e.target.value)}
                placeholder="Ej: 200"
                className="flex-1 bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-slate-100 placeholder-slate-500"
              />
              <button
                onClick={() => {
                  if (personalizado && parseInt(personalizado) > 0) {
                    handleRegistrar(parseInt(personalizado))
                  }
                }}
                disabled={saving || !personalizado}
                className="bg-green-600 hover:bg-green-700 disabled:opacity-50 text-white font-medium px-6 py-2 rounded-lg transition-colors"
              >
                Añadir
              </button>
            </div>
          </div>

          {/* Lista de registros */}
          <div className="bg-slate-800 border border-slate-700 rounded-xl p-6">
            <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4">Registros del día</h2>
            {diario.registros.length === 0 ? (
              <p className="text-slate-400 text-center py-8">No hay registros de agua hoy</p>
            ) : (
              <div className="space-y-2">
                {diario.registros.map(registro => (
                  <div
                    key={registro.id}
                    className="flex items-center justify-between bg-slate-700 rounded-lg p-3"
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
                    <button
                      onClick={() => handleEliminar(registro.id)}
                      className="text-red-400 hover:text-red-300 hover:bg-red-900/30 p-2 rounded-lg transition-colors text-lg"
                    >
                      🗑️
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  )
}
