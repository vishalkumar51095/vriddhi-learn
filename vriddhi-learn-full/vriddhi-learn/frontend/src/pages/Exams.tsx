import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { fetchExams } from '../api/exams'
import { startAttempt } from '../api/attempts'
import type { ExamSummaryResponse } from '../api/types'

export function ExamsPage() {
  const navigate = useNavigate()
  const [exams, setExams] = useState<ExamSummaryResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [startingId, setStartingId] = useState<string | null>(null)

  useEffect(() => {
    fetchExams()
      .then((res) => setExams(res.content))
      .catch(() => setError('Could not load exams. Is the API gateway running?'))
      .finally(() => setLoading(false))
  }, [])

  async function handleStart(examId: string) {
    setStartingId(examId)
    try {
      const attempt = await startAttempt(examId)
      navigate(`/attempts/${attempt.attemptId}`, { state: { navigationMode: attempt.navigationMode } })
    } catch {
      setError('Could not start this exam. Please try again.')
      setStartingId(null)
    }
  }

  return (
    <Layout>
      <h1 className="mb-6 font-serif text-2xl font-semibold">Mock tests</h1>

      {error && <p className="mb-4 rounded bg-red-soft px-4 py-3 text-sm text-red">{error}</p>}
      {loading && <p className="text-sm text-muted">Loading exams…</p>}

      {!loading && exams.length === 0 && !error && (
        <p className="text-sm text-muted">
          No published exams yet — create one via the exam-service API (POST /api/exams, then POST
          /api/exams/{'{id}'}/publish) to see it listed here.
        </p>
      )}

      <div className="divide-y divide-line overflow-hidden rounded border border-line bg-bg-raised">
        {exams.map((exam) => (
          <div key={exam.id} className="flex items-center justify-between px-6 py-4">
            <div>
              <h3 className="font-semibold">{exam.title}</h3>
              <p className="text-sm text-muted">
                {exam.durationMinutes} min · {exam.sectionCount} sections · {exam.totalQuestions} questions
              </p>
            </div>
            <button
              onClick={() => handleStart(exam.id)}
              disabled={startingId === exam.id}
              className="rounded bg-ink px-4 py-2 text-sm font-semibold text-bg disabled:opacity-50"
            >
              {startingId === exam.id ? 'Starting…' : 'Start'}
            </button>
          </div>
        ))}
      </div>
    </Layout>
  )
}
