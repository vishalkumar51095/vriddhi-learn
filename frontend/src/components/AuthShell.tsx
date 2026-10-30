import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'

interface Props {
  brandClass: string
  eyebrow: string
  title: string
  blurb: string
  bullets: string[]
  /** Active tab of the Student / Teacher switcher shown above the form. */
  tab?: 'student' | 'teacher'
  children: ReactNode
  wide?: boolean
}

/** Two-panel layout shared by the sign-up pages (same look as the sign-in portals). */
export function AuthShell({ brandClass, eyebrow, title, blurb, bullets, tab, children, wide }: Props) {
  return (
    <div className="grid min-h-screen md:grid-cols-[minmax(0,5fr)_minmax(0,6fr)]">
      <div className={`brand-panel relative hidden flex-col justify-between overflow-hidden p-10 md:flex ${brandClass}`}>
        <Link to="/" className="relative z-10 font-serif text-lg font-bold text-white">
          VRIDDHI <span className="opacity-80">LEARN</span>
        </Link>
        <div className="relative z-10 float-in max-w-sm" key={eyebrow}>
          <div className="soft mb-3 text-sm font-semibold uppercase tracking-wide">{eyebrow}</div>
          <h1 className="mb-4 font-serif text-3xl font-semibold leading-tight text-white">{title}</h1>
          <p className="soft mb-6 text-sm leading-relaxed">{blurb}</p>
          <ul className="space-y-2 text-sm text-white">
            {bullets.map((b, i) => (
              <li key={b} className="float-in flex items-start gap-2" style={{ animationDelay: `${150 + i * 120}ms` }}>
                <span className="mt-1.5 h-1.5 w-1.5 flex-shrink-0 rounded-full bg-white/70" />
                {b}
              </li>
            ))}
          </ul>
        </div>
        <div className="soft relative z-10 text-xs">Vriddhi Learn</div>
        <div aria-hidden className="pointer-events-none absolute -right-24 -top-24 h-72 w-72 rounded-full bg-white/10" />
        <div aria-hidden className="pointer-events-none absolute -bottom-32 -left-16 h-80 w-80 rounded-full bg-black/10" />
      </div>

      <div className="flex items-start justify-center bg-bg px-6 py-10 md:items-center md:py-12">
        <div className={`w-full ${wide ? 'max-w-lg' : 'max-w-sm'}`}>
          <div className="mb-6 md:hidden">
            <Link to="/" className="font-serif text-lg font-bold text-ink">VRIDDHI <span className="text-amber">LEARN</span></Link>
          </div>

          {tab && (
            <div role="tablist" aria-label="Account type" className="relative mb-6 grid grid-cols-2 rounded-lg border border-line bg-surface-2 p-1 text-sm font-semibold">
              <span aria-hidden className="absolute bottom-1 top-1 w-[calc(50%-4px)] rounded-md bg-bg-raised shadow transition-transform duration-300"
                    style={{ transform: tab === 'student' ? 'translateX(0)' : 'translateX(100%)', left: 4 }} />
              <Link role="tab" aria-selected={tab === 'student'} to="/register"
                    className={`relative z-10 rounded-md py-2 text-center ${tab === 'student' ? 'text-ink' : 'text-muted hover:text-ink'}`}>I'm a student</Link>
              <Link role="tab" aria-selected={tab === 'teacher'} to="/teacher/register"
                    className={`relative z-10 rounded-md py-2 text-center ${tab === 'teacher' ? 'text-ink' : 'text-muted hover:text-ink'}`}>I'm a teacher</Link>
            </div>
          )}
          {children}
        </div>
      </div>
    </div>
  )
}
