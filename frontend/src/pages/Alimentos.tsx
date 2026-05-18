import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Search, Trash2 } from 'lucide-react'
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
  const [confirmDelete, setConfirmDelete] = useState<number | null>(null)
  const [deleteError, setDeleteError] = useState('')

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
    setConfirmDelete(id)
    setDeleteError('')
  }

  const confirmDeleteFood = async (id: number) => {
    try {
      await deleteAlimento(id)
      setAlimentos((prev) => prev.filter((a) => a.id !== id))
      setConfirmDelete(null)
    } catch {
      setDeleteError('No se pudo eliminar')
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
    <motion.div
      className="p-6 max-w-5xl mx-auto"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
    >
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <motion.div initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: 0.1 }}>
          <h1 className="text-2xl font-bold gradient-text">Alimentos</h1>
          <p className="text-white/50 text-sm mt-0.5">Catálogo de alimentos con información nutricional</p>
        </motion.div>
        <motion.button
          onClick={() => setShowForm(!showForm)}
          className="btn-primary"
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.1 }}
        >
          {showForm ? 'Cancelar' : '+ Nuevo alimento'}
        </motion.button>
      </div>

      <AnimatePresence>
        {showForm && (
          <motion.div
            className="card mb-6"
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ type: 'spring', stiffness: 100, damping: 15 }}
          >
            <h2 className="text-base font-semibold text-white mb-4">Añadir alimento</h2>
            {error && (
              <motion.p
                className="text-red-400 text-sm mb-3"
                initial={{ opacity: 0, y: -8 }}
                animate={{ opacity: 1, y: 0 }}
              >
                {error}
              </motion.p>
            )}
            <form onSubmit={handleCreate} className="grid grid-cols-2 md:grid-cols-3 gap-4">
              <div className="col-span-2 md:col-span-3">{field('nombre', 'Nombre', 'text')}</div>
              {field('porcionG', 'Porción (g)')}
              {field('kcalPor100g', 'Kcal / 100g')}
              {field('proteinasG', 'Proteínas (g)')}
              {field('grasasG', 'Grasas (g)')}
              {field('carbosG', 'Carbohidratos (g)')}
              <div className="col-span-2 md:col-span-3 flex justify-end gap-2">
                <motion.button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setShowForm(false)}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  Cancelar
                </motion.button>
                <motion.button
                  type="submit"
                  className="btn-primary"
                  disabled={saving}
                  whileHover={!saving ? { scale: 1.05 } : {}}
                  whileTap={!saving ? { scale: 0.95 } : {}}
                >
                  {saving ? 'Guardando...' : 'Guardar'}
                </motion.button>
              </div>
            </form>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="relative mb-6">
        <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-white/30 w-5 h-5" />
        <motion.input
          type="text"
          className="input pl-12 text-lg py-4 rounded-2xl w-full"
          placeholder="Buscar alimentos..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          initial={{ opacity: 0, y: -8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15 }}
        />
      </div>

      <AnimatePresence>
        {deleteError && (
          <motion.p
            className="text-red-400 text-sm mb-4"
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
          >
            {deleteError}
          </motion.p>
        )}
      </AnimatePresence>

      {loading ? (
        <div className="flex justify-center py-16">
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
            className="w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full"
          />
        </div>
      ) : alimentos.length === 0 ? (
        <motion.div
          className="card text-center py-16 text-white/40"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ type: 'spring', stiffness: 100 }}
        >
          <p>No se encontraron alimentos{query ? ` para "${query}"` : ''}.</p>
        </motion.div>
      ) : (
        <motion.div
          className="card overflow-hidden p-0"
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-white/10 text-left">
                  <th className="px-4 py-3 text-white/50 font-medium">Nombre</th>
                  <th className="px-4 py-3 text-white/50 font-medium text-right">Kcal/100g</th>
                  <th className="px-4 py-3 text-white/50 font-medium text-right hidden md:table-cell">Prot.</th>
                  <th className="px-4 py-3 text-white/50 font-medium text-right hidden md:table-cell">Grasas</th>
                  <th className="px-4 py-3 text-white/50 font-medium text-right hidden md:table-cell">Carbos</th>
                  <th className="px-4 py-3 text-white/50 font-medium hidden sm:table-cell">Fuente</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody>
                <AnimatePresence>
                  {alimentos.map((a) => (
                    <motion.tr
                      key={a.id}
                      className="border-b border-white/5 hover:bg-white/5 transition-colors"
                      layout
                      initial={{ opacity: 0, x: -16 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: 16 }}
                      transition={{ type: 'spring', stiffness: 100, damping: 15 }}
                      whileHover={{ backgroundColor: 'rgba(255, 255, 255, 0.08)' }}
                    >
                      <td className="px-4 py-3 font-medium text-white">{a.nombre}</td>
                      <td className="px-4 py-3 text-right text-amber-400 font-semibold">{a.kcalPor100g}</td>
                      <td className="px-4 py-3 text-right text-blue-400 hidden md:table-cell">{a.proteinasG}g</td>
                      <td className="px-4 py-3 text-right text-amber-300 hidden md:table-cell">{a.grasasG}g</td>
                      <td className="px-4 py-3 text-right text-purple-400 hidden md:table-cell">{a.carbosG}g</td>
                      <td className="px-4 py-3 hidden sm:table-cell">
                        <motion.span
                          className="px-2 py-1 rounded-full bg-white/10 text-white/50 text-xs"
                          whileHover={{ backgroundColor: 'rgba(255, 255, 255, 0.15)' }}
                        >
                          {a.fuente ?? 'manual'}
                        </motion.span>
                      </td>
                      <td className="px-4 py-3">
                        <AnimatePresence mode="wait">
                          {confirmDelete === a.id ? (
                            <motion.div
                              key="confirm"
                              className="flex gap-1"
                              initial={{ opacity: 0 }}
                              animate={{ opacity: 1 }}
                              exit={{ opacity: 0 }}
                            >
                              <motion.button
                                onClick={() => confirmDeleteFood(a.id)}
                                className="btn-danger text-xs py-1 px-2"
                                whileTap={{ scale: 0.9 }}
                              >
                                Sí
                              </motion.button>
                              <motion.button
                                onClick={() => setConfirmDelete(null)}
                                className="btn-secondary text-xs py-1 px-2"
                                whileTap={{ scale: 0.9 }}
                              >
                                No
                              </motion.button>
                            </motion.div>
                          ) : (
                            <motion.button
                              key="delete"
                              onClick={() => handleDelete(a.id)}
                              className="btn-danger"
                              whileHover={{ scale: 1.1 }}
                              whileTap={{ scale: 0.9 }}
                            >
                              <Trash2 className="w-4 h-4" />
                            </motion.button>
                          )}
                        </AnimatePresence>
                      </td>
                    </motion.tr>
                  ))}
                </AnimatePresence>
              </tbody>
            </table>
          </div>
        </motion.div>
      )}
    </motion.div>
  )
}
