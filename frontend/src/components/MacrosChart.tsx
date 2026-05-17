import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import type { MacrosPoint } from '../api/tendencias'

interface Props {
  data: MacrosPoint[]
}

export default function MacrosChart({ data }: Props) {
  if (data.length === 0) {
    return (
      <div className="card">
        <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4">🥗 Macros Promedio Semanal</h2>
        <div className="flex items-center justify-center h-80 text-slate-500">
          <p>Sin datos de macros disponibles</p>
        </div>
      </div>
    )
  }

  const chartData = data.map(m => ({
    semana: m.semana,
    proteinas: Math.round(m.proteinasPromedio),
    carbos: Math.round(m.carbosPromedio),
    grasas: Math.round(m.grasasPromedio),
  }))

  return (
    <div className="card">
      <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4">🥗 Macros Promedio Semanal</h2>
      <ResponsiveContainer width="100%" height={320}>
        <BarChart data={chartData} margin={{ top: 5, right: 30, left: -20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis
            dataKey="semana"
            stroke="#64748B"
            tick={{ fontSize: 11 }}
            interval={Math.max(0, Math.floor(data.length / 4))}
          />
          <YAxis stroke="#64748B" tick={{ fontSize: 12 }} label={{ value: 'g/día', angle: -90, position: 'insideLeft' }} />
          <Tooltip
            contentStyle={{
              backgroundColor: '#1e293b',
              border: '1px solid #475569',
              borderRadius: '8px',
            }}
            labelStyle={{ color: '#e2e8f0' }}
            formatter={(value: number) => `${value}g`}
          />
          <Legend wrapperStyle={{ paddingTop: '20px' }} />
          <Bar dataKey="proteinas" stackId="a" fill="#3B82F6" name="Proteínas" isAnimationActive={false} />
          <Bar dataKey="carbos" stackId="a" fill="#FBBF24" name="Carbohidratos" isAnimationActive={false} />
          <Bar dataKey="grasas" stackId="a" fill="#EF4444" name="Grasas" isAnimationActive={false} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
