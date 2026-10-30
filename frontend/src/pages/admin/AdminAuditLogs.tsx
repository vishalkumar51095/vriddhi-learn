import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/admin/AdminLayout'
import { fetchAuditLogs, type AuditLogRow } from '../../api/admin'

const EVENTS = ['LOGIN_SUCCESS', 'LOGIN_FAILURE', 'LOGIN_DENIED', 'REGISTER', 'LOGOUT', 'PROFILE_UPDATE', 'USER_STATUS_CHANGED', 'USER_ROLES_CHANGED']
const TONE: Record<string, string> = {
  LOGIN_SUCCESS: 'badge-green', LOGIN_FAILURE: 'badge-red', LOGIN_DENIED: 'badge-red',
  REGISTER: 'badge-accent', USER_STATUS_CHANGED: 'badge-purple', USER_ROLES_CHANGED: 'badge-purple',
  PROFILE_UPDATE: 'badge-muted', LOGOUT: 'badge-muted',
}

export function AdminAuditLogsPage() {
  const [rows, setRows] = useState<AuditLogRow[]>([])
  const [total, setTotal] = useState(0)
  const [q, setQ] = useState('')
  const [event, setEvent] = useState('')
  const [page, setPage] = useState(0)
  const size = 25

  function load() { fetchAuditLogs({ q, event, page, size }).then((r) => { setRows(r.content); setTotal(r.totalElements) }) }
  useEffect(load, [page]) // eslint-disable-line react-hooks/exhaustive-deps
  useEffect(() => { const t = setTimeout(() => { setPage(0); load() }, 300); return () => clearTimeout(t) }, [q, event]) // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <AdminLayout>
      <h1 className="mb-6 font-serif text-2xl font-semibold text-ink">Audit logs</h1>

      <div className="mb-5 flex flex-wrap gap-3">
        <input placeholder="Search actor, target or details…" value={q} onChange={(e) => setQ(e.target.value)} className="input max-w-sm" />
        <select value={event} onChange={(e) => setEvent(e.target.value)} className="input w-auto">
          <option value="">All events</option>
          {EVENTS.map((e) => <option key={e} value={e}>{e.replace(/_/g, ' ')}</option>)}
        </select>
      </div>

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table">
            <thead><tr><th>Event</th><th>Actor</th><th>Target</th><th>Details</th><th>IP</th><th>When</th></tr></thead>
            <tbody>
              {rows.map((log) => (
                <tr key={log.id}>
                  <td><span className={`badge ${TONE[log.eventType] ?? 'badge-muted'}`}>{log.eventType.replace(/_/g, ' ')}</span></td>
                  <td className="whitespace-nowrap text-ink-soft">{log.actorEmail ?? '—'}</td>
                  <td className="whitespace-nowrap text-xs text-muted">{log.targetType ? `${log.targetType} ${log.targetId?.slice(0, 8)}` : '—'}</td>
                  <td className="max-w-md text-ink-soft">{log.details}</td>
                  <td className="whitespace-nowrap text-xs text-muted">{log.ipAddress ?? '—'}</td>
                  <td className="whitespace-nowrap text-xs text-muted">{new Date(log.createdAt).toLocaleString()}</td>
                </tr>
              ))}
              {rows.length === 0 && <tr><td colSpan={6} className="py-8 text-center text-muted">No matching activity.</td></tr>}
            </tbody>
          </table>
        </div>
        <div className="flex items-center justify-between border-t border-line px-5 py-3 text-sm text-muted">
          <span>{total} entries</span>
          <div className="flex gap-2">
            <button disabled={page === 0} onClick={() => setPage((p) => p - 1)} className="btn btn-ghost btn-sm">Previous</button>
            <button disabled={(page + 1) * size >= total} onClick={() => setPage((p) => p + 1)} className="btn btn-ghost btn-sm">Next</button>
          </div>
        </div>
      </div>
    </AdminLayout>
  )
}
