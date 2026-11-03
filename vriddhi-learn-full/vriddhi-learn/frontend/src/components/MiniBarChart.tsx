/** A small dependency-free bar chart for daily counts — good enough for a dashboard sparkline. */
export function MiniBarChart({ data, height = 56 }: { data: { date: string; count: number }[]; height?: number }) {
  const max = Math.max(1, ...data.map((d) => d.count))
  return (
    <div className="flex items-end gap-1" style={{ height }}>
      {data.map((d) => (
        <div key={d.date} className="group relative flex-1">
          <div className="rounded-t bg-amber transition-opacity group-hover:opacity-80"
               style={{ height: Math.max(2, (d.count / max) * height) }} />
          <div className="pointer-events-none absolute -top-7 left-1/2 hidden -translate-x-1/2 whitespace-nowrap rounded bg-ink px-1.5 py-0.5 text-[10px] text-bg group-hover:block">
            {d.date.slice(5)}: {d.count}
          </div>
        </div>
      ))}
    </div>
  )
}
