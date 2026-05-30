import { useState } from 'react'

function hoy() {
  return new Date().toISOString().split('T')[0]
}

export function useFechaPersistente(key: string): [string, (f: string) => void] {
  const [fecha, setFechaState] = useState<string>(
    () => localStorage.getItem(key) ?? hoy()
  )

  function setFecha(f: string) {
    localStorage.setItem(key, f)
    setFechaState(f)
  }

  return [fecha, setFecha]
}
