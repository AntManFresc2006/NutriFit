import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getResumenDiario, getEvaluacionIA, getGamificacion } from '../api/resumen'
import { getRecuperacion } from '../api/ejercicios'
import MacroRing from '../components/MacroRing'
import type { ResumenDiario } from '../types'
import type { Gamificacion } from '../api/resumen'
import type { RecuperacionData } from '../api/ejercicios'

function today() {
  return new Date().toISOString().split('T')[0]
}

function balanceBadge(estado: string) {
  const map: Record<string, string> = {
    DEFICIT: 'badge bg-blue-500/20 text-blue-400',
    EQUILIBRADO: 'badge bg-green-500/20 text-green-400',
    SUPERAVIT: 'badge bg-amber-500/20 text-amber-400',
  }
  return map[estado] ?? 'badge bg-slate-700 text-slate-400'
}

export default function Dashboard() {
  const { user } = useAuth()
  const [resumen, setResumen] = useState<ResumenDiario | null>(null)
  const [loadingResumen, setLoadingResumen] = useState(true)
  const [ia, setIa] = useState('')
  const [loadingIa, setLoadingIa] = useState(false)
  const [fecha, setFecha] = useState(today())
  const [gamificacion, setGamificacion] = useState<Gamificacion | null>(null)
  const [recuperacion, setRecuperacion] = useState<RecuperacionData | null>(null)

  useEffect(() => {
    if (!user) return
    setLoadingResumen(true)
    setIa('')
    getResumenDiario(user.usuarioId, fecha)
      .then(setResumen)
      .catch(() => setResumen(null))
      .finally(() => setLoadingResumen(false))
    getGamificacion(user.usuarioId, fecha)
      .then(setGamificacion)
      .catch(() => setGamificacion(null))
    getRecuperacion(user.usuarioId, fecha)
      .then(setRecuperacion)
      .catch(() => setRecuperacion(null))
  }, [user, fecha])

  const handleIa = async () => {
    if (!resumen || !user) return
    setLoadingIa(true)
    setIa('')
    try {
      const res = await getEvaluacionIA({
        usuarioId: user.usuarioId,
        fecha,
        kcalConsumidas: resumen.kcalTotales,
        kcalQuemadas: resumen.kcalQuemadasTotales,
        proteinasTotales: resumen.proteinasTotales,
        grasasTotales: resumen.grasasTotales,
        carbosTotales: resumen.carbosTotales,
        tdee: resumen.tdee,
        balanceReal: resumen.balanceReal,
      })
      setIa(res.evaluacion)
    } catch {
      setIa('No se pudo obtener la evaluación. Inténtalo de nuevo.')
    } finally {
      setLoadingIa(false)
    }
  }

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Hola, {user?.nombre} 👋</h1>
          <p className="text-slate-400 text-sm mt-0.5">Resumen nutricional del día</p>
        </div>
        <input
          type="date"
          value={fecha}
          onChange={(e) => setFecha(e.target.value)}
          className="input w-auto"
        />
      </div>

      {loadingResumen ? (
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin w-8 h-8 border-2 border-green-500 border-t-transparent rounded-full" />
        </div>
      ) : !resumen ? (
        <div className="card text-center py-16 text-slate-400">
          <p className="text-4xl mb-3">📋</p>
          <p>Sin datos para este día. Registra comidas y ejercicios primero.</p>
        </div>
      ) : (
        <>
          {recuperacion?.tieneEjercicioIntensivo && (
            <div className="card border border-blue-500/30 bg-blue-500/5 mb-4">
              <div className="flex items-start gap-3">
                <span className="text-2xl">💪</span>
                <div>
                  <p className="font-semibold text-blue-300 text-sm">Ventana de recuperación activa</p>
                  <p className="text-slate-300 text-sm mt-0.5">
                    Registraste <strong>{recuperacion.ejercicioNombre}</strong> hoy (MET {recuperacion.met?.toFixed(1)}).
                    {' '}Prioriza: <strong>{recuperacion.sugerenciaProteinaG}g proteína</strong> + <strong>{recuperacion.sugerenciaCarbosG}g carbohidratos</strong> para recuperación muscular.
                  </p>
                  <p className="text-slate-500 text-xs mt-1">Opciones: atún + arroz · yogur griego + plátano · pollo + pasta.</p>
                </div>
              </div>
            </div>
          )}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Macro ring */}
          <div className="card lg:col-span-1 flex flex-col items-center">
            <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4 self-start">
              Calorías y macros
            </h2>
            <MacroRing
              kcal={resumen.kcalTotales}
              tdee={resumen.tdee}
              proteinas={resumen.proteinasTotales}
              grasas={resumen.grasasTotales}
              carbos={resumen.carbosTotales}
            />
          </div>

          {/* Stats */}
          <div className="lg:col-span-2 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <StatCard icon="🔥" label="Kcal consumidas" value={`${Math.round(resumen.kcalTotales)} kcal`} color="text-amber-400" />
              <StatCard icon="💪" label="Kcal quemadas" value={`${Math.round(resumen.kcalQuemadasTotales)} kcal`} color="text-blue-400" />
              <StatCard icon="⚖️" label="TDEE" value={`${Math.round(resumen.tdee)} kcal`} color="text-slate-300" />
              <StatCard
                icon="📉"
                label="Balance real"
                value={`${resumen.balanceReal > 0 ? '+' : ''}${Math.round(resumen.balanceReal)} kcal`}
                color={resumen.balanceReal > 0 ? 'text-amber-400' : 'text-blue-400'}
                extra={<span className={balanceBadge(resumen.estadoBalance)}>{resumen.estadoBalance}</span>}
              />
              {resumen.fechaObjetivo && (
                <StatCard
                  icon="🎯"
                  label="Fecha estimada objetivo"
                  value={resumen.fechaObjetivo}
                  color="text-green-400"
                  extra={<span className="text-xs text-slate-500">{resumen.diasParaObjetivo} días desde hoy</span>}
                />
              )}
            </div>

            {/* Racha y NutriScore */}
            {gamificacion && (
              <div className="grid grid-cols-2 gap-4">
                <div className="card py-4">
                  <p className="text-xs text-slate-500 uppercase tracking-wide">Racha de registro</p>
                  <p className="text-xl font-bold mt-1 text-orange-400">
                    🔥 {gamificacion.racha} {gamificacion.racha === 1 ? 'día' : 'días'}
                  </p>
                  <p className="text-xs text-slate-500 mt-1">días consecutivos</p>
                </div>
                <div className="card py-4">
                  <p className="text-xs text-slate-500 uppercase tracking-wide">NutriScore hoy</p>
                  <p className={`text-xl font-bold mt-1 ${
                    ['A','B'].includes(gamificacion.nutriGrade) ? 'text-green-400' :
                    gamificacion.nutriGrade === 'C' ? 'text-yellow-400' : 'text-red-400'
                  }`}>
                    {gamificacion.nutriGrade}
                    <span className="text-sm font-normal text-slate-400 ml-1">({gamificacion.nutriScore}/100)</span>
                  </p>
                  <div className="mt-1 flex gap-2 text-xs text-slate-400 flex-wrap">
                    <span title="Proteína">{gamificacion.cumpleProteina ? '✅' : '❌'} Prot.</span>
                    <span title="Balance">{gamificacion.cumpleBalance ? '✅' : '❌'} Bal.</span>
                    <span title="Ejercicio">{gamificacion.cumpleEjercicio ? '✅' : '❌'} Ej.</span>
                    <span title="Variedad">{gamificacion.cumpleVariedad ? '✅' : '❌'} Var.</span>
                  </div>
                </div>
              </div>
            )}

            {/* IA evaluation */}
            <div className="card">
              <div className="flex items-center justify-between mb-3">
                <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide">Evaluación IA</h2>
                <button onClick={handleIa} className="btn-primary text-sm py-1.5" disabled={loadingIa}>
                  {loadingIa ? (
                    <span className="flex items-center gap-2">
                      <span className="animate-spin w-3.5 h-3.5 border border-white border-t-transparent rounded-full" />
                      Analizando...
                    </span>
                  ) : (
                    '✨ Analizar día'
                  )}
                </button>
              </div>
              {ia ? (
                <p className="text-slate-300 text-sm leading-relaxed whitespace-pre-wrap">{ia}</p>
              ) : (
                <p className="text-slate-500 text-sm">Pulsa el botón para obtener un análisis personalizado de tu día.</p>
              )}
            </div>
          </div>
          </div>
        </>
      )}
    </div>
  )
}

function StatCard({ icon, label, value, color, extra }: { icon: string; label: string; value: string; color: string; extra?: React.ReactNode }) {
  return (
    <div className="card py-4">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs text-slate-500 uppercase tracking-wide">{label}</p>
          <p className={`text-xl font-bold mt-1 ${color}`}>{value}</p>
          {extra && <div className="mt-1">{extra}</div>}
        </div>
        <span className="text-2xl">{icon}</span>
      </div>
    </div>
  )
}
