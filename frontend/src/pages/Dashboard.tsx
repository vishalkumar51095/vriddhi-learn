import { Link } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { useAuth } from '../context/AuthContext'

// Illustrative only — there's no progress-tracking service yet, so these
// numbers aren't wired to real attempt/practice history. Swap this section
// out once such a service (or an aggregation endpoint on exam-service)
// exists to compute them for real.
const SUBJECT_PROGRESS = [
  { name: 'Core Java', pct: 82, weak: false },
  { name: 'DSA', pct: 54, weak: false },
  { name: 'Spring Boot', pct: 31, weak: true },
  { name: 'SQL', pct: 67, weak: false },
]

const WEAK_TOPICS = ['Spring Security', 'Multithreading', 'Kafka basics', 'SQL joins']

const RECOMMENDED = [
  { title: 'Practice: Spring Security (weak area)', meta: 'Instant grading · a few minutes', to: '/practice', cta: 'Start' },
  { title: 'Java Backend Developer — Full Mock', meta: '90 min · 4 sections · not started', to: '/exams', cta: 'View' },
]

export function DashboardPage() {
  const { user } = useAuth()

  return (
    <Layout>
      <div className="mb-8 flex flex-wrap items-center justify-between gap-3">
        <h1 className="font-serif text-2xl font-semibold">Welcome back, {user?.fullName?.split(' ')[0]}</h1>
        <span className="rounded-full border border-line px-3 py-1 text-xs text-muted">🔥 12 day streak</span>
      </div>

      <div className="mb-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
        {[
          { v: '68%', l: 'Preparation progress' },
          { v: '1,204', l: 'Questions attempted' },
          { v: '74%', l: 'Accuracy' },
          { v: '15', l: 'Daily target' },
        ].map((stat) => (
          <div key={stat.l} className="rounded border border-line border-l-[3px] border-l-green bg-bg-raised p-4">
            <div className="font-serif text-2xl font-semibold">{stat.v}</div>
            <div className="text-xs text-muted">{stat.l}</div>
          </div>
        ))}
      </div>

      <div className="mb-6 rounded border border-line bg-bg-raised p-6">
        <h2 className="mb-4 font-serif text-lg font-semibold">Subject progress</h2>
        {SUBJECT_PROGRESS.map((s) => (
          <div key={s.name} className="mb-3 grid grid-cols-[130px_1fr_44px] items-center gap-3 text-sm last:mb-0">
            <span>{s.name}</span>
            <div className="h-2 overflow-hidden rounded-full bg-line">
              <div className={`h-full rounded-full ${s.weak ? 'bg-red' : 'bg-green'}`} style={{ width: `${s.pct}%` }} />
            </div>
            <span>{s.pct}%</span>
          </div>
        ))}
      </div>

      <div className="mb-6 grid gap-6 md:grid-cols-2">
        <div className="rounded border border-line bg-bg-raised p-6">
          <h2 className="mb-3 font-serif text-lg font-semibold">Weak topics</h2>
          <div className="flex flex-wrap gap-2">
            {WEAK_TOPICS.map((t) => (
              <span key={t} className="rounded-full bg-red-soft px-3 py-1 text-xs font-semibold text-red">
                {t}
              </span>
            ))}
          </div>
          <Link to="/practice" className="mt-4 inline-block text-sm font-semibold text-amber">
            Practice weak topics →
          </Link>
        </div>

        <div className="rounded border border-line bg-bg-raised p-6">
          <h2 className="mb-3 font-serif text-lg font-semibold">Your account</h2>
          <dl className="space-y-2 text-sm">
            <div className="flex justify-between border-t border-line py-2 first:border-t-0 first:pt-0">
              <dt className="text-muted">Email</dt>
              <dd>{user?.email}</dd>
            </div>
            <div className="flex justify-between border-t border-line py-2">
              <dt className="text-muted">Roles</dt>
              <dd>{user?.roles?.join(', ')}</dd>
            </div>
          </dl>
          <Link to="/profile" className="mt-4 inline-block text-sm font-semibold text-amber">
            Edit profile →
          </Link>
        </div>
      </div>

      <div className="rounded border border-line bg-bg-raised p-6">
        <h2 className="mb-4 font-serif text-lg font-semibold">Recommended today</h2>
        <div className="divide-y divide-line">
          {RECOMMENDED.map((item) => (
            <div key={item.title} className="flex items-center justify-between py-3 first:pt-0 last:pb-0">
              <div>
                <div className="text-sm font-semibold">{item.title}</div>
                <div className="text-xs text-muted">{item.meta}</div>
              </div>
              <Link to={item.to} className="rounded border border-line px-4 py-2 text-sm font-semibold hover:border-ink">
                {item.cta}
              </Link>
            </div>
          ))}
        </div>
      </div>
    </Layout>
  )
}
