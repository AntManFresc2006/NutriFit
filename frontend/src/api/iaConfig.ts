import client from './client'

export interface IaConfigData {
  proxyUrl: string
  model: string
  apiKey: string
}

export const getIaConfig = (usuarioId: number) =>
  client.get<IaConfigData>('/api/ia-config', { params: { usuarioId } })
    .then(r => r.data)
    .catch(() => null)

export const saveIaConfig = (usuarioId: number, data: IaConfigData) =>
  client.put<IaConfigData>('/api/ia-config', data, { params: { usuarioId } })
    .then(r => r.data)

export const deleteIaConfig = (usuarioId: number) =>
  client.delete('/api/ia-config', { params: { usuarioId } })
