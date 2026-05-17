import { useEffect, useState } from 'react'
import { motion, AnimatePresence as AnimatePresenceMotion } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'
import { getRetos, sincronizarProgreso, aceptarReto, abandonarReto, type Reto } from '../api/retos'

const AnimatePresence = AnimatePresenceMotion
const container = { hidden: {}, show: { transition: { staggerChildren: 0.1 } } }
const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } }

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
    <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="flex-1 overflow-auto p-6">
      <div className="max-w-6xl mx-auto">
        <AnimatePresence>
          {notificacion && (
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="fixed top-4 right-4 bg-gradient-to-r from-emerald-500 to-cyan-500 text-white px-6 py-3 rounded-lg shadow-lg shadow-emerald-500/30 z-50"
            >
              {notificacion}
            </motion.div>
          )}
        </AnimatePresence>

        <div className="mb-8">
          <h1 className="gradient-text text-3xl font-bold mb-2">🏆 Modo Reto</h1>
          <p className="text-white/50 mb-6">Acepta desafíos y gana puntos completando objetivos</p>

          <motion.div
            variants={container}
            initial="hidden"
            animate="show"
            className="grid grid-cols-2 gap-4"
          >
            <motion.div variants={item} className="card py-4">
              <p className="text-xs text-white/50 uppercase tracking-wide">Retos completados</p>
              <p className="text-2xl font-bold text-emerald-400 mt-2">{totalCompletados}</p>
            </motion.div>
            <motion.div variants={item} className="card py-4">
              <p className="text-xs text-white/50 uppercase tracking-wide">Puntos ganados</p>
              <p className="text-2xl font-bold text-amber-400 mt-2">{totalPuntos}</p>
            </motion.div>
          </motion.div>
        </div>

        <div className="flex gap-2 mb-6 border-b border-white/10">
          {(['disponibles', 'activos', 'completados'] as const).map((t) => (
            <motion.button
              key={t}
              onClick={() => setTab(t)}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className={`px-4 py-2 font-medium text-sm transition-all border-b-2 ${
                tab === t
                  ? 'border-emerald-400 text-emerald-400'
                  : 'border-transparent text-white/50 hover:text-white'
              }`}
            >
              {t === 'disponibles' && `Disponibles (${disponibles.length})`}
              {t === 'activos' && `Mis retos (${activos.length})`}
              {t === 'completados' && `Completados (${completados.length})`}
            </motion.button>
          ))}
        </div>

        <motion.div
          key={tab}
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25 }}
        >
          {loading ? (
            <div className="flex items-center justify-center h-64">
              <div className="animate-spin w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full" />
            </div>
          ) : (
            <>
              {tab === 'disponibles' && (
                <motion.div
                  variants={container}
                  initial="hidden"
                  animate="show"
                  className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"
                >
                  {disponibles.length === 0 ? (
                    <motion.div variants={item} className="col-span-full text-center py-12">
                      <p className="text-white/50">No hay retos disponibles</p>
                    </motion.div>
                  ) : (
                    disponibles.map((reto) => (
                      <motion.div
                        key={reto.id}
                        variants={item}
                        whileHover={{ scale: 1.05, y: -8 }}
                        transition={{ type: 'spring', stiffness: 300 }}
                        className="card hover:border-emerald-500/50 transition-colors"
                      >
                        <div className="text-5xl mb-3">{reto.icono}</div>
                        <h3 className="font-bold text-white mb-2">{reto.titulo}</h3>
                        <p className="text-white/50 text-sm mb-3 line-clamp-2">{reto.descripcion}</p>
                        <div className="flex items-center gap-3 mb-4 text-xs text-white/50">
                          <span>⏱️ {reto.duracionDias} días</span>
                          <span>🏆 {reto.puntos} pts</span>
                        </div>
                        <motion.button
                          onClick={() => handleAceptarReto(reto.id)}
                          whileHover={{ scale: 1.02 }}
                          whileTap={{ scale: 0.98 }}
                          className="btn-primary w-full"
                        >
                          Aceptar reto
                        </motion.button>
                      </motion.div>
                    ))
                  )}
                </motion.div>
              )}

              {tab === 'activos' && (
                <motion.div
                  variants={container}
                  initial="hidden"
                  animate="show"
                  className="space-y-3"
                >
                  {activos.length === 0 ? (
                    <motion.div variants={item} className="text-center py-12">
                      <p className="text-white/50">No tienes retos en curso</p>
                    </motion.div>
                  ) : (
                    activos.map((reto) => {
                      const diasRestantes = reto.fechaFin
                        ? Math.ceil((new Date(reto.fechaFin).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24))
                        : 0
                      const porcentaje = reto.progreso && reto.metaValor ? Math.min((reto.progreso / reto.metaValor) * 100, 100) : 0

                      return (
                        <motion.div key={reto.id} variants={item} className="card border-l-4 border-l-cyan-500">
                          <div className="flex items-start justify-between mb-3">
                            <div className="flex items-start gap-3 flex-1">
                              <span className="text-3xl">{reto.icono}</span>
                              <div>
                                <h3 className="font-bold text-white">{reto.titulo}</h3>
                                <p className="text-white/50 text-sm mt-0.5">{reto.descripcion}</p>
                              </div>
                            </div>
                            <motion.button
                              onClick={() => setConfirmandoId(reto.usuarioRetoId)}
                              whileHover={{ scale: 1.2 }}
                              whileTap={{ scale: 0.9 }}
                              className="text-white/50 hover:text-red-400 text-xs font-medium shrink-0"
                            >
                              ✕
                            </motion.button>
                          </div>

                          <AnimatePresence>
                            {confirmandoId === reto.usuarioRetoId && (
                              <motion.div
                                initial={{ opacity: 0, y: -10 }}
                                animate={{ opacity: 1, y: 0 }}
                                exit={{ opacity: 0, y: -10 }}
                                className="bg-red-500/10 border border-red-500/30 rounded p-3 mb-4 text-sm text-red-400"
                              >
                                <div className="flex items-center justify-between gap-2">
                                  <span>¿Abandonar este reto?</span>
                                  <div className="flex gap-2">
                                    <motion.button
                                      onClick={() => handleAbandonarReto(reto.usuarioRetoId!)}
                                      whileHover={{ scale: 1.05 }}
                                      whileTap={{ scale: 0.95 }}
                                      className="px-2 py-1 bg-red-600 hover:bg-red-700 rounded text-xs font-medium transition-colors"
                                      disabled={abandonandoId === reto.usuarioRetoId}
                                    >
                                      {abandonandoId === reto.usuarioRetoId ? 'Abandonando...' : 'Confirmar'}
                                    </motion.button>
                                    <motion.button
                                      onClick={() => setConfirmandoId(null)}
                                      whileHover={{ scale: 1.05 }}
                                      whileTap={{ scale: 0.95 }}
                                      className="px-2 py-1 bg-white/10 hover:bg-white/20 rounded text-xs font-medium transition-colors"
                                    >
                                      Cancelar
                                    </motion.button>
                                  </div>
                                </div>
                              </motion.div>
                            )}
                          </AnimatePresence>

                          <div className="mb-4">
                            <div className="flex items-center justify-between mb-2">
                              <span className="text-xs text-white/50">
                                {reto.progreso}/{reto.metaValor} días
                              </span>
                              <span className={`text-xs ${diasRestantes > 0 ? 'text-white/50' : 'text-red-400'}`}>
                                {diasRestantes > 0 ? `${diasRestantes} días` : 'Terminado'}
                              </span>
                            </div>
                            <div className="h-2 rounded-full bg-white/10 overflow-hidden">
                              <motion.div
                                className={`h-full rounded-full ${
                                  porcentaje >= 50 ? 'bg-gradient-to-r from-emerald-500 to-cyan-500' : 'bg-gradient-to-r from-yellow-400 to-orange-400'
                                }`}
                                initial={{ width: 0 }}
                                animate={{ width: `${porcentaje}%` }}
                                transition={{ duration: 1, delay: 0.3, ease: 'easeOut' }}
                              />
                            </div>
                          </div>

                          <motion.button
                            onClick={sincronizar}
                            whileHover={{ scale: 1.02 }}
                            whileTap={{ scale: 0.98 }}
                            className="btn-secondary w-full text-sm"
                          >
                            Sincronizar progreso
                          </motion.button>
                        </motion.div>
                      )
                    })
                  )}
                </motion.div>
              )}

              {tab === 'completados' && (
                <motion.div
                  variants={container}
                  initial="hidden"
                  animate="show"
                  className="space-y-3"
                >
                  {completados.length === 0 ? (
                    <motion.div variants={item} className="text-center py-12">
                      <p className="text-white/50">No hay retos completados aún</p>
                    </motion.div>
                  ) : (
                    completados.map((reto) => (
                      <motion.div
                        key={reto.id}
                        variants={item}
                        whileHover={{ scale: 1.02 }}
                        className="card border border-emerald-500/30 bg-emerald-500/5"
                      >
                        <div className="flex items-start gap-4">
                          <div className="text-3xl">{reto.icono}</div>
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <h3 className="font-bold text-emerald-400">{reto.titulo}</h3>
                              <span className="text-xs bg-emerald-500/20 text-emerald-400 px-2 py-0.5 rounded">✓ Completado</span>
                            </div>
                            <p className="text-white/50 text-sm">{reto.descripcion}</p>
                            <div className="mt-3 flex items-center gap-4 text-sm">
                              <span className="text-amber-400 font-bold">🏆 +{reto.puntos} puntos</span>
                              {reto.fechaFin && (
                                <span className="text-white/40">
                                  {new Date(reto.fechaFin).toLocaleDateString('es-ES')}
                                </span>
                              )}
                            </div>
                          </div>
                        </div>
                      </motion.div>
                    ))
                  )}
                </motion.div>
              )}
            </>
          )}
        </motion.div>
      </div>
    </motion.div>
  )
}
