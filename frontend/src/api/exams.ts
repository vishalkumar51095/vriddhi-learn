import { apiClient } from './client'
import type { ExamDetailResponse, ExamSummaryResponse } from './types'

export async function fetchExams(category?: string): Promise<{ content: ExamSummaryResponse[] }> {
  const { data } = await apiClient.get('/exams', { params: category ? { category } : {} })
  return data
}

export async function fetchExamDetail(examId: string): Promise<ExamDetailResponse> {
  const { data } = await apiClient.get<ExamDetailResponse>(`/exams/${examId}`)
  return data
}
