import { useState } from 'react'
import type { EjercicioPoint } from '../api/tendencias'

interface Props {
  data: EjercicioPoint[]
}

export default function EjercicioHeatmap({ data }: Props) {
  const [hoveredCell, setHoveredCell] = useState<string | null>(null)

  if (data.length === 0) {
    return (
      <div className="card">
        <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4">🏃 Actividad Física</h2>
        <div className="flex items-center justify-center h-80 text-slate-500">
          <p>Sin datos de ejercicio disponibles</p>
        </div>
      </div>
    )
  }

  const sortedData = [...data].sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime())
  const weeks: EjercicioPoint[][] = []

  let currentWeek: EjercicioPoint[] = []
  let lastDate = new Date(sortedData[0].fecha)

  for (const punto of sortedData) {
    const fecha = new Date(punto.fecha)
    const daysDiff = (fecha.getTime() - lastDate.getTime()) / (1000 * 60 * 60 * 24)

    if (currentWeek.length > 0 && daysDiff > 7) {
      weeks.push(currentWeek)
      currentWeek = []
    }

    currentWeek.push(punto)
    lastDate = fecha
  }

  if (currentWeek.length > 0) {
    weeks.push(currentWeek)
  }

  const getColor = (punto: EjercicioPoint): string => {
    if (!punto.tuvoEjercicio) return 'bg-slate-700'
    if (punto.duracionMin >= 45) return 'bg-green-400'
    if (punto.duracionMin >= 20) return 'bg-green-600'
    return 'bg-green-800'
  }

  const getTooltip = (punto: EjercicioPoint): string => {
    if (!punto.tuvoEjercicio) return 'Sin ejercicio'
    return `${punto.duracionMin}min • ${punto.kcalQuemadas.toFixed(0)} kcal`
  }

  const dayNames = ['L', 'M', 'X', 'J', 'V', 'S', 'D']

  return (
    <div className="card">
      <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-6">🏃 Actividad Física</h2>
      <div className="space-y-4">
        {weeks.map((week, weekIdx) => (
          <div key={weekIdx} className="flex items-center gap-2">
            {/* Labels de días */}
            {weekIdx === 0 && (
              <div className="flex gap-1 w-14">
                {dayNames.slice(0, week.length).map(day => (
                  <div key={day} className="text-center text-xs text-slate-500 font-semibold w-6 h-6 flex items-center justify-center">
                    {day}
                  </div>
                ))}
              </div>
            )}

            {/* Celdas de la semana */}
            <div className="flex gap-1">
              {week.map((punto, dayIdx) => {
                const cellId = `${weekIdx}-${dayIdx}`
                const color = getColor(punto)
                const tooltip = getTooltip(punto)
                const fecha = new Date(punto.fecha).toLocaleDateString('es-ES')

                return (
                  <div
                    key={cellId}
                    className={`w-6 h-6 rounded cursor-pointer transition-opacity hover:opacity-75 relative group ${color}`}
                    onMouseEnter={() => setHoveredCell(cellId)}
                    onMouseLeave={() => setHoveredCell(null)}
                    title={`${fecha} - ${tooltip}`}
                  >
                    {hoveredCell === cellId && (
                      <div className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 bg-slate-900 text-slate-100 text-xs px-2 py-1 rounded whitespace-nowrap border border-slate-700 z-10 pointer-events-none">
                        {fecha}
                        <br />
                        {tooltip}
                      </div>
                    )}
                  </div>
                )
              })}
            </div>
          </div>
        ))}
      </div>

      <div className="mt-6 flex gap-4 text-xs justify-center flex-wrap">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded bg-slate-700" />
          <span className="text-slate-400">Sin ejercicio</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded bg-green-800" />
          <span className="text-slate-400">&lt;20 min</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded bg-green-600" />
          <span className="text-slate-400">20-45 min</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded bg-green-400" />
          <span className="text-slate-400">&gt;45 min</span>
        </div>
      </div>
    </div>
  )
}
