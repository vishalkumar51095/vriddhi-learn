import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="flex min-h-screen items-center justify-center text-muted">Loading…</div>
  if (!user) return <Navigate to="/login" replace />
  return <>{children}</>
}

/**
 * Gates a route to accounts holding at least one of `roles`. Used for
 * /admin/** (ADMIN) and /teacher/** (TEACHER) — a student who wanders onto
 * an admin URL is bounced to their own dashboard, not shown a blank page.
 */
export function RoleProtectedRoute({ roles, redirectTo = '/dashboard', children }:
    { roles: string[]; redirectTo?: string; children: ReactNode }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="flex min-h-screen items-center justify-center text-muted">Loading…</div>
  if (!user) return <Navigate to="/login" replace />
  if (!user.roles.some((r) => roles.includes(r))) return <Navigate to={redirectTo} replace />
  return <>{children}</>
}
