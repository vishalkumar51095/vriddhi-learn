import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/admin/AdminLayout'
import { fetchAdminExams, type AdminExamRow } from '../../api/admin'

const STATUS_TONE: Record<string, string> = { PUBLISHED: 'badge-green', DRAFT: 'badge-accent', ARCHIVED: 'badge-muted' }

export function AdminExamsPage() {
  const [rows, setRows] = useState<AdminExamRow[] | null>(null)
  useEffect(() => { fetchAdminExams().then(setRows) }, [])

  return (
    <AdminLayout>
      <h1 className="mb-6 font-serif text-2xl font-semibold text-ink">Exams</h1>
      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table">
            <thead><tr><th>Title</th><th>Category</th><th>Duration</th><th>Navigation</th><th>Sections</th><th>Attempts</th><th>Avg score</th><th>Status</th></tr></thead>
            <tbody>
              {rows?.map((e) => (
                <tr key={e.id}>
                  <td className="font-medium">{e.title}</td>
                  <td><span className="badge badge-accent">{e.categoryCode}</span></td>
                  <td className="whitespace-nowrap text-ink-soft">{e.durationMinutes} min</td>
                  <td className="whitespace-nowrap text-xs text-muted">{e.navigationMode.replace('_', ' ')}</td>
                  <td className="text-ink-soft">{e.sections} sec · {e.totalQuestions} q</td>
                  <td className="text-ink-soft">{e.attempts}</td>
                  <td className="text-ink-soft">{e.avgScore != null ? e.avgScore.toFixed(2) : '—'}</td>
                  <td><span className={`badge ${STATUS_TONE[e.status] ?? 'badge-muted'}`}>{e.status}</span></td>
                </tr>
              ))}
              {rows?.length === 0 && <tr><td colSpan={8} className="py-8 text-center text-muted">No exams yet.</td></tr>}
              {rows === null && <tr><td colSpan={8} className="py-8 text-center text-muted">Loading…</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </AdminLayout>
  )
}
