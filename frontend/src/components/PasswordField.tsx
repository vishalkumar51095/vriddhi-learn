import { type KeyboardEvent, useState } from 'react'

interface PasswordCheck { label: string; ok: boolean }

function passwordChecks(pw: string): PasswordCheck[] {
  return [
    { label: 'At least 8 characters', ok: pw.length >= 8 },
    { label: 'A letter and a number', ok: /[A-Za-z]/.test(pw) && /\d/.test(pw) },
    { label: 'Upper and lower case', ok: /[a-z]/.test(pw) && /[A-Z]/.test(pw) },
    { label: 'A symbol (or 12+ characters)', ok: /[^A-Za-z0-9]/.test(pw) || pw.length >= 12 },
  ]
}

const LEVELS = [
  { label: 'Too short', bar: 'var(--danger)' },
  { label: 'Weak', bar: 'var(--danger)' },
  { label: 'Okay', bar: 'var(--solid-amber)' },
  { label: 'Good', bar: 'var(--solid-green)' },
  { label: 'Strong', bar: 'var(--solid-green)' },
]

interface Props {
  id: string
  value: string
  onChange: (v: string) => void
  label?: string
  placeholder?: string
  autoComplete?: string
  /** Show the live strength meter + checklist (registration only; sign-in leaves this off). */
  showStrength?: boolean
}

/** Password input with show/hide, caps-lock warning and an optional live strength meter. */
export function PasswordField({ id, value, onChange, label = 'Password', placeholder = '••••••••', autoComplete, showStrength }: Props) {
  const [visible, setVisible] = useState(false)
  const [caps, setCaps] = useState(false)
  const checks = passwordChecks(value)
  // The 8-character rule is the backend's hard minimum, so anything shorter is always "Too short".
  const score = value.length < 8 ? 0 : checks.filter((c) => c.ok).length
  const level = LEVELS[score]

  function onKey(e: KeyboardEvent<HTMLInputElement>) { setCaps(e.getModifierState?.('CapsLock') ?? false) }

  return (
    <div>
      <label className="label" htmlFor={id}>{label}</label>
      <div className="relative">
        <input id={id} type={visible ? 'text' : 'password'} required minLength={showStrength ? 8 : undefined} autoComplete={autoComplete}
               placeholder={placeholder} value={value} onChange={(e) => onChange(e.target.value)}
               onKeyUp={onKey} onKeyDown={onKey} onBlur={() => setCaps(false)}
               className="input pr-16" />
        <button type="button" onClick={() => setVisible((v) => !v)} aria-pressed={visible}
                aria-label={visible ? 'Hide password' : 'Show password'}
                className="absolute right-2 top-1/2 -translate-y-1/2 rounded px-2 py-1 text-xs font-semibold text-muted hover:text-ink">
          {visible ? 'Hide' : 'Show'}
        </button>
      </div>
      {caps && <p className="mt-1 text-xs font-semibold text-amber" role="status">Caps Lock is on</p>}

      {showStrength && value.length > 0 && (
        <div className="mt-2 float-in" aria-live="polite">
          <div className="flex items-center gap-2">
            <div className="flex flex-1 gap-1">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="h-1.5 flex-1 rounded-full transition-colors duration-300"
                     style={{ background: i <= score ? level.bar : 'var(--line)' }} />
              ))}
            </div>
            <span className="w-16 text-right text-xs font-semibold text-ink-soft">{level.label}</span>
          </div>
          <ul className="mt-2 grid grid-cols-2 gap-x-3 gap-y-1 text-xs">
            {checks.map((c) => (
              <li key={c.label} className={`flex items-center gap-1.5 transition-colors ${c.ok ? 'text-green' : 'text-muted'}`}>
                <span aria-hidden>{c.ok ? '✓' : '○'}</span>{c.label}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
