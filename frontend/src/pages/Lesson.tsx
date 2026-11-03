import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { MarkdownView } from '../components/MarkdownView'
import { fetchLesson, fetchLessons, setLessonCompleted, type LessonDetail, type LessonSummary } from '../api/learning'

export function LessonPage() {
  const { slug } = useParams<{ slug: string }>()
  const [lesson, setLesson] = useState<LessonDetail | null>(null)
  const [siblings, setSiblings] = useState<LessonSummary[]>([])
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!slug) return
    let cancelled = false
    setLesson(null)
    setError(null)
    fetchLesson(slug)
      .then((loaded) => {
        if (cancelled) return
        setLesson(loaded)
        window.scrollTo({ top: 0 })
        return fetchLessons({ category: loaded.categoryCode }).then((list) => { if (!cancelled) setSiblings(list) })
      })
      .catch(() => { if (!cancelled) setError('Could not load this lesson.') })
    return () => { cancelled = true }
  }, [slug])

  async function toggleCompleted() {
    if (!lesson) return
    setSaving(true)
    try {
      await setLessonCompleted(lesson.slug, !lesson.completed)
      setLesson({ ...lesson, completed: !lesson.completed })
    } catch {
      setError('Could not update completion. Please try again.')
    } finally {
      setSaving(false)
    }
  }

  const position = lesson ? siblings.findIndex((l) => l.slug === lesson.slug) : -1
  const previous = position > 0 ? siblings[position - 1] : null
  const next = position >= 0 && position < siblings.length - 1 ? siblings[position + 1] : null

  return (
    <Layout>
      <Link to="/learn" className="mb-4 inline-block text-sm text-ink-soft hover:text-ink">← All study notes</Link>

      {error && <p className="mb-4 rounded bg-red-soft px-4 py-3 text-sm text-red">{error}</p>}
      {!lesson && !error && <p className="text-sm text-ink-soft">Loading…</p>}

      {lesson && (
        <article>
          <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-amber">{lesson.subjectName}</p>
          <h1 className="mb-6 font-serif text-3xl font-semibold">{lesson.title}</h1>

          <MarkdownView markdown={lesson.bodyMarkdown} />

          <div className="mt-10 flex flex-wrap items-center gap-3 border-t border-line pt-6 text-sm">
            <button
              onClick={toggleCompleted}
              disabled={saving}
              className={`rounded px-5 py-2.5 font-semibold disabled:opacity-50 ${lesson.completed ? 'bg-green-soft text-green' : 'bg-ink text-bg'}`}
            >
              {lesson.completed ? 'Completed ✓ (click to undo)' : 'Mark as completed'}
            </button>
            {lesson.topicId && (
              <Link
                to={`/daily?topicId=${lesson.topicId}&topicName=${encodeURIComponent(lesson.topicName ?? lesson.title)}`}
                className="rounded border border-line px-5 py-2.5 font-semibold hover:border-amber"
              >
                Practice this topic
              </Link>
            )}
          </div>

          <div className="mt-6 flex justify-between gap-3 text-sm">
            {previous ? <Link to={`/learn/${previous.slug}`} className="text-ink-soft hover:text-ink">← {previous.title}</Link> : <span />}
            {next ? <Link to={`/learn/${next.slug}`} className="text-right text-ink-soft hover:text-ink">{next.title} →</Link> : <span />}
          </div>
        </article>
      )}
    </Layout>
  )
}
