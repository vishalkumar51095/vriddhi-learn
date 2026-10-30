// Mirrors the DTOs returned by auth-service / content-service / exam-service.

export interface UserResponse {
  id: string
  email: string
  fullName: string
  status: string
  roles: string[]
  createdAt: string
  lastLoginAt: string | null
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  expiresInSeconds: number
  user: UserResponse
}

export interface SubjectResponse {
  id: string
  name: string
  description: string | null
}

export interface TopicResponse {
  id: string
  subjectId: string
  name: string
  description: string | null
}

export interface QuestionOptionResponse {
  id: string
  text: string
  correct: boolean | null
}

export interface QuestionResponse {
  id: string
  topicId: string
  topicName: string
  subjectName: string
  type: 'SINGLE_CORRECT' | 'MULTIPLE_CORRECT' | 'TRUE_FALSE' | 'FILL_BLANK' | 'CODING' | 'SUBJECTIVE'
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  status: string
  versionNumber: number
  questionText: string
  codeSnippet: string | null
  explanation: string | null
  options: QuestionOptionResponse[]
  tags: string[]
  updatedAt: string
}

export interface ExamSummaryResponse {
  id: string
  title: string
  categoryCode: string
  durationMinutes: number
  sectionCount: number
  totalQuestions: number
  status: string
}

export interface ExamDetailResponse {
  id: string
  title: string
  categoryCode: string
  durationMinutes: number
  negativeMarking: number
  navigationMode: 'FREE' | 'FORWARD_ONLY' | 'SECTION_LOCKED'
  status: string
  sections: { id: string; name: string; questionCount: number }[]
}

export interface AttemptQuestionSummary {
  attemptQuestionId: string
  sectionId: string
  sectionName: string
  sortOrder: number
  status: AnswerStatus
}

export interface StartAttemptResponse {
  attemptId: string
  expiresAt: string
  navigationMode: 'FREE' | 'FORWARD_ONLY' | 'SECTION_LOCKED'
  questions: AttemptQuestionSummary[]
}

export type AnswerStatus = 'NOT_VISITED' | 'NOT_ANSWERED' | 'ANSWERED' | 'MARKED' | 'ANSWERED_MARKED'

export interface AttemptQuestionDetailResponse {
  attemptQuestionId: string
  sectionName: string
  sortOrder: number
  questionType: string
  questionText: string
  codeSnippet: string | null
  options: { id: string; text: string }[]
  status: AnswerStatus
  selectedOptionIds: string[]
  freeTextAnswer: string | null
  secondsRemaining: number
}

export interface PaletteItem {
  attemptQuestionId: string
  sectionName: string
  sortOrder: number
  status: AnswerStatus
}

export interface PaletteResponse {
  secondsRemaining: number
  items: PaletteItem[]
}

export interface AttemptResultResponse {
  attemptId: string
  status: 'SUBMITTED' | 'AUTO_SUBMITTED'
  score: number
  totalQuestions: number
  correctCount: number
  wrongCount: number
  skippedCount: number
  manualPendingCount: number
  startedAt: string
  submittedAt: string
  sectionResults: { sectionName: string; correct: number; gradable: number }[]
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  fieldErrors?: Record<string, string>
}
