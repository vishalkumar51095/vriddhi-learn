import { apiClient } from './client'

export interface PublicUserStats { learners: number; teachers: number }
export interface PublicContentStats { publishedQuestions: number; subjects: number; topics: number; categories: number }
export interface PublicExamStats { publishedExams: number; attemptsTaken: number }
export interface PublicExam {
  id: string; title: string; categoryCode: string; durationMinutes: number
  navigationMode: string; sections: number; totalQuestions: number
}

// These three hit different services behind the gateway; requested in parallel by callers.
export const fetchPublicUserStats = () => apiClient.get<PublicUserStats>('/public/stats/users').then((r) => r.data)
export const fetchPublicContentStats = () => apiClient.get<PublicContentStats>('/public/stats/content').then((r) => r.data)
export const fetchPublicExamStats = () => apiClient.get<PublicExamStats>('/public/stats/exams').then((r) => r.data)
export const fetchPublicExams = () => apiClient.get<PublicExam[]>('/public/exams').then((r) => r.data)
