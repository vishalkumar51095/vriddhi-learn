import { apiClient } from './client'

export interface RecordResultPayload {
  questionId: string
  topicId: string
  topicName: string
  subjectName: string
  correct: boolean
}

export interface TopicProgress {
  topicId: string
  topicName: string
  subjectName: string
  attempts: number
  correct: number
  accuracy: number
  lastPracticedAt: string
}

export interface ProgressSummary {
  totalAttempts: number
  totalCorrect: number
  accuracy: number
  streakDays: number
  answeredToday: number
  dueForReview: number
  topics: TopicProgress[]
}

export type DailyPlanReason = 'DUE_FOR_REVIEW' | 'WEAK_TOPIC' | 'NEW_TOPIC' | 'MIXED_REVIEW'

export interface DailyPlanItem {
  kind: 'QUESTION' | 'TOPIC'
  questionId: string | null
  topicId: string
  topicName: string
  subjectName: string
  count: number
  reason: DailyPlanReason
}

export interface DailyPlan {
  date: string
  categoryCode: string | null
  totalQuestions: number
  items: DailyPlanItem[]
}

export async function recordResult(payload: RecordResultPayload): Promise<void> {
  await apiClient.post('/progress/results', payload)
}

export async function fetchProgressSummary(): Promise<ProgressSummary> {
  const { data } = await apiClient.get<ProgressSummary>('/progress/summary')
  return data
}

export async function fetchDailyPlan(category?: string): Promise<DailyPlan> {
  const { data } = await apiClient.get<DailyPlan>('/progress/daily-plan', { params: category ? { category } : undefined })
  return data
}
