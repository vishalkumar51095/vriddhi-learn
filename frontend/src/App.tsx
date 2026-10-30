import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ThemeProvider } from './context/ThemeContext'
import { ProtectedRoute, RoleProtectedRoute } from './components/ProtectedRoute'
import { HomePage } from './pages/Home'
import { AboutPage } from './pages/About'
import { LoginPage } from './pages/Login'
import { TeacherLoginPage } from './pages/TeacherLogin'
import { AdminLoginPage } from './pages/AdminLogin'
import { RegisterPage } from './pages/Register'
import { TeacherRegisterPage } from './pages/TeacherRegister'
import { AdminTeacherApplicationsPage } from './pages/admin/AdminTeacherApplications'
import { DashboardPage } from './pages/Dashboard'
import { TeacherDashboardPage } from './pages/TeacherDashboard'
import { PracticePage } from './pages/Practice'
import { ExamsPage } from './pages/Exams'
import { ExamAttemptPage } from './pages/ExamAttempt'
import { ResultsPage } from './pages/Results'
import { ProfilePage } from './pages/Profile'
import { AdminDashboardPage } from './pages/admin/AdminDashboard'
import { AdminUsersPage } from './pages/admin/AdminUsers'
import { AdminAuditLogsPage } from './pages/admin/AdminAuditLogs'
import { AdminExamsPage } from './pages/admin/AdminExams'
import { AdminContentPage } from './pages/admin/AdminContent'

export default function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            {/* Public */}
            <Route path="/" element={<HomePage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/teacher/register" element={<TeacherRegisterPage />} />

            {/* Three separate portal sign-ins — see components/PortalLogin.tsx */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/teacher/login" element={<TeacherLoginPage />} />
            <Route path="/admin/login" element={<AdminLoginPage />} />

            {/* Learner */}
            <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            <Route path="/practice" element={<ProtectedRoute><PracticePage /></ProtectedRoute>} />
            <Route path="/exams" element={<ProtectedRoute><ExamsPage /></ProtectedRoute>} />
            <Route path="/attempts/:attemptId" element={<ProtectedRoute><ExamAttemptPage /></ProtectedRoute>} />
            <Route path="/attempts/:attemptId/result" element={<ProtectedRoute><ResultsPage /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />

            {/* Teacher */}
            <Route
              path="/teacher"
              element={
                <RoleProtectedRoute roles={['TEACHER']} redirectTo="/dashboard">
                  <TeacherDashboardPage />
                </RoleProtectedRoute>
              }
            />

            {/* Admin */}
            <Route path="/admin" element={<RoleProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}><AdminDashboardPage /></RoleProtectedRoute>} />
            <Route path="/admin/users" element={<RoleProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}><AdminUsersPage /></RoleProtectedRoute>} />
            <Route path="/admin/teacher-applications" element={<RoleProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}><AdminTeacherApplicationsPage /></RoleProtectedRoute>} />
            <Route path="/admin/audit-logs" element={<RoleProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}><AdminAuditLogsPage /></RoleProtectedRoute>} />
            <Route path="/admin/exams" element={<RoleProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}><AdminExamsPage /></RoleProtectedRoute>} />
            <Route path="/admin/content" element={<RoleProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}><AdminContentPage /></RoleProtectedRoute>} />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  )
}
