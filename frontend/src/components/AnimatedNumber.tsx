import { useEffect, useState } from 'react'
import { useInView } from '../hooks/useInView'

const prefersReducedMotion = () => typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches

/** Counts up from 0 to `value` the first time it is visible. Renders `fallback` while value is unknown. */
export function AnimatedNumber({ value, suffix = '', fallback = '—', duration = 900 }:
    { value: number | null | undefined; suffix?: string; fallback?: string; duration?: number }) {
  const { ref, inView } = useInView<HTMLSpanElement>(0.4)
  const [shown, setShown] = useState(0)
  const reduce = prefersReducedMotion()

  useEffect(() => {
    if (!inView || value == null || reduce) return
    const start = performance.now()
    let raf = 0
    const tick = (now: number) => {
      const t = Math.min((now - start) / duration, 1)
      setShown(Math.round(value * (1 - Math.pow(1 - t, 3)))) // ease-out cubic
      if (t < 1) raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(raf)
  }, [inView, value, duration, reduce])

  const display = reduce ? value : shown
  return <span ref={ref}>{value == null ? fallback : `${(display ?? 0).toLocaleString()}${suffix}`}</span>
}
