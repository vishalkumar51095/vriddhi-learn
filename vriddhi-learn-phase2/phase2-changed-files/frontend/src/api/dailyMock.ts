import { apiClient } from './client'

export type DailyMockState = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'

export interface DailyMockStatus {
  state: DailyMockState
  examId: string | null
  attemptId: string | null
  expiresAt: string | null
  durationMinutes: number | null
  totalQuestions: number | null
  score: number | null
  correctCount: number | null
}

/** Starts today's timed mock, or resumes / reports the one that already exists (one per learner, category and day). */
export async function startDailyMock(category: string): Promise<DailyMockStatus> {
  const { data } = await apiClient.post<DailyMockStatus>('/attempts/daily', null, { params: { category } })
  return data
}

export async function fetchDailyMockToday(category: string): Promise<DailyMockStatus> {
  const { data } = await apiClient.get<DailyMockStatus>('/attempts/daily/today', { params: { category } })
  return data
}
