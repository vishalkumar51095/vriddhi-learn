import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { fetchCategories, type CategoryResponse } from '../api/catalog'
import { fetchLessons, type LessonSummary } from '../api/learning'

export function LearnPage() {
  const [lessons, setLessons] = useState<LessonSummary[]>([])
  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [categoryCode, setCategoryCode] = useState('')

  useEffect(() => {
    Promise.all([fetchLessons(), fetchCategories()])
      .then(([loadedLessons, loadedCategories]) => {
        setLessons(loadedLessons)
        setCategories(loadedCategories)
        const first = loadedLessons[0]?.categoryCode ?? ''
        setCategoryCode(first)
      })
      .catch(() => setError('Could not load the study notes. Please try again.'))
      .finally(() => setLoading(false))
  }, [])

  const categoryName = (code: string) => categories.find((c) => c.code === code)?.name ?? code
  const availableCodes = useMemo(() => Array.from(new Set(lessons.map((l) => l.categoryCode))), [lessons])
  const visible = useMemo(() => lessons.filter((l) => l.categoryCode === categoryCode), [lessons, categoryCode])
  const bySubject = useMemo(() => {
    const groups = new Map<string, LessonSummary[]>()
    for (const lesson of visible) {
      groups.set(lesson.subjectName, [...(groups.get(lesson.subjectName) ?? []), lesson])
    }
    return Array.from(groups.entries())
  }, [visible])
  const doneCount = visible.filter((l) => l.completed).length

  return (
    <Layout>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="font-serif text-2xl font-semibold">Study notes</h1>
          {visible.length > 0 && (
            <p className="mt-1 text-sm text-ink-soft">{doneCount} of {visible.length} lessons completed</p>
          )}
        </div>
        {availableCodes.length > 1 && (
          <select
            value={categoryCode}
            onChange={(e) => setCategoryCode(e.target.value)}
            className="rounded border border-line bg-bg-raised px-3 py-2 text-sm"
          >
            {availableCodes.map((code) => (
              <option key={code} value={code}>{categoryName(code)}</option>
            ))}
          </select>
        )}
      </div>

      {loading && <p className="text-sm text-ink-soft">Loading…</p>}
      {error && <p className="mb-4 rounded bg-red-soft px-4 py-3 text-sm text-red">{error}</p>}
      {!loading && !error && lessons.length === 0 && (
        <p className="rounded border border-line bg-bg-raised px-4 py-3 text-sm text-ink-soft">No study notes have been published yet.</p>
      )}

      <div className="space-y-8">
        {bySubject.map(([subject, items]) => (
          <section key={subject}>
            <h2 className="mb-3 font-serif text-lg font-semibold">{subject}</h2>
            <div className="space-y-2">
              {items.map((lesson) => (
                <Link
                  key={lesson.id}
                  to={`/learn/${lesson.slug}`}
                  className="flex items-center justify-between gap-3 rounded border border-line bg-bg-raised px-4 py-3 text-sm hover:border-amber"
                >
                  <span className="font-medium">{lesson.title}</span>
                  <span className={`flex-shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold ${lesson.completed ? 'bg-green-soft text-green' : 'bg-amber-soft text-amber'}`}>
                    {lesson.completed ? 'Completed' : 'Read'}
                  </span>
                </Link>
              ))}
            </div>
          </section>
        ))}
      </div>
    </Layout>
  )
}
