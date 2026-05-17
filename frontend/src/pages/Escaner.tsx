import { useEffect, useRef, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Html5QrcodeScanner } from 'html5-qrcode'
import { escanearBarcode, type EscanerResult } from '../api/escaner'
import { getComidas, createComida } from '../api/comidas'
import { useAuth } from '../contexts/AuthContext'
import type { Comida } from '../types'

const COMIDA_TIPOS = ['DESAYUNO', 'ALMUERZO', 'CENA', 'MERIENDA']

const macroVariants = {
  hidden: { opacity: 0, y: 10 },
  show: {
    opacity: 1,
    y: 0,
    transition: { staggerChildren: 0.08 }
  }
}

export default function Escaner() {
  const { user } = useAuth()
  const [barcode, setBarcode] = useState('')
  const [resultado, setResultado] = useState<EscanerResult | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [cameraActive, setCameraActive] = useState(false)
  const scannerRef = useRef<Html5QrcodeScanner | null>(null)

  // Estados del formulario para añadir a comida
  const [gramos, setGramos] = useState(100)
  const [tipoComida, setTipoComida] = useState('ALMUERZO')
  const [fecha, setFecha] = useState(new Date().toISOString().split('T')[0])
  const [comidas, setComidas] = useState<Comida[]>([])
  const [addingToMeal, setAddingToMeal] = useState(false)
  const [addError, setAddError] = useState('')

  useEffect(() => {
    if (resultado && user) {
      cargarComidas()
    }
  }, [resultado, user])

  const cargarComidas = async () => {
    if (!user) return
    try {
      const data = await getComidas(user.usuarioId, fecha)
      setComidas(data)
    } catch {
      // Silenciar error, comidas puede estar vacía
    }
  }

  const iniciarScanner = () => {
    if (cameraActive) {
      detenerScanner()
      return
    }

    setCameraActive(true)
    setError('')

    scannerRef.current = new Html5QrcodeScanner(
      'html5-qrcode-reader',
      {
        fps: 10,
        qrbox: { width: 250, height: 250 },
        aspectRatio: 1.33,
      },
      false
    )

    scannerRef.current.render(
      (decodedText) => {
        setBarcode(decodedText)
        buscarProducto(decodedText)
        detenerScanner()
      },
      (error) => {
        if (error && !error.includes('NotFoundException')) {
          console.log('[Scanner]', error)
        }
      }
    )
  }

  const detenerScanner = () => {
    if (scannerRef.current) {
      scannerRef.current.clear().catch(() => {})
      scannerRef.current = null
    }
    setCameraActive(false)
  }

  const buscarProducto = async (code: string) => {
    setLoading(true)
    setError('')
    setResultado(null)
    setAddError('')

    try {
      const result = await escanearBarcode(code)
      setResultado(result)
    } catch (err: any) {
      const message = err.response?.data?.message || err.message || 'No se encontró el producto'
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  const handleBuscar = (e: React.FormEvent) => {
    e.preventDefault()
    if (!barcode.trim()) {
      setError('Ingresa un código de barras')
      return
    }
    buscarProducto(barcode)
  }

  const handleAnadirAComida = async () => {
    if (!resultado || !user) return

    setAddingToMeal(true)
    setAddError('')

    try {
      // Buscar comida existente del tipo y fecha
      let comida = comidas.find(c => c.tipo === tipoComida && c.fecha === fecha)

      // Si no existe, crearla
      if (!comida) {
        comida = await createComida(user.usuarioId, fecha, tipoComida)
        setComidas([...comidas, comida])
      }

      // Aquí debería buscar si el alimento existe en la BD local de NutriFit
      // Para este módulo de escáner, asumimos que el usuario necesitará buscarlo en Alimentos
      // Pero mostramos un mensaje informativo
      setError('')
      setAddError(
        'Este producto no está en nuestra base de datos aún. Puedes buscarlo en Alimentos para añadirlo. ' +
        'Nombre: ' + resultado.nombre + ' | Kcal: ' + resultado.kcalPor100g
      )
    } catch (err: any) {
      const message = err.response?.data?.message || 'Error al añadir a comida'
      setAddError(message)
    } finally {
      setAddingToMeal(false)
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="p-6 max-w-3xl mx-auto"
    >
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1, duration: 0.3 }}
        className="mb-6"
      >
        <h1 className="text-2xl font-bold gradient-text">📱 Escáner de Códigos</h1>
        <p className="text-slate-400 text-sm mt-0.5">Busca productos por su código de barras</p>
      </motion.div>

      {/* Sección de entrada de barcode */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.15, duration: 0.3 }}
        className="card mb-6"
      >
        <h2 className="text-base font-semibold gradient-text mb-4">Buscar producto</h2>
        <form onSubmit={handleBuscar} className="flex gap-2 mb-4">
          <input
            type="text"
            placeholder="Ingresa código de barras"
            value={barcode}
            onChange={(e) => setBarcode(e.target.value)}
            className="input flex-1"
          />
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            type="submit"
            disabled={loading}
            className="btn-primary disabled:opacity-50"
          >
            {loading ? '...' : 'Buscar'}
          </motion.button>
        </form>

        <motion.button
          animate={cameraActive ? { scale: [1, 1.02, 1] } : {}}
          transition={cameraActive ? { repeat: Infinity, duration: 1.5 } : {}}
          onClick={iniciarScanner}
          className="w-full py-3 px-3 rounded-xl transition-colors font-medium text-sm flex items-center justify-center gap-2"
          style={{
            backgroundColor: cameraActive ? 'rgba(239, 68, 68, 0.2)' : 'rgba(16, 185, 129, 0.2)',
            border: cameraActive ? '1px solid rgba(239, 68, 68, 0.5)' : '1px solid rgba(16, 185, 129, 0.5)',
            color: cameraActive ? '#fca5a5' : '#86efac'
          }}
        >
          <span>{cameraActive ? '📷 Parar cámara' : '📷 Usar cámara'}</span>
        </motion.button>

        <AnimatePresence>
          {error && !resultado && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              className="mt-4 p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-red-400 text-sm"
            >
              {error}
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>

      {/* Scanner de cámara */}
      <AnimatePresence>
        {cameraActive && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.3 }}
            className="card mb-6"
          >
            <div id="html5-qrcode-reader" style={{ width: '100%' }}></div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Resultados */}
      <AnimatePresence>
        {resultado && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            transition={{ type: 'spring', stiffness: 120 }}
            className="card"
          >
            <div className="flex gap-6">
              {resultado.imagenUrl && (
                <motion.div
                  initial={{ scale: 0.9, opacity: 0 }}
                  animate={{ scale: 1, opacity: 1 }}
                  transition={{ delay: 0.1 }}
                  className="flex-shrink-0"
                >
                  <img
                    src={resultado.imagenUrl}
                    alt={resultado.nombre}
                    className="w-32 h-32 object-cover rounded-lg bg-slate-700 border border-white/10"
                  />
                </motion.div>
              )}

              <div className="flex-1">
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.1 }}
                  className="mb-4"
                >
                  <h3 className="text-lg font-semibold gradient-text">{resultado.nombre}</h3>
                  <p className="text-slate-400 text-sm">
                    {resultado.marca && resultado.marca !== 'No especificada' ? resultado.marca : 'Marca no especificada'}
                  </p>
                </motion.div>

                {/* Cards de macros */}
                <motion.div
                  variants={macroVariants}
                  initial="hidden"
                  animate="show"
                  className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6"
                >
                  <motion.div
                    variants={{ hidden: { opacity: 0, y: 10 }, show: { opacity: 1, y: 0 } }}
                    className="bg-white/5 border-t-2 border-emerald-500 border-l border-r border-b border-white/10 rounded-lg p-3 hover:border-emerald-500/50 transition-colors"
                  >
                    <p className="text-slate-400 text-xs font-medium">Kcal</p>
                    <p className="text-emerald-400 text-lg font-bold">{resultado.kcalPor100g}</p>
                    <p className="text-slate-500 text-xs">por 100g</p>
                  </motion.div>
                  <motion.div
                    variants={{ hidden: { opacity: 0, y: 10 }, show: { opacity: 1, y: 0 } }}
                    className="bg-white/5 border-t-2 border-blue-500 border-l border-r border-b border-white/10 rounded-lg p-3 hover:border-blue-500/50 transition-colors"
                  >
                    <p className="text-slate-400 text-xs font-medium">Proteínas</p>
                    <p className="text-blue-400 text-lg font-bold">{resultado.proteinasPor100g}g</p>
                    <p className="text-slate-500 text-xs">por 100g</p>
                  </motion.div>
                  <motion.div
                    variants={{ hidden: { opacity: 0, y: 10 }, show: { opacity: 1, y: 0 } }}
                    className="bg-white/5 border-t-2 border-yellow-500 border-l border-r border-b border-white/10 rounded-lg p-3 hover:border-yellow-500/50 transition-colors"
                  >
                    <p className="text-slate-400 text-xs font-medium">Grasas</p>
                    <p className="text-yellow-400 text-lg font-bold">{resultado.grasasPor100g}g</p>
                    <p className="text-slate-500 text-xs">por 100g</p>
                  </motion.div>
                  <motion.div
                    variants={{ hidden: { opacity: 0, y: 10 }, show: { opacity: 1, y: 0 } }}
                    className="bg-white/5 border-t-2 border-orange-500 border-l border-r border-b border-white/10 rounded-lg p-3 hover:border-orange-500/50 transition-colors"
                  >
                    <p className="text-slate-400 text-xs font-medium">Carbos</p>
                    <p className="text-orange-400 text-lg font-bold">{resultado.carbosPor100g}g</p>
                    <p className="text-slate-500 text-xs">por 100g</p>
                  </motion.div>
                </motion.div>

                {/* Nota informativa */}
                <AnimatePresence>
                  {addError && (
                    <motion.div
                      initial={{ opacity: 0, y: -10 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -10 }}
                      className="p-3 bg-amber-500/10 border border-amber-500/30 rounded-lg text-amber-400 text-sm mb-4"
                    >
                      {addError}
                    </motion.div>
                  )}
                </AnimatePresence>

                {/* Formulario para añadir a comida */}
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: 0.4 }}
                  className="space-y-3"
                >
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                    <div>
                      <label htmlFor="gramos" className="label">Gramos</label>
                      <input
                        id="gramos"
                        type="number"
                        value={gramos}
                        onChange={(e) => setGramos(Number.parseFloat(e.target.value) || 0)}
                        className="input"
                        min="1"
                      />
                    </div>
                    <div>
                      <label htmlFor="tipoComida" className="label">Tipo de comida</label>
                      <select id="tipoComida" value={tipoComida} onChange={(e) => setTipoComida(e.target.value)} className="input">
                        {COMIDA_TIPOS.map((t) => (
                          <option key={t} value={t}>
                            {t}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label htmlFor="fecha" className="label">Fecha</label>
                      <input
                        id="fecha"
                        type="date"
                        value={fecha}
                        onChange={(e) => {
                          setFecha(e.target.value)
                          setComidas([])
                        }}
                        className="input"
                      />
                    </div>
                  </div>

                  <motion.button
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={handleAnadirAComida}
                    disabled={addingToMeal}
                    className="w-full btn-primary disabled:opacity-50"
                  >
                    {addingToMeal ? 'Procesando...' : '+ Añadir a registro'}
                  </motion.button>
                </motion.div>
              </div>
            </div>

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => {
                setResultado(null)
                setBarcode('')
                setError('')
                setAddError('')
              }}
              className="mt-6 w-full py-2 px-3 rounded-xl bg-white/8 hover:bg-white/12 border border-white/10 hover:border-white/20 text-slate-200 transition-colors text-sm font-medium"
            >
              Escanear otro producto
            </motion.button>
          </motion.div>
        )}
      </AnimatePresence>

      {!resultado && !loading && (
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.2, duration: 0.3 }}
          className="card text-center text-slate-400"
        >
          <p className="text-sm">Usa el formulario anterior para buscar un producto</p>
        </motion.div>
      )}
    </motion.div>
  )
}
