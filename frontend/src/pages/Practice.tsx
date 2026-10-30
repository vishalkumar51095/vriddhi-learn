import { useEffect, useState } from 'react'
import { Layout } from '../components/Layout'
import {
  checkPracticeAnswer,
  fetchCategories,
  fetchRandomQuestion,
  fetchSubjects,
  fetchTopics,
  type CategoryResponse,
  type PracticeCheckResponse,
} from '../api/catalog'
import type { QuestionResponse, SubjectResponse, TopicResponse } from '../api/types'

export function PracticePage() {
  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [subjects, setSubjects] = useState<SubjectResponse[]>([])
  const [topics, setTopics] = useState<TopicResponse[]>([])

  const [categoryCode, setCategoryCode] = useState('')
  const [subjectId, setSubjectId] = useState('')
  const [topicId, setTopicId] = useState('')

  const [question, setQuestion] = useState<QuestionResponse | null>(null)
  const [selected, setSelected] = useState<string[]>([])
  const [freeText, setFreeText] = useState('')
  const [result, setResult] = useState<PracticeCheckResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchCategories().then(setCategories).catch(() => setError('Could not load categories.'))
  }, [])

  useEffect(() => {
    if (!categoryCode) {
      setSubjects([])
      setSubjectId('')
      return
    }
    fetchSubjects(categoryCode).then(setSubjects).catch(() => setError('Could not load subjects.'))
  }, [categoryCode])

  useEffect(() => {
    if (!subjectId) {
      setTopics([])
      setTopicId('')
      return
    }
    fetchTopics(subjectId).then(setTopics).catch(() => setError('Could not load topics.'))
  }, [subjectId])

  async function handleFetchQuestion() {
    if (!topicId) return
    setLoading(true)
    setError(null)
    setResult(null)
    setSelected([])
    setFreeText('')
    try {
      const q = await fetchRandomQuestion([topicId])
      if (!q) {
        setQuestion(null)
        setError('No published questions in this topic yet.')
      } else {
        setQuestion(q)
      }
    } catch {
      setError('Could not fetch a question. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  function toggleOption(optionId: string) {
    if (!question) return
    if (question.type === 'MULTIPLE_CORRECT') {
      setSelected((prev) => (prev.includes(optionId) ? prev.filter((id) => id !== optionId) : [...prev, optionId]))
    } else {
      setSelected([optionId])
    }
  }

  async function handleCheck() {
    if (!question) return
    const isFreeText = question.type === 'FILL_BLANK' || question.type === 'CODING' || question.type === 'SUBJECTIVE'
    const res = await checkPracticeAnswer(question.id, {
      selectedOptionIds: isFreeText ? undefined : selected,
      freeTextAnswer: isFreeText ? freeText : undefined,
    })
    setResult(res)
  }

  const isFreeText = question && (question.type === 'FILL_BLANK' || question.type === 'CODING' || question.type === 'SUBJECTIVE')

  return (
    <Layout>
      <h1 className="mb-6 font-serif text-2xl font-semibold">Practice</h1>

      <div className="mb-6 grid gap-3 sm:grid-cols-3">
        <select
          value={categoryCode}
          onChange={(e) => setCategoryCode(e.target.value)}
          className="rounded border border-line bg-bg-raised px-3 py-2 text-sm"
        >
          <option value="">Select category…</option>
          {categories.map((c) => (
            <option key={c.id} value={c.code}>{c.name}</option>
          ))}
        </select>
        <select
          value={subjectId}
          onChange={(e) => setSubjectId(e.target.value)}
          disabled={!subjects.length}
          className="rounded border border-line bg-bg-raised px-3 py-2 text-sm disabled:opacity-50"
        >
          <option value="">Select subject…</option>
          {subjects.map((s) => (
            <option key={s.id} value={s.id}>{s.name}</option>
          ))}
        </select>
        <select
          value={topicId}
          onChange={(e) => setTopicId(e.target.value)}
          disabled={!topics.length}
          className="rounded border border-line bg-bg-raised px-3 py-2 text-sm disabled:opacity-50"
        >
          <option value="">Select topic…</option>
          {topics.map((t) => (
            <option key={t.id} value={t.id}>{t.name}</option>
          ))}
        </select>
      </div>

      <button
        onClick={handleFetchQuestion}
        disabled={!topicId || loading}
        className="mb-6 rounded bg-ink px-5 py-2.5 text-sm font-semibold text-bg disabled:opacity-50"
      >
        {loading ? 'Fetching…' : question ? 'Next question' : 'Get a question'}
      </button>

      {error && <p className="mb-4 rounded bg-red-soft px-4 py-3 text-sm text-red">{error}</p>}

      {question && (
        <div className="rounded border border-line bg-bg-raised p-6">
          <div className="mb-4 flex gap-2 text-xs">
            <span className="rounded-full bg-amber-soft px-2.5 py-1 font-semibold text-amber">
              {question.subjectName} · {question.topicName}
            </span>
            <span className="rounded-full bg-green-soft px-2.5 py-1 font-semibold text-green">{question.difficulty}</span>
          </div>

          <p className="mb-5 whitespace-pre-line text-base leading-relaxed">{question.questionText}</p>

          {question.codeSnippet && (
            <pre className="mb-5 overflow-x-auto rounded border border-line bg-bg p-4 font-mono text-sm">{question.codeSnippet}</pre>
          )}

          {!isFreeText && (
            <div className="mb-5 space-y-2">
              {question.options.map((opt, i) => {
                const isCorrectReveal = result && !result.manualReview && result.correctOptionIds.includes(opt.id)
                const isWrongPick = result && !result.manualReview && selected.includes(opt.id) && !result.correctOptionIds.includes(opt.id)
                return (
                  <div
                    key={opt.id}
                    onClick={() => !result && toggleOption(opt.id)}
                    className={`flex items-center gap-3 rounded border px-4 py-3 text-sm ${result ? '' : 'cursor-pointer hover:border-amber'} ${
                      isCorrectReveal ? 'border-green bg-green-soft' : isWrongPick ? 'border-red bg-red-soft' : selected.includes(opt.id) ? 'border-ink bg-amber-soft' : 'border-line'
                    }`}
                  >
                    <span className="flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full border border-line text-xs">
                      {String.fromCharCode(65 + i)}
                    </span>
                    {opt.text}
                  </div>
                )
              })}
            </div>
          )}

          {isFreeText && (
            <div className="mb-5">
              {question.type === 'CODING' ? (
                <textarea
                  value={freeText}
                  onChange={(e) => setFreeText(e.target.value)}
                  disabled={!!result}
                  className="min-h-[140px] w-full rounded border border-line bg-bg p-4 font-mono text-sm"
                />
              ) : (
                <input
                  type="text"
                  value={freeText}
                  onChange={(e) => setFreeText(e.target.value)}
                  disabled={!!result}
                  placeholder="Type your answer"
                  className="w-full rounded border border-line bg-bg px-4 py-3 text-sm"
                />
              )}
            </div>
          )}

          {!result ? (
            <button
              onClick={handleCheck}
              disabled={selected.length === 0 && !freeText}
              className="rounded bg-ink px-5 py-2.5 text-sm font-semibold text-bg disabled:opacity-50"
            >
              Check answer
            </button>
          ) : (
            <div className={`rounded px-4 py-3 text-sm ${result.manualReview ? 'bg-amber-soft text-amber' : result.correct ? 'bg-green-soft text-green' : 'bg-red-soft text-red'}`}>
              <p className="mb-1 font-semibold">
                {result.manualReview ? 'Submitted for manual review' : result.correct ? 'Correct!' : 'Not quite.'}
              </p>
              {question.explanation && <p className="text-ink-soft">{question.explanation}</p>}
            </div>
          )}
        </div>
      )}
    </Layout>
  )
}
