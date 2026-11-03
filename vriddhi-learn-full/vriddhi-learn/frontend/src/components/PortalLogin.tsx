import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { AxiosError } from 'axios'
import { useAuth } from '../context/AuthContext'
import type { ApiError } from '../api/types'
import { PasswordField } from './PasswordField'

export interface PortalConfig {
  portal: 'STUDENT' | 'TEACHER' | 'ADMIN'
  brandClass: string
  eyebrow: string
  title: string
  blurb: string
  bullets: string[]
  registerTo?: string
  registerLabel?: string
  homeAfterLogin: string
  otherPortals: { label: string; to: string }[]
}

/**
 * One shared shell, three distinct portals. Each renders at its own route
 * (/login, /teacher/login, /admin/login) with its own brand panel and copy —
 * they read and feel like separate sign-in pages, not one generic form with
 * a dropdown, while sharing the auth wiring and layout.
 */
export function PortalLogin({ config }: { config: PortalConfig }) {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [shakeKey, setShakeKey] = useState(0)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(email, password, config.portal)
      navigate(config.homeAfterLogin)
    } catch (err) {
      const status = (err as AxiosError).response?.status
      const apiError = (err as AxiosError<ApiError>).response?.data
      if (status === 403) {
        setError(apiError?.message ?? "This account doesn't have access to this portal.")
      } else {
        setError(apiError?.message ?? 'Something went wrong. Please try again.')
      }
      setShakeKey((k) => k + 1)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid min-h-screen md:grid-cols-2">
      <div className={`brand-panel relative hidden flex-col justify-between overflow-hidden p-10 md:flex ${config.brandClass}`}>
        <div className="relative z-10">
          <Link to="/" className="font-serif text-lg font-bold text-white">
            VRIDDHI <span className="opacity-80">LEARN</span>
          </Link>
        </div>
        <div className="relative z-10 float-in max-w-sm">
          <div className="soft mb-3 text-sm font-semibold uppercase tracking-wide">{config.eyebrow}</div>
          <h1 className="mb-4 font-serif text-3xl font-semibold leading-tight text-white">{config.title}</h1>
          <p className="soft mb-6 text-sm leading-relaxed">{config.blurb}</p>
          <ul className="space-y-2 text-sm text-white">
            {config.bullets.map((b) => (
              <li key={b} className="flex items-start gap-2">
                <span className="mt-1 h-1.5 w-1.5 flex-shrink-0 rounded-full bg-white/70" />
                {b}
              </li>
            ))}
          </ul>
        </div>
        <div className="soft relative z-10 text-xs">Vriddhi Learn</div>
        <div aria-hidden className="pointer-events-none absolute -right-24 -top-24 h-72 w-72 rounded-full bg-white/10" />
        <div aria-hidden className="pointer-events-none absolute -bottom-32 -left-16 h-80 w-80 rounded-full bg-black/10" />
      </div>

      <div className="flex items-center justify-center bg-bg px-6 py-12">
        <div className="w-full max-w-sm">
          <div className="mb-8 md:hidden">
            <Link to="/" className="font-serif text-lg font-bold text-ink">
              VRIDDHI <span className="text-amber">LEARN</span>
            </Link>
          </div>

          <div className="mb-1 text-xs font-semibold uppercase tracking-wide text-muted">{config.eyebrow}</div>
          <h2 className="mb-1 font-serif text-2xl font-semibold text-ink">{config.title}</h2>
          <p className="mb-6 text-sm text-muted">Sign in to continue.</p>

          {error && <p key={shakeKey} role="alert" className="badge-red shake mb-4 block rounded-lg px-3 py-2 text-sm">{error}</p>}

          <form onSubmit={handleSubmit} className="space-y-3">
            <div>
              <label className="label" htmlFor="email">Email</label>
              <input id="email" type="email" required autoComplete="username" placeholder="you@example.com"
                     value={email} onChange={(e) => setEmail(e.target.value)} className="input" />
            </div>
            <PasswordField id="password" value={password} onChange={setPassword} autoComplete="current-password" />
            <button type="submit" disabled={submitting} className="btn btn-primary w-full">
              {submitting ? 'Signing in…' : 'Sign in'}
            </button>
          </form>

          {config.registerTo && (
            <p className="mt-4 text-center text-sm text-muted">
              New here?{' '}
              <Link to={config.registerTo} className="font-semibold text-amber">{config.registerLabel ?? 'Create an account'}</Link>
            </p>
          )}

          <div className="mt-8 border-t border-line pt-5">
            <p className="mb-2 text-xs font-semibold text-muted">Looking for a different portal?</p>
            <div className="flex flex-wrap gap-2">
              {config.otherPortals.map((p) => (
                <Link key={p.to} to={p.to} className="btn btn-ghost btn-sm">{p.label}</Link>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
