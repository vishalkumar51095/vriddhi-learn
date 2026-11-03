import type { CSSProperties, ReactNode } from 'react'
import { useInView } from '../hooks/useInView'

/** Fades + lifts its children in when scrolled into view. `delay` (ms) staggers siblings. */
export function Reveal({ children, delay = 0, className = '' }: { children: ReactNode; delay?: number; className?: string }) {
  const { ref, inView } = useInView<HTMLDivElement>()
  const style: CSSProperties = { transitionDelay: `${delay}ms` }
  return (
    <div ref={ref} style={style} className={`reveal ${inView ? 'reveal-in' : ''} ${className}`}>
      {children}
    </div>
  )
}
