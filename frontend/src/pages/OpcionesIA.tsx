import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { useAuth } from '../contexts/AuthContext'
import { getIaConfig, saveIaConfig, deleteIaConfig, type IaConfigData } from '../api/iaConfig'

const CANVA_GUIDE_URL = 'https://openrouter.ai'
const MODELOS_RECOMENDADOS = [
  'google/gemma-3-27b-it:free',
  'deepseek/deepseek-chat:free'
]

const container = { hidden: {}, show: { transition: { staggerChildren: 0.1 } } }
const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } }

export default function OpcionesIA() {
  const { user } = useAuth()
  const [config, setConfig] = useState<IaConfigData | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState<IaConfigData>({
    proxyUrl: '',
    model: '',
    apiKey: ''
  })
  const [showApiKey, setShowApiKey] = useState(false)

  useEffect(() => {
    if (!user) return
    setLoading(true)
    getIaConfig(user.usuarioId)
      .then((data) => {
        if (data) {
          setConfig(data)
          setForm(data)
        }
      })
      .catch(() => setError('No se pudo cargar la configuración'))
      .finally(() => setLoading(false))
  }, [user])

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user) return

    setSaving(true)
    setError(null)
    try {
      const updated = await saveIaConfig(user.usuarioId, form)
      setConfig(updated)
      setSaved(true)
      setTimeout(() => setSaved(false), 2500)
    } catch {
      setError('No se pudo guardar la configuración. Intenta de nuevo.')
    } finally {
      setSaving(false)
    }
  }

  const handleReset = async () => {
    if (!user || !config) return
    if (!confirm('¿Estás seguro? Se eliminará tu configuración personalizada.')) return

    setSaving(true)
    setError(null)
    try {
      await deleteIaConfig(user.usuarioId)
      setConfig(null)
      setForm({
        proxyUrl: '',
        model: '',
        apiKey: ''
      })
      setSaved(true)
      setTimeout(() => setSaved(false), 2500)
    } catch {
      setError('No se pudo eliminar la configuración. Intenta de nuevo.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex-1 overflow-auto flex justify-center items-center">
        <div className="animate-spin w-8 h-8 border-2 border-emerald-500 border-t-transparent rounded-full" />
      </motion.div>
    )
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="flex-1 overflow-auto p-6"
    >
      <div className="max-w-3xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="flex items-center justify-between mb-6 flex-wrap gap-3"
        >
          <div>
            <h1 className="gradient-text text-3xl font-bold">Opciones de IA</h1>
            <p className="text-white/50 text-sm mt-0.5">Personaliza tu IA para análisis y recomendaciones</p>
          </div>
          {saved && (
            <motion.span
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              className="text-emerald-400 text-sm font-medium flex items-center gap-1"
            >
              ✓ Guardado
            </motion.span>
          )}
        </motion.div>

        {error && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="card bg-red-500/10 border-red-500/30 text-red-400 mb-6"
          >
            {error}
          </motion.div>
        )}

        <motion.div
          variants={container}
          initial="hidden"
          animate="show"
          className="space-y-6"
        >
          {/* Card 1: Tu IA personalizada */}
          <motion.div variants={item} className="card">
            <div className="flex items-center gap-3 mb-6">
              <span className="text-2xl">🤖</span>
              <div>
                <h2 className="text-xl font-bold text-white">Tu IA personalizada</h2>
                <p className="text-white/50 text-sm">Configura tu proveedor y modelo preferido</p>
              </div>
            </div>

            {/* Status badge */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.15 }}
              className="mb-6"
            >
              {config ? (
                <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/20 border border-emerald-500/30">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                  <span className="text-sm font-medium text-emerald-300">✓ Configuración personalizada activa</span>
                </div>
              ) : (
                <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-white/10 border border-white/20">
                  <span className="w-2 h-2 rounded-full bg-white/40" />
                  <span className="text-sm font-medium text-white/60">Usando IA por defecto</span>
                </div>
              )}
            </motion.div>

            {/* Form */}
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="label">Proxy URL</label>
                <input
                  type="text"
                  className="input"
                  placeholder="https://openrouter.ai/api/v1"
                  value={form.proxyUrl}
                  onChange={(e) => setForm((f) => ({ ...f, proxyUrl: e.target.value }))}
                />
              </div>

              <div>
                <label className="label">Modelo</label>
                <input
                  type="text"
                  className="input"
                  placeholder="google/gemma-3-27b-it:free"
                  value={form.model}
                  onChange={(e) => setForm((f) => ({ ...f, model: e.target.value }))}
                />
              </div>

              <div>
                <label className="label">API Key</label>
                <div className="relative">
                  <input
                    type={showApiKey ? 'text' : 'password'}
                    className="input pr-12"
                    placeholder="sk-or-..."
                    value={form.apiKey}
                    onChange={(e) => setForm((f) => ({ ...f, apiKey: e.target.value }))}
                  />
                  <button
                    type="button"
                    onClick={() => setShowApiKey(!showApiKey)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-white/50 hover:text-white transition-colors"
                  >
                    {showApiKey ? '🙈' : '👁️'}
                  </button>
                </div>
              </div>

              <div className="flex gap-2 pt-2">
                <motion.button
                  type="submit"
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  className="btn-primary flex-1"
                  disabled={saving}
                >
                  {saving ? 'Guardando...' : 'Guardar configuración'}
                </motion.button>
                {config && (
                  <motion.button
                    type="button"
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    className="btn-danger"
                    onClick={handleReset}
                    disabled={saving}
                  >
                    Restablecer
                  </motion.button>
                )}
              </div>
            </form>
          </motion.div>

          {/* Card 2: Cómo obtener una API key */}
          <motion.div variants={item} className="card">
            <div className="flex items-center gap-3 mb-4">
              <span className="text-2xl">📖</span>
              <div>
                <h2 className="text-xl font-bold text-white">¿Cómo obtener una API key?</h2>
              </div>
            </div>

            <p className="text-white/70 mb-6">
              Aprende a crear tu cuenta en OpenRouter y obtener tu API key gratuita con nuestra guía paso a paso.
            </p>

            <div className="mb-6">
              <h3 className="text-sm font-semibold text-white/80 mb-3 uppercase tracking-wide">Modelos gratuitos recomendados:</h3>
              <div className="space-y-2">
                {MODELOS_RECOMENDADOS.map((modelo) => (
                  <motion.div
                    key={modelo}
                    whileHover={{ x: 4 }}
                    className="flex items-center gap-2 p-3 bg-white/5 rounded-lg border border-white/10 text-white/80 text-sm"
                  >
                    <span className="text-emerald-400">✓</span>
                    <code className="font-mono">{modelo}</code>
                  </motion.div>
                ))}
              </div>
            </div>

            <motion.a
              href={CANVA_GUIDE_URL}
              target="_blank"
              rel="noopener noreferrer"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="btn-primary w-full text-center"
            >
              Ver guía en OpenRouter
            </motion.a>
          </motion.div>
        </motion.div>
      </div>
    </motion.div>
  )
}
