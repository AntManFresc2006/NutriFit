import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import type { PesoHistorial } from '../types'

interface Props {
  data: PesoHistorial[]
}

export default function WeightChart({ data }: Props) {
  if (data.length === 0) return (
    <p className="text-slate-500 text-sm text-center py-4">Sin registros de peso. Registra tu peso diario para ver la evolución.</p>
  )

  const formatted = data.map(d => ({
    fecha: d.fecha.slice(5).replace('-', '/'),
    peso: d.pesoKg,
  }))

  return (
    <ResponsiveContainer width="100%" height={180}>
      <LineChart data={formatted} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
        <XAxis dataKey="fecha" tick={{ fill: '#94a3b8', fontSize: 11 }} />
        <YAxis domain={['auto', 'auto']} tick={{ fill: '#94a3b8', fontSize: 11 }} />
        <Tooltip
          contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155', borderRadius: 8 }}
          labelStyle={{ color: '#94a3b8' }}
          itemStyle={{ color: '#4ade80' }}
          formatter={(v: number) => [`${v} kg`, 'Peso']}
        />
        <Line type="monotone" dataKey="peso" stroke="#4ade80" strokeWidth={2} dot={{ r: 3, fill: '#4ade80' }} />
      </LineChart>
    </ResponsiveContainer>
  )
}
