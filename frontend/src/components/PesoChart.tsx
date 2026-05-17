import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts'
import type { PesoPoint } from '../api/tendencias'

interface Props {
  data: PesoPoint[]
  pesoObjetivo: number | null
}

export default function PesoChart({ data, pesoObjetivo }: Props) {
  if (data.length < 2) {
    return (
      <div className="card">
        <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4">📉 Evolución del Peso</h2>
        <div className="flex items-center justify-center h-80 text-slate-500">
          <p>Registra al menos 2 pesajes para ver la tendencia</p>
        </div>
      </div>
    )
  }

  const minPeso = Math.min(...data.map(p => p.pesoKg))
  const maxPeso = Math.max(...data.map(p => p.pesoKg))
  const padding = (maxPeso - minPeso) * 0.1 || 5

  const chartData = data.map(p => ({
    ...p,
    fecha: new Date(p.fecha).toLocaleDateString('es-ES', { month: 'short', day: 'numeric' }),
    fechaCompleta: p.fecha,
  }))

  return (
    <div className="card">
      <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4">📉 Evolución del Peso</h2>
      <ResponsiveContainer width="100%" height={320}>
        <LineChart data={chartData} margin={{ top: 5, right: 30, left: -20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis
            dataKey="fecha"
            stroke="#64748B"
            tick={{ fontSize: 12 }}
            interval={Math.max(0, Math.floor(data.length / 6))}
          />
          <YAxis
            stroke="#64748B"
            tick={{ fontSize: 12 }}
            domain={[minPeso - padding, maxPeso + padding]}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: '#1e293b',
              border: '1px solid #475569',
              borderRadius: '8px',
            }}
            labelStyle={{ color: '#e2e8f0' }}
            formatter={(value: number) => `${value.toFixed(1)} kg`}
            labelFormatter={(label) => `${label}`}
          />
          {pesoObjetivo && (
            <ReferenceLine
              y={pesoObjetivo}
              stroke="#10B981"
              strokeDasharray="5 5"
              label={{ value: `Objetivo: ${pesoObjetivo} kg`, position: 'right', fill: '#10B981', fontSize: 12 }}
            />
          )}
          <Line
            type="monotone"
            dataKey="pesoKg"
            stroke="#3B82F6"
            dot={false}
            strokeWidth={2}
            isAnimationActive={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
