import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { getActivePinia } from 'pinia'
import { ElMessage } from 'element-plus'
import type { ApiResult } from '@/types/api'
import { getToken, clearAuthStorage } from '@/utils/storage'

declare module 'axios' {
  interface AxiosRequestConfig<D = any> {
    crmSessionVersion?: number
  }
}

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

function normalizePaginationParams(params?: Record<string, unknown>) {
  if (!params) return params
  const normalized = { ...params }
  if (normalized.pageNo !== undefined && normalized.page === undefined) normalized.page = normalized.pageNo
  if (normalized.pageSize !== undefined && normalized.size === undefined) normalized.size = normalized.pageSize
  delete normalized.pageNo
  delete normalized.pageSize
  return normalized
}

function normalizePaginationResult(data: unknown) {
  if (!data || typeof data !== 'object' || Array.isArray(data)) return
  const payload = data as Record<string, unknown>
  if (payload.page !== undefined && payload.pageNo === undefined) payload.pageNo = payload.page
  if (payload.size !== undefined && payload.pageSize === undefined) payload.pageSize = payload.size
}

let expiryTask: Promise<void> | null = null

async function expireSession(config?: InternalAxiosRequestConfig): Promise<void> {
  // A bad password on /login and a delayed response from an old token do not expire a new session.
  if (config?.url === '/auth/login') return
  const currentToken = getToken()
  const usedAuthorization = config?.headers?.Authorization
  if (currentToken && usedAuthorization !== `Bearer ${currentToken}`) return
  if (expiryTask) return expiryTask
  const task = (async () => {
    const activePinia = getActivePinia()
    if (activePinia) {
      const { useAuthStore } = await import('@/stores/auth')
      if (getToken() !== currentToken) return
      const authStore = useAuthStore(activePinia)
      if (config?.crmSessionVersion !== undefined && config.crmSessionVersion !== authStore.sessionVersion) return
      authStore.clearAuth()
    } else {
      clearAuthStorage()
    }
    const { default: router } = await import('@/router')
    if (getToken()) return
    ElMessage.error('登录已失效，请重新登录')
    const current = router.currentRoute.value
    if (current.path !== '/login') {
      // Do not await navigation from a request awaited by a navigation guard.
      void router.replace({ name: 'Login', query: { redirect: current.fullPath } })
    }
  })()
  expiryTask = task
  try { await task } finally { if (expiryTask === task) expiryTask = null }
}

function errorMessage(error: any): string {
  return error?.response?.data?.message || error?.message || '请求失败'
}

instance.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  const activePinia = getActivePinia()
  if (activePinia) {
    const { useAuthStore } = await import('@/stores/auth')
    config.crmSessionVersion = useAuthStore(activePinia).sessionVersion
  }
  const token = getToken()
  if (token && config.headers) config.headers.Authorization = `Bearer ${token}`
  config.params = normalizePaginationParams(config.params as Record<string, unknown> | undefined)
  return config
}, (error) => Promise.reject(error))

instance.interceptors.response.use(async (response: AxiosResponse<ApiResult>) => {
  const { code, message, data } = response.data
  if (code === 200) {
    normalizePaginationResult(data)
    return data as any
  }
  if (code === 401 && response.config.url !== '/auth/login') await expireSession(response.config)
  else ElMessage.error(message || '请求失败')
  return Promise.reject(new Error(message || '请求失败'))
}, async (error) => {
  const status = error.response?.status
  if (status === 401 && error.config?.url !== '/auth/login') {
    await expireSession(error.config)
  } else if (status === 403) {
    // Keep the current page: an action can be denied without forbidding read access.
    ElMessage.error(errorMessage(error) || '没有操作权限')
  } else if (status) {
    ElMessage.error(errorMessage(error))
  } else if (error.code === 'ECONNABORTED') {
    ElMessage.error('请求超时，请稍后重试')
  } else {
    ElMessage.error('网络连接失败，请检查服务器连接')
  }
  return Promise.reject(error)
})

export default instance
