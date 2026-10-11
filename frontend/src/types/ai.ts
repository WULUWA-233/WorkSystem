/**
 * AI Customer Analysis Types
 */

/** Quantitative metrics */
export interface CustomerAiMetricsVO {
  opportunityCount: number
  orderCount: number
  totalOrderAmount: number
  paidAmount: number
  followRecordCount: number
  lastFollowTime?: string
}

/** Full AI analysis result */
export interface CustomerAiAnalysisVO {
  customerId: number
  customerName: string
  customerLevel: string
  riskLevel: string
  summary: string
  riskTips: string[]
  suggestions: string[]
  metrics: CustomerAiMetricsVO
}
