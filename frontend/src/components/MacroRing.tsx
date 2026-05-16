interface MacroRingProps {
  kcal: number
  tdee: number
  proteinas: number
  grasas: number
  carbos: number
}

function Ring({ value, max, color, size = 120, stroke = 10 }: { value: number; max: number; color: string; size?: number; stroke?: number }) {
  const r = (size - stroke) / 2
  const circ = 2 * Math.PI * r
  const pct = Math.min(value / Math.max(max, 1), 1)
  const dash = pct * circ

  return (
    <svg width={size} height={size} className="rotate-[-90deg]">
      <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#1e293b" strokeWidth={stroke} />
      <circle
        cx={size / 2} cy={size / 2} r={r} fill="none"
        stroke={color} strokeWidth={stroke}
        strokeDasharray={`${dash} ${circ}`}
        strokeLinecap="round"
        style={{ transition: 'stroke-dasharray 0.6s ease' }}
      />
    </svg>
  )
}

export default function MacroRing({ kcal, tdee, proteinas, grasas, carbos }: MacroRingProps) {
  const total = proteinas + grasas + carbos || 1

  return (
    <div className="flex flex-col items-center gap-6">
      <div className="relative">
        <Ring value={kcal} max={tdee} color="#22c55e" size={160} stroke={14} />
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-3xl font-bold text-slate-100">{Math.round(kcal)}</span>
          <span className="text-xs text-slate-400">/ {Math.round(tdee)} kcal</span>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-4 w-full">
        <MacroBar label="Proteínas" value={proteinas} total={total} color="bg-blue-500" unit="g" />
        <MacroBar label="Grasas" value={grasas} total={total} color="bg-amber-500" unit="g" />
        <MacroBar label="Carbos" value={carbos} total={total} color="bg-purple-500" unit="g" />
      </div>
    </div>
  )
}

function MacroBar({ label, value, total, color, unit }: { label: string; value: number; total: number; color: string; unit: string }) {
  const pct = Math.round((value / total) * 100)
  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs text-slate-400">{label}</span>
      <div className="h-1.5 bg-slate-700 rounded-full overflow-hidden">
        <div className={`h-full ${color} rounded-full transition-all duration-500`} style={{ width: `${pct}%` }} />
      </div>
      <span className="text-sm font-semibold text-slate-200">{Math.round(value)}{unit}</span>
    </div>
  )
}
