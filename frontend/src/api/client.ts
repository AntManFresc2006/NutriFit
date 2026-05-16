import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_URL ?? ''

const client = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: 65000,
})

// Permite que Layout detecte cuándo el backend ha respondido por primera vez
let _onFirstResponse: (() => void) | null = null
export function onServerReady(cb: () => void) { _onFirstResponse = cb }

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('nf_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(
  (r) => { _onFirstResponse?.(); _onFirstResponse = null; return r },
  (err) => {
    _onFirstResponse?.(); _onFirstResponse = null
    if (err.response?.status === 401) {
      localStorage.removeItem('nf_token')
      localStorage.removeItem('nf_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default client
