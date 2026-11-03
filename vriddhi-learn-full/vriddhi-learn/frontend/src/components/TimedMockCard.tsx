import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchDailyMockToday, startDailyMock, type DailyMockStatus } from '../api/dailyMock'

interface Props {
  categoryCode: string
}

function minutesLeft(expiresAt: string | null): number {
  if (!expiresAt) return 0
  return Math.max(0, Math.ceil((new Date(expiresAt).getTime() - Date.now()) / 60000))
}

/**
 * Today's timed mock. Starting it creates a private exam for today and opens the normal exam screen, so the
 * server owns the clock: closing the tab does not pause it, and it is auto-submitted when time runs out.
 */
export function TimedMockCard({ categoryCode }: Props) {
  const navigate = useNavigate()
  const [status, setStatus] = useState<DailyMockStatus | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setStatus(null)
    setError(null)
    fetchDailyMockToday(categoryCode)
      .then((s) => { if (!cancelled) setStatus(s) })
      .catch(() => { if (!cancelled) setError('Could not check today\'s mock.') })
    return () => { cancelled = true }
  }, [categoryCode])

  async function handleStart() {
    setBusy(true)
    setError(null)
    try {
      const started = await startDailyMock(categoryCode)
      if (started.attemptId) {
        navigate(started.state === 'COMPLETED' ? `/attempts/${started.attemptId}/result` : `/attempts/${started.attemptId}`)
      }
    } catch {
      setError('Could not start today\'s mock. Please try again in a moment.')
    } finally {
      setBusy(false)
    }
  }

  const running = status?.state === 'IN_PROGRESS'
  const done = status?.state === 'COMPLETED'

  return (
    <div className="mb-6 rounded border border-line bg-bg-raised p-6">
      <div className="mb-1 flex flex-wrap items-center justify-between gap-2">
        <h2 className="font-serif text-lg font-semibold">Today's timed mock</h2>
        {running && (
          <span className="rounded-full bg-amber-soft px-2.5 py-1 text-xs font-semibold text-amber">
            In progress · about {minutesLeft(status?.expiresAt ?? null)} min left
          </span>
        )}
        {done && (
          <span className="rounded-full bg-green-soft px-2.5 py-1 text-xs font-semibold text-green">Completed</span>
        )}
      </div>

      <p className="mb-4 text-sm text-ink-soft">
        {done
          ? 'You have taken today\'s mock. A new one is built tomorrow from your revision queue and weak topics.'
          : 'Ten questions picked for you, on a server-side clock (1.5 minutes per question). It is submitted automatically when time is up.'}
      </p>

      {error && <p className="mb-3 rounded bg-red-soft px-3 py-2 text-sm text-red">{error}</p>}
      {!status && !error && <p className="text-sm text-ink-soft">Checking…</p>}

      {status && (
        <button
          onClick={handleStart}
          disabled={busy}
          className="rounded bg-ink px-5 py-2.5 text-sm font-semibold text-bg disabled:opacity-50"
        >
          {busy ? 'Please wait…' : done ? 'View result' : running ? 'Resume mock' : 'Start timed mock'}
        </button>
      )}
    </div>
  )
}
