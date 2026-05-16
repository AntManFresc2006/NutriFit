import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getPerfil, updatePerfil } from '../api/perfil'
import { getPesoHistorial, registrarPeso } from '../api/pesoHistorial'
import WeightChart from '../components/WeightChart'
import type { Perfil as PerfilType, PesoHistorial } from '../types'

const NIVEL_ACTIVIDAD = [
  { value: 'SEDENTARIO', label: 'Sedentario (sin ejercicio)' },
  { value: 'LIGERO', label: 'Ligeramente activo (1-3 días/sem)' },
  { value: 'MODERADO', label: 'Moderadamente activo (3-5 días/sem)' },
  { value: 'ALTO', label: 'Muy activo (6-7 días/sem)' },
  { value: 'MUY_ALTO', label: 'Extra activo (trabajo físico + deporte)' },
]

export default function Perfil() {
  const { user } = useAuth()
  const [perfil, setPerfil] = useState<PerfilType | null>(null)
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState<Partial<PerfilType>>({})
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [pesoHistorial, setPesoHistorial] = useState<PesoHistorial[]>([])
  const [pesoHoy, setPesoHoy] = useState('')
  const [savingPeso, setSavingPeso] = useState(false)

  useEffect(() => {
    if (!user) return
    setError(null)
    Promise.all([
      getPerfil(user.usuarioId).then((p) => { setPerfil(p); setForm(p) }),
      getPesoHistorial(user.usuarioId).then((ph) => setPesoHistorial(ph)),
    ]).catch(() => setError('No se pudo cargar el perfil. El servidor puede estar iniciándose, intenta de nuevo en unos segundos.'))
  }, [user])

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user || !perfil) return
    setSaving(true)
    try {
      const updated = await updatePerfil(user.usuarioId, form)
      setPerfil(updated)
      setForm(updated)
      setEditing(false)
      setSaved(true)
      setTimeout(() => setSaved(false), 2500)
    } finally {
      setSaving(false)
    }
  }

  const handleRegistrarPeso = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user || !pesoHoy) return
    setSavingPeso(true)
    try {
      const today = new Date().toISOString().split('T')[0]
      const nuevo = await registrarPeso(user.usuarioId, today, parseFloat(pesoHoy))
      setPesoHistorial(prev => {
        const sinHoy = prev.filter(p => p.fecha !== today)
        return [...sinHoy, nuevo].sort((a, b) => a.fecha.localeCompare(b.fecha))
      })
      setPesoHoy('')
    } finally {
      setSavingPeso(false)
    }
  }

  const f = (key: keyof PerfilType, label: string, type = 'text', options?: { value: string; label: string }[]) => {
    const val = form[key] ?? ''
    if (options) {
      return (
        <div>
          <label className="label">{label}</label>
          <select
            className="input"
            value={String(val)}
            onChange={(e) => setForm((f) => ({ ...f, [key]: e.target.value }))}
          >
            {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
      )
    }
    return (
      <div>
        <label className="label">{label}</label>
        <input
          type={type}
          className="input"
          value={String(val)}
          onChange={(e) => setForm((f) => ({ ...f, [key]: type === 'number' ? parseFloat(e.target.value) || 0 : e.target.value }))}
        />
      </div>
    )
  }

  if (error) {
    return (
      <div className="p-6 max-w-3xl mx-auto">
        <div className="card text-center py-12 space-y-4">
          <p className="text-red-400 font-medium">{error}</p>
          <button
            className="btn-primary"
            onClick={() => { setError(null); getPerfil(user!.usuarioId).then((p) => { setPerfil(p); setForm(p) }).catch(() => setError('No se pudo cargar el perfil. El servidor puede estar iniciándose, intenta de nuevo en unos segundos.')) }}
          >
            Reintentar
          </button>
        </div>
      </div>
    )
  }

  if (!perfil) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin w-8 h-8 border-2 border-green-500 border-t-transparent rounded-full" />
      </div>
    )
  }

  return (
    <div className="p-6 max-w-3xl mx-auto">
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Perfil</h1>
          <p className="text-slate-400 text-sm mt-0.5">Datos personales y objetivos</p>
        </div>
        {saved && <span className="text-green-400 text-sm">✓ Guardado</span>}
        <button onClick={() => setEditing(!editing)} className={editing ? 'btn-secondary' : 'btn-primary'}>
          {editing ? 'Cancelar' : '✏️ Editar'}
        </button>
      </div>

      {/* Stats cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <StatCard label="TMB" value={`${Math.round(perfil.tmb)} kcal`} />
        <StatCard label="TDEE" value={`${Math.round(perfil.tdee)} kcal`} color="text-green-400" />
        <StatCard label="Peso actual" value={`${perfil.pesoKgActual} kg`} />
        <StatCard label="Peso objetivo" value={perfil.pesoObjetivo ? `${perfil.pesoObjetivo} kg` : '—'} />
      </div>

      {/* Historial de peso */}
      <div className="card mb-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wide">Evolución del peso</h2>
          <form onSubmit={handleRegistrarPeso} className="flex items-center gap-2">
            <input
              type="number"
              step="0.1"
              min="20"
              max="500"
              placeholder="kg hoy"
              value={pesoHoy}
              onChange={e => setPesoHoy(e.target.value)}
              className="input w-24 py-1 text-sm"
            />
            <button type="submit" className="btn-primary text-sm py-1.5" disabled={savingPeso}>
              {savingPeso ? '...' : 'Registrar'}
            </button>
          </form>
        </div>
        <WeightChart data={pesoHistorial} />
      </div>

      {editing ? (
        <form onSubmit={handleSave} className="card space-y-4">
          <h2 className="text-base font-semibold text-slate-200 mb-2">Editar datos</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {f('nombre', 'Nombre')}
            {f('fechaNacimiento', 'Fecha de nacimiento', 'date')}
            {f('alturaCm', 'Altura (cm)', 'number')}
            {f('pesoKgActual', 'Peso actual (kg)', 'number')}
            {f('pesoObjetivo', 'Peso objetivo (kg)', 'number')}
            {f('sexo', 'Sexo', 'text', [{ value: 'H', label: 'Masculino' }, { value: 'M', label: 'Femenino' }])}
            <div className="sm:col-span-2">
              {f('nivelActividad', 'Nivel de actividad', 'text', NIVEL_ACTIVIDAD)}
            </div>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setEditing(false)}>Cancelar</button>
            <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Guardando...' : 'Guardar cambios'}</button>
          </div>
        </form>
      ) : (
        <div className="card">
          <h2 className="text-base font-semibold text-slate-200 mb-4">Información personal</h2>
          <dl className="grid grid-cols-1 sm:grid-cols-2 gap-y-4 gap-x-8">
            <InfoRow label="Nombre" value={perfil.nombre} />
            <InfoRow label="Email" value={perfil.email} />
            <InfoRow label="Fecha de nacimiento" value={perfil.fechaNacimiento ?? '—'} />
            <InfoRow label="Sexo" value={perfil.sexo === 'H' ? 'Masculino' : 'Femenino'} />
            <InfoRow label="Altura" value={`${perfil.alturaCm} cm`} />
            <InfoRow label="Peso actual" value={`${perfil.pesoKgActual} kg`} />
            <InfoRow label="Peso objetivo" value={perfil.pesoObjetivo ? `${perfil.pesoObjetivo} kg` : '—'} />
            <InfoRow label="Nivel de actividad" value={NIVEL_ACTIVIDAD.find(n => n.value === perfil.nivelActividad)?.label ?? perfil.nivelActividad} />
          </dl>
        </div>
      )}
    </div>
  )
}

function StatCard({ label, value, color = 'text-slate-100' }: { label: string; value: string; color?: string }) {
  return (
    <div className="card py-4">
      <p className="text-xs text-slate-500 uppercase tracking-wide">{label}</p>
      <p className={`text-lg font-bold mt-1 ${color}`}>{value}</p>
    </div>
  )
}

function InfoRow({ label, value }: { label: string; value: string | number }) {
  return (
    <div>
      <dt className="text-xs text-slate-500 uppercase tracking-wide">{label}</dt>
      <dd className="text-slate-200 font-medium mt-0.5">{value}</dd>
    </div>
  )
}
