import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { fetchCurrentUser, login as apiLogin, logout as apiLogout, register as apiRegister } from '../api/auth'
import { tokenStore } from '../api/client'
import type { UserResponse } from '../api/types'

interface AuthContextValue {
  user: UserResponse | null
  loading: boolean
  login: (email: string, password: string, portal?: 'STUDENT' | 'TEACHER' | 'ADMIN') => Promise<void>
  register: (fullName: string, email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function restoreSession() {
      if (!tokenStore.getAccessToken()) {
        setLoading(false)
        return
      }
      try {
        setUser(await fetchCurrentUser())
      } catch {
        tokenStore.clear()
      } finally {
        setLoading(false)
      }
    }
    restoreSession()
  }, [])

  async function login(email: string, password: string, portal?: 'STUDENT' | 'TEACHER' | 'ADMIN') {
    const response = await apiLogin(email, password, portal)
    setUser(response.user)
  }

  async function register(fullName: string, email: string, password: string) {
    const response = await apiRegister(fullName, email, password)
    setUser(response.user)
  }

  async function logout() {
    await apiLogout()
    setUser(null)
  }

  async function refreshUser() {
    setUser(await fetchCurrentUser())
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
