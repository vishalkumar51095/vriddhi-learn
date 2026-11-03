import type { ReactNode } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { ThemeSwitcher } from '../ThemeSwitcher'

const NAV = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/teacher-applications', label: 'Teacher applications' },
  { to: '/admin/audit-logs', label: 'Audit logs' },
  { to: '/admin/exams', label: 'Exams' },
  { to: '/admin/content', label: 'Content' },
]

export function AdminLayout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="grid min-h-screen md:grid-cols-[220px_1fr]" style={{ background: 'var(--bg)' }}>
      <aside className="hidden flex-col p-5 md:flex" style={{ background: 'var(--side-bg)', color: 'var(--side-text)' }}>
        <div className="mb-8 font-serif text-lg font-bold text-white">VRIDDHI <span className="opacity-70">ADMIN</span></div>
        <nav className="flex flex-1 flex-col gap-1">
          {NAV.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end}
                     className={({ isActive }) =>
                       `rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                         isActive ? 'text-white' : 'hover:text-white'
                       }`
                     }
                     style={({ isActive }) => ({ background: isActive ? 'var(--side-active)' : 'transparent', color: isActive ? '#fff' : 'var(--side-muted)' })}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t pt-4 text-xs" style={{ borderColor: 'rgba(255,255,255,.1)', color: 'var(--side-muted)' }}>
          Signed in as<br /><span className="font-semibold" style={{ color: 'var(--side-text)' }}>{user?.fullName}</span>
        </div>
      </aside>

      <div className="flex min-w-0 flex-col">
        <header className="flex items-center justify-between gap-3 border-b border-line bg-bg-raised px-5 py-3">
          <div className="font-serif text-base font-semibold md:hidden">VRIDDHI ADMIN</div>
          <div className="hidden text-sm text-muted md:block">Platform administration</div>
          <div className="flex items-center gap-2">
            <ThemeSwitcher />
            <button onClick={async () => { await logout(); navigate('/admin/login') }} className="btn btn-ghost btn-sm">
              Log out
            </button>
          </div>
        </header>
        <main className="flex-1 p-5 md:p-8">{children}</main>
      </div>
    </div>
  )
}
