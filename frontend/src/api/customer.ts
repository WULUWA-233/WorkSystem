/**
 * Customer Management API (ADMIN / SALES)
 */
import request from './request'
import type { PageQuery, PageResult } from '@/types/api'
import type { CustomerVO, CustomerDetailVO } from '@/types/customer'
import type { OpportunityVO } from '@/types/opportunity'
import type { FollowRecordVO } from '@/types/followRecord'
import type { OrderVO } from '@/types/order'

const BASE = '/customer/customers'

// ---- Pagination ----
export function getCustomerPage(params?: Record<string, any>): Promise<PageResult<CustomerVO>> {
  return request.get(BASE, { params })
}

// ---- Detail ----
export function getCustomerDetail(id: number): Promise<CustomerDetailVO> {
  return request.get(`${BASE}/${id}`)
}

// ---- Create ----
export function createCustomer(data: Record<string, any>): Promise<any> {
  return request.post(BASE, data)
}

// ---- Update ----
export function updateCustomer(id: number, data: Record<string, any>): Promise<any> {
  return request.put(`${BASE}/${id}`, data)
}

// ---- Delete (logical) ----
export function deleteCustomer(id: number): Promise<any> {
  return request.delete(`${BASE}/${id}`)
}

// ---- Status Change ----
export function changeCustomerStatus(id: number, status: number): Promise<any> {
  return request.patch(`${BASE}/${id}/status`, { status })
}

// ---- Check Name Duplicate ----
export function checkCustomerName(params: { customerName: string; excludeId?: number }): Promise<{ exists: boolean }> {
  return request.get(`${BASE}/check-name`, { params })
}

// ---- Customer associated opportunities ----
export function getCustomerOpportunities(customerId: number): Promise<OpportunityVO[]> {
  return request.get(`${BASE}/${customerId}/opportunities`)
}

// ---- Customer associated follow records ----
export function getCustomerFollowRecords(customerId: number, params?: PageQuery): Promise<PageResult<FollowRecordVO>> {
  return request.get(`${BASE}/${customerId}/follow-records`, { params })
}

// ---- Customer associated orders ----
export function getCustomerOrders(customerId: number, params?: PageQuery): Promise<PageResult<OrderVO>> {
  return request.get(`${BASE}/${customerId}/orders`, { params })
}
