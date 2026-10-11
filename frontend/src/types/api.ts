/**
 * Unified API response wrapper (back-end: com.crm.common.Result)
 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * Pagination response (back-end: com.crm.common.PageResult)
 */
export interface PageResult<T> {
  total: number
  pageNo: number
  pageSize: number
  records: T[]
}

/**
 * Pagination query parameters
 */
export interface PageQuery {
  pageNo?: number
  pageSize?: number
}

/**
 * Generic option item (dropdown / select)
 */
export interface OptionItem {
  label: string
  value: string | number
}
