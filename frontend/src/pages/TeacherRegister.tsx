import { type FormEvent, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import type { AxiosError } from 'axios'
import { registerTeacher } from '../api/auth'
import type { ApiError } from '../api/types'
import { AuthShell } from '../components/AuthShell'
import { PasswordField } from '../components/PasswordField'
import { isEmail } from '../lib/validation'

const SUGGESTED_SUBJECTS = [
  'Java', 'Spring Boot', 'Spring Security', 'SQL / PostgreSQL', 'DSA', 'Microservices', 'System Design',
  'Kafka', 'Redis', 'Docker', 'Quantitative Aptitude', 'Reasoning', 'English', 'General Awareness',
]
const MAX_SUBJECTS = 8
const BIO_MIN = 30
const BIO_MAX = 600

const STEPS = ['Account', 'Expertise', 'Review']
// Which step each backend field error belongs to, so a server-side rejection jumps to the right place.
const FIELD_STEP: Record<string, number> = {
  fullName: 0, email: 0, password: 0, subjects: 1, experienceYears: 1, qualification: 1, organization: 1, bio: 2,
}

function experienceLabel(y: number) {
  if (y === 0) return 'New to teaching'
  if (y <= 2) return 'Early career'
  if (y <= 6) return 'Experienced'
  if (y <= 14) return 'Senior'
  return 'Veteran'
}

export function TeacherRegisterPage() {
  const [step, setStep] = useState(0)
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [subjects, setSubjects] = useState<string[]>([])
  const [custom, setCustom] = useState('')
  const [years, setYears] = useState(2)
  const [qualification, setQualification] = useState('')
  const [organization, setOrganization] = useState('')
  const [bio, setBio] = useState('')
  const [agreed, setAgreed] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [done, setDone] = useState<string | null>(null)
  const [shakeKey, setShakeKey] = useState(0)
  const [tried, setTried] = useState(false) // show "what's missing" only after a Next attempt

  const missing = useMemo(() => {
    const m: string[][] = [[], [], []]
    if (fullName.trim().length < 2) m[0].push('your full name')
    if (!isEmail(email)) m[0].push('a valid email')
    if (password.length < 8) m[0].push('a password of 8+ characters')
    else if (confirm !== password) m[0].push('matching passwords')
    if (subjects.length === 0) m[1].push('at least one subject')
    if (qualification.trim().length < 2) m[1].push('your qualification')
    if (bio.trim().length < BIO_MIN) m[2].push(`a short bio (${BIO_MIN}+ characters)`)
    if (!agreed) m[2].push('the confirmation tick')
    return m
  }, [fullName, email, password, confirm, subjects, qualification, bio, agreed])

  const stepOk = (i: number) => missing[i].length === 0

  function toggleSubject(s: string) {
    setSubjects((prev) => prev.includes(s) ? prev.filter((x) => x !== s) : prev.length >= MAX_SUBJECTS ? prev : [...prev, s])
  }
  function addCustom() {
    const v = custom.trim().slice(0, 60)
    if (!v) return
    if (!subjects.some((s) => s.toLowerCase() === v.toLowerCase()) && subjects.length < MAX_SUBJECTS) setSubjects([...subjects, v])
    setCustom('')
  }

  function next() {
    setTried(true)
    if (!stepOk(step)) { setShakeKey((k) => k + 1); return }
    setTried(false)
    setStep((s) => Math.min(s + 1, 2))
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (step < 2) { next(); return }
    setTried(true)
    if (!stepOk(2)) { setShakeKey((k) => k + 1); return }
    setError(null)
    setSubmitting(true)
    try {
      const res = await registerTeacher({
        fullName: fullName.trim(), email: email.trim(), password, subjects, experienceYears: years,
        qualification: qualification.trim(), organization: organization.trim() || undefined, bio: bio.trim(),
      })
      setDone(res.email)
    } catch (err) {
      const apiError = (err as AxiosError<ApiError>).response?.data
      const [rawField, msg] = Object.entries(apiError?.fieldErrors ?? {})[0] ?? []
      const field = rawField?.replace(/\[\d+\]$/, '') // list items report as e.g. subjects[0]
      if (field && field in FIELD_STEP) setStep(FIELD_STEP[field])
      else if (/already exists/i.test(apiError?.message ?? '')) setStep(0)
      setError(msg ?? apiError?.message ?? 'Something went wrong. Please try again.')
      setShakeKey((k) => k + 1)
    } finally {
      setSubmitting(false)
    }
  }

  if (done) return <Submitted email={done} />

  const bioLen = bio.trim().length
  return (
    <AuthShell
      wide brandClass="brand-teacher" tab="teacher"
      eyebrow="Teacher application" title="Share what you know"
      blurb="Write questions, build mock exams and see how your content performs. Teacher access is granted after a quick admin review."
      bullets={['Question editor with versioning', 'Submit content for review before it goes live', 'See attempt counts and accuracy per question']}
    >
      {/* stepper */}
      <ol className="mb-6 flex items-center" aria-label="Application progress">
        {STEPS.map((label, i) => (
          <li key={label} className="flex flex-1 items-center last:flex-none">
            <button type="button" disabled={i >= step} onClick={() => setStep(i)}
                    className="flex items-center gap-2" aria-current={i === step ? 'step' : undefined}>
              <span className="step-dot" data-state={i < step ? 'done' : i === step ? 'active' : 'idle'}>{i < step ? '✓' : i + 1}</span>
              <span className={`hidden text-sm font-semibold sm:inline ${i === step ? 'text-ink' : 'text-muted'}`}>{label}</span>
            </button>
            {i < STEPS.length - 1 && <div className="step-line mx-3" data-done={i < step} />}
          </li>
        ))}
      </ol>

      <div key={shakeKey} className={shakeKey > 0 && (error || (tried && !stepOk(step))) ? 'shake' : ''}>
        {error && <p role="alert" className="badge-red mb-4 block rounded-lg px-3 py-2 text-sm">{error}</p>}

        <form onSubmit={handleSubmit} noValidate>
          <div key={step} className="slide-in space-y-4">
            {step === 0 && (
              <>
                <div>
                  <h2 className="font-serif text-2xl font-semibold">Create your account</h2>
                  <p className="text-sm text-muted">You can sign in as a student straight away while we review your application.</p>
                </div>
                <div>
                  <label className="label" htmlFor="t-name">Full name</label>
                  <input id="t-name" className="input" autoComplete="name" value={fullName} onChange={(e) => setFullName(e.target.value)} placeholder="As you'd like students to see it" />
                </div>
                <div>
                  <label className="label" htmlFor="t-email">Email</label>
                  <input id="t-email" type="email" className="input" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" />
                  {email.length > 3 && <p className={`mt-1 text-xs ${isEmail(email) ? 'text-green' : 'text-red'}`}>{isEmail(email) ? '✓ Looks good' : 'That email doesn’t look right'}</p>}
                </div>
                <PasswordField id="t-password" value={password} onChange={setPassword} autoComplete="new-password" placeholder="At least 8 characters" showStrength />
                <div>
                  <label className="label" htmlFor="t-confirm">Confirm password</label>
                  <input id="t-confirm" type="password" className="input" autoComplete="new-password" value={confirm} onChange={(e) => setConfirm(e.target.value)} placeholder="Type it again" />
                  {confirm.length > 0 && <p className={`mt-1 text-xs ${confirm === password ? 'text-green' : 'text-red'}`}>{confirm === password ? '✓ Passwords match' : 'Passwords don’t match yet'}</p>}
                </div>
              </>
            )}

            {step === 1 && (
              <>
                <div>
                  <h2 className="font-serif text-2xl font-semibold">What do you teach?</h2>
                  <p className="text-sm text-muted">Pick up to {MAX_SUBJECTS} subjects. These help admins route you to the right content.</p>
                </div>
                <div>
                  <div className="flex flex-wrap gap-2" role="group" aria-label="Subjects">
                    {[...SUGGESTED_SUBJECTS, ...subjects.filter((s) => !SUGGESTED_SUBJECTS.includes(s))].map((s) => (
                      <button key={s} type="button" className="chip" aria-pressed={subjects.includes(s)} onClick={() => toggleSubject(s)}
                              disabled={!subjects.includes(s) && subjects.length >= MAX_SUBJECTS}>
                        {subjects.includes(s) && <span aria-hidden className="pop">✓</span>}{s}
                      </button>
                    ))}
                  </div>
                  <div className="mt-3 flex gap-2">
                    <input className="input" placeholder="Something else? Type and press Add" value={custom} maxLength={60}
                           onChange={(e) => setCustom(e.target.value)}
                           onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addCustom() } }} />
                    <button type="button" className="btn btn-ghost" onClick={addCustom} disabled={!custom.trim() || subjects.length >= MAX_SUBJECTS}>Add</button>
                  </div>
                  <p className="mt-1 text-xs text-muted">{subjects.length}/{MAX_SUBJECTS} selected</p>
                </div>

                <div>
                  <label className="label" htmlFor="t-years">Teaching / industry experience</label>
                  <input id="t-years" type="range" min={0} max={30} value={years} onChange={(e) => setYears(Number(e.target.value))} className="w-full" />
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-semibold text-ink">{years === 30 ? '30+' : years} year{years === 1 ? '' : 's'}</span>
                    <span className="badge badge-accent">{experienceLabel(years)}</span>
                  </div>
                </div>

                <div>
                  <label className="label" htmlFor="t-qual">Highest qualification</label>
                  <input id="t-qual" className="input" value={qualification} maxLength={200} onChange={(e) => setQualification(e.target.value)} placeholder="e.g. MCA, B.Tech (CSE), M.Sc Mathematics" />
                </div>
                <div>
                  <label className="label" htmlFor="t-org">Organisation <span className="font-normal text-muted">(optional)</span></label>
                  <input id="t-org" className="input" value={organization} maxLength={200} onChange={(e) => setOrganization(e.target.value)} placeholder="Where you work or teach" />
                </div>
              </>
            )}

            {step === 2 && (
              <>
                <div>
                  <h2 className="font-serif text-2xl font-semibold">Tell us about you</h2>
                  <p className="text-sm text-muted">A few lines on your background and what you'd like to contribute.</p>
                </div>
                <div>
                  <label className="label" htmlFor="t-bio">Short bio</label>
                  <textarea id="t-bio" rows={4} className="input resize-y" maxLength={BIO_MAX} value={bio} onChange={(e) => setBio(e.target.value)}
                            placeholder="I have taught Java and Spring Boot for … and would like to write questions on …" />
                  <div className="mt-1 flex items-center gap-3">
                    <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-line">
                      <div className="h-full rounded-full transition-all duration-300"
                           style={{ width: `${Math.min(100, (bioLen / BIO_MIN) * 100)}%`, background: bioLen >= BIO_MIN ? 'var(--solid-green)' : 'var(--solid-amber)' }} />
                    </div>
                    <span className={`text-xs ${bioLen >= BIO_MIN ? 'text-green' : 'text-muted'}`}>{bioLen}/{BIO_MAX}{bioLen < BIO_MIN ? ` · ${BIO_MIN - bioLen} more` : ''}</span>
                  </div>
                </div>

                <div className="card p-4">
                  <div className="mb-2 flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="truncate font-semibold">{fullName.trim() || 'Your name'}</div>
                      <div className="truncate text-xs text-muted">{qualification.trim() || 'Qualification'}{organization.trim() ? ` · ${organization.trim()}` : ''}</div>
                    </div>
                    <span className="badge badge-accent shrink-0">{years === 30 ? '30+' : years} yr · {experienceLabel(years)}</span>
                  </div>
                  <div className="mb-2 flex flex-wrap gap-1.5">{subjects.map((s) => <span key={s} className="badge badge-muted">{s}</span>)}</div>
                  <p className="line-clamp-3 text-sm text-ink-soft">{bio.trim() || 'Your bio will appear here.'}</p>
                  <div className="mt-3 flex gap-3 border-t border-line pt-2 text-xs">
                    <button type="button" className="font-semibold text-amber" onClick={() => setStep(0)}>Edit account</button>
                    <button type="button" className="font-semibold text-amber" onClick={() => setStep(1)}>Edit expertise</button>
                  </div>
                </div>

                <label className="flex cursor-pointer items-start gap-2 text-sm text-ink-soft">
                  <input type="checkbox" className="mt-1" checked={agreed} onChange={(e) => setAgreed(e.target.checked)} />
                  I confirm these details are accurate and understand an administrator will review my application before teacher access is enabled.
                </label>
              </>
            )}
          </div>

          {tried && !stepOk(step) && (
            <p role="alert" className="mt-4 text-sm text-red">Still needed: {missing[step].join(', ')}.</p>
          )}

          <div className="mt-6 flex items-center gap-3">
            {step > 0 && <button type="button" className="btn btn-ghost" onClick={() => { setStep(step - 1); setTried(false) }}>Back</button>}
            {step < 2
              ? <button type="submit" className="btn btn-primary flex-1">Continue</button>
              : <button type="submit" className="btn btn-primary flex-1" disabled={submitting}>{submitting ? 'Submitting…' : 'Submit application'}</button>}
          </div>
        </form>
      </div>

      <p className="mt-5 text-center text-sm text-muted">
        Already approved? <Link to="/teacher/login" className="font-semibold text-amber">Teacher sign-in</Link>
        {' · '}
        <Link to="/register" className="font-semibold text-amber">Student sign-up</Link>
      </p>
    </AuthShell>
  )
}

function Submitted({ email }: { email: string }) {
  const steps = [
    { t: 'Application submitted', d: 'Your account exists and your details are with the admins.', state: 'done' },
    { t: 'Admin review', d: 'An administrator checks your background and subjects.', state: 'active' },
    { t: 'Teacher access enabled', d: 'Sign in again on the teacher portal once you are approved.', state: 'idle' },
  ]
  return (
    <AuthShell
      brandClass="brand-teacher" eyebrow="Teacher application" title="Thanks — we've got it"
      blurb="You'll be able to author questions and mock exams as soon as an admin approves your application."
      bullets={['Sign in as a student in the meantime', 'Approval is manual, so it isn’t instant']}
    >
      <div className="text-center">
        <div className="pop mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-solid-green text-3xl text-white" aria-hidden>✓</div>
        <h2 className="mb-1 font-serif text-2xl font-semibold">Application submitted</h2>
        <p className="mb-6 text-sm text-muted">Account created for <span className="font-semibold text-ink">{email}</span>.</p>
      </div>
      <ol className="mb-6 space-y-4">
        {steps.map((s, i) => (
          <li key={s.t} className="float-in flex items-start gap-3" style={{ animationDelay: `${200 + i * 150}ms` }}>
            <span className="step-dot shrink-0" data-state={s.state}>{s.state === 'done' ? '✓' : i + 1}</span>
            <div><div className="text-sm font-semibold">{s.t}</div><div className="text-sm text-muted">{s.d}</div></div>
          </li>
        ))}
      </ol>
      <div className="flex flex-col gap-2 sm:flex-row">
        <Link to="/login" className="btn btn-primary flex-1">Sign in as a student</Link>
        <Link to="/" className="btn btn-ghost flex-1">Back to home</Link>
      </div>
    </AuthShell>
  )
}
