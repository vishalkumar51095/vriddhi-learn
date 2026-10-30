import { PortalLogin } from '../components/PortalLogin'

export function TeacherLoginPage() {
  return (
    <PortalLogin
      config={{
        portal: 'TEACHER',
        brandClass: 'brand-teacher',
        eyebrow: 'Teacher sign-in',
        title: 'Author & review content',
        blurb: 'Write questions, build mock exams, and track how your content performs once it\u2019s published.',
        bullets: ['Question editor with versioning', 'Submit content for review before it goes live', 'See attempt counts and accuracy per question'],
        registerTo: '/teacher/register',
        registerLabel: 'Apply to teach',
        homeAfterLogin: '/teacher',
        otherPortals: [
          { label: 'Student sign-in', to: '/login' },
          { label: 'Admin sign-in', to: '/admin/login' },
        ],
      }}
    />
  )
}
