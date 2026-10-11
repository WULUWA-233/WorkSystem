/**
 * Follow Record Management API (ADMIN / SALES)
 */
import request from './request'
import type { PageResult } from '@/types/api'

const BASE = '/customer/follow-records'

// ---- Pagination ----
export function getFollowRecordPage(params?: Record<string, any>): Promise<PageResult<any>> {
  return request.get(BASE, { params })
}

// ---- Create ----
export function createFollowRecord(data: Record<string, any>): Promise<any> {
  return request.post(BASE, data)
}

// ---- Detail ----
export function getFollowRecordDetail(id: number): Promise<any> {
  return request.get(`${BASE}/${id}`)
}

// ---- Delete ----
export function deleteFollowRecord(id: number): Promise<any> {
  return request.delete(`${BASE}/${id}`)
}
