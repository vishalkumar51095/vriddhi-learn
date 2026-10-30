import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/admin/AdminLayout'
import { fetchAdminUsers, updateUserRoles, updateUserStatus, type AdminUserRow } from '../../api/admin'
import { useAuth } from '../../context/AuthContext'

const ALL_ROLES = ['LEARNER', 'TEACHER', 'CONTENT_REVIEWER', 'ADMIN']
const STATUS_TONE: Record<string, string> = { ACTIVE: 'badge-green', SUSPENDED: 'badge-red', PENDING: 'badge-accent', INACTIVE: 'badge-muted', DELETED: 'badge-muted' }

function RoleEditor({ row, onSaved }: { row: AdminUserRow; onSaved: (u: AdminUserRow) => void }) {
  const [open, setOpen] = useState(false)
  const [roles, setRoles] = useState<string[]>(row.roles)
  const [saving, setSaving] = useState(false)
  const { user: me } = useAuth()

  async function save() {
    setSaving(true)
    try { onSaved(await updateUserRoles(row.id, roles)); setOpen(false) } finally { setSaving(false) }
  }

  return (
    <div className="relative inline-block">
      <button onClick={() => setOpen((o) => !o)} className="flex flex-wrap gap-1">
        {row.roles.map((r) => <span key={r} className="badge badge-accent">{r}</span>)}
      </button>
      {open && (
        <div className="absolute left-0 top-full z-10 mt-1 w-56 rounded-lg border border-line bg-bg-raised p-3 shadow-lg">
          {ALL_ROLES.map((r) => (
            <label key={r} className="mb-1.5 flex items-center gap-2 text-sm text-ink last:mb-0">
              <input type="checkbox" checked={roles.includes(r)}
                     disabled={row.id === me?.id && r === 'ADMIN' && roles.includes('ADMIN') && roles.filter((x) => x === 'ADMIN').length <= 1}
                     onChange={(e) => setRoles((prev) => e.target.checked ? [...prev, r] : prev.filter((x) => x !== r))} />
              {r}
            </label>
          ))}
          <div className="mt-2 flex gap-2 border-t border-line pt-2">
            <button onClick={save} disabled={saving || roles.length === 0} className="btn btn-primary btn-sm flex-1">
              {saving ? 'Saving…' : 'Save'}
            </button>
            <button onClick={() => { setRoles(row.roles); setOpen(false) }} className="btn btn-ghost btn-sm">Cancel</button>
          </div>
        </div>
      )}
    </div>
  )
}

export function AdminUsersPage() {
  const { user: me } = useAuth()
  const [rows, setRows] = useState<AdminUserRow[]>([])
  const [total, setTotal] = useState(0)
  const [q, setQ] = useState('')
  const [role, setRole] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const size = 15

  function load() {
    setLoading(true)
    fetchAdminUsers({ q, role, status, page, size }).then((r) => { setRows(r.content); setTotal(r.totalElements) }).finally(() => setLoading(false))
  }
  useEffect(load, [page]) // eslint-disable-line react-hooks/exhaustive-deps
  useEffect(() => { const t = setTimeout(() => { setPage(0); load() }, 300); return () => clearTimeout(t) }, [q, role, status]) // eslint-disable-line react-hooks/exhaustive-deps

  async function toggleSuspend(u: AdminUserRow) {
    const next = u.status === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED'
    const updated = await updateUserStatus(u.id, next)
    setRows((prev) => prev.map((r) => (r.id === u.id ? { ...r, status: updated.status } : r)))
  }

  return (
    <AdminLayout>
      <h1 className="mb-6 font-serif text-2xl font-semibold text-ink">Users</h1>

      <div className="mb-5 flex flex-wrap gap-3">
        <input placeholder="Search name or email…" value={q} onChange={(e) => setQ(e.target.value)} className="input max-w-xs" />
        <select value={role} onChange={(e) => setRole(e.target.value)} className="input w-auto">
          <option value="">All roles</option>
          {ALL_ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
        </select>
        <select value={status} onChange={(e) => setStatus(e.target.value)} className="input w-auto">
          <option value="">All statuses</option>
          {['ACTIVE', 'SUSPENDED', 'PENDING', 'INACTIVE'].map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table">
            <thead><tr><th>Name</th><th>Email</th><th>Roles</th><th>Status</th><th>Last sign-in</th><th>Joined</th><th></th></tr></thead>
            <tbody>
              {rows.map((u) => (
                <tr key={u.id}>
                  <td className="font-medium">{u.fullName}</td>
                  <td className="text-ink-soft">{u.email}</td>
                  <td><RoleEditor row={u} onSaved={(updated) => setRows((prev) => prev.map((r) => (r.id === u.id ? updated : r)))} /></td>
                  <td><span className={`badge ${STATUS_TONE[u.status] ?? 'badge-muted'}`}>{u.status}</span></td>
                  <td className="whitespace-nowrap text-xs text-muted">{u.lastLoginAt ? new Date(u.lastLoginAt).toLocaleString() : 'Never'}</td>
                  <td className="whitespace-nowrap text-xs text-muted">{new Date(u.createdAt).toLocaleDateString()}</td>
                  <td>
                    <button onClick={() => toggleSuspend(u)} disabled={u.id === me?.id}
                            className={`btn btn-sm ${u.status === 'SUSPENDED' ? 'btn-ghost' : 'btn-danger'}`}>
                      {u.status === 'SUSPENDED' ? 'Reactivate' : 'Suspend'}
                    </button>
                  </td>
                </tr>
              ))}
              {!loading && rows.length === 0 && <tr><td colSpan={7} className="py-8 text-center text-muted">No users match these filters.</td></tr>}
            </tbody>
          </table>
        </div>
        <div className="flex items-center justify-between border-t border-line px-5 py-3 text-sm text-muted">
          <span>{total} user{total === 1 ? '' : 's'}</span>
          <div className="flex gap-2">
            <button disabled={page === 0} onClick={() => setPage((p) => p - 1)} className="btn btn-ghost btn-sm">Previous</button>
            <button disabled={(page + 1) * size >= total} onClick={() => setPage((p) => p + 1)} className="btn btn-ghost btn-sm">Next</button>
          </div>
        </div>
      </div>
    </AdminLayout>
  )
}
