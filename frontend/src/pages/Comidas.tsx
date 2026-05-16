import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getComidas, createComida, deleteComida, getComidaItems, addItemToComida, deleteComidaItem } from '../api/comidas'
import { getAlimentos } from '../api/alimentos'
import type { Comida, ComidaItem, Alimento } from '../types'

const TIPOS = ['DESAYUNO', 'COMIDA', 'MERIENDA', 'CENA', 'SNACK']

function today() {
  return new Date().toISOString().split('T')[0]
}

export default function Comidas() {
  const { user } = useAuth()
  const [fecha, setFecha] = useState(today())
  const [comidas, setComidas] = useState<Comida[]>([])
  const [items, setItems] = useState<Record<number, ComidaItem[]>>({})
  const [loading, setLoading] = useState(true)
  const [creatingTipo, setCreatingTipo] = useState('')
  const [addingTo, setAddingTo] = useState<number | null>(null)
  const [alimentos, setAlimentos] = useState<Alimento[]>([])
  const [alimentoQ, setAlimentoQ] = useState('')
  const [selectedAlimento, setSelectedAlimento] = useState<Alimento | null>(null)
  const [gramos, setGramos] = useState(100)

  const loadComidas = () => {
    if (!user) return
    setLoading(true)
    getComidas(user.usuarioId, fecha)
      .then(async (cs) => {
        setComidas(cs)
        const itemsMap: Record<number, ComidaItem[]> = {}
        await Promise.all(cs.map(async (c) => { itemsMap[c.id] = await getComidaItems(c.id) }))
        setItems(itemsMap)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadComidas() }, [user, fecha])

  useEffect(() => {
    const t = setTimeout(() => {
      if (addingTo !== null) getAlimentos(alimentoQ || undefined).then(setAlimentos)
    }, 300)
    return () => clearTimeout(t)
  }, [alimentoQ, addingTo])

  const handleCreateComida = async (tipo: string) => {
    if (!user) return
    setCreatingTipo(tipo)
    try {
      const c = await createComida(user.usuarioId, fecha, tipo)
      setComidas((prev) => [...prev, c])
      setItems((prev) => ({ ...prev, [c.id]: [] }))
    } finally {
      setCreatingTipo('')
    }
  }

  const handleDeleteComida = async (id: number) => {
    if (!confirm('¿Eliminar esta comida y todos sus alimentos?')) return
    await deleteComida(id)
    setComidas((prev) => prev.filter((c) => c.id !== id))
    setItems((prev) => { const n = { ...prev }; delete n[id]; return n })
  }

  const openAddItem = (comidaId: number) => {
    setAddingTo(comidaId)
    setSelectedAlimento(null)
    setAlimentoQ('')
    setGramos(100)
    getAlimentos().then(setAlimentos)
  }

  const handleAddItem = async () => {
    if (!selectedAlimento || addingTo === null) return
    await addItemToComida(addingTo, selectedAlimento.id, gramos)
    const newItems = await getComidaItems(addingTo)
    setItems((prev) => ({ ...prev, [addingTo]: newItems }))
    setAddingTo(null)
  }

  const handleDeleteItem = async (comidaId: number, itemId: number) => {
    await deleteComidaItem(comidaId, itemId)
    setItems((prev) => ({ ...prev, [comidaId]: prev[comidaId].filter((i) => i.itemId !== itemId) }))
  }

  const tiposRegistrados = comidas.map((c) => c.tipo)
  const tiposDisponibles = TIPOS.filter((t) => !tiposRegistrados.includes(t))

  const kcalComida = (comidaId: number) =>
    (items[comidaId] ?? []).reduce((s, i) => s + i.kcalEstimadas, 0)

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Comidas</h1>
          <p className="text-slate-400 text-sm mt-0.5">Registro de comidas del día</p>
        </div>
        <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} className="input w-auto" />
      </div>

      {loading ? (
        <div className="flex justify-center py-16">
          <div className="animate-spin w-8 h-8 border-2 border-green-500 border-t-transparent rounded-full" />
        </div>
      ) : (
        <div className="space-y-4">
          {comidas.map((c) => (
            <div key={c.id} className="card">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <span className="text-lg">{tipoIcon(c.tipo)}</span>
                  <h2 className="font-semibold text-slate-200">{c.tipo}</h2>
                  <span className="badge bg-amber-500/20 text-amber-400">{Math.round(kcalComida(c.id))} kcal</span>
                </div>
                <div className="flex gap-2">
                  <button onClick={() => openAddItem(c.id)} className="btn-secondary text-sm py-1.5">+ Alimento</button>
                  <button onClick={() => handleDeleteComida(c.id)} className="btn-danger">🗑</button>
                </div>
              </div>

              {addingTo === c.id && (
                <div className="bg-slate-700/40 rounded-xl p-4 mb-3 space-y-3">
                  <input
                    type="text"
                    className="input"
                    placeholder="Buscar alimento..."
                    value={alimentoQ}
                    onChange={(e) => setAlimentoQ(e.target.value)}
                    autoFocus
                  />
                  {alimentos.length > 0 && !selectedAlimento && (
                    <div className="max-h-48 overflow-y-auto space-y-1">
                      {alimentos.map((a) => (
                        <button
                          key={a.id}
                          onClick={() => setSelectedAlimento(a)}
                          className="w-full text-left px-3 py-2 rounded-lg hover:bg-slate-600 transition-colors text-sm"
                        >
                          <span className="text-slate-200">{a.nombre}</span>
                          <span className="text-slate-400 ml-2">{a.kcalPor100g} kcal/100g</span>
                        </button>
                      ))}
                    </div>
                  )}
                  {selectedAlimento && (
                    <div className="flex items-center gap-3 flex-wrap">
                      <span className="text-green-400 font-medium text-sm">{selectedAlimento.nombre}</span>
                      <div className="flex items-center gap-2">
                        <label className="text-sm text-slate-400">Gramos:</label>
                        <input
                          type="number"
                          className="input w-24"
                          value={gramos}
                          min={1}
                          onChange={(e) => setGramos(parseFloat(e.target.value) || 0)}
                        />
                      </div>
                      <button onClick={handleAddItem} className="btn-primary text-sm py-1.5">Añadir</button>
                      <button onClick={() => setSelectedAlimento(null)} className="btn-secondary text-sm py-1.5">Cambiar</button>
                    </div>
                  )}
                  <button onClick={() => setAddingTo(null)} className="text-slate-500 text-xs hover:text-slate-300">Cancelar</button>
                </div>
              )}

              {(items[c.id] ?? []).length === 0 ? (
                <p className="text-slate-500 text-sm">Sin alimentos registrados.</p>
              ) : (
                <div className="space-y-1">
                  {(items[c.id] ?? []).map((item) => (
                    <div key={item.itemId} className="flex items-center justify-between py-1.5 px-2 rounded-lg hover:bg-slate-700/40">
                      <div className="flex-1 min-w-0">
                        <span className="text-slate-200 text-sm font-medium">{item.nombre}</span>
                        <span className="text-slate-500 text-xs ml-2">{item.gramos}g</span>
                      </div>
                      <div className="flex items-center gap-3 text-xs shrink-0">
                        <span className="text-amber-400">{Math.round(item.kcalEstimadas)} kcal</span>
                        <span className="text-blue-400 hidden sm:block">{Math.round(item.proteinasEstimadas)}g P</span>
                        <span className="text-amber-300 hidden sm:block">{Math.round(item.grasasEstimadas)}g G</span>
                        <span className="text-purple-400 hidden sm:block">{Math.round(item.carbosEstimados)}g C</span>
                        <button onClick={() => handleDeleteItem(c.id, item.itemId)} className="text-slate-600 hover:text-red-400 ml-1">✕</button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}

          {tiposDisponibles.length > 0 && (
            <div className="card border-dashed">
              <p className="text-sm text-slate-500 mb-3">Añadir comida</p>
              <div className="flex flex-wrap gap-2">
                {tiposDisponibles.map((t) => (
                  <button
                    key={t}
                    onClick={() => handleCreateComida(t)}
                    disabled={creatingTipo === t}
                    className="btn-secondary text-sm flex items-center gap-1.5"
                  >
                    {tipoIcon(t)} {t}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

function tipoIcon(tipo: string) {
  const map: Record<string, string> = {
    DESAYUNO: '☀️', COMIDA: '🍽️', MERIENDA: '🍎', CENA: '🌙', SNACK: '🍿',
  }
  return map[tipo] ?? '🍴'
}
