import { apiClient } from './client'
import type { QuestionResponse, SubjectResponse, TopicResponse } from './types'

export interface CategoryResponse {
  id: string
  code: string
  name: string
  description: string | null
}

export interface PracticeCheckResponse {
  manualReview: boolean
  correct: boolean | null
  correctOptionIds: string[]
  explanation: string | null
}

export async function fetchCategories(): Promise<CategoryResponse[]> {
  const { data } = await apiClient.get<CategoryResponse[]>('/categories')
  return data
}

export async function fetchSubjects(category: string): Promise<SubjectResponse[]> {
  const { data } = await apiClient.get<SubjectResponse[]>('/subjects', { params: { category } })
  return data
}

export async function fetchTopics(subjectId: string): Promise<TopicResponse[]> {
  const { data } = await apiClient.get<TopicResponse[]>('/topics', { params: { subjectId } })
  return data
}

export async function fetchPracticeQuestion(questionId: string): Promise<QuestionResponse> {
  const { data } = await apiClient.get<QuestionResponse>(`/questions/${questionId}/practice`)
  return data
}

export async function fetchRandomQuestions(topicIds: string[], count: number): Promise<QuestionResponse[]> {
  const { data } = await apiClient.get<QuestionResponse[]>('/questions/random', {
    params: { topicIds, count },
    paramsSerializer: { indexes: null },
  })
  return data
}

export async function fetchRandomQuestion(topicIds: string[]): Promise<QuestionResponse | null> {
  const questions = await fetchRandomQuestions(topicIds, 1)
  return questions[0] ?? null
}

export async function checkPracticeAnswer(
  questionId: string,
  payload: { selectedOptionIds?: string[]; freeTextAnswer?: string },
): Promise<PracticeCheckResponse> {
  const { data } = await apiClient.post<PracticeCheckResponse>(`/questions/${questionId}/practice/check`, payload)
  return data
}
