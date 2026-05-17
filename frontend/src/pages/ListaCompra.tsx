import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
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
    <motion.div
      className="flex-1 overflow-auto"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
    >
      <div className="max-w-3xl mx-auto p-6">
        {/* Header */}
        <motion.div
          className="flex items-center justify-between mb-6 flex-wrap gap-4"
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <div>
            <h1 className="text-3xl font-bold gradient-text mb-2">🛒 Lista de la Compra</h1>
            <AnimatePresence>
              {totalItems > 0 && (
                <motion.div
                  className="flex gap-4 text-sm"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                >
                  <motion.span
                    className="text-emerald-400 font-medium"
                    whileHover={{ scale: 1.05 }}
                  >
                    ✓ {pendientes} pendientes
                  </motion.span>
                  {completados > 0 && (
                    <motion.span
                      className="text-white/40"
                      whileHover={{ scale: 1.05 }}
                    >
                      ✓ {completados} completados
                    </motion.span>
                  )}
                </motion.div>
              )}
            </AnimatePresence>
          </div>
          <AnimatePresence>
            {hasCompletados && (
              <motion.button
                onClick={handleClearCompletados}
                className="px-4 py-2 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400 hover:bg-red-500/20 hover:border-red-500/30 text-sm font-medium transition-all"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                Vaciar completados
              </motion.button>
            )}
          </AnimatePresence>
        </motion.div>

        {/* Progress bar */}
        <AnimatePresence>
          {totalItems > 0 && (
            <motion.div
              className="h-1.5 rounded-full bg-white/10 mb-6 overflow-hidden"
              initial={{ opacity: 0, width: 0 }}
              animate={{ opacity: 1, width: '100%' }}
              exit={{ opacity: 0 }}
            >
              <motion.div
                className="h-full rounded-full bg-gradient-to-r from-emerald-500 to-cyan-500"
                initial={{ width: 0 }}
                animate={{ width: `${(completados / totalItems) * 100}%` }}
                transition={{ duration: 0.6, ease: 'easeOut' }}
              />
            </motion.div>
          )}
        </AnimatePresence>

        {/* Form */}
        <motion.form
          onSubmit={handleAddItem}
          className="card mb-6"
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
        >
          <div className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-3">
            <motion.input
              type="text"
              placeholder="Nombre del producto"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className="md:col-span-2 input"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            />
            <motion.input
              type="text"
              placeholder="Cantidad (ej. 500g)"
              value={cantidad}
              onChange={(e) => setCantidad(e.target.value)}
              className="input"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            />
            <motion.select
              value={categoria}
              onChange={(e) => setCategoria(e.target.value)}
              className="input"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            >
              {Object.keys(categoryLabels).map((cat) => (
                <option key={cat} value={cat}>
                  {categoryLabels[cat]}
                </option>
              ))}
            </motion.select>
          </div>
          <motion.button
            type="submit"
            disabled={loading || !nombre.trim()}
            className="w-full btn-primary py-2 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            whileHover={loading || !nombre.trim() ? {} : { scale: 1.02 }}
            whileTap={loading || !nombre.trim() ? {} : { scale: 0.98 }}
          >
            {loading ? 'Añadiendo...' : 'Añadir +'}
          </motion.button>
        </motion.form>

        {/* Sugerencias */}
        <AnimatePresence>
          {sugerencias.length > 0 && (
            <motion.div
              className="mb-6"
              initial={{ opacity: 0, y: -8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
            >
              <motion.button
                onClick={() => setExpandSugerencias(!expandSugerencias)}
                className="text-sm font-medium text-white/40 hover:text-white/60 mb-3 flex items-center gap-2 transition-colors"
                whileHover={{ x: 2 }}
              >
                <motion.span
                  animate={{ rotate: expandSugerencias ? 180 : 0 }}
                  transition={{ duration: 0.2 }}
                >
                  ▼
                </motion.span>
                💡 Tus alimentos habituales:
              </motion.button>
              <AnimatePresence>
                {expandSugerencias && (
                  <motion.div
                    className="flex flex-wrap gap-2"
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                  >
                    {sugerencias.map((sug) => (
                      <motion.button
                        key={sug}
                        onClick={() => handleSugerencia(sug)}
                        className="px-3 py-1.5 rounded-full bg-white/5 border border-white/10 text-white/60 hover:text-emerald-400 hover:bg-emerald-500/10 hover:border-emerald-500/20 text-sm transition-all"
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        initial={{ opacity: 0, scale: 0.8 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.8 }}
                      >
                        {sug}
                      </motion.button>
                    ))}
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Lista vacía */}
        <AnimatePresence>
          {totalItems === 0 && (
            <motion.div
              className="text-center py-16"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              transition={{ type: 'spring', stiffness: 100 }}
            >
              <motion.div
                className="text-6xl mb-4"
                animate={{ y: [0, -12, 0] }}
                transition={{ duration: 3, repeat: Infinity, ease: 'easeInOut' }}
              >
                🛒
              </motion.div>
              <p className="text-xl gradient-text font-semibold mb-2">Tu lista está vacía</p>
              <p className="text-white/40">Añade productos usando el formulario de arriba</p>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Categorías */}
        <AnimatePresence>
          {totalItems > 0 && (
            <motion.div
              className="space-y-6"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              variants={{ hidden: {}, show: { transition: { staggerChildren: 0.06 } } }}
            >
              {Object.entries(items).map(([cat, catItems]) => (
                <motion.div
                  key={cat}
                  variants={{ hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0 } }}
                  initial="hidden"
                  animate="show"
                >
                  <div className="flex items-center gap-3 mb-3">
                    <span className="text-2xl">{categoryIcons[cat]}</span>
                    <span className="section-title">{categoryLabels[cat]}</span>
                    <motion.span className="text-xs text-white/30" whileHover={{ color: 'rgba(255, 255, 255, 0.5)' }}>
                      ({catItems.length})
                    </motion.span>
                    <div className="flex-1 h-px bg-white/8" />
                  </div>
                  <motion.div
                    className="space-y-1"
                    variants={{ hidden: {}, show: { transition: { staggerChildren: 0.03 } } }}
                    initial="hidden"
                    animate="show"
                  >
                    <AnimatePresence>
                      {catItems.map((item) => (
                        <motion.div
                          key={item.id}
                          className={`flex items-center gap-3 p-3 rounded-xl transition-all ${
                            item.completado ? 'opacity-40' : 'bg-white/3 hover:bg-white/5'
                          }`}
                          layout
                          initial={{ opacity: 0, x: -20 }}
                          animate={{ opacity: 1, x: 0 }}
                          exit={{ opacity: 0, x: 20, height: 0, marginBottom: 0 }}
                          transition={{ type: 'spring', stiffness: 100, damping: 15 }}
                          variants={{ hidden: { opacity: 0, x: -20 }, show: { opacity: 1, x: 0 } }}
                        >
                          <motion.input
                            type="checkbox"
                            checked={item.completado}
                            onChange={() => handleToggle(item.id)}
                            className="w-5 h-5 rounded accent-emerald-500 cursor-pointer shrink-0"
                            whileHover={{ scale: 1.15 }}
                            whileTap={{ scale: 0.9 }}
                          />
                          <div className="flex-1 min-w-0">
                            <motion.p
                              className={`text-white truncate transition-all ${
                                item.completado ? 'line-through text-white/30' : 'text-white'
                              }`}
                              animate={{
                                textDecoration: item.completado ? 'line-through' : 'none',
                                opacity: item.completado ? 0.3 : 1,
                              }}
                            >
                              {item.nombre}
                            </motion.p>
                            {item.cantidad && (
                              <motion.p
                                className="text-xs text-white/40"
                                animate={{ opacity: item.completado ? 0.2 : 0.6 }}
                              >
                                {item.cantidad}
                              </motion.p>
                            )}
                          </div>
                          <motion.button
                            onClick={() => handleDelete(item.id)}
                            className="p-2 text-red-400/0 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors shrink-0"
                            whileHover={{ scale: 1.15, color: 'rgb(248, 113, 113)' }}
                            whileTap={{ scale: 0.9 }}
                          >
                            🗑️
                          </motion.button>
                        </motion.div>
                      ))}
                    </AnimatePresence>
                  </motion.div>
                </motion.div>
              ))}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </motion.div>
  )
}
