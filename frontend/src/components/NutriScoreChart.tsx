import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts'
import type { NutriScorePoint } from '../api/tendencias'

interface Props {
  data: NutriScorePoint[]
}

const gradeColor: Record<string, string> = {
  'A': '#10B981',
  'B': '#3B82F6',
  'C': '#FBBF24',
  'D': '#FB923C',
  'F': '#EF4444',
}

export default function NutriScoreChart({ data }: Props) {
  if (data.length === 0) {
    return (
      <div className="card">
        <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4">⭐ Evolución NutriScore</h2>
        <div className="flex items-center justify-center h-80 text-slate-500">
          <p>Sin datos de NutriScore disponibles</p>
        </div>
      </div>
    )
  }

  const chartData = data.map(p => ({
    ...p,
    fecha: new Date(p.fecha).toLocaleDateString('es-ES', { month: 'short', day: 'numeric' }),
    color: gradeColor[p.grade] || '#64748B',
  }))

  const colors = chartData.map(d => d.color)
  const dominantColor = colors.length > 0 ? colors[0] : '#3B82F6'

  return (
    <div className="card">
      <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide mb-4">⭐ Evolución NutriScore</h2>
      <ResponsiveContainer width="100%" height={320}>
        <AreaChart data={chartData} margin={{ top: 5, right: 30, left: -20, bottom: 5 }}>
          <defs>
            <linearGradient id="colorScore" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={dominantColor} stopOpacity={0.4} />
              <stop offset="95%" stopColor={dominantColor} stopOpacity={0.05} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis
            dataKey="fecha"
            stroke="#64748B"
            tick={{ fontSize: 12 }}
            interval={Math.max(0, Math.floor(data.length / 6))}
          />
          <YAxis stroke="#64748B" tick={{ fontSize: 12 }} domain={[0, 100]} />
          <Tooltip
            contentStyle={{
              backgroundColor: '#1e293b',
              border: '1px solid #475569',
              borderRadius: '8px',
            }}
            labelStyle={{ color: '#e2e8f0' }}
            formatter={(value: number, name: string) => {
              if (name === 'score') return [`${value}/100`, 'Score']
              return value
            }}
            content={({ active, payload }) => {
              if (active && payload && payload.length) {
                const data = payload[0].payload
                return (
                  <div className="bg-slate-800 border border-slate-700 rounded p-2 text-sm">
                    <p className="text-slate-300">{data.fecha}</p>
                    <p className="font-bold" style={{ color: data.color }}>
                      {data.grade} ({data.score}/100)
                    </p>
                  </div>
                )
              }
              return null
            }}
          />
          <ReferenceLine
            y={75}
            stroke="#10B981"
            strokeDasharray="5 5"
            label={{ value: 'Buen nivel (75+)', position: 'right', fill: '#10B981', fontSize: 11 }}
          />
          <Area
            type="monotone"
            dataKey="score"
            stroke={dominantColor}
            fillOpacity={1}
            fill="url(#colorScore)"
            isAnimationActive={false}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  )
}
