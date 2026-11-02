import { type FormEvent, useState } from 'react'
import { Layout } from '../components/Layout'
import { useAuth } from '../context/AuthContext'
import { updateProfile } from '../api/auth'

export function ProfilePage() {
  const { user, refreshUser } = useAuth()
  const [fullName, setFullName] = useState(user?.fullName ?? '')
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    setSaved(false)
    try {
      await updateProfile(fullName)
      await refreshUser()
      setSaved(true)
    } catch {
      setError('Could not save changes. Please try again.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <Layout>
      <h1 className="mb-6 font-serif text-2xl font-semibold">Profile</h1>

      <form onSubmit={handleSubmit} className="max-w-md rounded border border-line bg-bg-raised p-6">
        {error && <p className="mb-4 rounded bg-red-soft px-3 py-2 text-sm text-red">{error}</p>}
        {saved && <p className="mb-4 rounded bg-green-soft px-3 py-2 text-sm text-green">Saved.</p>}

        <label className="mb-1 block text-xs text-muted">Full name</label>
        <input
          type="text"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          className="mb-4 w-full rounded border border-line bg-bg px-3 py-2.5 text-sm"
        />

        <label className="mb-1 block text-xs text-muted">Email</label>
        <input
          type="email"
          value={user?.email ?? ''}
          disabled
          className="mb-4 w-full rounded border border-line bg-bg px-3 py-2.5 text-sm opacity-60"
        />

        <label className="mb-1 block text-xs text-muted">Roles</label>
        <input
          type="text"
          value={user?.roles?.join(', ') ?? ''}
          disabled
          className="mb-4 w-full rounded border border-line bg-bg px-3 py-2.5 text-sm opacity-60"
        />

        <div className="mb-5 grid grid-cols-2 gap-4 text-xs text-muted">
          <div><div className="mb-0.5 font-semibold text-ink-soft">Member since</div>{user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : '—'}</div>
          <div><div className="mb-0.5 font-semibold text-ink-soft">Last sign-in</div>{user?.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : '—'}</div>
        </div>

        <button
          type="submit"
          disabled={saving}
          className="rounded bg-ink px-5 py-2.5 text-sm font-semibold text-bg disabled:opacity-50"
        >
          {saving ? 'Saving…' : 'Save changes'}
        </button>
      </form>
    </Layout>
  )
}
