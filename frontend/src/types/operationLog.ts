/**
 * Operation Log Types
 */

// ---- List VO (abbreviated — no requestParams/responseResult) ----
export interface OperationLogVO {
  id: number
  username: string
  moduleName: string
  operationType: string
  operationDesc: string
  requestMethod: string
  requestUrl: string
  status: number
  costTime: number
  ipAddress: string
  createdAt: string
}

// ---- Detail VO (includes full request/response data) ----
export interface OperationLogDetailVO extends OperationLogVO {
  requestParams?: string
  responseResult?: string
  userAgent?: string
  errorMessage?: string
}

// ---- Query ----
export interface OperationLogQuery {
  pageNo?: number
  pageSize?: number
  username?: string
  moduleName?: string
  operationType?: string
  requestMethod?: string
  status?: number
  startTime?: string
  endTime?: string
}
