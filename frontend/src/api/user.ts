/**
 * User Management API (ADMIN only)
 */
import request from './request'
import type { PageResult } from '@/types/api'

const BASE = '/system/users'

// ---- Pagination ----
export function getUserPage(params?: Record<string, any>): Promise<PageResult<any>> {
  return request.get(BASE, { params })
}

// ---- Detail ----
export function getUserDetail(id: number): Promise<any> {
  return request.get(`${BASE}/${id}`)
}

// ---- Create ----
export function createUser(data: Record<string, any>): Promise<any> {
  return request.post(BASE, data)
}

// ---- Update ----
export function updateUser(id: number, data: Record<string, any>): Promise<any> {
  return request.put(`${BASE}/${id}`, data)
}

// ---- Delete (logical) ----
export function deleteUser(id: number): Promise<any> {
  return request.delete(`${BASE}/${id}`)
}

// ---- Enable / Disable ----
export function toggleUserStatus(id: number, status: number): Promise<any> {
  return request.patch(`${BASE}/${id}/status`, { status })
}

// ---- Reset Password ----
export function resetUserPassword(id: number, newPassword: string): Promise<any> {
  return request.patch(`${BASE}/${id}/password/reset`, { newPassword })
}

// ---- Assign Roles ----
export function assignUserRoles(id: number, roleIds: number[]): Promise<any> {
  return request.put(`${BASE}/${id}/roles`, { roleIds })
}
