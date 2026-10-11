/**
 * Opportunity Management Types
 */

export interface OpportunityVO {
  id: number
  opportunityName: string
  customerId: number
  customerName?: string
  stage: number
  stageName?: string
  amount?: number
  probability?: number
  expectedDate?: string
  ownerId?: number
  ownerName?: string
  winTime?: string
  closeTime?: string
  createdAt: string
  updatedAt?: string
}

export interface OpportunityDetailVO extends OpportunityVO {
  remark?: string
  loseReason?: string
}

export interface OpportunityQuery {
  pageNo?: number
  pageSize?: number
  customerId?: number
  opportunityName?: string
  stage?: number
  ownerId?: number
  minAmount?: number
  maxAmount?: number
  expectedStartDate?: string
  expectedEndDate?: string
}

export interface OpportunityCreateDTO {
  customerId: number
  opportunityName: string
  stage?: number
  amount?: number
  probability?: number
  expectedDate?: string
  ownerId: number
  remark?: string
}

export interface OpportunityUpdateDTO {
  opportunityName?: string
  amount?: number
  probability?: number
  expectedDate?: string
  ownerId?: number
  remark?: string
}

export interface OpportunityStageDTO {
  stage: 1 | 2 | 3 | 4
  probability?: number
}

export interface OpportunityWinDTO {
  remark?: string
}

export interface OpportunityLoseDTO {
  loseReason: string
  remark?: string
}
