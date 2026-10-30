import { apiClient } from './client'
import type {
  AttemptQuestionDetailResponse,
  AttemptResultResponse,
  PaletteResponse,
  StartAttemptResponse,
} from './types'

export async function startAttempt(examId: string): Promise<StartAttemptResponse> {
  const { data } = await apiClient.post<StartAttemptResponse>('/attempts/start', null, { params: { examId } })
  return data
}

export async function fetchQuestion(attemptId: string, attemptQuestionId: string): Promise<AttemptQuestionDetailResponse> {
  const { data } = await apiClient.get<AttemptQuestionDetailResponse>(
    `/attempts/${attemptId}/questions/${attemptQuestionId}`,
  )
  return data
}

export async function submitAnswer(
  attemptId: string,
  attemptQuestionId: string,
  payload: { selectedOptionIds?: string[]; freeTextAnswer?: string },
): Promise<void> {
  await apiClient.put(`/attempts/${attemptId}/questions/${attemptQuestionId}/answer`, payload)
}

export async function clearAnswer(attemptId: string, attemptQuestionId: string): Promise<void> {
  await apiClient.delete(`/attempts/${attemptId}/questions/${attemptQuestionId}/answer`)
}

export async function markForReview(attemptId: string, attemptQuestionId: string, marked: boolean): Promise<void> {
  await apiClient.put(`/attempts/${attemptId}/questions/${attemptQuestionId}/mark`, { marked })
}

export async function fetchPalette(attemptId: string): Promise<PaletteResponse> {
  const { data } = await apiClient.get<PaletteResponse>(`/attempts/${attemptId}/palette`)
  return data
}

export async function submitAttempt(attemptId: string): Promise<AttemptResultResponse> {
  const { data } = await apiClient.post<AttemptResultResponse>(`/attempts/${attemptId}/submit`)
  return data
}

export async function fetchResult(attemptId: string): Promise<AttemptResultResponse> {
  const { data } = await apiClient.get<AttemptResultResponse>(`/attempts/${attemptId}/result`)
  return data
}
