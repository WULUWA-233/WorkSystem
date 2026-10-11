/**
 * Customer Management Types
 */

export interface CustomerVO {
  id: number
  customerName: string
  industry?: string
  source?: string
  level: number
  region?: string
  address?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  ownerId?: number
  ownerName?: string
  status: number
  score?: number
  lastFollowTime?: string
  createdAt: string
  updatedAt?: string
}

export interface CustomerDetailVO extends CustomerVO {
  remark?: string
}

export interface CustomerQuery {
  pageNo?: number
  pageSize?: number
  customerName?: string
  industry?: string
  source?: string
  level?: number
  region?: string
  status?: number
  ownerId?: number
  startTime?: string
  endTime?: string
}

export interface CustomerCreateDTO {
  customerName: string
  industry?: string
  source?: string
  level?: number
  region?: string
  address?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  ownerId: number
  status?: number
  score?: number
  remark?: string
}

export interface CustomerUpdateDTO {
  customerName: string
  industry?: string
  source?: string
  level?: number
  region?: string
  address?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  ownerId?: number
  status?: number
  score?: number
  remark?: string
}

export interface CustomerStatusDTO {
  status: 0 | 1 | 2
}

export interface CustomerNameCheckResult {
  exists: boolean
}
