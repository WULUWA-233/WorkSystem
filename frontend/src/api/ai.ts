/**
 * AI Analysis API
 */
import request from './request'
import type { CustomerAiAnalysisVO } from '@/types/ai'

/**
 * Get AI analysis for a customer
 */
export function getCustomerAnalysis(customerId: number): Promise<CustomerAiAnalysisVO> {
  return request.get(`/ai/customers/${customerId}/analysis`)
}
