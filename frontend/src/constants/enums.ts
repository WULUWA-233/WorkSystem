import type { OptionItem } from '@/types/api'

// ==============================
// 1. User Status (sys_user.status)
// ==============================
export const USER_STATUS = {
  DISABLED: 0,
  ENABLED: 1
} as const

export const USER_STATUS_OPTIONS: OptionItem[] = [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 }
]

// ==============================
// 2. Gender (sys_user.gender)
// ==============================
export const GENDER = {
  UNKNOWN: 0,
  MALE: 1,
  FEMALE: 2
} as const

export const GENDER_OPTIONS: OptionItem[] = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

// ==============================
// 3. Role Code (sys_role.role_code)
// ==============================
export const ROLE_CODE = {
  ADMIN: 'ADMIN',
  SALES: 'SALES',
  MANAGER: 'MANAGER',
  AUDITOR: 'AUDITOR',
  USER: 'USER'
} as const

export const ROLE_CODE_OPTIONS: OptionItem[] = [
  { label: '系统管理员', value: 'ADMIN' },
  { label: '销售人员', value: 'SALES' },
  { label: '销售经理', value: 'MANAGER' },
  { label: '只读审计员', value: 'AUDITOR' },
  { label: '普通用户', value: 'USER' }
]

// ==============================
// 4. Customer Status (crm_customer.status)
// ==============================
export const CUSTOMER_STATUS = {
  INVALID: 0,
  VALID: 1,
  PUBLIC_POOL: 2
} as const

export const CUSTOMER_STATUS_OPTIONS: OptionItem[] = [
  { label: '无效', value: 0 },
  { label: '有效', value: 1 },
  { label: '公海', value: 2 }
]

// ==============================
// 5. Customer Level (crm_customer.level)
// ==============================
export const CUSTOMER_LEVEL = {
  NORMAL: 1,
  POTENTIAL: 2,
  IMPORTANT: 3,
  CORE: 4,
  STRATEGIC: 5
} as const

export const CUSTOMER_LEVEL_OPTIONS: OptionItem[] = [
  { label: '普通客户', value: 1 },
  { label: '潜力客户', value: 2 },
  { label: '重要客户', value: 3 },
  { label: '核心客户', value: 4 },
  { label: '战略客户', value: 5 }
]

// ==============================
// 6. Opportunity Stage (crm_opportunity.stage)
// ==============================
export const OPPORTUNITY_STAGE = {
  INITIAL_CONTACT: 1,
  NEEDS_CONFIRMATION: 2,
  PROPOSAL_QUOTATION: 3,
  BUSINESS_NEGOTIATION: 4,
  WON: 5,
  LOST: 6
} as const

export const OPPORTUNITY_STAGE_OPTIONS: OptionItem[] = [
  { label: '初步接触', value: 1 },
  { label: '需求确认', value: 2 },
  { label: '方案报价', value: 3 },
  { label: '商务谈判', value: 4 },
  { label: '赢单', value: 5 },
  { label: '输单', value: 6 }
]

/** Stages that can be set via create or /stage (not win/lose) */
export const OPPORTUNITY_ACTIVE_STAGES = [1, 2, 3, 4]

// ==============================
// 7. Follow-up Customer Intention (crm_follow_record.customer_intention)
// ==============================
export const CUSTOMER_INTENTION = {
  LOW: 1,
  MEDIUM: 2,
  HIGH: 3,
  STRONG: 4
} as const

export const CUSTOMER_INTENTION_OPTIONS: OptionItem[] = [
  { label: '低', value: 1 },
  { label: '中', value: 2 },
  { label: '高', value: 3 },
  { label: '强烈', value: 4 }
]

// ==============================
// 8. Follow-up Type (crm_follow_record.follow_type)
// ==============================
export const FOLLOW_TYPE_OPTIONS: OptionItem[] = [
  { label: '电话', value: '电话' },
  { label: '微信', value: '微信' },
  { label: '邮件', value: '邮件' },
  { label: '拜访', value: '拜访' },
  { label: '会议', value: '会议' },
  { label: '其他', value: '其他' }
]

// ==============================
// 9. Order Status (crm_order.status)
// ==============================
export const ORDER_STATUS = {
  PENDING_PAYMENT: 1,
  PAID: 2,
  SHIPPED: 3,
  COMPLETED: 4,
  CANCELLED: 5
} as const

export const ORDER_STATUS_OPTIONS: OptionItem[] = [
  { label: '待付款', value: 1 },
  { label: '已付款', value: 2 },
  { label: '已发货', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 }
]

/** Order end states — no further changes allowed */
export const ORDER_END_STATES = [4, 5]

// ==============================
// 10. Operation Log Status (sys_operation_log.status)
// ==============================
export const OPERATION_LOG_STATUS = {
  FAILED: 0,
  SUCCESS: 1
} as const

export const OPERATION_LOG_STATUS_OPTIONS: OptionItem[] = [
  { label: '失败', value: 0 },
  { label: '成功', value: 1 }
]

// ==============================
// Utility: get label by value
// ==============================

/**
 * Generic label lookup from an options array.
 * Returns the value itself as string if not found.
 */
export function getLabelByValue(options: OptionItem[], value: string | number): string {
  const found = options.find((opt) => opt.value === value)
  return found ? found.label : String(value)
}
