/**
 * Analytics / Dashboard API
 */
import request from './request'

// ---- Overview ----
export function getOverview(params?: { startTime?: string; endTime?: string }): Promise<any> {
  return request.get('/analytics/overview', { params })
}

// ---- Customer Trend ----
export function getCustomerTrend(params?: {
  startDate?: string
  endDate?: string
  groupBy?: 'day' | 'month'
}): Promise<any> {
  return request.get('/analytics/customers/new', { params })
}

// ---- Opportunity Stage ----
export function getOpportunityStage(params?: { startTime?: string; endTime?: string }): Promise<any> {
  return request.get('/analytics/opportunities/stage', { params })
}

// ---- Order Status ----
export function getOrderStatus(params?: { startTime?: string; endTime?: string }): Promise<any> {
  return request.get('/analytics/orders/status', { params })
}

// ---- Sales Performance ----
export function getSalesPerformance(params?: { ownerId?: number; startTime?: string; endTime?: string }): Promise<any> {
  return request.get('/analytics/sales/performance', { params })
}

// ---- Industry Distribution ----
export function getCustomerIndustry(params?: { startTime?: string; endTime?: string }): Promise<any> {
  return request.get('/analytics/customers/industry', { params })
}

// ---- Region Distribution ----
export function getCustomerRegion(params?: { startTime?: string; endTime?: string }): Promise<any> {
  return request.get('/analytics/customers/region', { params })
}
