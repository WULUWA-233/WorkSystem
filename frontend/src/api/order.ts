/**
 * Order Management API (ADMIN / SALES)
 */
import request from './request'
import type { PageResult } from '@/types/api'
import type { OrderVO, OrderDetailVO, OrderStatusDTO } from '@/types/order'

const BASE = '/sales/orders'

// ---- Pagination ----
export function getOrderPage(params?: Record<string, any>): Promise<PageResult<OrderVO>> {
  return request.get(BASE, { params })
}

// ---- Detail ----
export function getOrderDetail(id: number): Promise<OrderDetailVO> {
  return request.get(`${BASE}/${id}`)
}

// ---- Create ----
export function createOrder(data: Record<string, any>): Promise<any> {
  return request.post(BASE, data)
}

// ---- Update ----
export function updateOrder(id: number, data: Record<string, any>): Promise<any> {
  return request.put(`${BASE}/${id}`, data)
}

// ---- Delete ----
export function deleteOrder(id: number): Promise<any> {
  return request.delete(`${BASE}/${id}`)
}

// ---- Status Update ----
export function updateOrderStatus(id: number, data: OrderStatusDTO): Promise<any> {
  return request.patch(`${BASE}/${id}/status`, data)
}

// ---- Pay ----
export function payOrder(id: number, data: { paymentMethod: string }): Promise<any> {
  return request.patch(`${BASE}/${id}/pay`, data)
}

// ---- Ship ----
export function shipOrder(id: number, data: { logisticsNo: string; logisticsCompany?: string }): Promise<any> {
  return request.patch(`${BASE}/${id}/ship`, data)
}

// ---- Complete ----
export function completeOrder(id: number): Promise<any> {
  return request.patch(`${BASE}/${id}/complete`, {})
}

// ---- Cancel ----
export function cancelOrder(id: number): Promise<any> {
  return request.patch(`${BASE}/${id}/cancel`, {})
}
