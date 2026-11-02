import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { PracticeSession } from '../components/PracticeSession'
import { fetchCategories, fetchPracticeQuestion, fetchRandomQuestions, type CategoryResponse } from '../api/catalog'
import { fetchDailyPlan, type DailyPlan, type DailyPlanReason } from '../api/progress'
import type { QuestionResponse } from '../api/types'

const REASON_LABEL: Record<DailyPlanReason, string> = {
  DUE_FOR_REVIEW: 'Due for revision',
  WEAK_TOPIC: 'Needs work',
  NEW_TOPIC: 'New topic',
  MIXED_REVIEW: 'Mixed review',
}

const QUICK_PRACTICE_COUNT = 5

export function DailyPage() {
  const [params] = useSearchParams()
  const topicId = params.get('topicId')
  const topicName = params.get('topicName') ?? 'this topic'

  return (
    <Layout>
      {topicId ? <TopicPractice topicId={topicId} topicName={topicName} /> : <DailyPractice />}
    </Layout>
  )
}

function TopicPractice({ topicId, topicName }: { topicId: string; topicName: string }) {
  const [started, setStarted] = useState(false)
  const loadQuestions = useCallback(() => fetchRandomQuestions([topicId], QUICK_PRACTICE_COUNT), [topicId])

  return (
    <>
      <h1 className="mb-2 font-serif text-2xl font-semibold">Quick practice</h1>
      <p className="mb-6 text-sm text-ink-soft">{QUICK_PRACTICE_COUNT} random questions on {topicName}.</p>
      {started
        ? <PracticeSession title="Quick practice" loadQuestions={loadQuestions} />
        : <button onClick={() => setStarted(true)} className="rounded bg-ink px-5 py-2.5 text-sm font-semibold text-bg">Start</button>}
    </>
  )
}

function DailyPractice() {
  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [categoryCode, setCategoryCode] = useState('')
  const [plan, setPlan] = useState<DailyPlan | null>(null)
  const [started, setStarted] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchCategories()
      .then((list) => {
        setCategories(list)
        const preferred = list.find((c) => c.code === 'JAVA_INTERVIEW') ?? list[0]
        setCategoryCode(preferred?.code ?? '')
      })
      .catch(() => setError('Could not load categories.'))
  }, [])

  useEffect(() => {
    if (!categoryCode) return
    setPlan(null)
    setStarted(false)
    setError(null)
    fetchDailyPlan(categoryCode)
      .then(setPlan)
      .catch(() => setError('Could not build today\'s plan. Please try again in a moment.'))
  }, [categoryCode])

  const loadQuestions = useCallback(async (): Promise<QuestionResponse[]> => {
    if (!plan) return []
    const groups = await Promise.all(plan.items.map(async (item): Promise<QuestionResponse[]> => {
      try {
        if (item.kind === 'QUESTION' && item.questionId) return [await fetchPracticeQuestion(item.questionId)]
        return await fetchRandomQuestions([item.topicId], item.count)
      } catch {
        return [] // one unavailable topic or question should not cancel the whole session
      }
    }))
    const seen = new Set<string>()
    return groups.flat().filter((q) => (seen.has(q.id) ? false : (seen.add(q.id), true)))
  }, [plan])

  return (
    <>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="font-serif text-2xl font-semibold">Daily practice</h1>
          <p className="mt-1 text-sm text-ink-soft">
            Today's set mixes questions due for revision, your weaker topics and topics you have not tried yet.
          </p>
        </div>
        {categories.length > 1 && !started && (
          <select
            value={categoryCode}
            onChange={(e) => setCategoryCode(e.target.value)}
            className="rounded border border-line bg-bg-raised px-3 py-2 text-sm"
          >
            {categories.map((c) => <option key={c.id} value={c.code}>{c.name}</option>)}
          </select>
        )}
      </div>

      {error && <p className="mb-4 rounded bg-red-soft px-4 py-3 text-sm text-red">{error}</p>}
      {!plan && !error && <p className="text-sm text-ink-soft">Building today's plan…</p>}

      {plan && plan.items.length === 0 && (
        <p className="rounded border border-line bg-bg-raised px-4 py-3 text-sm text-ink-soft">There are no published questions in this category yet.</p>
      )}

      {plan && plan.items.length > 0 && !started && (
        <div className="rounded border border-line bg-bg-raised p-6">
          <p className="mb-4 text-sm text-ink-soft">{plan.totalQuestions} questions planned for {plan.date}</p>
          <ul className="mb-6 space-y-2 text-sm">
            {plan.items.map((item, i) => (
              <li key={`${item.topicId}-${i}`} className="flex flex-wrap items-center justify-between gap-2 rounded border border-line px-4 py-2.5">
                <span>
                  <span className="font-medium">{item.topicName}</span>
                  <span className="text-ink-soft"> · {item.subjectName}</span>
                </span>
                <span className="flex items-center gap-2 text-xs">
                  <span className="text-ink-soft">{item.count} {item.count === 1 ? 'question' : 'questions'}</span>
                  <span className="rounded-full bg-amber-soft px-2.5 py-1 font-semibold text-amber">{REASON_LABEL[item.reason]}</span>
                </span>
              </li>
            ))}
          </ul>
          <button onClick={() => setStarted(true)} className="rounded bg-ink px-5 py-2.5 text-sm font-semibold text-bg">
            Start today's practice
          </button>
        </div>
      )}

      {plan && started && <PracticeSession title="Daily practice" loadQuestions={loadQuestions} />}
    </>
  )
}
