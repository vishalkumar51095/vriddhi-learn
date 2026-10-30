import { PortalLogin } from '../components/PortalLogin'

export function LoginPage() {
  return (
    <PortalLogin
      config={{
        portal: 'STUDENT',
        brandClass: 'brand-student',
        eyebrow: 'Student sign-in',
        title: 'Welcome back',
        blurb: 'Pick up where you left off — practice, mock exams, and progress tracking built for exam prep.',
        bullets: ['Instant-graded practice by topic', 'Timed mock exams with a real question palette', 'Section and difficulty-wise analytics'],
        registerTo: '/register',
        homeAfterLogin: '/dashboard',
        otherPortals: [
          { label: 'Teacher sign-in', to: '/teacher/login' },
          { label: 'Admin sign-in', to: '/admin/login' },
        ],
      }}
    />
  )
}
