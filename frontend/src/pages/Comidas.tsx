import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'
import { getComidas, createComida, deleteComida, getComidaItems, addItemToComida, deleteComidaItem } from '../api/comidas'
import { getAlimentos, buscarExternoAlimentos, createAlimento } from '../api/alimentos'
import type { Comida, ComidaItem, Alimento, AlimentoExterno } from '../types'

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
  const [alimentosExterno, setAlimentosExterno] = useState<AlimentoExterno[]>([])
  const [loadingExterno, setLoadingExterno] = useState(false)
  const [alimentoQ, setAlimentoQ] = useState('')
  const [selectedAlimento, setSelectedAlimento] = useState<Alimento | null>(null)
  const [gramos, setGramos] = useState(100)
  const [confirmDeleteComida, setConfirmDeleteComida] = useState<number | null>(null)

  const loadComidas = () => {
    if (!user) return
    setLoading(true)
    let mounted = true

    getComidas(user.usuarioId, fecha)
      .then(async (cs) => {
        if (!mounted) return
        setComidas(cs)
        const itemsMap: Record<number, ComidaItem[]> = {}
        await Promise.all(cs.map(async (c) => { itemsMap[c.id] = await getComidaItems(c.id) }))
        if (!mounted) return
        setItems(itemsMap)
      })
      .catch(() => { if (mounted) setComidas([]) })
      .finally(() => { if (mounted) setLoading(false) })

    return () => { mounted = false }
  }

  useEffect(() => {
    const cleanup = loadComidas()
    return cleanup
  }, [user, fecha])

  useEffect(() => {
    const t = setTimeout(() => {
      if (addingTo !== null) {
        getAlimentos(alimentoQ || undefined).then(setAlimentos)
        if (alimentoQ.trim().length >= 2) {
          setLoadingExterno(true)
          buscarExternoAlimentos(alimentoQ.trim())
            .then(setAlimentosExterno)
            .finally(() => setLoadingExterno(false))
        } else {
          setAlimentosExterno([])
        }
      }
    }, 400)
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
    setConfirmDeleteComida(id)
  }

  const confirmDeleteComidaAction = async (id: number) => {
    await deleteComida(id)
    setComidas((prev) => prev.filter((c) => c.id !== id))
    setItems((prev) => { const n = { ...prev }; delete n[id]; return n })
    setConfirmDeleteComida(null)
  }

  const openAddItem = (comidaId: number) => {
    setAddingTo(comidaId)
    setSelectedAlimento(null)
    setAlimentoQ('')
    setGramos(100)
    setAlimentosExterno([])
    getAlimentos().then(setAlimentos)
  }

  const handleSelectExterno = async (ext: AlimentoExterno) => {
    const saved = await createAlimento({
      nombre: ext.nombre,
      porcionG: 100,
      kcalPor100g: ext.kcalPor100g,
      proteinasG: ext.proteinasG,
      grasasG: ext.grasasG,
      carbosG: ext.carbosG,
      fuente: ext.fuente,
    })
    setSelectedAlimento(saved)
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
    <motion.div
      className="p-6 max-w-4xl mx-auto"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
    >
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <motion.div initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: 0.1 }}>
          <h1 className="text-2xl font-bold gradient-text">Comidas</h1>
          <p className="text-white/50 text-sm mt-0.5">Registro de comidas del día</p>
        </motion.div>
        <motion.input
          type="date"
          value={fecha}
          onChange={(e) => setFecha(e.target.value)}
          className="input w-auto"
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.1 }}
        />
      </div>

      {loading ? (
        <div className="flex justify-center py-16">
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
            className="w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full"
          />
        </div>
      ) : (
        <motion.div
          className="space-y-4"
          variants={{ hidden: {}, show: { transition: { staggerChildren: 0.08 } } }}
          initial="hidden"
          animate="show"
        >
          <AnimatePresence>
            {comidas.map((c) => (
              <motion.div
                key={c.id}
                className="card"
                variants={{ hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0 } }}
                exit={{ opacity: 0, y: -16 }}
                transition={{ type: 'spring', stiffness: 100, damping: 15 }}
              >
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <span className="text-lg">{tipoIcon(c.tipo)}</span>
                    <h2 className="font-semibold text-white">{c.tipo}</h2>
                    <motion.span
                      className="px-3 py-1 rounded-full text-sm font-medium bg-amber-500/20 text-amber-400"
                      whileHover={{ scale: 1.05 }}
                    >
                      {Math.round(kcalComida(c.id))} kcal
                    </motion.span>
                  </div>
                  <div className="flex gap-2">
                    <motion.button
                      onClick={() => openAddItem(c.id)}
                      className="btn-secondary text-sm py-1.5"
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                    >
                      + Alimento
                    </motion.button>
                    <AnimatePresence mode="wait">
                      {confirmDeleteComida === c.id ? (
                        <motion.div
                          key="confirm"
                          className="flex gap-1"
                          initial={{ opacity: 0, width: 0 }}
                          animate={{ opacity: 1, width: 'auto' }}
                          exit={{ opacity: 0, width: 0 }}
                        >
                          <motion.button
                            onClick={() => confirmDeleteComidaAction(c.id)}
                            className="btn-danger text-xs py-1 px-2"
                            whileTap={{ scale: 0.9 }}
                          >
                            Sí
                          </motion.button>
                          <motion.button
                            onClick={() => setConfirmDeleteComida(null)}
                            className="btn-secondary text-xs py-1 px-2"
                            whileTap={{ scale: 0.9 }}
                          >
                            No
                          </motion.button>
                        </motion.div>
                      ) : (
                        <motion.button
                          key="delete"
                          onClick={() => handleDeleteComida(c.id)}
                          className="btn-danger"
                          whileHover={{ scale: 1.1 }}
                          whileTap={{ scale: 0.9 }}
                        >
                          🗑
                        </motion.button>
                      )}
                    </AnimatePresence>
                  </div>
                </div>

                <AnimatePresence>
                  {addingTo === c.id && (
                    <motion.div
                      className="card bg-white/3 mb-3 space-y-3"
                      initial={{ opacity: 0, height: 0 }}
                      animate={{ opacity: 1, height: 'auto' }}
                      exit={{ opacity: 0, height: 0 }}
                      transition={{ type: 'spring', stiffness: 100, damping: 15 }}
                    >
                      <input
                        type="text"
                        className="input"
                        placeholder="Buscar alimento..."
                        value={alimentoQ}
                        onChange={(e) => setAlimentoQ(e.target.value)}
                        autoFocus
                      />
                      <AnimatePresence>
                        {!selectedAlimento && (alimentos.length > 0 || alimentosExterno.length > 0) && (
                          <motion.div
                            className="max-h-64 overflow-y-auto space-y-1"
                            initial={{ opacity: 0, y: -8 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -8 }}
                          >
                            {alimentos.map((a) => (
                              <motion.button
                                key={a.id}
                                onClick={() => setSelectedAlimento(a)}
                                className="w-full text-left px-3 py-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors text-sm"
                                whileHover={{ x: 4, backgroundColor: 'rgba(255, 255, 255, 0.15)' }}
                                whileTap={{ scale: 0.98 }}
                              >
                                <span className="text-white">{a.nombre}</span>
                                <span className="text-white/50 ml-2">{a.kcalPor100g} kcal/100g</span>
                              </motion.button>
                            ))}
                            {loadingExterno && (
                              <motion.p
                                className="text-xs text-white/40 px-3 py-1"
                                animate={{ opacity: [0.5, 1, 0.5] }}
                                transition={{ duration: 1.5, repeat: Infinity }}
                              >
                                Buscando en Open Food Facts…
                              </motion.p>
                            )}
                            {!loadingExterno && alimentosExterno.length > 0 && (
                              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
                                <p className="text-xs text-white/40 px-3 pt-2 pb-1 border-t border-white/10 mt-1">
                                  🌐 Open Food Facts
                                </p>
                                {alimentosExterno.map((ext, i) => (
                                  <motion.button
                                    key={i}
                                    onClick={() => handleSelectExterno(ext)}
                                    className="w-full text-left px-3 py-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors text-sm"
                                    whileHover={{ x: 4 }}
                                    whileTap={{ scale: 0.98 }}
                                  >
                                    <span className="text-white/80">{ext.nombre}</span>
                                    <span className="text-white/40 ml-2">{Math.round(ext.kcalPor100g)} kcal/100g</span>
                                  </motion.button>
                                ))}
                              </motion.div>
                            )}
                          </motion.div>
                        )}
                      </AnimatePresence>
                      <AnimatePresence>
                        {selectedAlimento && (
                          <motion.div
                            className="flex items-center gap-3 flex-wrap pt-2 border-t border-white/10"
                            initial={{ opacity: 0, y: -8 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -8 }}
                          >
                            <motion.span
                              className="text-emerald-400 font-medium text-sm"
                              initial={{ scale: 0.8 }}
                              animate={{ scale: 1 }}
                            >
                              {selectedAlimento.nombre}
                            </motion.span>
                            <div className="flex items-center gap-2">
                              <label className="text-sm text-white/50">Gramos:</label>
                              <input
                                type="number"
                                className="input w-24"
                                value={gramos}
                                min={1}
                                onChange={(e) => setGramos(parseFloat(e.target.value) || 0)}
                              />
                            </div>
                            <motion.button
                              onClick={handleAddItem}
                              className="btn-primary text-sm py-1.5"
                              whileHover={{ scale: 1.05 }}
                              whileTap={{ scale: 0.95 }}
                            >
                              Añadir
                            </motion.button>
                            <motion.button
                              onClick={() => setSelectedAlimento(null)}
                              className="btn-secondary text-sm py-1.5"
                              whileHover={{ scale: 1.05 }}
                              whileTap={{ scale: 0.95 }}
                            >
                              Cambiar
                            </motion.button>
                          </motion.div>
                        )}
                      </AnimatePresence>
                      <motion.button
                        onClick={() => setAddingTo(null)}
                        className="text-white/40 text-xs hover:text-white/60 transition-colors"
                        whileHover={{ x: -2 }}
                      >
                        ← Cancelar
                      </motion.button>
                    </motion.div>
                  )}
                </AnimatePresence>

                {(items[c.id] ?? []).length === 0 ? (
                  <motion.p
                    className="text-white/40 text-sm"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                  >
                    Sin alimentos registrados.
                  </motion.p>
                ) : (
                  <motion.div className="space-y-1">
                    <AnimatePresence>
                      {(items[c.id] ?? []).map((item) => (
                        <motion.div
                          key={item.itemId}
                          className="flex items-center justify-between py-2 px-2 rounded-lg hover:bg-white/5 transition-colors"
                          layout
                          initial={{ opacity: 0, x: -16 }}
                          animate={{ opacity: 1, x: 0 }}
                          exit={{ opacity: 0, x: 16, height: 0 }}
                          transition={{ type: 'spring', stiffness: 100, damping: 15 }}
                        >
                          <div className="flex-1 min-w-0">
                            <span className="text-white text-sm font-medium">{item.nombre}</span>
                            <span className="text-white/40 text-xs ml-2">{item.gramos}g</span>
                          </div>
                          <div className="flex items-center gap-3 text-xs shrink-0">
                            <motion.span className="text-amber-400" whileHover={{ scale: 1.1 }}>
                              {Math.round(item.kcalEstimadas)} kcal
                            </motion.span>
                            <motion.span className="text-blue-400 hidden sm:block" whileHover={{ scale: 1.1 }}>
                              {Math.round(item.proteinasEstimadas)}g P
                            </motion.span>
                            <motion.span className="text-amber-300 hidden sm:block" whileHover={{ scale: 1.1 }}>
                              {Math.round(item.grasasEstimadas)}g G
                            </motion.span>
                            <motion.span className="text-purple-400 hidden sm:block" whileHover={{ scale: 1.1 }}>
                              {Math.round(item.carbosEstimados)}g C
                            </motion.span>
                            <motion.button
                              onClick={() => handleDeleteItem(c.id, item.itemId)}
                              className="text-white/40 hover:text-red-400 ml-1 transition-colors"
                              whileHover={{ scale: 1.2 }}
                              whileTap={{ scale: 0.9 }}
                            >
                              ✕
                            </motion.button>
                          </div>
                        </motion.div>
                      ))}
                    </AnimatePresence>
                  </motion.div>
                )}
              </motion.div>
            ))}
          </AnimatePresence>

          {tiposDisponibles.length > 0 && (
            <motion.div
              className="card border border-dashed border-white/10 bg-white/2"
              variants={{ hidden: { opacity: 0, y: 16 }, show: { opacity: 1, y: 0 } }}
            >
              <p className="text-sm text-white/40 mb-3 section-title">+ Añadir comida</p>
              <motion.div className="flex flex-wrap gap-2" variants={{ hidden: {}, show: { transition: { staggerChildren: 0.05 } } }} initial="hidden" animate="show">
                {tiposDisponibles.map((t) => (
                  <motion.button
                    key={t}
                    onClick={() => handleCreateComida(t)}
                    disabled={creatingTipo === t}
                    className="px-4 py-2 rounded-full bg-white/5 border border-white/10 text-white/60 text-sm hover:bg-white/10 hover:text-white hover:border-white/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1.5"
                    whileHover={{ scale: 1.04, backgroundColor: 'rgba(255, 255, 255, 0.15)' }}
                    whileTap={{ scale: 0.96 }}
                    variants={{ hidden: { opacity: 0, y: 8 }, show: { opacity: 1, y: 0 } }}
                  >
                    {tipoIcon(t)} {t}
                  </motion.button>
                ))}
              </motion.div>
            </motion.div>
          )}
        </motion.div>
      )}
    </motion.div>
  )
}

function tipoIcon(tipo: string) {
  const map: Record<string, string> = {
    DESAYUNO: '☀️', COMIDA: '🍽️', MERIENDA: '🍎', CENA: '🌙', SNACK: '🍿',
  }
  return map[tipo] ?? '🍴'
}
