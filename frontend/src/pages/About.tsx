import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const TABS = ['Platform', 'Roles & portals', 'How scoring works', 'FAQ'] as const
type Tab = (typeof TABS)[number]

const FAQS: { q: string; a: string }[] = [
  { q: 'Is the exam timer trustworthy?', a: "The on-screen countdown is a display only. The server records an expires_at deadline when your attempt starts, checks it on every read and write, and auto-submits the moment it passes — even if you just close the tab." },
  { q: 'Can I prepare for more than one exam category?', a: 'Yes. Role is independent of preparation category, so one learner account can practice Java, TCS NQT, SSC CGL and IBPS PO at once.' },
  { q: 'What happens to a question after I answer it in an exam?', a: "Nothing changes it. Starting an attempt snapshots the question text, options and correct answers into the exam service's own database, so a later edit in content-service can never affect a submitted or in-progress attempt." },
  { q: 'Why do fill-in-the-blank and coding questions say "pending review"?', a: "Those two types can't be graded automatically. They're scored by a teacher or reviewer after submission, same in practice mode as in a real exam." },
  { q: "What's the difference between the three navigation modes?", a: 'Free lets you move anywhere at any time. Forward-only lets you move ahead but never back. Section-locked (used by the TCS NQT full mock) requires finishing a section before the next one unlocks — matching how the real test behaves.' },
]

function PaletteDemo() {
  const [statuses, setStatuses] = useState<string[]>(Array.from({ length: 16 }, (_, i) => (i % 4 === 0 ? 'answered' : i % 4 === 1 ? 'marked' : i % 4 === 2 ? 'not-answered' : 'not-visited')))
  const cycle = (s: string) => ({ 'not-visited': 'not-answered', 'not-answered': 'answered', answered: 'marked', marked: 'not-visited' })[s] ?? 'not-visited'
  const styles: Record<string, string> = {
    answered: 'bg-solid-green text-white border-transparent',
    marked: 'bg-solid-amber text-white border-transparent',
    'not-answered': 'badge-red border-transparent',
    'not-visited': 'bg-surface-2 text-muted border-line',
  }
  return (
    <div className="card p-5">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="text-sm font-semibold text-ink">Try it — click a cell to cycle its state</h3>
        <span className="text-xs text-muted">This is exactly how the real exam palette behaves</span>
      </div>
      <div className="mb-4 grid grid-cols-8 gap-1.5">
        {statuses.map((s, i) => (
          <button key={i} onClick={() => setStatuses((prev) => prev.map((x, j) => (i === j ? cycle(x) : x)))}
                  className={`flex aspect-square items-center justify-center rounded border text-[11px] font-semibold transition-colors ${styles[s]}`}>
            {i + 1}
          </button>
        ))}
      </div>
      <div className="flex flex-wrap gap-3 text-xs text-muted">
        <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-sm bg-solid-green" />Answered</span>
        <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-sm bg-solid-amber" />Marked for review</span>
        <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-sm badge-red" />Not answered</span>
        <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-sm border border-line bg-surface-2" />Not visited</span>
      </div>
    </div>
  )
}

function ScoreCalculator() {
  const [total, setTotal] = useState(20)
  const [correct, setCorrect] = useState(14)
  const [wrong, setWrong] = useState(4)
  const [negMark, setNegMark] = useState(0.25)
  const maxWrong = Math.max(0, total - correct)
  const safeWrong = Math.min(wrong, maxWrong)
  // Exactly exam-service's AttemptService.finalizeAttempt formula.
  const score = useMemo(() => (correct - negMark * safeWrong), [correct, negMark, safeWrong])
  const skipped = total - correct - safeWrong

  return (
    <div className="card p-5">
      <h3 className="mb-1 text-sm font-semibold text-ink">Negative-marking calculator</h3>
      <p className="mb-4 text-xs text-muted">The same formula exam-service uses: score = correct − (negative marking × wrong)</p>
      <div className="mb-4 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <div>
          <label className="label">Total questions</label>
          <input type="number" min={1} className="input" value={total}
                 onChange={(e) => { const v = Math.max(1, +e.target.value || 1); setTotal(v); setCorrect((c) => Math.min(c, v)) }} />
        </div>
        <div>
          <label className="label">Correct</label>
          <input type="number" min={0} max={total} className="input" value={correct}
                 onChange={(e) => setCorrect(Math.max(0, Math.min(total, +e.target.value || 0)))} />
        </div>
        <div>
          <label className="label">Wrong</label>
          <input type="number" min={0} max={maxWrong} className="input" value={safeWrong}
                 onChange={(e) => setWrong(Math.max(0, Math.min(maxWrong, +e.target.value || 0)))} />
        </div>
        <div>
          <label className="label">Negative marking</label>
          <select className="input" value={negMark} onChange={(e) => setNegMark(+e.target.value)}>
            <option value={0}>0 (e.g. TCS NQT)</option>
            <option value={0.25}>0.25 per wrong</option>
            <option value={0.33}>0.33 per wrong</option>
            <option value={0.5}>0.5 per wrong</option>
          </select>
        </div>
      </div>
      <div className="flex flex-wrap items-center gap-6 border-t border-line pt-4">
        <div><div className="font-serif text-2xl font-semibold text-ink">{score.toFixed(2)}</div><div className="text-xs text-muted">Score</div></div>
        <div><div className="font-serif text-lg font-semibold text-ink">{skipped}</div><div className="text-xs text-muted">Not attempted</div></div>
        <div className="text-xs text-muted">{correct} correct − {negMark} × {safeWrong} wrong = {correct} − {(negMark * safeWrong).toFixed(2)}</div>
      </div>
    </div>
  )
}

function FaqAccordion() {
  const [open, setOpen] = useState<number | null>(0)
  return (
    <div className="card divide-y divide-line overflow-hidden">
      {FAQS.map((f, i) => (
        <div key={f.q}>
          <button onClick={() => setOpen(open === i ? null : i)}
                  className="flex w-full items-center justify-between gap-4 px-5 py-4 text-left">
            <span className="text-sm font-semibold text-ink">{f.q}</span>
            <span className={`flex-shrink-0 text-muted transition-transform ${open === i ? 'rotate-45' : ''}`}>+</span>
          </button>
          {open === i && <div className="px-5 pb-4 text-sm text-ink-soft">{f.a}</div>}
        </div>
      ))}
    </div>
  )
}

const ROLES = [
  { role: 'Learner', panel: 'brand-student', body: 'Practice by topic, take timed mock exams, and review results with section-wise accuracy.' },
  { role: 'Teacher', panel: 'brand-teacher', body: 'Author questions with versioning, submit content for review, and build exam papers from published questions.' },
  { role: 'Content Reviewer', panel: 'brand-teacher', body: 'Approve or reject content a teacher submits before it can appear in practice or an exam.' },
  { role: 'Admin', panel: 'brand-admin', body: 'Manage users and roles, oversee published content and exams, and read the full audit trail.' },
]

export function AboutPage() {
  const { user } = useAuth()
  const [tab, setTab] = useState<Tab>('Platform')

  return (
    <div className="min-h-screen bg-bg text-ink">
      <header className="sticky top-0 z-20 border-b border-line bg-bg/90 backdrop-blur">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <Link to="/" className="font-serif text-lg font-bold">VRIDDHI <span className="text-amber">LEARN</span></Link>
          <Link to={user ? '/dashboard' : '/login'} className="btn btn-primary btn-sm">
            {user ? 'Go to dashboard' : 'Log in'}
          </Link>
        </div>
      </header>

      <section className="mx-auto max-w-5xl px-6 pb-16 pt-14">
        <div className="mb-2 text-sm font-semibold text-amber">About</div>
        <h1 className="mb-4 font-serif text-3xl font-semibold">A preparation platform, not a quiz app</h1>
        <p className="max-w-2xl text-ink-soft">
          Vriddhi Learn started with software developer and interview preparation and is built so new exam
          categories are added through content and configuration, not new codebases — the same exam engine that
          runs a Spring Boot mock test also runs a TCS NQT paper.
        </p>

        <div className="mt-8 flex flex-wrap gap-2 border-b border-line">
          {TABS.map((t) => (
            <button key={t} onClick={() => setTab(t)}
                    className={`-mb-px border-b-2 px-3 py-2.5 text-sm font-semibold transition-colors ${
                      tab === t ? 'border-amber text-ink' : 'border-transparent text-muted hover:text-ink'
                    }`}>
              {t}
            </button>
          ))}
        </div>

        <div className="mt-8">
          {tab === 'Platform' && (
            <div className="grid gap-6 sm:grid-cols-2">
              {[
                ['Deep question bank', 'MCQ, multiple-correct, fill-in-the-blank, coding and subjective — versioned, so past attempts never change under you.'],
                ['Real exam engine', 'Sections, a server-enforced timer, a question palette, mark-for-review, and three navigation modes.'],
                ['Actual analytics', 'Section and difficulty-wise accuracy after every attempt, not just a final score.'],
                ['Service architecture', 'Separate auth, content and exam services behind one gateway, with Eureka service discovery.'],
              ].map(([t, d]) => (
                <div key={t} className="card p-5">
                  <h3 className="mb-1 font-serif text-base font-semibold">{t}</h3>
                  <p className="text-sm text-muted">{d}</p>
                </div>
              ))}
            </div>
          )}

          {tab === 'Roles & portals' && (
            <div className="space-y-6">
              <div className="grid gap-4 sm:grid-cols-4">
                {ROLES.map((r) => (
                  <div key={r.role} className="card overflow-hidden">
                    <div className={`brand-panel px-4 py-3 ${r.panel}`}><span className="text-sm font-semibold text-white">{r.role}</span></div>
                    <p className="p-4 text-sm text-muted">{r.body}</p>
                  </div>
                ))}
              </div>
              <p className="text-sm text-muted">
                Students, teachers and admins each sign in through their own portal —
                {' '}<Link to="/login" className="font-semibold text-amber">student</Link>,
                {' '}<Link to="/teacher/login" className="font-semibold text-amber">teacher</Link>,
                {' '}<Link to="/admin/login" className="font-semibold text-amber">admin</Link> — so signing in with
                the wrong account tells you plainly instead of guessing which dashboard to show.
              </p>
            </div>
          )}

          {tab === 'How scoring works' && (
            <div className="space-y-6">
              <PaletteDemo />
              <ScoreCalculator />
            </div>
          )}

          {tab === 'FAQ' && <FaqAccordion />}
        </div>
      </section>

      <footer className="border-t border-line">
        <div className="mx-auto max-w-5xl px-6 py-10 text-sm text-muted">
          Vriddhi Learn — Software Developer track, TCS NQT and UGC NET live; SSC, banking and Railway configured on the same generic model.
        </div>
      </footer>
    </div>
  )
}
