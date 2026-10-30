import type { ReactNode } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { ThemeSwitcher } from './ThemeSwitcher'

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/practice', label: 'Practice' },
  { to: '/exams', label: 'Exams' },
  { to: '/profile', label: 'Profile' },
]

export function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  const crossLink = user?.roles?.includes('ADMIN') ? { to: '/admin', label: 'Admin dashboard' }
    : user?.roles?.includes('TEACHER') ? { to: '/teacher', label: 'Teacher dashboard' } : null

  return (
    <div className="min-h-screen bg-bg text-ink">
      <header className="sticky top-0 z-20 border-b border-line bg-bg/90 backdrop-blur">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <div className="font-serif text-lg font-bold">
            VRIDDHI <span className="text-amber">LEARN</span>
          </div>
          <nav className="flex items-center gap-5 text-sm">
            {NAV_ITEMS.map((item) => (
              <NavLink key={item.to} to={item.to}
                       className={({ isActive }) => isActive ? 'font-semibold text-ink' : 'text-ink-soft hover:text-ink'}>
                {item.label}
              </NavLink>
            ))}
            {crossLink && (
              <NavLink to={crossLink.to} className="badge badge-accent">{crossLink.label}</NavLink>
            )}
            <ThemeSwitcher />
            <span className="hidden text-ink-soft sm:inline">{user?.fullName}</span>
            <button onClick={handleLogout} className="btn btn-ghost btn-sm">Log out</button>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-6 py-8">{children}</main>
    </div>
  )
}
