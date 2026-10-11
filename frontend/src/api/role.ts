/**
 * Role Management API (ADMIN only)
 */
import request from './request'
import type { PageResult } from '@/types/api'
import type { RoleOptionVO } from '@/types/role'

const BASE = '/system/roles'

// ---- Pagination ----
export function getRolePage(params?: Record<string, any>): Promise<PageResult<any>> {
  return request.get(BASE, { params })
}

// ---- Options (dropdown) — returns {id, roleName, roleCode}[] ----
export function getRoleOptions(): Promise<RoleOptionVO[]> {
  return request.get(`${BASE}/options`)
}

// ---- Detail ----
export function getRoleDetail(id: number): Promise<any> {
  return request.get(`${BASE}/${id}`)
}

export function getRoleUsers(id: number): Promise<any[]> {
  return request.get(`${BASE}/${id}/users`)
}

// ---- Create ----
export function createRole(data: Record<string, any>): Promise<any> {
  return request.post(BASE, data)
}

// ---- Update ----
export function updateRole(id: number, data: Record<string, any>): Promise<any> {
  return request.put(`${BASE}/${id}`, data)
}

// ---- Delete ----
export function deleteRole(id: number): Promise<any> {
  return request.delete(`${BASE}/${id}`)
}
