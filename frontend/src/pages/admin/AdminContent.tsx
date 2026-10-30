import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/admin/AdminLayout'
import { MiniBarChart } from '../../components/MiniBarChart'
import { fetchAdminContentStats, type AdminContentStats } from '../../api/admin'

function Breakdown({ title, data }: { title: string; data: Record<string, number> }) {
  const total = Object.values(data).reduce((a, b) => a + b, 0) || 1
  return (
    <div className="card p-5">
      <h3 className="mb-3 text-sm font-semibold text-ink">{title}</h3>
      {Object.entries(data).map(([k, v]) => (
        <div key={k} className="mb-2.5 grid grid-cols-[130px_1fr_32px] items-center gap-3 text-sm last:mb-0">
          <span className="text-ink-soft">{k.replace(/_/g, ' ')}</span>
          <div className="h-2 overflow-hidden rounded-full bg-surface-2"><div className="h-full rounded-full bg-amber" style={{ width: `${(v / total) * 100}%` }} /></div>
          <span className="text-right text-ink-soft">{v}</span>
        </div>
      ))}
    </div>
  )
}

export function AdminContentPage() {
  const [stats, setStats] = useState<AdminContentStats | null>(null)
  useEffect(() => { fetchAdminContentStats().then(setStats) }, [])

  return (
    <AdminLayout>
      <h1 className="mb-6 font-serif text-2xl font-semibold text-ink">Content</h1>
      <div className="mb-6 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <div className="card p-4"><div className="font-serif text-2xl font-semibold text-ink">{stats?.totalQuestions ?? '—'}</div><div className="text-xs text-muted">Total questions</div></div>
        <div className="card border-l-[3px] border-l-solid-amber p-4"><div className="font-serif text-2xl font-semibold text-ink">{stats?.pendingReview ?? '—'}</div><div className="text-xs text-muted">Pending review</div></div>
        <div className="card border-l-[3px] border-l-solid-green p-4"><div className="font-serif text-2xl font-semibold text-ink">{stats?.byStatus?.PUBLISHED ?? 0}</div><div className="text-xs text-muted">Published</div></div>
        <div className="card p-4"><div className="font-serif text-2xl font-semibold text-ink">{stats?.byStatus?.DRAFT ?? 0}</div><div className="text-xs text-muted">Drafts</div></div>
      </div>
      <div className="mb-6 card p-5">
        <h3 className="mb-1 text-sm font-semibold text-ink">Questions created, last 14 days</h3>
        {stats ? <MiniBarChart data={stats.createdPerDay} /> : <p className="text-sm text-muted">Loading…</p>}
      </div>
      <div className="grid gap-6 md:grid-cols-3">
        {stats && <Breakdown title="By status" data={stats.byStatus} />}
        {stats && <Breakdown title="By difficulty" data={stats.byDifficulty} />}
        {stats && <Breakdown title="By type" data={stats.byType} />}
      </div>
    </AdminLayout>
  )
}
