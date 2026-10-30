import { useCallback, useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  clearAnswer,
  fetchPalette,
  fetchQuestion,
  markForReview,
  submitAnswer,
  submitAttempt,
} from '../api/attempts'
import type { AttemptQuestionDetailResponse, PaletteItem } from '../api/types'

const STATUS_STYLES: Record<string, string> = {
  ANSWERED: 'bg-solid-green text-white border-solid-green',
  MARKED: 'bg-solid-amber text-white border-solid-amber',
  ANSWERED_MARKED: 'bg-solid-purple text-white border-solid-purple',
  NOT_ANSWERED: 'bg-red-soft text-red border-red',
  NOT_VISITED: 'bg-bg text-ink-soft border-line',
}

function formatTime(totalSeconds: number): string {
  const m = Math.floor(totalSeconds / 60).toString().padStart(2, '0')
  const s = Math.floor(totalSeconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

export function ExamAttemptPage() {
  const { attemptId } = useParams<{ attemptId: string }>()
  const navigate = useNavigate()
  const location = useLocation() as { state?: { navigationMode?: string } }
  const navigationMode = location.state?.navigationMode ?? 'FREE'

  const [items, setItems] = useState<PaletteItem[]>([])
  const [currentIndex, setCurrentIndex] = useState(0)
  const [question, setQuestion] = useState<AttemptQuestionDetailResponse | null>(null)
  const [secondsRemaining, setSecondsRemaining] = useState(0)
  const [selected, setSelected] = useState<string[]>([])
  const [freeText, setFreeText] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const submittedRef = useRef(false)

  const loadPalette = useCallback(async () => {
    if (!attemptId) return
    const palette = await fetchPalette(attemptId)
    setItems(palette.items)
    setSecondsRemaining(palette.secondsRemaining)
    return palette
  }, [attemptId])

  const loadQuestion = useCallback(
    async (attemptQuestionId: string) => {
      if (!attemptId) return
      const detail = await fetchQuestion(attemptId, attemptQuestionId)
      setQuestion(detail)
      setSelected(detail.selectedOptionIds)
      setFreeText(detail.freeTextAnswer ?? '')
      setSecondsRemaining(detail.secondsRemaining)
    },
    [attemptId],
  )

  // Initial load
  useEffect(() => {
    loadPalette().then((palette) => {
      if (palette && palette.items.length > 0) {
        loadQuestion(palette.items[0].attemptQuestionId)
      }
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleSubmitAttempt = useCallback(async () => {
    if (!attemptId || submittedRef.current) return
    submittedRef.current = true
    setSubmitting(true)
    try {
      await submitAttempt(attemptId)
      navigate(`/attempts/${attemptId}/result`)
    } catch {
      setError('Could not submit. Please try again.')
      submittedRef.current = false
      setSubmitting(false)
    }
  }, [attemptId, navigate])

  // Local countdown — display only; the server's expires_at is authoritative
  // and re-syncs every time we fetch a question or the palette.
  useEffect(() => {
    if (secondsRemaining <= 0) return
    const interval = setInterval(() => {
      setSecondsRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(interval)
          handleSubmitAttempt()
          return 0
        }
        return prev - 1
      })
    }, 1000)
    return () => clearInterval(interval)
  }, [secondsRemaining > 0, handleSubmitAttempt]) // eslint-disable-line react-hooks/exhaustive-deps

  async function goTo(index: number) {
    if (index < 0 || index >= items.length) return
    setError(null)
    try {
      await loadQuestion(items[index].attemptQuestionId)
      setCurrentIndex(index)
    } catch {
      setError('This question is not available right now.')
    }
  }

  async function handleSave(andAdvance: boolean) {
    if (!attemptId || !question) return
    setError(null)
    try {
      const isFreeText = question.questionType === 'FILL_BLANK' || question.questionType === 'CODING' || question.questionType === 'SUBJECTIVE'

      await submitAnswer(attemptId, question.attemptQuestionId, {
        selectedOptionIds: isFreeText ? undefined : selected,
        freeTextAnswer: isFreeText ? freeText : undefined,
      })
      await loadPalette()
      if (andAdvance) {
        if (currentIndex === items.length - 1) {
          await handleSubmitAttempt()
        } else {
          await goTo(currentIndex + 1)
        }
      }
    } catch {
      setError('Could not save your answer. Please try again.')
    }
  }

  async function handleClear() {
    if (!attemptId || !question) return
    await clearAnswer(attemptId, question.attemptQuestionId)
    setSelected([])
    setFreeText('')
    await loadPalette()
  }

  async function handleToggleMark() {
    if (!attemptId || !question) return
    const isMarked = question.status === 'MARKED' || question.status === 'ANSWERED_MARKED'
    await markForReview(attemptId, question.attemptQuestionId, !isMarked)
    await loadQuestion(question.attemptQuestionId)
    await loadPalette()
  }

  function toggleOption(optionId: string) {
    if (!question) return
    const isMulti = question.questionType === 'MULTIPLE_CORRECT'
    if (isMulti) {
      setSelected((prev) => (prev.includes(optionId) ? prev.filter((id) => id !== optionId) : [...prev, optionId]))
    } else {
      setSelected([optionId])
    }
  }

  if (!question) {
    return <div className="flex min-h-screen items-center justify-center text-muted">Loading exam…</div>
  }

  const isLast = currentIndex === items.length - 1
  const prevDisabled = currentIndex === 0 || navigationMode === 'FORWARD_ONLY'

  return (
    <div className="flex min-h-screen flex-col bg-bg">
      <div className="flex items-center justify-between border-b border-line bg-bg-raised px-6 py-3">
        <div className="text-sm font-bold">
          {question.sectionName} <span className="ml-2 rounded-full border border-line px-2 py-0.5 text-xs font-normal text-muted">{navigationMode.replace('_', ' ')}</span>
        </div>
        <div
          className={`rounded border px-3 py-1.5 font-mono text-sm font-bold ${secondsRemaining <= 30 ? 'border-red text-red' : 'border-amber text-amber'}`}
        >
          {formatTime(secondsRemaining)}
        </div>
      </div>

      <div className="grid flex-1 grid-cols-1 overflow-hidden md:grid-cols-[1fr_300px]">
        <div className="overflow-y-auto p-8">
          {error && <p className="mb-4 rounded bg-red-soft px-4 py-3 text-sm text-red">{error}</p>}

          <div className="mb-4 flex gap-2 text-xs">
            <span className="rounded-full bg-amber-soft px-2.5 py-1 font-semibold text-amber">
              Question {currentIndex + 1} of {items.length}
            </span>
            <span className="rounded-full bg-purple-soft px-2.5 py-1 font-semibold text-purple">
              {question.questionType.replace('_', ' ')}
            </span>
          </div>

          <p className="mb-6 max-w-2xl whitespace-pre-line text-lg leading-relaxed">{question.questionText}</p>

          {question.codeSnippet && (
            <pre className="mb-6 max-w-2xl overflow-x-auto rounded border border-line bg-bg-raised p-4 font-mono text-sm">
              {question.codeSnippet}
            </pre>
          )}

          {(question.questionType === 'SINGLE_CORRECT' ||
            question.questionType === 'MULTIPLE_CORRECT' ||
            question.questionType === 'TRUE_FALSE') && (
            <div className="max-w-2xl space-y-2">
              {question.options.map((opt, i) => (
                <div
                  key={opt.id}
                  onClick={() => toggleOption(opt.id)}
                  className={`flex cursor-pointer items-center gap-3 rounded border px-4 py-3 text-sm ${
                    selected.includes(opt.id) ? 'border-ink bg-amber-soft' : 'border-line hover:border-amber'
                  }`}
                >
                  <span
                    className={`flex h-5 w-5 flex-shrink-0 items-center justify-center border text-xs ${
                      question.questionType === 'MULTIPLE_CORRECT' ? 'rounded' : 'rounded-full'
                    } border-line`}
                  >
                    {question.questionType === 'MULTIPLE_CORRECT'
                      ? selected.includes(opt.id) && '✓'
                      : String.fromCharCode(65 + i)}
                  </span>
                  {opt.text}
                </div>
              ))}
            </div>
          )}

          {(question.questionType === 'FILL_BLANK' || question.questionType === 'CODING' || question.questionType === 'SUBJECTIVE') && (
            <div className="max-w-2xl">
              {question.questionType === 'CODING' ? (
                <textarea
                  value={freeText}
                  onChange={(e) => setFreeText(e.target.value)}
                  placeholder="Write your solution here"
                  className="min-h-[160px] w-full rounded border border-line bg-bg-raised p-4 font-mono text-sm"
                />
              ) : (
                <input
                  type="text"
                  value={freeText}
                  onChange={(e) => setFreeText(e.target.value)}
                  placeholder="Type your answer"
                  className="w-full rounded border border-line bg-bg-raised px-4 py-3 text-sm"
                />
              )}
              <p className="mt-2 text-xs text-muted">This answer is manually graded by a reviewer.</p>
            </div>
          )}

          <div className="mt-8 flex flex-wrap gap-3">
            <button onClick={handleClear} className="rounded border border-line px-4 py-2 text-sm font-semibold">
              Clear answer
            </button>
            <button onClick={handleToggleMark} className="rounded border border-line px-4 py-2 text-sm font-semibold">
              {question.status === 'MARKED' || question.status === 'ANSWERED_MARKED' ? 'Unmark review' : 'Mark for review'}
            </button>
            <button
              onClick={() => goTo(currentIndex - 1)}
              disabled={prevDisabled}
              className="rounded border border-line px-4 py-2 text-sm font-semibold disabled:opacity-40"
            >
              Previous
            </button>
            <button
              onClick={() => handleSave(true)}
              disabled={submitting}
              className="rounded bg-ink px-4 py-2 text-sm font-semibold text-bg disabled:opacity-50"
            >
              {isLast ? 'Save & Finish' : 'Save & Next'}
            </button>
          </div>
        </div>

        <aside className="hidden overflow-y-auto border-l border-line bg-bg-raised p-5 md:block">
          <h3 className="mb-3 text-sm font-semibold">Question palette</h3>
          <div className="mb-4 grid grid-cols-5 gap-2">
            {items.map((item, i) => (
              <button
                key={item.attemptQuestionId}
                onClick={() => goTo(i)}
                className={`flex aspect-square items-center justify-center rounded border text-xs font-semibold ${STATUS_STYLES[item.status]} ${
                  i === currentIndex ? 'ring-2 ring-ink ring-offset-1' : ''
                }`}
              >
                {item.sortOrder + 1}
              </button>
            ))}
          </div>
          <div className="mb-5 space-y-1.5 text-xs text-muted">
            <div className="flex items-center gap-2"><span className="h-2.5 w-2.5 rounded-sm bg-solid-green" />Answered</div>
            <div className="flex items-center gap-2"><span className="h-2.5 w-2.5 rounded-sm bg-solid-amber" />Marked for review</div>
            <div className="flex items-center gap-2"><span className="h-2.5 w-2.5 rounded-sm bg-solid-purple" />Answered &amp; marked</div>
            <div className="flex items-center gap-2"><span className="h-2.5 w-2.5 rounded-sm border border-red bg-red-soft" />Not answered</div>
            <div className="flex items-center gap-2"><span className="h-2.5 w-2.5 rounded-sm border border-line bg-bg" />Not visited</div>
          </div>
          <button
            onClick={handleSubmitAttempt}
            disabled={submitting}
            className="w-full rounded bg-ink py-2.5 text-sm font-semibold text-bg disabled:opacity-50"
          >
            {submitting ? 'Submitting…' : 'Submit exam'}
          </button>
        </aside>
      </div>
    </div>
  )
}
