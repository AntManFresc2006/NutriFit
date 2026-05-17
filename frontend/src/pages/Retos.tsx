import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getRetos, sincronizarProgreso, aceptarReto, abandonarReto, type Reto } from '../api/retos'

function today() {
  return new Date().toISOString().split('T')[0]
}

export default function Retos() {
  const { user } = useAuth()
  const [retos, setRetos] = useState<Reto[]>([])
  const [loading, setLoading] = useState(true)
  const [tab, setTab] = useState<'disponibles' | 'activos' | 'completados'>('disponibles')
  const [notificacion, setNotificacion] = useState<string | null>(null)
  const [abandonandoId, setAbandonandoId] = useState<number | null>(null)
  const [confirmandoId, setConfirmandoId] = useState<number | null>(null)

  useEffect(() => {
    if (!user) return
    cargarRetos()

    const timer = setTimeout(() => {
      sincronizar()
    }, 500)

    return () => clearTimeout(timer)
  }, [user])

  const cargarRetos = async () => {
    if (!user) return
    try {
      setLoading(true)
      const datos = await getRetos(user.usuarioId)
      setRetos(datos)
    } catch (error) {
      console.error('Error cargando retos:', error)
    } finally {
      setLoading(false)
    }
  }

  const sincronizar = async () => {
    if (!user) return
    try {
      const completados = await sincronizarProgreso(user.usuarioId, today())
      if (completados && completados.length > 0) {
        const titulos = completados.map(r => r.titulo).join(', ')
        setNotificacion(`¡Retos completados! ${titulos}`)
        setTimeout(() => setNotificacion(null), 4000)
      }
      await cargarRetos()
    } catch (error) {
      console.error('Error sincronizando:', error)
    }
  }

  const handleAceptarReto = async (retoId: number) => {
    if (!user) return
    try {
      await aceptarReto(user.usuarioId, retoId)
      await cargarRetos()
    } catch (error) {
      console.error('Error aceptando reto:', error)
    }
  }

  const handleAbandonarReto = async (usuarioRetoId: number) => {
    if (!user) return
    try {
      setAbandonandoId(usuarioRetoId)
      await abandonarReto(user.usuarioId, usuarioRetoId)
      await cargarRetos()
      setConfirmandoId(null)
    } catch (error) {
      console.error('Error abandonando reto:', error)
    } finally {
      setAbandonandoId(null)
    }
  }

  const disponibles = retos.filter(r => !r.aceptado)
  const activos = retos.filter(r => r.aceptado && !r.completado)
  const completados = retos.filter(r => r.completado)

  const totalCompletados = completados.length
  const totalPuntos = completados.reduce((sum, r) => sum + r.puntos, 0)

  return (
    <div className="p-6 max-w-6xl mx-auto">
      {notificacion && (
        <div className="fixed top-4 right-4 bg-gradient-to-r from-green-500 to-emerald-600 text-white px-6 py-3 rounded-lg shadow-lg animate-bounce z-50">
          {notificacion}
        </div>
      )}

      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-100 mb-2">🏆 Modo Reto</h1>
        <p className="text-slate-400">Acepta desafíos y gana puntos completando objetivos</p>

        <div className="grid grid-cols-2 gap-4 mt-6">
          <div className="card py-4">
            <p className="text-xs text-slate-500 uppercase tracking-wide">Retos completados</p>
            <p className="text-2xl font-bold text-green-400 mt-2">{totalCompletados}</p>
          </div>
          <div className="card py-4">
            <p className="text-xs text-slate-500 uppercase tracking-wide">Puntos ganados</p>
            <p className="text-2xl font-bold text-amber-400 mt-2">{totalPuntos}</p>
          </div>
        </div>
      </div>

      <div className="flex gap-2 mb-6 border-b border-slate-700">
        {(['disponibles', 'activos', 'completados'] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-2 font-medium text-sm transition-colors border-b-2 ${
              tab === t
                ? 'border-green-400 text-green-400'
                : 'border-transparent text-slate-400 hover:text-slate-300'
            }`}
          >
            {t === 'disponibles' && `Disponibles (${disponibles.length})`}
            {t === 'activos' && `Mis retos (${activos.length})`}
            {t === 'completados' && `Completados (${completados.length})`}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin w-8 h-8 border-2 border-green-500 border-t-transparent rounded-full" />
        </div>
      ) : (
        <>
          {tab === 'disponibles' && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {disponibles.length === 0 ? (
                <div className="col-span-full text-center py-12">
                  <p className="text-slate-400">No hay retos disponibles</p>
                </div>
              ) : (
                disponibles.map((reto) => (
                  <div key={reto.id} className="card hover:border-green-500/50 transition-colors">
                    <div className="text-5xl mb-3">{reto.icono}</div>
                    <h3 className="font-bold text-slate-100 mb-2">{reto.titulo}</h3>
                    <p className="text-slate-400 text-sm mb-3 line-clamp-2">{reto.descripcion}</p>
                    <div className="flex items-center gap-3 mb-4 text-xs text-slate-400">
                      <span>⏱️ {reto.duracionDias} días</span>
                      <span>🏆 {reto.puntos} pts</span>
                    </div>
                    <button
                      onClick={() => handleAceptarReto(reto.id)}
                      className="btn-primary w-full"
                    >
                      Aceptar reto
                    </button>
                  </div>
                ))
              )}
            </div>
          )}

          {tab === 'activos' && (
            <div className="space-y-3">
              {activos.length === 0 ? (
                <div className="text-center py-12">
                  <p className="text-slate-400">No tienes retos en curso</p>
                </div>
              ) : (
                activos.map((reto) => {
                  const diasRestantes = reto.fechaFin
                    ? Math.ceil((new Date(reto.fechaFin).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24))
                    : 0
                  const porcentaje = reto.progreso && reto.metaValor ? Math.min((reto.progreso / reto.metaValor) * 100, 100) : 0

                  return (
                    <div key={reto.id} className="card">
                      <div className="flex items-start justify-between mb-3">
                        <div className="flex items-start gap-3 flex-1">
                          <span className="text-3xl">{reto.icono}</span>
                          <div>
                            <h3 className="font-bold text-slate-100">{reto.titulo}</h3>
                            <p className="text-slate-400 text-sm mt-0.5">{reto.descripcion}</p>
                          </div>
                        </div>
                        <button
                          onClick={() => setConfirmandoId(reto.usuarioRetoId)}
                          className="text-slate-500 hover:text-red-400 text-xs font-medium shrink-0"
                        >
                          ✕
                        </button>
                      </div>

                      {confirmandoId === reto.usuarioRetoId && (
                        <div className="bg-red-500/10 border border-red-500/30 rounded p-3 mb-4 text-sm text-red-400">
                          <div className="flex items-center justify-between gap-2">
                            <span>¿Abandonar este reto?</span>
                            <div className="flex gap-2">
                              <button
                                onClick={() => handleAbandonarReto(reto.usuarioRetoId!)}
                                className="px-2 py-1 bg-red-600 hover:bg-red-700 rounded text-xs font-medium transition-colors"
                                disabled={abandonandoId === reto.usuarioRetoId}
                              >
                                {abandonandoId === reto.usuarioRetoId ? 'Abandonando...' : 'Confirmar'}
                              </button>
                              <button
                                onClick={() => setConfirmandoId(null)}
                                className="px-2 py-1 bg-slate-700 hover:bg-slate-600 rounded text-xs font-medium transition-colors"
                              >
                                Cancelar
                              </button>
                            </div>
                          </div>
                        </div>
                      )}

                      <div className="mb-4">
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-xs text-slate-400">
                            {reto.progreso}/{reto.metaValor} días
                          </span>
                          <span className={`text-xs ${diasRestantes > 0 ? 'text-slate-400' : 'text-red-400'}`}>
                            {diasRestantes > 0 ? `${diasRestantes} días` : 'Terminado'}
                          </span>
                        </div>
                        <div className="w-full bg-slate-700 rounded-full h-2">
                          <div
                            className={`h-2 rounded-full transition-all ${
                              porcentaje >= 50 ? 'bg-green-500' : 'bg-yellow-400'
                            }`}
                            style={{ width: `${porcentaje}%` }}
                          />
                        </div>
                      </div>

                      <button
                        onClick={sincronizar}
                        className="btn-secondary w-full text-sm"
                      >
                        Sincronizar progreso
                      </button>
                    </div>
                  )
                })
              )}
            </div>
          )}

          {tab === 'completados' && (
            <div className="space-y-3">
              {completados.length === 0 ? (
                <div className="text-center py-12">
                  <p className="text-slate-400">No hay retos completados aún</p>
                </div>
              ) : (
                completados.map((reto) => (
                  <div key={reto.id} className="card border border-green-500/30 bg-green-500/5">
                    <div className="flex items-start gap-4">
                      <div className="text-3xl">{reto.icono}</div>
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <h3 className="font-bold text-green-400">{reto.titulo}</h3>
                          <span className="text-xs bg-green-500/20 text-green-400 px-2 py-0.5 rounded">✓ Completado</span>
                        </div>
                        <p className="text-slate-400 text-sm">{reto.descripcion}</p>
                        <div className="mt-3 flex items-center gap-4 text-sm">
                          <span className="text-amber-400 font-bold">🏆 +{reto.puntos} puntos</span>
                          {reto.fechaFin && (
                            <span className="text-slate-500">
                              {new Date(reto.fechaFin).toLocaleDateString('es-ES')}
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </>
      )}
    </div>
  )
}
