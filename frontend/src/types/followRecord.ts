/**
 * Follow Record Types
 */

export interface FollowRecordVO {
  id: number
  customerId: number
  customerName?: string
  opportunityId?: number
  opportunityName?: string
  followType: string
  followContent?: string
  followResult?: string
  customerIntention?: number
  nextPlan?: string
  nextFollowTime?: string
  followTime?: string
  followerId?: number
  followerName?: string
  createdAt: string
}

export interface FollowRecordDetailVO extends FollowRecordVO {
  remark?: string
}

export interface FollowRecordQuery {
  pageNo?: number
  pageSize?: number
  customerId?: number
  opportunityId?: number
  followerId?: number
  followType?: string
  customerIntention?: number
  startTime?: string
  endTime?: string
}

export interface FollowRecordCreateDTO {
  customerId: number
  opportunityId: number
  followType: string
  followContent: string
  followResult?: string
  customerIntention?: number
  nextPlan?: string
  nextFollowTime?: string
  followTime?: string
  followerId: number
  remark?: string
}
