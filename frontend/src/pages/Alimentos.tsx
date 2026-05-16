import { useEffect, useState } from 'react'
import { getAlimentos, createAlimento, deleteAlimento } from '../api/alimentos'
import type { Alimento, AlimentoRequest } from '../types'

const empty: AlimentoRequest = { nombre: '', porcionG: 100, kcalPor100g: 0, proteinasG: 0, grasasG: 0, carbosG: 0 }

export default function Alimentos() {
  const [alimentos, setAlimentos] = useState<Alimento[]>([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState<AlimentoRequest>(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const load = (q?: string) => {
    setLoading(true)
    getAlimentos(q)
      .then(setAlimentos)
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  useEffect(() => {
    const t = setTimeout(() => load(query || undefined), 350)
    return () => clearTimeout(t)
  }, [query])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const created = await createAlimento(form)
      setAlimentos((prev) => [created, ...prev])
      setForm(empty)
      setShowForm(false)
    } catch {
      setError('Error al crear el alimento')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('¿Eliminar este alimento?')) return
    try {
      await deleteAlimento(id)
      setAlimentos((prev) => prev.filter((a) => a.id !== id))
    } catch {
      alert('No se pudo eliminar')
    }
  }

  const field = (key: keyof AlimentoRequest, label: string, type = 'number') => (
    <div>
      <label className="label">{label}</label>
      <input
        type={type}
        className="input"
        value={form[key]}
        step={type === 'number' ? '0.1' : undefined}
        onChange={(e) => setForm((f) => ({ ...f, [key]: type === 'number' ? parseFloat(e.target.value) || 0 : e.target.value }))}
        required
      />
    </div>
  )

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Alimentos</h1>
          <p className="text-slate-400 text-sm mt-0.5">Catálogo de alimentos con información nutricional</p>
        </div>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          {showForm ? '✕ Cancelar' : '+ Nuevo alimento'}
        </button>
      </div>

      {showForm && (
        <div className="card mb-6">
          <h2 className="text-base font-semibold text-slate-200 mb-4">Añadir alimento</h2>
          {error && <p className="text-red-400 text-sm mb-3">{error}</p>}
          <form onSubmit={handleCreate} className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div className="col-span-2 md:col-span-3">{field('nombre', 'Nombre', 'text')}</div>
            {field('porcionG', 'Porción (g)')}
            {field('kcalPor100g', 'Kcal / 100g')}
            {field('proteinasG', 'Proteínas (g)')}
            {field('grasasG', 'Grasas (g)')}
            {field('carbosG', 'Carbohidratos (g)')}
            <div className="col-span-2 md:col-span-3 flex justify-end gap-2">
              <button type="button" className="btn-secondary" onClick={() => setShowForm(false)}>Cancelar</button>
              <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</button>
            </div>
          </form>
        </div>
      )}

      <div className="mb-4">
        <input
          type="text"
          className="input"
          placeholder="🔍 Buscar alimentos..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>

      {loading ? (
        <div className="flex justify-center py-16">
          <div className="animate-spin w-8 h-8 border-2 border-green-500 border-t-transparent rounded-full" />
        </div>
      ) : alimentos.length === 0 ? (
        <div className="card text-center py-16 text-slate-400">
          <p className="text-4xl mb-3">🥗</p>
          <p>No se encontraron alimentos{query ? ` para "${query}"` : ''}.</p>
        </div>
      ) : (
        <div className="card overflow-hidden p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-700 text-left">
                  <th className="px-4 py-3 text-slate-400 font-medium">Nombre</th>
                  <th className="px-4 py-3 text-slate-400 font-medium text-right">Kcal/100g</th>
                  <th className="px-4 py-3 text-slate-400 font-medium text-right hidden md:table-cell">Prot.</th>
                  <th className="px-4 py-3 text-slate-400 font-medium text-right hidden md:table-cell">Grasas</th>
                  <th className="px-4 py-3 text-slate-400 font-medium text-right hidden md:table-cell">Carbos</th>
                  <th className="px-4 py-3 text-slate-400 font-medium hidden sm:table-cell">Fuente</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody>
                {alimentos.map((a) => (
                  <tr key={a.id} className="border-b border-slate-700/50 hover:bg-slate-700/30 transition-colors">
                    <td className="px-4 py-3 font-medium text-slate-100">{a.nombre}</td>
                    <td className="px-4 py-3 text-right text-amber-400 font-semibold">{a.kcalPor100g}</td>
                    <td className="px-4 py-3 text-right text-blue-400 hidden md:table-cell">{a.proteinasG}g</td>
                    <td className="px-4 py-3 text-right text-amber-300 hidden md:table-cell">{a.grasasG}g</td>
                    <td className="px-4 py-3 text-right text-purple-400 hidden md:table-cell">{a.carbosG}g</td>
                    <td className="px-4 py-3 hidden sm:table-cell">
                      <span className="badge bg-slate-700 text-slate-400">{a.fuente ?? 'manual'}</span>
                    </td>
                    <td className="px-4 py-3">
                      <button onClick={() => handleDelete(a.id)} className="btn-danger">🗑</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
