import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { AdminLayout } from '../../components/admin/AdminLayout'
import { MiniBarChart } from '../../components/MiniBarChart'
import {
  fetchAdminContentStats, fetchAdminExamStats, fetchAuditLogs, fetchUserStats,
  type AdminContentStats, type AdminExamStats, type AuditLogRow, type UserStatsResponse,
} from '../../api/admin'

function StatCard({ v, l, tone }: { v: string | number; l: string; tone?: 'green' | 'red' | 'amber' }) {
  const border = tone === 'green' ? 'border-l-solid-green' : tone === 'red' ? 'border-l-solid-red' : 'border-l-amber'
  return (
    <div className={`card border-l-[3px] p-4 ${border}`}>
      <div className="font-serif text-2xl font-semibold text-ink">{v}</div>
      <div className="text-xs text-muted">{l}</div>
    </div>
  )
}

const EVENT_TONE: Record<string, string> = {
  LOGIN_SUCCESS: 'badge-green', LOGIN_FAILURE: 'badge-red', LOGIN_DENIED: 'badge-red',
  REGISTER: 'badge-accent', USER_STATUS_CHANGED: 'badge-purple', USER_ROLES_CHANGED: 'badge-purple',
  PROFILE_UPDATE: 'badge-muted', LOGOUT: 'badge-muted',
}

export function AdminDashboardPage() {
  const [userStats, setUserStats] = useState<UserStatsResponse | null>(null)
  const [contentStats, setContentStats] = useState<AdminContentStats | null>(null)
  const [examStats, setExamStats] = useState<AdminExamStats | null>(null)
  const [logs, setLogs] = useState<AuditLogRow[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    Promise.all([fetchUserStats(), fetchAdminContentStats(), fetchAdminExamStats(), fetchAuditLogs({ size: 8 })])
      .then(([u, c, e, l]) => { setUserStats(u); setContentStats(c); setExamStats(e); setLogs(l.content) })
      .catch(() => setError('Could not load one or more admin data sources.'))
  }, [])

  return (
    <AdminLayout>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="font-serif text-2xl font-semibold text-ink">Dashboard</h1>
        <span className="text-xs text-muted">Live from auth-service, content-service &amp; exam-service</span>
      </div>

      {error && <p className="badge-red mb-6 block rounded-lg px-4 py-3 text-sm">{error}</p>}

      {/* ---- entries: users ---- */}
      <h2 className="mb-3 text-sm font-semibold text-muted">Users</h2>
      <div className="mb-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <StatCard v={userStats?.total ?? '—'} l="Total users" />
        <StatCard v={userStats?.active ?? '—'} l="Active" tone="green" />
        <StatCard v={userStats?.suspended ?? '—'} l="Suspended" tone="red" />
        <StatCard v={userStats?.newLast7Days ?? '—'} l="New (7 days)" />
      </div>

      {/* ---- entries: content ---- */}
      <h2 className="mb-3 text-sm font-semibold text-muted">Content</h2>
      <div className="mb-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <StatCard v={contentStats?.totalQuestions ?? '—'} l="Total questions" />
        <StatCard v={contentStats?.byStatus?.PUBLISHED ?? 0} l="Published" tone="green" />
        <StatCard v={contentStats?.pendingReview ?? 0} l="Pending review" tone="amber" />
        <StatCard v={contentStats?.byStatus?.DRAFT ?? 0} l="Drafts" />
      </div>

      {/* ---- entries: exams ---- */}
      <h2 className="mb-3 text-sm font-semibold text-muted">Exams &amp; attempts</h2>
      <div className="mb-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <StatCard v={examStats?.totalExams ?? '—'} l="Total exams" />
        <StatCard v={examStats?.attemptsSubmitted ?? 0} l="Submitted" tone="green" />
        <StatCard v={examStats?.attemptsAutoSubmitted ?? 0} l="Auto-submitted (timeout)" tone="amber" />
        <StatCard v={examStats?.avgScore != null ? examStats.avgScore.toFixed(2) : '—'} l="Average score" />
      </div>

      <div className="mb-8 grid gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <h3 className="mb-1 text-sm font-semibold text-ink">Registrations, last 14 days</h3>
          <p className="mb-4 text-xs text-muted">New user sign-ups per day</p>
          {userStats ? <MiniBarChart data={userStats.registrationsPerDay} /> : <p className="text-sm text-muted">Loading…</p>}
        </div>
        <div className="card p-5">
          <h3 className="mb-1 text-sm font-semibold text-ink">Exam attempts, last 14 days</h3>
          <p className="mb-4 text-xs text-muted">Attempts started per day</p>
          {examStats ? <MiniBarChart data={examStats.attemptsPerDay} /> : <p className="text-sm text-muted">Loading…</p>}
        </div>
      </div>

      <div className="mb-8 grid gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <h3 className="mb-3 text-sm font-semibold text-ink">Users by role</h3>
          {userStats && Object.entries(userStats.byRole).map(([role, count]) => (
            <div key={role} className="mb-2.5 grid grid-cols-[110px_1fr_32px] items-center gap-3 text-sm last:mb-0">
              <span className="text-ink-soft">{role}</span>
              <div className="h-2 overflow-hidden rounded-full bg-surface-2">
                <div className="h-full rounded-full bg-amber" style={{ width: `${userStats.total ? (count / userStats.total) * 100 : 0}%` }} />
              </div>
              <span className="text-right text-ink-soft">{count}</span>
            </div>
          ))}
        </div>
        <div className="card p-5">
          <h3 className="mb-3 text-sm font-semibold text-ink">Most-attempted exams</h3>
          {examStats?.topExams.map((e) => (
            <div key={e.title} className="mb-2.5 flex items-center justify-between text-sm last:mb-0">
              <span className="text-ink-soft">{e.title}</span>
              <span className="text-xs text-muted">{e.attempts} attempts{e.avgScore != null ? ` · avg ${e.avgScore.toFixed(1)}` : ''}</span>
            </div>
          )) ?? <p className="text-sm text-muted">Loading…</p>}
        </div>
      </div>

      {/* ---- logs ---- */}
      <div className="card overflow-hidden">
        <div className="flex items-center justify-between border-b border-line px-5 py-4">
          <h3 className="text-sm font-semibold text-ink">Recent activity</h3>
          <Link to="/admin/audit-logs" className="text-sm font-semibold text-amber">View all logs →</Link>
        </div>
        <div className="overflow-x-auto">
          <table className="table">
            <thead><tr><th>Event</th><th>Actor</th><th>Details</th><th>When</th></tr></thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td><span className={`badge ${EVENT_TONE[log.eventType] ?? 'badge-muted'}`}>{log.eventType.replace(/_/g, ' ')}</span></td>
                  <td className="whitespace-nowrap">{log.actorEmail ?? '—'}</td>
                  <td className="max-w-xs truncate text-ink-soft">{log.details}</td>
                  <td className="whitespace-nowrap text-xs text-muted">{new Date(log.createdAt).toLocaleString()}</td>
                </tr>
              ))}
              {logs.length === 0 && <tr><td colSpan={4} className="py-6 text-center text-muted">No activity yet.</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </AdminLayout>
  )
}
