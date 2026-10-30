import { useTheme } from '../context/ThemeContext'

const ACCENTS: { key: 'amber' | 'indigo' | 'teal'; label: string; swatch: string }[] = [
  { key: 'amber', label: 'Amber', swatch: '#A25800' },
  { key: 'indigo', label: 'Indigo', swatch: '#4338CA' },
  { key: 'teal', label: 'Teal', swatch: '#0F766E' },
]

export function ThemeSwitcher() {
  const { mode, accent, resolvedDark, setMode, setAccent } = useTheme()
  return (
    <div className="flex items-center gap-1.5">
      <div className="flex items-center gap-0.5 rounded-lg border border-line bg-bg p-0.5">
        {ACCENTS.map((a) => (
          <button key={a.key} title={a.label} onClick={() => setAccent(a.key)}
                  aria-pressed={accent === a.key}
                  className={`h-6 w-6 rounded-md ring-offset-1 transition-all ${accent === a.key ? 'ring-2 ring-amber' : ''}`}
                  style={{ background: a.swatch }} />
        ))}
      </div>
      <button onClick={() => setMode(resolvedDark ? 'light' : 'dark')} className="btn btn-ghost btn-sm" title="Toggle dark mode">
        {resolvedDark ? '☀️ Light' : '🌙 Dark'}
      </button>
      {mode !== 'system' && (
        <button onClick={() => setMode('system')} className="text-xs text-muted underline" title="Follow system preference">
          system
        </button>
      )}
    </div>
  )
}
