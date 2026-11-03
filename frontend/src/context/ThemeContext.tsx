import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'

type Mode = 'light' | 'dark' | 'system'
type Accent = 'amber' | 'indigo' | 'teal'

interface ThemeContextValue {
  mode: Mode
  accent: Accent
  resolvedDark: boolean
  setMode: (m: Mode) => void
  setAccent: (a: Accent) => void
}

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined)

function apply(mode: Mode, accent: Accent) {
  const dark = mode === 'dark' || (mode === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)
  document.documentElement.dataset.theme = dark ? 'dark' : 'light'
  document.documentElement.dataset.accent = accent
  return dark
}

// Applied synchronously before first paint by the inline script in index.html —
// this provider just keeps React state in sync with that, and persists changes.
export function ThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setModeState] = useState<Mode>(() => (localStorage.getItem('vriddhi.theme') as Mode) || 'system')
  const [accent, setAccentState] = useState<Accent>(() => (localStorage.getItem('vriddhi.accent') as Accent) || 'amber')
  const [resolvedDark, setResolvedDark] = useState(() => apply(mode, accent))

  useEffect(() => {
    setResolvedDark(apply(mode, accent))
    if (mode !== 'system') return
    const mq = window.matchMedia('(prefers-color-scheme: dark)')
    const onChange = () => setResolvedDark(apply(mode, accent))
    mq.addEventListener('change', onChange)
    return () => mq.removeEventListener('change', onChange)
  }, [mode, accent])

  function setMode(m: Mode) {
    localStorage.setItem('vriddhi.theme', m)
    setModeState(m)
  }
  function setAccent(a: Accent) {
    localStorage.setItem('vriddhi.accent', a)
    setAccentState(a)
  }

  return (
    <ThemeContext.Provider value={{ mode, accent, resolvedDark, setMode, setAccent }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme(): ThemeContextValue {
  const ctx = useContext(ThemeContext)
  if (!ctx) throw new Error('useTheme must be used within a ThemeProvider')
  return ctx
}
