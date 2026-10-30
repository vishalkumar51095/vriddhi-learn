import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { fetchResult } from '../api/attempts'
import type { AttemptResultResponse } from '../api/types'

export function ResultsPage() {
  const { attemptId } = useParams<{ attemptId: string }>()
  const [result, setResult] = useState<AttemptResultResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!attemptId) return
    fetchResult(attemptId)
      .then(setResult)
      .catch(() => setError('Could not load results for this attempt.'))
  }, [attemptId])

  if (error) {
    return (
      <Layout>
        <p className="rounded bg-red-soft px-4 py-3 text-sm text-red">{error}</p>
      </Layout>
    )
  }
  if (!result) {
    return (
      <Layout>
        <p className="text-sm text-muted">Loading results…</p>
      </Layout>
    )
  }

  const accuracy = result.correctCount + result.wrongCount > 0
    ? Math.round((result.correctCount / (result.correctCount + result.wrongCount)) * 100)
    : 0

  return (
    <Layout>
      <div className="mb-10 text-center">
        <div className="font-serif text-5xl font-bold">{result.score}</div>
        <p className="text-muted">{result.status === 'AUTO_SUBMITTED' ? 'Auto-submitted — time expired' : 'Submitted'}</p>
      </div>

      <div className="mb-8 grid grid-cols-2 gap-4 sm:grid-cols-5">
        {[
          { v: `${accuracy}%`, l: 'Accuracy (auto-graded)' },
          { v: result.correctCount, l: 'Correct' },
          { v: result.wrongCount, l: 'Incorrect' },
          { v: result.skippedCount, l: 'Not attempted' },
          { v: result.manualPendingCount, l: 'Pending manual review' },
        ].map((stat) => (
          <div key={stat.l} className="rounded border border-line bg-bg-raised p-4 text-center">
            <div className="font-serif text-xl font-semibold">{stat.v}</div>
            <div className="text-xs text-muted">{stat.l}</div>
          </div>
        ))}
      </div>

      <div className="rounded border border-line bg-bg-raised p-6">
        <h2 className="mb-4 font-serif text-lg font-semibold">Section-wise performance</h2>
        {result.sectionResults.map((section) => {
          const pct = section.gradable ? Math.round((section.correct / section.gradable) * 100) : 0
          return (
            <div key={section.sectionName} className="mb-3 grid grid-cols-[130px_1fr_44px] items-center gap-3 text-sm">
              <span>{section.sectionName}</span>
              <div className="h-2 overflow-hidden rounded-full bg-line">
                <div className="h-full rounded-full bg-green" style={{ width: `${pct}%` }} />
              </div>
              <span>{pct}%</span>
            </div>
          )
        })}
      </div>
    </Layout>
  )
}
