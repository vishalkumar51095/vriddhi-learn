import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

const ACCESS_TOKEN_KEY = 'vriddhi.accessToken'
const REFRESH_TOKEN_KEY = 'vriddhi.refreshToken'

export const tokenStore = {
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  setTokens: (accessToken: string, refreshToken: string) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },
  clear: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  },
}

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
})

apiClient.interceptors.request.use((config) => {
  const token = tokenStore.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Access tokens are short-lived (15 min) by design (see auth-service's
// jwt.access-token-ttl-minutes) — on a 401, try exactly once to refresh
// using the opaque refresh token before giving up and forcing a re-login.
let refreshInFlight: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const refreshToken = tokenStore.getRefreshToken()
  if (!refreshToken) throw new Error('No refresh token available')

  const response = await axios.post(
    `${import.meta.env.VITE_API_BASE_URL || '/api'}/auth/refresh`,
    { refreshToken },
  )
  const { accessToken, refreshToken: newRefreshToken } = response.data
  tokenStore.setTokens(accessToken, newRefreshToken)
  return accessToken
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true
      try {
        refreshInFlight = refreshInFlight ?? refreshAccessToken()
        const newAccessToken = await refreshInFlight
        refreshInFlight = null
        originalRequest.headers = originalRequest.headers ?? {}
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return apiClient(originalRequest)
      } catch {
        refreshInFlight = null
        tokenStore.clear()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)
