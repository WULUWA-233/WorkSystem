/**
 * Analytics / Dashboard Types
 */

/** Overview statistics */
export interface OverviewVO {
  customerCount?: number
  customerTotal?: number
  newCustomerCount?: number
  opportunityCount?: number
  opportunityTotal?: number
  opportunityAmount?: number
  wonOpportunityCount?: number
  lostOpportunityCount?: number
  orderCount?: number
  orderTotal?: number
  orderAmount?: number
  paidAmount?: number
  paidOrderAmount?: number
  followCount?: number
  followRecordTotal?: number
  todayFollowCount?: number
}

/** Trend data point */
export interface TrendItemVO {
  date?: string
  count?: number
}

/** Opportunity stage distribution item */
export interface StageStatsVO {
  stage?: number
  stageName?: string
  count?: number
  amount?: number
}

/** Order status distribution item */
export interface OrderStatusStatsVO {
  status?: number
  statusName?: string
  count?: number
  amount?: number
}

/** Sales performance item */
export interface SalesPerformanceVO {
  ownerId?: number
  ownerName?: string
  customerCount?: number
  opportunityCount?: number
  orderCount?: number
  orderAmount?: number
}

/** Generic name-value pair */
export interface NameValueVO {
  name?: string
  value?: number
  count?: number
  amount?: number
}

/** Common analytics query */
export interface AnalyticsQuery {
  startTime?: string
  endTime?: string
}

/** Customer trend query */
export interface CustomerTrendQuery {
  startDate?: string
  endDate?: string
  groupBy?: 'day' | 'month'
}
