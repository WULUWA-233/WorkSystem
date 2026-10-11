/**
 * Operation Log API (ADMIN only)
 */
import request from './request'
import type { PageResult } from '@/types/api'

const BASE = '/system/operation-logs'

// ---- Pagination ----
export function getOperationLogPage(params?: Record<string, any>): Promise<PageResult<any>> {
  return request.get(BASE, { params })
}

// ---- Detail (includes requestParams / responseResult / userAgent) ----
export function getOperationLogDetail(id: number): Promise<any> {
  return request.get(`${BASE}/${id}`)
}

// ---- Delete (physical) ----
export function deleteOperationLog(id: number): Promise<any> {
  return request.delete(`${BASE}/${id}`)
}
