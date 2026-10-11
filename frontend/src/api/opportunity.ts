/**
 * Opportunity Management API (ADMIN / SALES)
 */
import request from './request'
import type { PageQuery, PageResult } from '@/types/api'
import type { OpportunityVO, OpportunityDetailVO } from '@/types/opportunity'
import type { FollowRecordVO } from '@/types/followRecord'
import type { OrderVO } from '@/types/order'

const BASE = '/sales/opportunities'

// ---- Pagination ----
export function getOpportunityPage(params?: Record<string, any>): Promise<PageResult<OpportunityVO>> {
  return request.get(BASE, { params })
}

// ---- Detail ----
export function getOpportunityDetail(id: number): Promise<OpportunityDetailVO> {
  return request.get(`${BASE}/${id}`)
}

// ---- Create ----
export function createOpportunity(data: Record<string, any>): Promise<any> {
  return request.post(BASE, data)
}

// ---- Update ----
export function updateOpportunity(id: number, data: Record<string, any>): Promise<any> {
  return request.put(`${BASE}/${id}`, data)
}

// ---- Delete (logical) ----
export function deleteOpportunity(id: number): Promise<any> {
  return request.delete(`${BASE}/${id}`)
}

// ---- Stage Advance ----
export function advanceOpportunityStage(id: number, data: { stage: number; probability?: number }): Promise<any> {
  return request.patch(`${BASE}/${id}/stage`, data)
}

// ---- Mark Won ----
export function winOpportunity(id: number, data: { remark?: string }): Promise<any> {
  return request.patch(`${BASE}/${id}/win`, data)
}

// ---- Mark Lost ----
export function loseOpportunity(id: number, data: { loseReason: string; remark?: string }): Promise<any> {
  return request.patch(`${BASE}/${id}/lose`, data)
}

// ---- Opportunity associated follow records ----
export function getOpportunityFollowRecords(opportunityId: number, params?: PageQuery): Promise<PageResult<FollowRecordVO>> {
  return request.get(`${BASE}/${opportunityId}/follow-records`, { params })
}

// ---- Opportunity associated orders ----
export function getOpportunityOrders(opportunityId: number, params?: PageQuery): Promise<PageResult<OrderVO>> {
  return request.get(`${BASE}/${opportunityId}/orders`, { params })
}
