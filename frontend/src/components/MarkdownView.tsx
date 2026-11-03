import ReactMarkdown, { type Components } from 'react-markdown'
import remarkGfm from 'remark-gfm'

// Lessons are written in Markdown. Code fences (including the ASCII diagrams) render in a monospaced,
// horizontally scrollable block, and tables scroll on small screens instead of breaking the layout.
const components: Components = {
  h1: ({ children }) => <h1 className="mb-4 mt-8 font-serif text-2xl font-semibold">{children}</h1>,
  h2: ({ children }) => <h2 className="mb-3 mt-9 border-b border-line pb-2 font-serif text-xl font-semibold">{children}</h2>,
  h3: ({ children }) => <h3 className="mb-2 mt-6 font-serif text-lg font-semibold">{children}</h3>,
  p: ({ children }) => <p className="mb-4 leading-relaxed">{children}</p>,
  ul: ({ children }) => <ul className="mb-4 list-disc space-y-1 pl-6">{children}</ul>,
  ol: ({ children }) => <ol className="mb-4 list-decimal space-y-1 pl-6">{children}</ol>,
  li: ({ children }) => <li className="leading-relaxed">{children}</li>,
  strong: ({ children }) => <strong className="font-semibold">{children}</strong>,
  a: ({ href, children }) => (
    <a href={href} target="_blank" rel="noreferrer" className="text-amber underline">{children}</a>
  ),
  blockquote: ({ children }) => (
    <blockquote className="mb-4 border-l-4 border-amber pl-4 text-ink-soft">{children}</blockquote>
  ),
  hr: () => <hr className="my-6 border-line" />,
  table: ({ children }) => (
    <div className="mb-5 overflow-x-auto">
      <table className="w-full border-collapse text-sm">{children}</table>
    </div>
  ),
  th: ({ children }) => <th className="border border-line bg-bg-raised px-3 py-2 text-left font-semibold">{children}</th>,
  td: ({ children }) => <td className="border border-line px-3 py-2 align-top">{children}</td>,
  pre: ({ children }) => (
    <pre className="mb-5 overflow-x-auto rounded border border-line bg-bg-raised p-4 font-mono text-sm leading-snug">{children}</pre>
  ),
  code: ({ className, children }) => {
    const text = String(children)
    const isBlock = /language-/.test(className ?? '') || text.includes('\n')
    return isBlock
      ? <code className={`font-mono ${className ?? ''}`}>{children}</code>
      : <code className="rounded bg-bg-raised px-1.5 py-0.5 font-mono text-[0.9em]">{children}</code>
  },
}

export function MarkdownView({ markdown }: { markdown: string }) {
  return (
    <div className="text-base">
      <ReactMarkdown remarkPlugins={[remarkGfm]} components={components}>{markdown}</ReactMarkdown>
    </div>
  )
}
