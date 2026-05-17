import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { generarPlan, getPlan, regenerarPlan, type PlanSemanal } from '../api/planSemanal'

export default function PlanSemanal() {
  const { user } = useAuth()
  const [semanaInicio, setSemanaInicio] = useState<string>(obtenerLunesActual())
  const [plan, setPlan] = useState<PlanSemanal | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    cargarPlan()
  }, [semanaInicio])

  const cargarPlan = async () => {
    if (!user) return
    setLoading(true)
    setError(null)
    try {
      const response = await getPlan(user.usuarioId, semanaInicio)
      if (response) {
        const parsedPlan = JSON.parse(response.planJson)
        setPlan(parsedPlan)
      } else {
        setPlan(null)
      }
    } catch (err) {
      console.error('Error cargando plan:', err)
      setPlan(null)
    } finally {
      setLoading(false)
    }
  }

  const handleGenerar = async () => {
    if (!user) return
    setLoading(true)
    setError(null)
    try {
      const response = await generarPlan(user.usuarioId, semanaInicio)
      const parsedPlan = JSON.parse(response.planJson)
      setPlan(parsedPlan)
    } catch (err) {
      setError('Error al generar el plan. Intenta nuevamente.')
      console.error('Error generando plan:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleRegenerar = async () => {
    if (!user) return
    setLoading(true)
    setError(null)
    try {
      const response = await regenerarPlan(user.usuarioId, semanaInicio)
      const parsedPlan = JSON.parse(response.planJson)
      setPlan(parsedPlan)
    } catch (err) {
      setError('Error al regenerar el plan. Intenta nuevamente.')
      console.error('Error regenerando plan:', err)
    } finally {
      setLoading(false)
    }
  }

  const irASemanaAnterior = () => {
    const fecha = new Date(semanaInicio)
    fecha.setDate(fecha.getDate() - 7)
    setSemanaInicio(fecha.toISOString().split('T')[0])
  }

  const irASemanaProxima = () => {
    const fecha = new Date(semanaInicio)
    fecha.setDate(fecha.getDate() + 7)
    setSemanaInicio(fecha.toISOString().split('T')[0])
  }

  const irASemanaActual = () => {
    setSemanaInicio(obtenerLunesActual())
  }

  const formatearFecha = (dateStr: string) => {
    const date = new Date(dateStr + 'T00:00:00')
    return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' })
  }

  const obtenerRangoSemana = () => {
    const inicio = new Date(semanaInicio + 'T00:00:00')
    const fin = new Date(inicio)
    fin.setDate(fin.getDate() + 6)
    const inicioStr = inicio.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' })
    const finStr = fin.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' })
    return `${inicioStr} al ${finStr}`
  }

  const colorComida = (tipo: string) => {
    switch (tipo) {
      case 'desayuno':
        return 'bg-amber-500/10 border-amber-500/30 text-amber-400'
      case 'almuerzo':
        return 'bg-green-500/10 border-green-500/30 text-green-400'
      case 'merienda':
        return 'bg-orange-500/10 border-orange-500/30 text-orange-400'
      case 'cena':
        return 'bg-blue-500/10 border-blue-500/30 text-blue-400'
      default:
        return 'bg-slate-500/10 border-slate-500/30 text-slate-400'
    }
  }

  const colorIconoComida = (tipo: string) => {
    switch (tipo) {
      case 'desayuno':
        return 'text-amber-400'
      case 'almuerzo':
        return 'text-green-400'
      case 'merienda':
        return 'text-orange-400'
      case 'cena':
        return 'text-blue-400'
      default:
        return 'text-slate-400'
    }
  }

  const emojiComida = (tipo: string) => {
    switch (tipo) {
      case 'desayuno':
        return '🥐'
      case 'almuerzo':
        return '🍽️'
      case 'merienda':
        return '🥤'
      case 'cena':
        return '🍲'
      default:
        return '🍴'
    }
  }

  return (
    <div className="flex-1 overflow-auto">
      <div className="max-w-7xl mx-auto p-6">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-slate-100 mb-4">Plan Semanal IA</h1>

          {/* Navegación de semanas */}
          <div className="flex items-center justify-between bg-slate-700/50 border border-slate-600 rounded-lg p-4">
            <button
              onClick={irASemanaAnterior}
              className="px-4 py-2 bg-slate-600 hover:bg-slate-500 text-slate-100 rounded-lg transition-colors"
            >
              ← Anterior
            </button>

            <div className="text-center flex-1 mx-4">
              <div className="text-slate-400 text-sm mb-1">Semana del</div>
              <div className="text-lg font-semibold text-slate-100">{obtenerRangoSemana()}</div>
            </div>

            <div className="flex gap-2">
              <button
                onClick={irASemanaActual}
                className="px-4 py-2 bg-slate-600 hover:bg-slate-500 text-slate-100 rounded-lg transition-colors text-sm"
              >
                Hoy
              </button>
              <button
                onClick={irASemanaProxima}
                className="px-4 py-2 bg-slate-600 hover:bg-slate-500 text-slate-100 rounded-lg transition-colors"
              >
                Próxima →
              </button>
            </div>
          </div>
        </div>

        {/* Estado sin plan */}
        {!loading && !plan && !error && (
          <div className="flex flex-col items-center justify-center py-24 bg-slate-700/30 rounded-lg border border-slate-600">
            <div className="text-7xl mb-6">📅</div>
            <h2 className="text-2xl font-semibold text-slate-100 mb-2">No tienes plan para esta semana</h2>
            <p className="text-slate-400 mb-8">Genera un plan personalizado con IA para esta semana</p>
            <button
              onClick={handleGenerar}
              className="px-8 py-3 bg-green-500 hover:bg-green-600 text-white font-semibold rounded-lg transition-colors"
            >
              Generar Plan con IA
            </button>
          </div>
        )}

        {/* Cargando */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-24 bg-slate-700/30 rounded-lg border border-slate-600">
            <div className="animate-spin w-12 h-12 border-4 border-green-500 border-t-transparent rounded-full mb-6" />
            <p className="text-slate-300 text-lg">
              {plan ? 'Regenerando tu plan personalizado...' : 'Generando tu plan personalizado...'}
            </p>
            <p className="text-slate-400 text-sm mt-2">(puede tardar 30 segundos)</p>
          </div>
        )}

        {/* Error */}
        {error && (
          <div className="mb-6 bg-red-500/10 border border-red-500/30 rounded-lg p-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-red-400 font-semibold">Error</h3>
                <p className="text-red-300 text-sm mt-1">{error}</p>
              </div>
              <button
                onClick={handleGenerar}
                className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-colors text-sm"
              >
                Reintentar
              </button>
            </div>
          </div>
        )}

        {/* Plan con 7 días */}
        {!loading && plan && !error && (
          <div>
            <div className="flex justify-end mb-4">
              <button
                onClick={handleRegenerar}
                className="px-6 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/50 rounded-lg transition-colors"
              >
                🔄 Regenerar plan
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              {plan.dias.map((dia, idx) => (
                <div
                  key={idx}
                  className="bg-slate-700/50 border border-slate-600 rounded-lg overflow-hidden hover:border-green-500/50 transition-colors"
                >
                  {/* Header del día */}
                  <div className="bg-green-500/20 border-b border-green-500/30 px-4 py-3">
                    <div className="flex items-baseline justify-between">
                      <div>
                        <div className="font-semibold text-slate-100">{dia.dia}</div>
                        <div className="text-xs text-slate-400">{formatearFecha(dia.fecha)}</div>
                      </div>
                      <div className="text-right">
                        <div className="text-lg font-bold text-green-400">{dia.totalKcal}</div>
                        <div className="text-xs text-slate-400">kcal</div>
                      </div>
                    </div>
                  </div>

                  {/* Comidas */}
                  <div className="p-4 space-y-3">
                    {Object.entries(dia.comidas).map(([tipo, comida]) => (
                      <div
                        key={tipo}
                        className={`border rounded p-3 ${colorComida(tipo)}`}
                      >
                        <div className="flex items-start gap-2 mb-2">
                          <span className={`text-lg ${colorIconoComida(tipo)}`}>{emojiComida(tipo)}</span>
                          <div className="flex-1">
                            <div className="font-semibold text-sm capitalize text-slate-200">{tipo}</div>
                            <div className="text-xs text-slate-400 mt-0.5 line-clamp-2">
                              {comida.descripcion}
                            </div>
                          </div>
                        </div>

                        <div className="text-xs space-y-1 text-slate-300">
                          <div className="flex justify-between">
                            <span>Proteínas:</span>
                            <span className="font-semibold">{comida.proteinas}g</span>
                          </div>
                          <div className="flex justify-between">
                            <span>Carbos:</span>
                            <span className="font-semibold">{comida.carbos}g</span>
                          </div>
                          <div className="flex justify-between">
                            <span>Grasas:</span>
                            <span className="font-semibold">{comida.grasas}g</span>
                          </div>
                        </div>

                        {/* Barra de progreso kcal */}
                        <div className="mt-2 bg-slate-800/50 rounded h-1.5 overflow-hidden">
                          <div
                            className="bg-current h-full"
                            style={{
                              width: `${Math.min((comida.kcal / dia.totalKcal) * 100, 100)}%`,
                            }}
                          />
                        </div>
                        <div className="text-xs text-slate-400 mt-1">{comida.kcal} kcal</div>
                      </div>
                    ))}
                  </div>

                  {/* Totales */}
                  <div className="bg-slate-600/50 border-t border-slate-600 px-4 py-3 text-xs text-slate-300 space-y-1">
                    <div className="flex justify-between">
                      <span>Proteínas:</span>
                      <span className="font-semibold">{dia.totalProteinas}g</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Carbos:</span>
                      <span className="font-semibold">{dia.totalCarbos}g</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Grasas:</span>
                      <span className="font-semibold">{dia.totalGrasas}g</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

function obtenerLunesActual(): string {
  const hoy = new Date()
  const dia = hoy.getDay()
  const diferencia = dia === 0 ? -6 : 1 - dia
  const lunes = new Date(hoy)
  lunes.setDate(lunes.getDate() + diferencia)
  return lunes.toISOString().split('T')[0]
}
