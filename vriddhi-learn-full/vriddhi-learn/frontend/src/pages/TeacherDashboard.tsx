import { useEffect, useState } from 'react'
import { Layout } from '../components/Layout'
import { useAuth } from '../context/AuthContext'
import { fetchMyQuestions, fetchMyQuestionsSummary, type TeacherSummary } from '../api/admin'
import type { QuestionResponse } from '../api/types'

const STATUS_TONE: Record<string, string> = { PUBLISHED: 'badge-green', PENDING_REVIEW: 'badge-accent', DRAFT: 'badge-muted', REJECTED: 'badge-red', ARCHIVED: 'badge-muted' }

export function TeacherDashboardPage() {
  const { user } = useAuth()
  const [summary, setSummary] = useState<TeacherSummary | null>(null)
  const [questions, setQuestions] = useState<QuestionResponse[]>([])

  useEffect(() => {
    fetchMyQuestionsSummary().then(setSummary).catch(() => {})
    fetchMyQuestions({ size: 10 }).then((r) => setQuestions(r.content)).catch(() => {})
  }, [])

  return (
    <Layout>
      <h1 className="mb-6 font-serif text-2xl font-semibold">Welcome, {user?.fullName?.split(' ')[0]}</h1>

      <div className="mb-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <div className="card p-4"><div className="font-serif text-2xl font-semibold text-ink">{summary?.total ?? '—'}</div><div className="text-xs text-muted">My questions</div></div>
        <div className="card border-l-[3px] border-l-solid-green p-4"><div className="font-serif text-2xl font-semibold text-ink">{summary?.byStatus?.PUBLISHED ?? 0}</div><div className="text-xs text-muted">Published</div></div>
        <div className="card border-l-[3px] border-l-solid-amber p-4"><div className="font-serif text-2xl font-semibold text-ink">{summary?.byStatus?.PENDING_REVIEW ?? 0}</div><div className="text-xs text-muted">Pending review</div></div>
        <div className="card p-4"><div className="font-serif text-2xl font-semibold text-ink">{summary?.byStatus?.DRAFT ?? 0}</div><div className="text-xs text-muted">Drafts</div></div>
      </div>

      <div className="card overflow-hidden">
        <div className="border-b border-line px-5 py-4"><h2 className="text-sm font-semibold text-ink">My recent questions</h2></div>
        <div className="overflow-x-auto">
          <table className="table">
            <thead><tr><th>Question</th><th>Subject / Topic</th><th>Type</th><th>Difficulty</th><th>Status</th></tr></thead>
            <tbody>
              {questions.map((q) => (
                <tr key={q.id}>
                  <td className="max-w-sm truncate">{q.questionText}</td>
                  <td className="whitespace-nowrap text-xs text-muted">{q.subjectName} · {q.topicName}</td>
                  <td className="whitespace-nowrap text-xs text-muted">{q.type.replace('_', ' ')}</td>
                  <td className="whitespace-nowrap text-xs text-muted">{q.difficulty}</td>
                  <td><span className={`badge ${STATUS_TONE[q.status] ?? 'badge-muted'}`}>{q.status.replace('_', ' ')}</span></td>
                </tr>
              ))}
              {questions.length === 0 && <tr><td colSpan={5} className="py-8 text-center text-muted">
                No questions yet — create one via POST /api/questions. A dedicated question editor screen isn't built yet.
              </td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </Layout>
  )
}
