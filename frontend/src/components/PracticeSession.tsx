import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { checkPracticeAnswer, type PracticeCheckResponse } from '../api/catalog'
import { recordResult } from '../api/progress'
import type { QuestionResponse } from '../api/types'

interface Props {
  title: string
  /** Loads the questions for this session once. Questions that fail to load should simply be left out. */
  loadQuestions: () => Promise<QuestionResponse[]>
}

const isFreeTextType = (q: QuestionResponse) => q.type === 'FILL_BLANK' || q.type === 'CODING' || q.type === 'SUBJECTIVE'

/**
 * Runs a fixed list of questions one after another. Every checked answer is reported to progress-service
 * (best effort, it never blocks the learner), which feeds the revision queue and tomorrow's daily plan.
 */
export function PracticeSession({ title, loadQuestions }: Props) {
  const [questions, setQuestions] = useState<QuestionResponse[] | null>(null)
  const [index, setIndex] = useState(0)
  const [selected, setSelected] = useState<string[]>([])
  const [freeText, setFreeText] = useState('')
  const [result, setResult] = useState<PracticeCheckResponse | null>(null)
  const [correctCount, setCorrectCount] = useState(0)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    loadQuestions()
      .then((loaded) => { if (!cancelled) setQuestions(loaded) })
      .catch(() => { if (!cancelled) setError('Could not load the questions. Please try again.') })
    return () => { cancelled = true }
  }, [loadQuestions])

  if (error) return <p className="rounded bg-red-soft px-4 py-3 text-sm text-red">{error}</p>
  if (!questions) return <p className="text-sm text-ink-soft">Preparing your questions…</p>
  if (questions.length === 0) {
    return <p className="rounded bg-amber-soft px-4 py-3 text-sm text-amber">There are no published questions for this practice yet.</p>
  }

  if (index >= questions.length) {
    return (
      <div className="rounded border border-line bg-bg-raised p-6">
        <h2 className="mb-2 font-serif text-xl font-semibold">{title} complete</h2>
        <p className="mb-5 text-ink-soft">You answered {correctCount} of {questions.length} correctly.</p>
        <div className="flex flex-wrap gap-3 text-sm">
          <Link to="/progress" className="rounded bg-ink px-5 py-2.5 font-semibold text-bg">See your progress</Link>
          <Link to="/learn" className="rounded border border-line px-5 py-2.5 font-semibold">Review notes</Link>
          <Link to="/dashboard" className="rounded border border-line px-5 py-2.5 font-semibold">Dashboard</Link>
        </div>
      </div>
    )
  }

  const question = questions[index]
  const freeTextQuestion = isFreeTextType(question)

  function toggleOption(optionId: string) {
    if (question.type === 'MULTIPLE_CORRECT') {
      setSelected((prev) => (prev.includes(optionId) ? prev.filter((id) => id !== optionId) : [...prev, optionId]))
    } else {
      setSelected([optionId])
    }
  }

  async function handleCheck() {
    try {
      const res = await checkPracticeAnswer(question.id, {
        selectedOptionIds: freeTextQuestion ? undefined : selected,
        freeTextAnswer: freeTextQuestion ? freeText : undefined,
      })
      setResult(res)
      if (!res.manualReview && res.correct !== null) {
        if (res.correct) setCorrectCount((n) => n + 1)
        recordResult({
          questionId: question.id,
          topicId: question.topicId,
          topicName: question.topicName,
          subjectName: question.subjectName,
          correct: res.correct,
        }).catch(() => { /* progress tracking must never block practice */ })
      }
    } catch {
      setError('Could not check your answer. Please try again.')
    }
  }

  function handleNext() {
    setIndex((i) => i + 1)
    setSelected([])
    setFreeText('')
    setResult(null)
  }

  return (
    <div className="rounded border border-line bg-bg-raised p-6">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-2 text-xs">
        <span className="rounded-full bg-amber-soft px-2.5 py-1 font-semibold text-amber">
          {question.subjectName} · {question.topicName}
        </span>
        <span className="text-ink-soft">Question {index + 1} of {questions.length} · {question.difficulty}</span>
      </div>

      <p className="mb-5 whitespace-pre-line text-base leading-relaxed">{question.questionText}</p>

      {question.codeSnippet && (
        <pre className="mb-5 overflow-x-auto rounded border border-line bg-bg p-4 font-mono text-sm">{question.codeSnippet}</pre>
      )}

      {!freeTextQuestion && (
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

      {freeTextQuestion && (
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
        <>
          <div className={`mb-4 rounded px-4 py-3 text-sm ${result.manualReview ? 'bg-amber-soft text-amber' : result.correct ? 'bg-green-soft text-green' : 'bg-red-soft text-red'}`}>
            <p className="mb-1 font-semibold">
              {result.manualReview ? 'Submitted for manual review' : result.correct ? 'Correct!' : 'Not quite.'}
            </p>
            {question.explanation && <p className="text-ink-soft">{question.explanation}</p>}
          </div>
          <button onClick={handleNext} className="rounded bg-ink px-5 py-2.5 text-sm font-semibold text-bg">
            {index + 1 >= questions.length ? 'Finish' : 'Next question'}
          </button>
        </>
      )}
    </div>
  )
}
