import { useEffect, useRef, useState } from 'react'
import { Html5QrcodeScanner } from 'html5-qrcode'
import { escanearBarcode, type EscanerResult } from '../api/escaner'
import { getComidas, createComida } from '../api/comidas'
import { useAuth } from '../contexts/AuthContext'
import type { Comida } from '../types'

const COMIDA_TIPOS = ['DESAYUNO', 'ALMUERZO', 'CENA', 'MERIENDA']

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
    <div className="p-6 max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-100">Escáner de Códigos de Barras</h1>
        <p className="text-slate-400 text-sm mt-0.5">Busca productos por su código de barras</p>
      </div>

      {/* Sección de entrada de barcode */}
      <div className="card mb-6">
        <h2 className="text-base font-semibold text-slate-200 mb-4">Buscar producto</h2>
        <form onSubmit={handleBuscar} className="flex gap-2 mb-4">
          <input
            type="text"
            placeholder="Ingresa código de barras o busca manualmente"
            value={barcode}
            onChange={(e) => setBarcode(e.target.value)}
            className="input flex-1"
          />
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? '...' : 'Buscar'}
          </button>
        </form>

        <button
          onClick={iniciarScanner}
          className="w-full py-2 px-3 rounded-xl transition-colors font-medium text-sm flex items-center justify-center gap-2"
          style={{
            backgroundColor: cameraActive ? '#ef4444' : '#10b981',
            color: 'white'
          }}
        >
          <span>{cameraActive ? '📷 Parar cámara' : '📷 Usar cámara'}</span>
        </button>

        {error && !resultado && (
          <div className="mt-4 p-3 bg-red-500/10 border border-red-500/30 rounded-lg text-red-400 text-sm">
            {error}
          </div>
        )}
      </div>

      {/* Scanner de cámara */}
      {cameraActive && (
        <div className="card mb-6">
          <div id="html5-qrcode-reader" style={{ width: '100%' }}></div>
        </div>
      )}

      {/* Resultados */}
      {resultado && (
        <div className="card">
          <div className="flex gap-6">
            {resultado.imagenUrl && (
              <div className="flex-shrink-0">
                <img
                  src={resultado.imagenUrl}
                  alt={resultado.nombre}
                  className="w-32 h-32 object-cover rounded-lg bg-slate-700"
                />
              </div>
            )}

            <div className="flex-1">
              <div className="mb-4">
                <h3 className="text-lg font-semibold text-slate-100">{resultado.nombre}</h3>
                <p className="text-slate-400 text-sm">
                  {resultado.marca && resultado.marca !== 'No especificada' ? resultado.marca : 'Marca no especificada'}
                </p>
              </div>

              {/* Cards de macros */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
                <div className="bg-slate-700/50 border border-slate-600 rounded-lg p-3">
                  <p className="text-slate-400 text-xs font-medium">Kcal</p>
                  <p className="text-green-400 text-lg font-bold">{resultado.kcalPor100g}</p>
                  <p className="text-slate-500 text-xs">por 100g</p>
                </div>
                <div className="bg-slate-700/50 border border-slate-600 rounded-lg p-3">
                  <p className="text-slate-400 text-xs font-medium">Proteínas</p>
                  <p className="text-blue-400 text-lg font-bold">{resultado.proteinasPor100g}g</p>
                  <p className="text-slate-500 text-xs">por 100g</p>
                </div>
                <div className="bg-slate-700/50 border border-slate-600 rounded-lg p-3">
                  <p className="text-slate-400 text-xs font-medium">Grasas</p>
                  <p className="text-yellow-400 text-lg font-bold">{resultado.grasasPor100g}g</p>
                  <p className="text-slate-500 text-xs">por 100g</p>
                </div>
                <div className="bg-slate-700/50 border border-slate-600 rounded-lg p-3">
                  <p className="text-slate-400 text-xs font-medium">Carbos</p>
                  <p className="text-orange-400 text-lg font-bold">{resultado.carbosPor100g}g</p>
                  <p className="text-slate-500 text-xs">por 100g</p>
                </div>
              </div>

              {/* Nota informativa */}
              {addError && (
                <div className="p-3 bg-amber-500/10 border border-amber-500/30 rounded-lg text-amber-400 text-sm mb-4">
                  {addError}
                </div>
              )}

              {/* Formulario para añadir a comida */}
              <div className="space-y-3">
                <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                  <div>
                    <label className="label">Gramos</label>
                    <input
                      type="number"
                      value={gramos}
                      onChange={(e) => setGramos(parseFloat(e.target.value) || 0)}
                      className="input"
                      min="1"
                    />
                  </div>
                  <div>
                    <label className="label">Tipo de comida</label>
                    <select value={tipoComida} onChange={(e) => setTipoComida(e.target.value)} className="input">
                      {COMIDA_TIPOS.map((t) => (
                        <option key={t} value={t}>
                          {t}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="label">Fecha</label>
                    <input
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

                <button
                  onClick={handleAnadirAComida}
                  disabled={addingToMeal}
                  className="w-full btn-primary"
                >
                  {addingToMeal ? 'Procesando...' : '+ Añadir a registro'}
                </button>
              </div>
            </div>
          </div>

          <button
            onClick={() => {
              setResultado(null)
              setBarcode('')
              setError('')
              setAddError('')
            }}
            className="mt-6 w-full py-2 px-3 rounded-xl bg-slate-700 hover:bg-slate-600 text-slate-200 transition-colors text-sm font-medium"
          >
            Escanear otro producto
          </button>
        </div>
      )}

      {!resultado && !loading && (
        <div className="card text-center text-slate-400">
          <p className="text-sm">Usa el formulario anterior para buscar un producto por su código de barras</p>
        </div>
      )}
    </div>
  )
}
