import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { fetchProgressSummary, type ProgressSummary } from '../api/progress'

const pct = (value: number) => `${Math.round(value * 100)}%`
const WEAK_BELOW = 0.6
const WEAK_MIN_ATTEMPTS = 3

export function ProgressPage() {
  const [summary, setSummary] = useState<ProgressSummary | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchProgressSummary().then(setSummary).catch(() => setError('Could not load your progress. Please try again.'))
  }, [])

  return (
    <Layout>
      <h1 className="mb-6 font-serif text-2xl font-semibold">Your progress</h1>

      {error && <p className="mb-4 rounded bg-red-soft px-4 py-3 text-sm text-red">{error}</p>}
      {!summary && !error && <p className="text-sm text-ink-soft">Loading…</p>}

      {summary && (
        <>
          <div className="mb-8 grid grid-cols-2 gap-3 sm:grid-cols-4">
            <Stat label="Accuracy" value={summary.totalAttempts ? pct(summary.accuracy) : '–'} />
            <Stat label="Day streak" value={String(summary.streakDays)} />
            <Stat label="Answered today" value={String(summary.answeredToday)} />
            <Stat label="Due for revision" value={String(summary.dueForReview)} />
          </div>

          {summary.totalAttempts === 0 ? (
            <div className="rounded border border-line bg-bg-raised p-6 text-sm">
              <p className="mb-3 text-ink-soft">You have not answered any practice questions yet.</p>
              <Link to="/daily" className="rounded bg-ink px-5 py-2.5 font-semibold text-bg">Start daily practice</Link>
            </div>
          ) : (
            <section>
              <h2 className="mb-3 font-serif text-lg font-semibold">Topics (weakest first)</h2>
              <div className="space-y-2">
                {summary.topics.map((t) => {
                  const weak = t.attempts >= WEAK_MIN_ATTEMPTS && t.accuracy < WEAK_BELOW
                  return (
                    <div key={t.topicId} className="rounded border border-line bg-bg-raised px-4 py-3 text-sm">
                      <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
                        <span>
                          <span className="font-medium">{t.topicName}</span>
                          <span className="text-ink-soft"> · {t.subjectName}</span>
                        </span>
                        <span className="flex items-center gap-2 text-xs">
                          {weak && <span className="rounded-full bg-red-soft px-2.5 py-1 font-semibold text-red">Needs work</span>}
                          <span className="text-ink-soft">{t.correct}/{t.attempts} correct · {pct(t.accuracy)}</span>
                        </span>
                      </div>
                      <div className="h-2 overflow-hidden rounded-full bg-bg">
                        <div className={`h-full ${weak ? 'bg-red' : 'bg-green'}`} style={{ width: pct(t.accuracy) }} />
                      </div>
                    </div>
                  )
                })}
              </div>
            </section>
          )}
        </>
      )}
    </Layout>
  )
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded border border-line bg-bg-raised px-4 py-4">
      <p className="text-xs uppercase tracking-wide text-ink-soft">{label}</p>
      <p className="mt-1 font-serif text-2xl font-semibold">{value}</p>
    </div>
  )
}
