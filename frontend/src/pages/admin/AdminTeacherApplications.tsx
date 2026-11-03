import { useEffect, useState } from 'react'
import type { AxiosError } from 'axios'
import { AdminLayout } from '../../components/admin/AdminLayout'
import {
  approveTeacherApplication, fetchTeacherApplications, rejectTeacherApplication, type TeacherApplicationRow,
} from '../../api/admin'
import type { ApiError } from '../../api/types'

const TABS = ['PENDING', 'APPROVED', 'REJECTED'] as const
const TONE: Record<string, string> = { PENDING: 'badge-accent', APPROVED: 'badge-green', REJECTED: 'badge-red' }

function ApplicationCard({ app, onDone }: { app: TeacherApplicationRow; onDone: (msg: string) => void }) {
  const [open, setOpen] = useState(false)
  const [rejecting, setRejecting] = useState(false)
  const [note, setNote] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function run(action: () => Promise<unknown>, doneMsg: string) {
    setBusy(true); setError(null)
    try { await action(); onDone(doneMsg) }
    catch (err) { setError((err as AxiosError<ApiError>).response?.data?.message ?? 'Could not complete that action.') }
    finally { setBusy(false) }
  }

  return (
    <div className="card lift overflow-hidden">
      <button type="button" onClick={() => setOpen((o) => !o)} aria-expanded={open}
              className="flex w-full items-start justify-between gap-4 p-4 text-left">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <span className="font-semibold text-ink">{app.fullName}</span>
            <span className={`badge ${TONE[app.status]}`}>{app.status}</span>
          </div>
          <div className="truncate text-sm text-ink-soft">{app.email}</div>
          <div className="mt-2 flex flex-wrap gap-1.5">{app.subjects.map((s) => <span key={s} className="badge badge-muted">{s}</span>)}</div>
        </div>
        <div className="shrink-0 text-right text-xs text-muted">
          <div>{app.experienceYears} yr experience</div>
          <div>{new Date(app.createdAt).toLocaleDateString()}</div>
          <div aria-hidden className={`mt-1 text-base text-amber transition-transform ${open ? 'rotate-180' : ''}`}>⌄</div>
        </div>
      </button>

      <div className="accordion-body" data-open={open}>
        <div>
          <div className="space-y-3 border-t border-line px-4 py-4 text-sm">
            <div><div className="label">Qualification</div><div className="text-ink-soft">{app.qualification}{app.organization ? ` · ${app.organization}` : ''}</div></div>
            <div><div className="label">Bio</div><p className="whitespace-pre-line text-ink-soft">{app.bio}</p></div>
            {app.status !== 'PENDING' && (
              <div className="text-xs text-muted">
                {app.status === 'APPROVED' ? 'Approved' : 'Rejected'}{app.reviewedByEmail ? ` by ${app.reviewedByEmail}` : ''}
                {app.reviewedAt ? ` on ${new Date(app.reviewedAt).toLocaleString()}` : ''}{app.reviewNote ? ` — “${app.reviewNote}”` : ''}
              </div>
            )}
            {error && <p role="alert" className="badge-red block rounded-lg px-3 py-2">{error}</p>}

            {app.status === 'PENDING' && !rejecting && (
              <div className="flex gap-2">
                <button className="btn btn-primary btn-sm" disabled={busy}
                        onClick={() => run(() => approveTeacherApplication(app.id), `${app.fullName} is now a teacher. They need to sign in again.`)}>
                  {busy ? 'Working…' : 'Approve'}
                </button>
                <button className="btn btn-ghost btn-sm" disabled={busy} onClick={() => setRejecting(true)}>Reject…</button>
              </div>
            )}
            {app.status === 'PENDING' && rejecting && (
              <div className="slide-in space-y-2">
                <label className="label" htmlFor={`note-${app.id}`}>Reason (shown in the audit log)</label>
                <textarea id={`note-${app.id}`} className="input" rows={2} maxLength={500} value={note} onChange={(e) => setNote(e.target.value)} autoFocus />
                <div className="flex gap-2">
                  <button className="btn btn-danger btn-sm" disabled={busy || note.trim().length < 3}
                          onClick={() => run(() => rejectTeacherApplication(app.id, note.trim()), `Application from ${app.fullName} rejected.`)}>
                    Confirm reject
                  </button>
                  <button className="btn btn-ghost btn-sm" disabled={busy} onClick={() => { setRejecting(false); setNote('') }}>Cancel</button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export function AdminTeacherApplicationsPage() {
  const [tab, setTab] = useState<(typeof TABS)[number]>('PENDING')
  const [rows, setRows] = useState<TeacherApplicationRow[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [flash, setFlash] = useState<string | null>(null)

  const [reloadKey, setReloadKey] = useState(0)

  // All setState calls here happen asynchronously (after the request), never synchronously in the effect body.
  useEffect(() => {
    let cancelled = false
    fetchTeacherApplications({ status: tab, size: 50 })
      .then((r) => { if (!cancelled) { setRows(r.content); setTotal(r.totalElements); setFailed(false) } })
      .catch(() => { if (!cancelled) setFailed(true) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [tab, reloadKey])

  function reload() { setLoading(true); setReloadKey((k) => k + 1) }
  function switchTab(t: (typeof TABS)[number]) { if (t !== tab) { setLoading(true); setTab(t) } }

  useEffect(() => {
    if (!flash) return
    const t = setTimeout(() => setFlash(null), 4000)
    return () => clearTimeout(t)
  }, [flash])

  return (
    <AdminLayout>
      <h1 className="mb-1 font-serif text-2xl font-semibold text-ink">Teacher applications</h1>
      <p className="mb-5 text-sm text-muted">Approving grants the TEACHER role; the applicant must sign in again to pick it up.</p>

      <div role="tablist" className="mb-5 flex gap-2">
        {TABS.map((t) => (
          <button key={t} role="tab" aria-selected={tab === t} aria-pressed={tab === t} className="chip" onClick={() => switchTab(t)}>
            {t.charAt(0) + t.slice(1).toLowerCase()}{tab === t && !loading ? ` · ${total}` : ''}
          </button>
        ))}
      </div>

      {flash && <p role="status" className="badge-green pop mb-4 block rounded-lg px-3 py-2 text-sm">{flash}</p>}
      {failed && <p role="alert" className="badge-red mb-4 block rounded-lg px-3 py-2 text-sm">Couldn’t load applications. <button className="font-semibold underline" onClick={reload}>Retry</button></p>}

      <div key={tab} className="slide-in space-y-3">
        {loading && <div className="card p-6 text-center text-sm text-muted">Loading…</div>}
        {!loading && !failed && rows.length === 0 && (
          <div className="card p-8 text-center text-sm text-muted">No {tab.toLowerCase()} applications.</div>
        )}
        {rows.map((a) => <ApplicationCard key={a.id} app={a} onDone={(msg) => { setFlash(msg); reload() }} />)}
      </div>
    </AdminLayout>
  )
}
