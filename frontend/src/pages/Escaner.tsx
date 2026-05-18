import { useEffect, useRef, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Html5Qrcode } from 'html5-qrcode'
import { escanearBarcode, type EscanerResult } from '../api/escaner'
import { createAlimento } from '../api/alimentos'
import { getComidas, createComida, addItemToComida } from '../api/comidas'
import { Camera, Keyboard } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import type { Comida } from '../types'

type Metodo = 'camara' | 'manual' | null

const COMIDA_TIPOS = ['DESAYUNO', 'ALMUERZO', 'CENA', 'MERIENDA']

export default function Escaner() {
  const { user } = useAuth()
  const [metodo, setMetodo] = useState<Metodo>(null)
  const [barcode, setBarcode] = useState('')
  const [resultado, setResultado] = useState<EscanerResult | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [cameraRunning, setCameraRunning] = useState(false)
  const qrRef = useRef<Html5Qrcode | null>(null)

  const [gramos, setGramos] = useState(100)
  const [tipoComida, setTipoComida] = useState('ALMUERZO')
  const [fecha, setFecha] = useState(new Date().toISOString().split('T')[0])
  const [comidas, setComidas] = useState<Comida[]>([])
  const [addingToMeal, setAddingToMeal] = useState(false)
  const [addError, setAddError] = useState('')
  const [addSuccess, setAddSuccess] = useState('')

  useEffect(() => {
    if (resultado && user) cargarComidas()
  }, [resultado, user])

  useEffect(() => {
    return () => { stopCamera() }
  }, [])

  useEffect(() => {
    if (metodo === 'camara') {
      const t = setTimeout(startCamera, 150)
      return () => clearTimeout(t)
    }
  }, [metodo])

  const stopCamera = async () => {
    if (qrRef.current) {
      try { await qrRef.current.stop(); qrRef.current.clear() } catch {}
      qrRef.current = null
    }
    setCameraRunning(false)
  }

  const startCamera = async () => {
    setError('')
    try {
      const qr = new Html5Qrcode('qr-reader')
      qrRef.current = qr
      await qr.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 240, height: 240 } },
        (decoded) => {
          buscarProducto(decoded)
          stopCamera()
          setMetodo(null)
        },
        () => {}
      )
      setCameraRunning(true)
    } catch {
      setError('No se pudo acceder a la cámara. Verifica los permisos del navegador.')
      setCameraRunning(false)
      setMetodo(null)
    }
  }

  const seleccionarMetodo = async (m: Metodo) => {
    if (metodo === 'camara') await stopCamera()
    setError('')
    setMetodo(m)
  }

  const cargarComidas = async () => {
    if (!user) return
    try {
      const data = await getComidas(user.usuarioId, fecha)
      setComidas(data)
    } catch {}
  }

  const buscarProducto = async (code: string) => {
    setLoading(true)
    setError('')
    setResultado(null)
    setAddError('')
    setAddSuccess('')
    try {
      const result = await escanearBarcode(code)
      setResultado(result)
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'No se encontró el producto')
    } finally {
      setLoading(false)
    }
  }

  const handleBuscar = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!barcode.trim()) { setError('Ingresa un código de barras'); return }
    buscarProducto(barcode)
  }

  const handleAnadirAComida = async () => {
    if (!resultado || !user) return
    setAddingToMeal(true)
    setAddError('')
    setAddSuccess('')
    try {
      const alimento = await createAlimento({
        nombre: resultado.nombre,
        porcionG: 100,
        kcalPor100g: resultado.kcalPor100g,
        proteinasG: resultado.proteinasPor100g,
        grasasG: resultado.grasasPor100g,
        carbosG: resultado.carbosPor100g,
        fuente: 'Escáner de código de barras'
      })
      let comida = comidas.find(c => c.tipo === tipoComida && c.fecha === fecha)
      if (!comida) {
        comida = await createComida(user.usuarioId, fecha, tipoComida)
        setComidas(prev => [...prev, comida!])
      }
      await addItemToComida(comida.id, alimento.id, gramos)
      setAddSuccess(`${resultado.nombre} añadido al registro (${gramos}g)`)
    } catch (err: any) {
      setAddError(err.response?.data?.message || 'Error al añadir a comida')
    } finally {
      setAddingToMeal(false)
    }
  }

  const resetTodo = () => {
    setResultado(null)
    setBarcode('')
    setError('')
    setAddError('')
    setAddSuccess('')
    setMetodo(null)
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="p-4 md:p-6 max-w-lg mx-auto"
    >
      <div className="mb-6">
        <h1 className="text-2xl font-bold gradient-text">Escáner</h1>
        <p className="text-white/40 text-sm mt-0.5">Identifica productos y añádelos a tu registro</p>
      </div>

      <AnimatePresence>
        {error && (
          <motion.div
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            className="mb-4 p-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm"
          >
            {error}
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence mode="wait">
        {/* Loading */}
        {loading && (
          <motion.div key="loading" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="card text-center py-12"
          >
            <div className="w-10 h-10 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-white/50 text-sm">Buscando producto...</p>
          </motion.div>
        )}

        {/* Resultado */}
        {!loading && resultado && (
          <motion.div key="resultado" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -20 }}
            className="card"
          >
            <div className="flex gap-4 mb-5">
              {resultado.imagenUrl && (
                <img src={resultado.imagenUrl} alt={resultado.nombre}
                  className="w-20 h-20 object-cover rounded-xl border border-white/10 shrink-0"
                />
              )}
              <div className="min-w-0">
                <h3 className="text-base font-semibold text-white truncate">{resultado.nombre}</h3>
                {resultado.marca && resultado.marca !== 'No especificada' && (
                  <p className="text-white/40 text-xs mt-0.5">{resultado.marca}</p>
                )}
                <span className="inline-block mt-2 text-xs bg-emerald-500/15 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded-full">
                  Producto encontrado
                </span>
              </div>
            </div>

            <div className="grid grid-cols-4 gap-2 mb-5">
              <div className="bg-white/5 rounded-xl p-2.5 border-t-2 border-emerald-500 border border-white/10">
                <p className="text-white/40 text-[10px]">Kcal</p>
                <p className="text-emerald-400 text-sm font-bold">{resultado.kcalPor100g}</p>
                <p className="text-white/25 text-[10px]">/100g</p>
              </div>
              <div className="bg-white/5 rounded-xl p-2.5 border-t-2 border-blue-500 border border-white/10">
                <p className="text-white/40 text-[10px]">Prot.</p>
                <p className="text-blue-400 text-sm font-bold">{resultado.proteinasPor100g}g</p>
                <p className="text-white/25 text-[10px]">/100g</p>
              </div>
              <div className="bg-white/5 rounded-xl p-2.5 border-t-2 border-yellow-500 border border-white/10">
                <p className="text-white/40 text-[10px]">Grasas</p>
                <p className="text-yellow-400 text-sm font-bold">{resultado.grasasPor100g}g</p>
                <p className="text-white/25 text-[10px]">/100g</p>
              </div>
              <div className="bg-white/5 rounded-xl p-2.5 border-t-2 border-orange-500 border border-white/10">
                <p className="text-white/40 text-[10px]">Carbos</p>
                <p className="text-orange-400 text-sm font-bold">{resultado.carbosPor100g}g</p>
                <p className="text-white/25 text-[10px]">/100g</p>
              </div>
            </div>

            <AnimatePresence>
              {addError && (
                <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
                  className="mb-3 p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-red-400 text-sm"
                >{addError}</motion.div>
              )}
              {addSuccess && (
                <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
                  className="mb-3 p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-lg text-emerald-400 text-sm"
                >{addSuccess}</motion.div>
              )}
            </AnimatePresence>

            <p className="text-white/40 text-xs font-medium uppercase tracking-wide mb-3">Añadir al registro</p>
            <div className="grid grid-cols-3 gap-2 mb-3">
              <div>
                <label htmlFor="res-gramos" className="label text-xs">Gramos</label>
                <input id="res-gramos" type="number" value={gramos} onChange={e => setGramos(Number(e.target.value) || 0)}
                  className="input" min="1" />
              </div>
              <div>
                <label htmlFor="res-tipo" className="label text-xs">Comida</label>
                <select id="res-tipo" value={tipoComida} onChange={e => setTipoComida(e.target.value)} className="input">
                  {COMIDA_TIPOS.map(t => (
                    <option key={t} value={t}>{t[0] + t.slice(1).toLowerCase()}</option>
                  ))}
                </select>
              </div>
              <div>
                <label htmlFor="res-fecha" className="label text-xs">Fecha</label>
                <input id="res-fecha" type="date" value={fecha} onChange={e => { setFecha(e.target.value); setComidas([]) }} className="input" />
              </div>
            </div>

            <button onClick={handleAnadirAComida} disabled={addingToMeal}
              className="w-full btn-primary disabled:opacity-50 mb-2"
            >
              {addingToMeal ? 'Añadiendo...' : '+ Añadir al registro'}
            </button>
            <button onClick={resetTodo}
              className="w-full py-2 rounded-xl bg-white/5 hover:bg-white/8 border border-white/10 text-white/40 hover:text-white/60 text-sm transition-colors"
            >
              Escanear otro producto
            </button>
          </motion.div>
        )}

        {/* Selector de método */}
        {!loading && !resultado && (
          <motion.div key="metodos" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>

            {/* Lista de métodos */}
            {!metodo && (
              <div className="space-y-3">
                <p className="text-white/30 text-xs uppercase tracking-widest font-medium px-1">Elige cómo identificar el producto</p>

                <button onClick={() => seleccionarMetodo('camara')}
                  className="w-full card flex items-center gap-4 p-4 hover:border-emerald-500/40 transition-all text-left group cursor-pointer"
                >
                  <div className="w-12 h-12 rounded-xl bg-emerald-500/15 flex items-center justify-center shrink-0 group-hover:bg-emerald-500/25 transition-colors">
                    <Camera className="w-6 h-6 text-emerald-400" />
                  </div>
                  <div className="flex-1">
                    <p className="text-white font-medium text-sm">Escanear código de barras</p>
                    <p className="text-white/40 text-xs mt-0.5">Apunta la cámara al código del envase</p>
                  </div>
                  <span className="text-white/25 group-hover:text-white/50 text-xl transition-colors">›</span>
                </button>

                <button onClick={() => seleccionarMetodo('manual')}
                  className="w-full card flex items-center gap-4 p-4 hover:border-blue-500/40 transition-all text-left group cursor-pointer"
                >
                  <div className="w-12 h-12 rounded-xl bg-blue-500/15 flex items-center justify-center shrink-0 group-hover:bg-blue-500/25 transition-colors">
                    <Keyboard className="w-6 h-6 text-blue-400" />
                  </div>
                  <div className="flex-1">
                    <p className="text-white font-medium text-sm">Introducir código manualmente</p>
                    <p className="text-white/40 text-xs mt-0.5">Escribe el número del código de barras</p>
                  </div>
                  <span className="text-white/25 group-hover:text-white/50 text-xl transition-colors">›</span>
                </button>

              </div>
            )}

            {/* Método: cámara */}
            {metodo === 'camara' && (
              <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="space-y-3">
                <button onClick={() => seleccionarMetodo(null)}
                  className="flex items-center gap-1.5 text-white/40 hover:text-white/70 transition-colors text-sm"
                >
                  ← Volver
                </button>
                <div className="card overflow-hidden !p-0">
                  <div id="qr-reader" className="w-full" />
                  <div className="p-4">
                    {cameraRunning
                      ? <p className="text-white/40 text-xs text-center mb-3">Centra el código de barras en el recuadro blanco</p>
                      : <p className="text-white/30 text-xs text-center mb-3">Iniciando cámara...</p>
                    }
                    <button onClick={() => seleccionarMetodo(null)}
                      className="w-full py-2.5 rounded-xl bg-red-500/15 border border-red-500/30 text-red-400 text-sm hover:bg-red-500/25 transition-colors"
                    >
                      Detener cámara
                    </button>
                  </div>
                </div>
              </motion.div>
            )}

            {/* Método: manual */}
            {metodo === 'manual' && (
              <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="space-y-3">
                <button onClick={() => seleccionarMetodo(null)}
                  className="flex items-center gap-1.5 text-white/40 hover:text-white/70 transition-colors text-sm"
                >
                  ← Volver
                </button>
                <div className="card">
                  <p className="text-white font-medium text-sm mb-1">Código de barras</p>
                  <p className="text-white/40 text-xs mb-4">Escribe el número que aparece bajo el código del producto</p>
                  <form onSubmit={handleBuscar} className="flex gap-2">
                    <input
                      type="text"
                      inputMode="numeric"
                      placeholder="Ej. 8410031630352"
                      value={barcode}
                      onChange={e => setBarcode(e.target.value)}
                      autoFocus
                      className="input flex-1"
                    />
                    <button type="submit" disabled={loading} className="btn-primary disabled:opacity-50 px-5">
                      Buscar
                    </button>
                  </form>
                </div>
              </motion.div>
            )}

          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  )
}
