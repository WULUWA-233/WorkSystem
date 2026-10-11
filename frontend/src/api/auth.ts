/**
 * Auth API — login / current user / logout
 */
import request from './request'
import type { LoginRequest, LoginResponse, CurrentUser } from '@/types/auth'

export function login(data: LoginRequest): Promise<LoginResponse> {
  return request.post('/auth/login', data)
}

export function getCurrentUser(): Promise<CurrentUser> {
  return request.get('/auth/me')
}

export function logout(): Promise<string> {
  return request.post('/auth/logout')
}
