import { PortalLogin } from '../components/PortalLogin'

export function AdminLoginPage() {
  return (
    <PortalLogin
      config={{
        portal: 'ADMIN',
        brandClass: 'brand-admin',
        eyebrow: 'Administrator sign-in',
        title: 'Platform control',
        blurb: 'Users, roles, published content, exam performance, and the full audit trail in one place.',
        bullets: ['User & role management', 'Platform-wide stats and audit logs', 'Exam and content oversight'],
        homeAfterLogin: '/admin',
        otherPortals: [
          { label: 'Student sign-in', to: '/login' },
          { label: 'Teacher sign-in', to: '/teacher/login' },
        ],
      }}
    />
  )
}
