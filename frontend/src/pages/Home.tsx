import { type ReactNode, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { ThemeSwitcher } from '../components/ThemeSwitcher'
import { Reveal } from '../components/Reveal'
import { AnimatedNumber } from '../components/AnimatedNumber'
import {
  fetchPublicContentStats, fetchPublicExamStats, fetchPublicUserStats,
  type PublicContentStats, type PublicExamStats, type PublicUserStats,
} from '../api/publicStats'

const JOURNEY = [
  { n: '01', title: 'Learn', body: 'Structured subject → topic → subtopic content, built by teachers and reviewed before it ever reaches a learner.' },
  { n: '02', title: 'Practice', body: 'Topic, mixed, and weak-area question sets — with instant grading and explanations.' },
  { n: '03', title: 'Test', body: 'Realistic, timed mock exams with sections, negative marking, and a real question palette.' },
  { n: '04', title: 'Analyze', body: 'Section, topic, and difficulty-wise breakdowns after every attempt.' },
  { n: '05', title: 'Improve', body: 'Revision queues built from your own mistakes, not a generic syllabus.' },
]

const CATEGORIES = [
  { name: 'Software Developer', desc: 'Java, DSA, SQL, Spring Boot, System Design', tag: 'Live now' },
  { name: 'TCS NQT', desc: 'Numerical, Verbal & Reasoning Ability, Programming Logic', tag: 'Live now' },
  { name: 'UGC NET', desc: 'Paper 1 Teaching & Research Aptitude, Paper 2 Computer Science', tag: 'Live now' },
  { name: 'SSC CGL', desc: 'Quant, Reasoning, English, General Awareness', tag: 'Same engine' },
  { name: 'IBPS PO', desc: 'Quant, Reasoning, English, Banking Awareness', tag: 'Same engine' },
]

const FEATURES = [
  { mark: 'Q', title: 'Deep question bank', body: 'MCQ, multiple-correct, fill-in-the-blank, coding and subjective questions — versioned, so past attempts never change under you.' },
  { mark: 'T', title: 'Real exam engine', body: 'Sections, a server-enforced timer, question palette, mark-for-review, and three navigation modes: free, forward-only, section-locked.' },
  { mark: 'A', title: 'Actual analytics', body: 'Section and difficulty-wise accuracy after every attempt — not just a final score.' },
  { mark: 'R', title: 'Practice that grades instantly', body: 'MCQ practice is graded on the spot; fill-blank and coding go to manual review, same as a real exam.' },
]

const EXAM_LIBRARY = [
  { title: 'Java Backend Developer — Full Mock', meta: '90 min · 4 sections · Negative marking', tag: 'Software' },
  { title: 'Core DSA Sprint', meta: '45 min · 2 sections', tag: 'Software' },
  { title: 'SSC CGL Tier I — Full Length', meta: '60 min · 4 sections', tag: 'Government' },
  { title: 'IBPS PO Prelims', meta: '60 min · 3 sections', tag: 'Government' },
]

type CellStatus = 'answered' | 'marked' | 'unvisited'
const NEXT_STATUS: Record<CellStatus, CellStatus> = { unvisited: 'answered', answered: 'marked', marked: 'unvisited' }
const INITIAL_CELLS: CellStatus[] = [
  'answered', 'answered', 'marked', 'unvisited', 'answered', 'unvisited', 'marked', 'answered',
  'answered', 'unvisited', 'answered', 'answered', 'unvisited', 'marked', 'answered', 'unvisited',
]

/** A working miniature of the exam palette: click a question to cycle its status; the timer really counts down. */
function PaletteWidget() {
  const [cells, setCells] = useState<CellStatus[]>(INITIAL_CELLS)
  const [seconds, setSeconds] = useState(18 * 60 + 42)

  useEffect(() => {
    const id = setInterval(() => setSeconds((s) => (s <= 0 ? 18 * 60 + 42 : s - 1)), 1000)
    return () => clearInterval(id)
  }, [])

  const styles: Record<CellStatus, string> = {
    answered: 'bg-solid-green text-white',
    marked: 'bg-solid-amber text-white',
    unvisited: 'bg-line text-muted',
  }
  const count = (st: CellStatus) => cells.filter((c) => c === st).length
  const mm = String(Math.floor(seconds / 60)).padStart(2, '0')
  const ss = String(seconds % 60).padStart(2, '0')

  return (
    <div className="rounded-md border border-line bg-bg-raised p-5 shadow-sm">
      <div className="mb-3 flex items-center justify-between">
        <span className="text-sm font-semibold">Spring Boot Mock — Section 2</span>
        <span className="font-mono text-sm font-bold text-amber" aria-label="Time remaining">{mm}:{ss}</span>
      </div>
      <div className="mb-4 grid grid-cols-8 gap-1.5">
        {cells.map((st, i) => (
          <button key={i} type="button" aria-label={`Question ${i + 1}: ${st}`}
                  onClick={() => setCells((prev) => prev.map((c, j) => (j === i ? NEXT_STATUS[c] : c)))}
                  className={`flex aspect-square items-center justify-center rounded text-[10px] font-semibold transition-transform hover:scale-110 active:scale-95 ${styles[st]}`}>
            {i + 1}
          </button>
        ))}
      </div>
      <div className="flex flex-wrap gap-3 text-xs text-muted">
        <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-sm bg-solid-green" />Answered {count('answered')}</span>
        <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-sm bg-solid-amber" />Marked {count('marked')}</span>
        <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-sm bg-line" />Not visited {count('unvisited')}</span>
      </div>
      <p className="mt-3 border-t border-line pt-2 text-xs text-muted">Try it — tap a number to change its status.</p>
    </div>
  )
}

function Faq({ items }: { items: { q: string; a: ReactNode }[] }) {
  const [open, setOpen] = useState<number | null>(0)
  return (
    <div className="divide-y divide-line rounded-md border border-line">
      {items.map((it, i) => (
        <div key={it.q}>
          <button type="button" onClick={() => setOpen(open === i ? null : i)} aria-expanded={open === i}
                  className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left text-sm font-semibold text-ink">
            {it.q}
            <span aria-hidden className={`text-amber transition-transform duration-200 ${open === i ? 'rotate-45' : ''}`}>+</span>
          </button>
          <div className="accordion-body" data-open={open === i}><div><div className="px-4 pb-3 text-sm text-ink-soft">{it.a}</div></div></div>
        </div>
      ))}
    </div>
  )
}

export function HomePage() {
  const { user } = useAuth()
  const [userStats, setUserStats] = useState<PublicUserStats | null>(null)
  const [contentStats, setContentStats] = useState<PublicContentStats | null>(null)
  const [examStats, setExamStats] = useState<PublicExamStats | null>(null)
  const [step, setStep] = useState(0)
  const [examFilter, setExamFilter] = useState<'All' | 'Software' | 'Government'>('All')

  useEffect(() => {
    // Independent services behind the gateway — fetched in parallel, and one
    // failing (e.g. exam-service still starting up) doesn't blank the others.
    fetchPublicUserStats().then(setUserStats).catch(() => {})
    fetchPublicContentStats().then(setContentStats).catch(() => {})
    fetchPublicExamStats().then(setExamStats).catch(() => {})
  }, [])

  const stats: { v: number | null; suffix: string; l: string }[] = [
    { v: userStats ? userStats.learners + userStats.teachers : null, suffix: '+', l: 'Active learners & teachers' },
    { v: contentStats ? contentStats.publishedQuestions : null, suffix: '+', l: 'Published questions' },
    { v: examStats ? examStats.publishedExams : null, suffix: '', l: 'Mock exam papers' },
    { v: examStats ? examStats.attemptsTaken : null, suffix: '+', l: 'Attempts completed' },
  ]
  const exams = EXAM_LIBRARY.filter((e) => examFilter === 'All' || e.tag === examFilter)

  return (
    <div className="min-h-screen bg-bg text-ink">
      <header className="sticky top-0 z-20 border-b border-line bg-bg/90 backdrop-blur">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <div className="font-serif text-lg font-bold">
            VRIDDHI <span className="text-amber">LEARN</span>
          </div>
          <nav className="hidden items-center gap-6 text-sm text-ink-soft sm:flex">
            <a href="#features" className="hover:text-ink">Features</a>
            <a href="#exams" className="hover:text-ink">Exams</a>
            <Link to="/about" className="hover:text-ink">About</Link>
            {!user && <Link to="/teacher/register" className="hover:text-ink">Teach with us</Link>}
          </nav>
          <div className="flex items-center gap-3">
            <ThemeSwitcher />
            {user ? (
              <Link to="/dashboard" className="rounded bg-ink px-4 py-2 text-sm font-semibold text-bg">
                Go to dashboard
              </Link>
            ) : (
              <>
                <Link to="/login" className="rounded border border-line px-4 py-2 text-sm font-semibold">
                  Log in
                </Link>
                <Link to="/register" className="rounded bg-ink px-4 py-2 text-sm font-semibold text-bg">
                  Start preparing
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* HERO */}
      <section className="mx-auto grid max-w-5xl gap-12 px-6 pb-16 pt-16 md:grid-cols-[1.1fr_0.9fr] md:items-center md:pt-24">
        <div>
          <div className="float-in mb-3 text-sm font-semibold text-amber">Learn. Practice. Test. Analyze. Improve.</div>
          <h1 className="max-w-lg font-serif text-4xl font-semibold leading-tight sm:text-5xl">
            Your preparation. Your progress. Your growth.
          </h1>
          <p className="mt-5 max-w-lg text-lg text-ink-soft">
            One platform for software interviews, coding assessments, and competitive exams. Structured content,
            realistic mock tests, and analytics that show you exactly where to focus next.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link to={user ? '/exams' : '/register'} className="rounded bg-ink px-6 py-3 text-sm font-semibold text-bg">
              {user ? 'Browse exams' : 'Start preparing'}
            </Link>
            <Link
              to={user ? '/practice' : '/login'}
              className="rounded border border-line px-6 py-3 text-sm font-semibold"
            >
              {user ? 'Practice now' : 'Log in'}
            </Link>
          </div>
          {!user && (
            <p className="mt-4 text-sm text-muted">
              Teach or write questions? <Link to="/teacher/register" className="font-semibold text-amber">Apply as a teacher →</Link>
            </p>
          )}
        </div>
        <PaletteWidget />
      </section>

      {/* STATS — live counts from auth-service, content-service and exam-service */}
      <section className="border-y border-line bg-bg-raised py-10">
        <div className="mx-auto grid max-w-5xl grid-cols-2 gap-6 px-6 sm:grid-cols-4">
          {stats.map((s) => (
            <div key={s.l} className="text-center sm:text-left">
              <div className="font-serif text-3xl font-semibold text-ink"><AnimatedNumber value={s.v} suffix={s.suffix} /></div>
              <div className="text-sm text-muted">{s.l}</div>
            </div>
          ))}
        </div>
      </section>

      {/* CATEGORIES */}
      <section className="py-16">
        <div className="mx-auto max-w-5xl px-6">
          <div className="mb-2 text-sm font-semibold text-amber">Preparation categories</div>
          <h2 className="mb-8 max-w-lg font-serif text-2xl font-semibold">
            Built for developers today, ready for every exam category tomorrow
          </h2>
          <div className="grid gap-px overflow-hidden rounded-md border border-line bg-line sm:grid-cols-2 lg:grid-cols-4">
            {CATEGORIES.map((cat) => (
              <div key={cat.name} className="bg-bg p-5 transition-colors hover:bg-bg-raised">
                <div className="mb-2 inline-block rounded-full bg-amber-soft px-2 py-0.5 text-[11px] font-semibold text-amber">
                  {cat.tag}
                </div>
                <h3 className="mb-1 font-serif text-base font-semibold">{cat.name}</h3>
                <p className="text-sm text-muted">{cat.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* JOURNEY + FEATURES */}
      <section id="features" className="border-t border-line bg-bg-raised py-16">
        <div className="mx-auto max-w-5xl px-6">
          <div className="mb-2 text-sm font-semibold text-amber">How it works</div>
          <h2 className="mb-8 font-serif text-2xl font-semibold">Five steps, one platform</h2>

          <div className="mb-14">
            <div role="tablist" aria-label="How it works" className="mb-4 flex flex-wrap">
              {JOURNEY.map((j, i) => (
                <button key={j.n} role="tab" aria-selected={i === step} onClick={() => setStep(i)} onMouseEnter={() => setStep(i)}
                        className={`min-w-[110px] flex-1 border-t-2 px-4 py-3 text-left transition-colors ${i <= step ? 'border-amber' : 'border-line'} ${i === step ? 'bg-bg' : ''}`}>
                  <div className="text-xs text-muted">{j.n}</div>
                  <div className={`font-serif text-base font-semibold ${i === step ? 'text-amber' : 'text-ink'}`}>{j.title}</div>
                </button>
              ))}
            </div>
            <div key={step} className="slide-in rounded-md border border-line bg-bg p-5" role="tabpanel">
              <p className="max-w-xl text-ink-soft">{JOURNEY[step].body}</p>
              <div className="mt-3 flex gap-2">
                <button className="btn btn-ghost btn-sm" disabled={step === 0} onClick={() => setStep(step - 1)}>← Previous</button>
                <button className="btn btn-ghost btn-sm" disabled={step === JOURNEY.length - 1} onClick={() => setStep(step + 1)}>Next →</button>
              </div>
            </div>
          </div>

          <div className="grid gap-8 sm:grid-cols-2">
            {FEATURES.map((f, i) => (
              <Reveal key={f.title} delay={i * 80}>
                <div className="mb-3 flex h-9 w-9 items-center justify-center rounded bg-amber-soft font-serif font-bold text-amber">
                  {f.mark}
                </div>
                <h3 className="mb-1 font-serif text-base font-semibold">{f.title}</h3>
                <p className="text-sm text-muted">{f.body}</p>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* EXAM LIBRARY */}
      <section id="exams" className="py-16">
        <div className="mx-auto max-w-5xl px-6">
          <div className="mb-2 text-sm font-semibold text-amber">Exam library</div>
          <h2 className="mb-5 font-serif text-2xl font-semibold">Pick an exam, see exactly what's inside</h2>
          <div className="mb-4 flex gap-2" role="group" aria-label="Filter exams">
            {(['All', 'Software', 'Government'] as const).map((f) => (
              <button key={f} className="chip" aria-pressed={examFilter === f} onClick={() => setExamFilter(f)}>{f}</button>
            ))}
          </div>
          <div key={examFilter} className="slide-in divide-y divide-line overflow-hidden rounded-md border border-line bg-bg-raised">
            {exams.map((exam) => (
              <div key={exam.title} className="flex items-center justify-between px-6 py-4 transition-colors hover:bg-surface-2">
                <div>
                  <h3 className="font-semibold">{exam.title}</h3>
                  <p className="text-sm text-muted">{exam.meta}</p>
                </div>
                <span className="rounded-full bg-green-soft px-3 py-1 text-xs font-semibold text-green">{exam.tag}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ABOUT / FAQ */}
      <section className="border-t border-line bg-bg-raised py-16">
        <div className="mx-auto grid max-w-5xl gap-12 px-6 md:grid-cols-2">
          <div>
            <div className="mb-2 text-sm font-semibold text-amber">About</div>
            <h2 className="mb-4 font-serif text-xl font-semibold">A preparation platform, not a quiz app</h2>
            <p className="text-ink-soft">
              Vriddhi Learn starts with software developer and interview preparation, and is built from day one so
              new exam categories can be added through content and configuration — not new codebases. The same exam
              engine that powers a Spring Boot mock test also powers a TCS NQT paper.
            </p>
            <Link to="/about" className="mt-4 inline-block text-sm font-semibold text-amber">More about the platform →</Link>
          </div>
          <div>
            <div className="mb-2 text-sm font-semibold text-amber">FAQ</div>
            <h2 className="mb-4 font-serif text-xl font-semibold">Good to know</h2>
            <Faq items={[
              { q: 'Is the timer trustworthy?', a: "Yes — the on-screen timer is a display only; the server's deadline is what actually decides when an attempt auto-submits." },
              { q: 'Can I prepare for more than one exam?', a: 'Yes. Role is independent of preparation category — one learner can prepare for Java, SSC CGL and IBPS PO at once.' },
              { q: 'How do I become a teacher?', a: <>Apply on the <Link to="/teacher/register" className="font-semibold text-amber">teacher application page</Link>. An administrator reviews each application before teacher access is enabled, and content is reviewed before it reaches learners.</> },
            ]} />
          </div>
        </div>
      </section>

      <footer className="border-t border-line">
        <div className="mx-auto max-w-5xl px-6 py-10 text-sm text-muted">
          Vriddhi Learn — Software Developer track live; SSC, banking and Railway configured on the same generic model.
        </div>
      </footer>
    </div>
  )
}
