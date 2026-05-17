import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'
import { getPerfil, updatePerfil } from '../api/perfil'
import { getPesoHistorial, registrarPeso } from '../api/pesoHistorial'
import WeightChart from '../components/WeightChart'
import type { Perfil as PerfilType, PesoHistorial } from '../types'

const container = { hidden: {}, show: { transition: { staggerChildren: 0.1 } } }
const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } }

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
  const [pesoFecha, setPesoFecha] = useState<string>(new Date().toISOString().split('T')[0])
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
      const nuevo = await registrarPeso(user.usuarioId, pesoFecha, parseFloat(pesoHoy))
      setPesoHistorial(prev => {
        const sinFecha = prev.filter(p => p.fecha !== pesoFecha)
        return [...sinFecha, nuevo].sort((a, b) => a.fecha.localeCompare(b.fecha))
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
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="flex-1 overflow-auto p-6">
        <div className="max-w-3xl mx-auto">
          <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="card text-center py-12 space-y-4">
            <p className="text-red-400 font-medium">{error}</p>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="btn-primary"
              onClick={() => { setError(null); getPerfil(user!.usuarioId).then((p) => { setPerfil(p); setForm(p) }).catch(() => setError('No se pudo cargar el perfil. El servidor puede estar iniciándose, intenta de nuevo en unos segundos.')) }}
            >
              Reintentar
            </motion.button>
          </motion.div>
        </div>
      </motion.div>
    )
  }

  if (!perfil) {
    return (
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex-1 overflow-auto flex justify-center items-center">
        <div className="animate-spin w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full" />
      </motion.div>
    )
  }

  return (
    <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="flex-1 overflow-auto p-6">
      <div className="max-w-3xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="flex items-center justify-between mb-6 flex-wrap gap-3"
        >
          <div>
            <h1 className="gradient-text text-3xl font-bold">Mi Perfil</h1>
            <p className="text-white/50 text-sm mt-0.5">Datos personales y objetivos de salud</p>
          </div>
          <div className="flex items-center gap-3">
            {saved && (
              <motion.span
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                className="text-emerald-400 text-sm font-medium flex items-center gap-1"
              >
                ✓ Guardado
              </motion.span>
            )}
            <motion.button
              onClick={() => setEditing(!editing)}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className={editing ? 'btn-secondary' : 'btn-primary'}
            >
              {editing ? 'Cancelar' : '✏️ Editar'}
            </motion.button>
          </div>
        </motion.div>

        {/* Profile hero card */}
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ type: 'spring', stiffness: 100, delay: 0.15 }}
          className="card mb-6 relative overflow-hidden"
        >
          <div className="absolute -top-10 -right-10 w-40 h-40 rounded-full bg-gradient-to-br from-emerald-500/20 to-cyan-500/10 blur-3xl pointer-events-none" />
          <div className="relative">
            <div className="w-20 h-20 rounded-full bg-gradient-to-br from-emerald-500 to-cyan-500 flex items-center justify-center text-3xl font-bold text-white mb-4 shadow-lg shadow-emerald-500/30">
              {perfil.nombre?.[0]?.toUpperCase() || '?'}
            </div>
            <h2 className="text-2xl font-bold text-white">{perfil.nombre}</h2>
            <p className="text-white/50 text-sm">{perfil.email}</p>
          </div>
        </motion.div>

        {/* Stats cards */}
        <motion.div
          variants={container}
          initial="hidden"
          animate="show"
          className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6"
        >
          <motion.div variants={item}>
            <StatCard label="TMB" value={`${Math.round(perfil.tmb)} kcal`} icon="🔥" />
          </motion.div>
          <motion.div variants={item}>
            <StatCard label="TDEE" value={`${Math.round(perfil.tdee)} kcal`} icon="⚡" color="text-cyan-400" />
          </motion.div>
          <motion.div variants={item}>
            <StatCard label="Peso actual" value={`${perfil.pesoKgActual} kg`} icon="⚖️" />
          </motion.div>
          <motion.div variants={item}>
            <StatCard label="Peso objetivo" value={perfil.pesoObjetivo ? `${perfil.pesoObjetivo} kg` : '—'} icon="🎯" color="text-emerald-400" />
          </motion.div>
        </motion.div>

        {/* Historial de peso */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="card mb-6"
        >
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-semibold text-white/50 uppercase tracking-wide">📈 Evolución del peso</h2>
            <form onSubmit={handleRegistrarPeso} className="flex items-center gap-2">
              <input
                type="date"
                value={pesoFecha}
                max={new Date().toISOString().split('T')[0]}
                onChange={e => setPesoFecha(e.target.value)}
                className="input w-32 py-1 text-sm"
              />
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
              <motion.button
                type="submit"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="btn-primary text-sm py-1.5"
                disabled={savingPeso}
              >
                {savingPeso ? '...' : 'Registrar'}
              </motion.button>
            </form>
          </div>
          <WeightChart data={pesoHistorial} />
        </motion.div>

        {editing ? (
          <motion.form
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            onSubmit={handleSave}
            className="card space-y-4"
          >
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
              <motion.button
                type="button"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="btn-secondary"
                onClick={() => setEditing(false)}
              >
                Cancelar
              </motion.button>
              <motion.button
                type="submit"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="btn-primary"
                disabled={saving}
              >
                {saving ? 'Guardando...' : 'Guardar cambios'}
              </motion.button>
            </div>
          </motion.form>
        ) : (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="card"
          >
            <h2 className="text-base font-semibold text-white mb-4">ℹ️ Información personal</h2>
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
          </motion.div>
        )}
      </div>
    </motion.div>
  )
}

function StatCard({ label, value, icon = '📊', color = 'text-white' }: { readonly label: string; readonly value: string; readonly icon?: string; readonly color?: string }) {
  return (
    <motion.div
      whileHover={{ scale: 1.05, y: -4 }}
      transition={{ type: 'spring', stiffness: 300 }}
      className="card py-4"
    >
      <p className="text-xs text-white/50 uppercase tracking-wide">{label}</p>
      <p className={`text-lg font-bold mt-1 ${color}`}>{value}</p>
      <span className="text-2xl block mt-2">{icon}</span>
    </motion.div>
  )
}

function InfoRow({ label, value }: { readonly label: string; readonly value: string | number }) {
  return (
    <motion.div whileHover={{ x: 4 }}>
      <dt className="text-xs text-white/50 uppercase tracking-wide">{label}</dt>
      <dd className="text-white font-medium mt-0.5">{value}</dd>
    </motion.div>
  )
}
