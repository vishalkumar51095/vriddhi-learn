import { apiClient, tokenStore } from './client'
import type { AuthResponse, UserResponse } from './types'

export async function register(fullName: string, email: string, password: string): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/register', { fullName, email, password })
  tokenStore.setTokens(data.accessToken, data.refreshToken)
  return data
}

export async function login(email: string, password: string, portal?: 'STUDENT' | 'TEACHER' | 'ADMIN'): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/login', { email, password, portal })
  tokenStore.setTokens(data.accessToken, data.refreshToken)
  return data
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post('/auth/logout')
  } finally {
    tokenStore.clear()
  }
}

export async function fetchCurrentUser(): Promise<UserResponse> {
  const { data } = await apiClient.get<UserResponse>('/auth/me')
  return data
}

export async function updateProfile(fullName: string): Promise<UserResponse> {
  const { data } = await apiClient.patch<UserResponse>('/auth/me', { fullName })
  return data
}

export interface TeacherApplicationPayload {
  fullName: string
  email: string
  password: string
  subjects: string[]
  experienceYears: number
  qualification: string
  organization?: string
  bio: string
}

export interface TeacherApplicationSubmitted {
  applicationId: string
  status: 'PENDING'
  email: string
}

/**
 * Creates a normal learner account plus a PENDING teacher application. Deliberately does NOT
 * sign the user in or grant TEACHER: an administrator has to approve first.
 */
export async function registerTeacher(payload: TeacherApplicationPayload): Promise<TeacherApplicationSubmitted> {
  const { data } = await apiClient.post<TeacherApplicationSubmitted>('/auth/register-teacher', payload)
  return data
}
