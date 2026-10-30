import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { AxiosError } from 'axios'
import { useAuth } from '../context/AuthContext'
import type { ApiError } from '../api/types'
import { AuthShell } from '../components/AuthShell'
import { PasswordField } from '../components/PasswordField'
import { isEmail } from '../lib/validation'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [touched, setTouched] = useState<Record<string, boolean>>({})
  const [error, setError] = useState<string | null>(null)
  const [emailTaken, setEmailTaken] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [shakeKey, setShakeKey] = useState(0)

  const touch = (k: string) => setTouched((t) => ({ ...t, [k]: true }))
  const nameOk = fullName.trim().length >= 2
  const emailOk = isEmail(email)
  const passOk = password.length >= 8
  const matchOk = confirm.length > 0 && confirm === password
  const formOk = nameOk && emailOk && passOk && matchOk

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setTouched({ name: true, email: true, confirm: true })
    if (!formOk) { setShakeKey((k) => k + 1); return }
    setError(null)
    setEmailTaken(false)
    setSubmitting(true)
    try {
      await register(fullName.trim(), email.trim(), password)
      navigate('/dashboard')
    } catch (err) {
      const apiError = (err as AxiosError<ApiError>).response?.data
      const fieldMessage = apiError?.fieldErrors && Object.values(apiError.fieldErrors)[0]
      const message = fieldMessage ?? apiError?.message ?? 'Something went wrong. Please try again.'
      setEmailTaken(/already exists/i.test(message))
      setError(message)
      setShakeKey((k) => k + 1)
    } finally {
      setSubmitting(false)
    }
  }

  const hint = (show: boolean, ok: boolean, bad: string, good: string) =>
    show ? <p className={`mt-1 text-xs ${ok ? 'text-green' : 'text-red'}`}>{ok ? `✓ ${good}` : bad}</p> : null

  return (
    <AuthShell
      brandClass="brand-student" tab="student"
      eyebrow="Student sign-up" title="Start preparing today"
      blurb="Free account. Practice by topic, sit realistic timed mocks, and see exactly where to focus next."
      bullets={['Instant-graded practice by topic', 'Timed mock exams with a real question palette', 'Section and difficulty-wise analytics']}
    >
      <h2 className="mb-1 font-serif text-2xl font-semibold">Create your account</h2>
      <p className="mb-6 text-sm text-muted">It takes under a minute.</p>

      <div key={shakeKey} className={shakeKey > 0 && (error || !formOk) ? 'shake' : ''}>
        {error && (
          <p role="alert" className="badge-red mb-4 block rounded-lg px-3 py-2 text-sm">
            {error}{' '}
            {emailTaken && <Link to="/login" className="font-semibold underline">Log in instead</Link>}
          </p>
        )}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div>
            <label className="label" htmlFor="name">Full name</label>
            <input id="name" className="input" autoComplete="name" placeholder="Your name" value={fullName}
                   onChange={(e) => setFullName(e.target.value)} onBlur={() => touch('name')} />
            {hint(!!touched.name, nameOk, 'Please enter your name', 'Looks good')}
          </div>

          <div>
            <label className="label" htmlFor="email">Email</label>
            <input id="email" type="email" className="input" autoComplete="email" placeholder="you@example.com" value={email}
                   onChange={(e) => { setEmail(e.target.value); setEmailTaken(false) }} onBlur={() => touch('email')} />
            {hint(!!touched.email && email.length > 0, emailOk, 'That email doesn’t look right', 'Looks good')}
          </div>

          <PasswordField id="password" value={password} onChange={setPassword} autoComplete="new-password"
                         placeholder="At least 8 characters" showStrength />

          <div>
            <label className="label" htmlFor="confirm">Confirm password</label>
            <input id="confirm" type="password" className="input" autoComplete="new-password" placeholder="Type it again"
                   value={confirm} onChange={(e) => setConfirm(e.target.value)} onBlur={() => touch('confirm')} />
            {hint(confirm.length > 0 && (!!touched.confirm || matchOk), matchOk, 'Passwords don’t match yet', 'Passwords match')}
          </div>

          <button type="submit" disabled={submitting} className="btn btn-primary w-full"
                  title={!formOk ? 'Fill in the fields above' : undefined}>
            {submitting ? 'Creating account…' : 'Create account'}
          </button>
          {!formOk && touched.confirm && (
            <p className="text-center text-xs text-muted">Complete every field to continue.</p>
          )}
        </form>
      </div>

      <p className="mt-5 text-center text-sm text-muted">
        Already have an account? <Link to="/login" className="font-semibold text-amber">Log in</Link>
      </p>
      <p className="mt-2 text-center text-sm text-muted">
        Want to teach? <Link to="/teacher/register" className="font-semibold text-amber">Apply as a teacher →</Link>
      </p>
    </AuthShell>
  )
}
