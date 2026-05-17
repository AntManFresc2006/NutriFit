import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getItems, addItem, toggleItem, deleteItem, clearCompletados, getSugerencias } from '../api/listaCompra'
import type { ListaAgrupada } from '../api/listaCompra'

const categoryIcons: Record<string, string> = {
  PROTEINAS: '💪',
  VERDURAS: '🥦',
  FRUTAS: '🍎',
  LACTEOS: '🥛',
  CEREALES: '🌾',
  BEBIDAS: '🥤',
  OTROS: '📦',
}

const categoryLabels: Record<string, string> = {
  PROTEINAS: 'Proteínas',
  VERDURAS: 'Verduras',
  FRUTAS: 'Frutas',
  LACTEOS: 'Lácteos',
  CEREALES: 'Cereales',
  BEBIDAS: 'Bebidas',
  OTROS: 'Otros',
}

export default function ListaCompra() {
  const { user } = useAuth()
  const [items, setItems] = useState<ListaAgrupada>({})
  const [sugerencias, setSugerencias] = useState<string[]>([])
  const [nombre, setNombre] = useState('')
  const [cantidad, setCantidad] = useState('')
  const [categoria, setCategoria] = useState('OTROS')
  const [loading, setLoading] = useState(false)
  const [expandSugerencias, setExpandSugerencias] = useState(false)

  useEffect(() => {
    if (!user) return
    cargarDatos()
  }, [user])

  const cargarDatos = async () => {
    if (!user) return
    try {
      const [listaData, sugsData] = await Promise.all([
        getItems(user.usuarioId),
        getSugerencias(user.usuarioId),
      ])
      setItems(listaData || {})
      setSugerencias(sugsData.sugerencias || [])
    } catch (err) {
      console.error('Error cargando datos:', err)
    }
  }

  const handleAddItem = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user || !nombre.trim()) return

    setLoading(true)
    try {
      await addItem(user.usuarioId, { nombre, cantidad: cantidad || undefined, categoria })
      setNombre('')
      setCantidad('')
      setCategoria('OTROS')
      await cargarDatos()
    } catch (err) {
      console.error('Error añadiendo item:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleToggle = async (id: number) => {
    if (!user) return
    try {
      await toggleItem(user.usuarioId, id)
      await cargarDatos()
    } catch (err) {
      console.error('Error al toggle:', err)
    }
  }

  const handleDelete = async (id: number) => {
    if (!user) return
    try {
      await deleteItem(user.usuarioId, id)
      await cargarDatos()
    } catch (err) {
      console.error('Error al eliminar:', err)
    }
  }

  const handleClearCompletados = async () => {
    if (!user) return
    try {
      await clearCompletados(user.usuarioId)
      await cargarDatos()
    } catch (err) {
      console.error('Error al limpiar:', err)
    }
  }

  const handleSugerencia = (sug: string) => {
    setNombre(sug)
  }

  const allItems = Object.values(items).flat()
  const totalItems = allItems.length
  const completados = allItems.filter(i => i.completado).length
  const pendientes = totalItems - completados
  const hasCompletados = completados > 0

  return (
    <div className="flex-1 overflow-auto">
      <div className="max-w-3xl mx-auto p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-3xl font-bold text-white mb-2">🛒 Lista de la Compra</h1>
            {totalItems > 0 && (
              <div className="flex gap-4 text-sm">
                <span className="text-green-400">✓ {pendientes} pendientes</span>
                {completados > 0 && <span className="text-slate-400">✓ {completados} completados</span>}
              </div>
            )}
          </div>
          {hasCompletados && (
            <button
              onClick={handleClearCompletados}
              className="px-3 py-2 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 text-sm font-medium transition-colors"
            >
              Vaciar completados
            </button>
          )}
        </div>

        {/* Form */}
        <form onSubmit={handleAddItem} className="bg-slate-700/30 rounded-xl p-4 mb-6 border border-slate-600">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-3">
            <input
              type="text"
              placeholder="Nombre del producto"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className="md:col-span-2 px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
            <input
              type="text"
              placeholder="Cantidad (ej. 500g)"
              value={cantidad}
              onChange={(e) => setCantidad(e.target.value)}
              className="px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
            <select
              value={categoria}
              onChange={(e) => setCategoria(e.target.value)}
              className="px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500"
            >
              {Object.keys(categoryLabels).map((cat) => (
                <option key={cat} value={cat}>
                  {categoryLabels[cat]}
                </option>
              ))}
            </select>
          </div>
          <button
            type="submit"
            disabled={loading || !nombre.trim()}
            className="w-full px-4 py-2 bg-green-500 hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-colors"
          >
            Añadir +
          </button>
        </form>

        {/* Sugerencias */}
        {sugerencias.length > 0 && (
          <div className="mb-6">
            <button
              onClick={() => setExpandSugerencias(!expandSugerencias)}
              className="text-sm font-medium text-slate-400 hover:text-slate-300 mb-2 flex items-center gap-1"
            >
              <span>{expandSugerencias ? '▼' : '▶'}</span>
              Tus alimentos habituales:
            </button>
            {expandSugerencias && (
              <div className="flex flex-wrap gap-2">
                {sugerencias.map((sug) => (
                  <button
                    key={sug}
                    onClick={() => handleSugerencia(sug)}
                    className="px-3 py-1 rounded-full bg-slate-700/50 border border-slate-600 text-slate-300 hover:bg-slate-600 hover:text-slate-100 text-sm transition-colors"
                  >
                    {sug}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Lista vacía */}
        {totalItems === 0 && (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">🛒</div>
            <p className="text-xl text-slate-300 mb-2">Tu lista está vacía</p>
            <p className="text-slate-500">Añade productos usando el formulario de arriba</p>
          </div>
        )}

        {/* Categorías */}
        {totalItems > 0 && (
          <div className="space-y-6">
            {Object.entries(items).map(([cat, catItems]) => (
              <div key={cat}>
                <h2 className="text-xs font-bold uppercase tracking-wide text-slate-400 border-b border-slate-700 pb-2 mb-3">
                  {categoryIcons[cat]} {categoryLabels[cat]} ({catItems.length})
                </h2>
                <div className="space-y-1 divide-y divide-slate-700/50">
                  {catItems.map((item) => (
                    <div
                      key={item.id}
                      className={`flex items-center gap-3 p-3 rounded-lg transition-all ${
                        item.completado ? 'opacity-60' : ''
                      }`}
                    >
                      <input
                        type="checkbox"
                        checked={item.completado}
                        onChange={() => handleToggle(item.id)}
                        className="w-5 h-5 rounded accent-green-500 cursor-pointer"
                      />
                      <div className="flex-1 min-w-0">
                        <p
                          className={`text-white truncate ${
                            item.completado ? 'line-through text-slate-500' : ''
                          }`}
                        >
                          {item.nombre}
                        </p>
                        {item.cantidad && (
                          <p className="text-xs text-slate-500">{item.cantidad}</p>
                        )}
                      </div>
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="p-2 text-red-400 hover:bg-red-500/10 rounded-lg transition-colors shrink-0"
                      >
                        🗑️
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
