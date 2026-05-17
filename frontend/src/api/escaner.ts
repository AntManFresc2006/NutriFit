import client from './client'

export interface EscanerResult {
  nombre: string
  marca: string
  kcalPor100g: number
  proteinasPor100g: number
  grasasPor100g: number
  carbosPor100g: number
  imagenUrl: string | null
}

export const escanearBarcode = (barcode: string) =>
  client.get<EscanerResult>(`/api/escaner/${barcode}`).then(r => r.data)
