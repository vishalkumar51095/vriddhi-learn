import { apiClient } from './client'

export interface LessonSummary {
  id: string
  slug: string
  title: string
  categoryCode: string
  subjectName: string
  topicId: string | null
  topicName: string | null
  sortOrder: number
  completed: boolean
}

export interface LessonDetail extends LessonSummary {
  bodyMarkdown: string
}

export async function fetchLessons(params?: { category?: string; topicId?: string }): Promise<LessonSummary[]> {
  const { data } = await apiClient.get<LessonSummary[]>('/lessons', { params })
  return data
}

export async function fetchLesson(slug: string): Promise<LessonDetail> {
  const { data } = await apiClient.get<LessonDetail>(`/lessons/${slug}`)
  return data
}

export async function setLessonCompleted(slug: string, completed: boolean): Promise<void> {
  if (completed) {
    await apiClient.post(`/lessons/${slug}/complete`)
  } else {
    await apiClient.delete(`/lessons/${slug}/complete`)
  }
}
