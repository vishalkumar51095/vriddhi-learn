import { apiClient } from './client'

// ---- shared ----
export interface PagedResponse<T> { content: T[]; totalElements: number; page: number; size: number; totalPages: number }
export interface DailyCount { date: string; count: number }

// ---- auth-service: users, roles, audit ----
export interface AdminUserRow {
  id: string; email: string; fullName: string; status: string; roles: string[]
  createdAt: string; lastLoginAt: string | null
}
export interface UserStatsResponse {
  total: number; active: number; suspended: number; pending: number
  newLast7Days: number; loginsLast24h: number; failedLoginsLast24h: number
  byRole: Record<string, number>; registrationsPerDay: DailyCount[]
}
export interface AuditLogRow {
  id: number; actorId: string | null; actorEmail: string | null; eventType: string
  targetType: string | null; targetId: string | null; details: string | null
  ipAddress: string | null; createdAt: string
}

export function fetchAdminUsers(params: { q?: string; role?: string; status?: string; page?: number; size?: number }) {
  return apiClient.get<PagedResponse<AdminUserRow>>('/admin/users', { params }).then((r) => r.data)
}
export function updateUserStatus(id: string, status: string) {
  return apiClient.patch<AdminUserRow>(`/admin/users/${id}/status`, { status }).then((r) => r.data)
}
export function updateUserRoles(id: string, roles: string[]) {
  return apiClient.patch<AdminUserRow>(`/admin/users/${id}/roles`, { roles }).then((r) => r.data)
}
export function fetchUserStats() {
  return apiClient.get<UserStatsResponse>('/admin/stats/users').then((r) => r.data)
}
export function fetchAuditLogs(params: { event?: string; q?: string; actorId?: string; page?: number; size?: number }) {
  return apiClient.get<PagedResponse<AuditLogRow>>('/admin/audit-logs', { params }).then((r) => r.data)
}

// ---- content-service: question/content stats ----
export interface AdminContentStats {
  totalQuestions: number; pendingReview: number
  byStatus: Record<string, number>; byDifficulty: Record<string, number>; byType: Record<string, number>
  createdPerDay: DailyCount[]
}
export function fetchAdminContentStats() {
  return apiClient.get<AdminContentStats>('/admin/stats/content').then((r) => r.data)
}

// ---- exam-service: exam/attempt stats ----
export interface TopExam { title: string; attempts: number; avgScore: number | null }
export interface AdminExamStats {
  totalExams: number; examsByStatus: Record<string, number>
  attemptsTotal: number; attemptsInProgress: number; attemptsSubmitted: number; attemptsAutoSubmitted: number
  avgScore: number | null; attemptsPerDay: DailyCount[]; topExams: TopExam[]
}
export interface AdminExamRow {
  id: string; title: string; categoryCode: string; durationMinutes: number; navigationMode: string
  status: string; sections: number; totalQuestions: number; attempts: number; avgScore: number | null; createdAt: string
}
export function fetchAdminExamStats() {
  return apiClient.get<AdminExamStats>('/admin/stats/exams').then((r) => r.data)
}
export function fetchAdminExams() {
  return apiClient.get<AdminExamRow[]>('/admin/exams').then((r) => r.data)
}

// ---- teacher's own content ----
export interface TeacherSummary { total: number; byStatus: Record<string, number> }
export function fetchMyQuestions(params: { page?: number; size?: number }) {
  return apiClient.get('/questions/mine', { params }).then((r) => r.data)
}
export function fetchMyQuestionsSummary() {
  return apiClient.get<TeacherSummary>('/questions/mine/summary').then((r) => r.data)
}

// ---- teacher applications (auth-service) ----
export interface TeacherApplicationRow {
  id: string; userId: string; fullName: string; email: string
  subjects: string[]; experienceYears: number; qualification: string; organization: string | null; bio: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  reviewNote: string | null; reviewedByEmail: string | null; reviewedAt: string | null; createdAt: string
}
export function fetchTeacherApplications(params: { status?: string; page?: number; size?: number }) {
  return apiClient.get<PagedResponse<TeacherApplicationRow>>('/admin/teacher-applications', { params }).then((r) => r.data)
}
export function approveTeacherApplication(id: string, note?: string) {
  return apiClient.post<TeacherApplicationRow>(`/admin/teacher-applications/${id}/approve`, { note }).then((r) => r.data)
}
export function rejectTeacherApplication(id: string, note: string) {
  return apiClient.post<TeacherApplicationRow>(`/admin/teacher-applications/${id}/reject`, { note }).then((r) => r.data)
}
