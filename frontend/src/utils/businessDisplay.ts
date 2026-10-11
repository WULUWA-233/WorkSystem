import {
  CUSTOMER_STATUS_OPTIONS,
  CUSTOMER_LEVEL_OPTIONS,
  CUSTOMER_INTENTION_OPTIONS,
  FOLLOW_TYPE_OPTIONS,
  OPPORTUNITY_STAGE_OPTIONS,
  ORDER_STATUS_OPTIONS,
  getLabelByValue
} from '@/constants/enums'

const TEXT_MAP: Record<string, string> = {
  'Initial Contact': '初步接触',
  'Requirements Confirmed': '需求确认',
  'Needs Confirmation': '需求确认',
  'Proposal/Quotation': '方案报价',
  Quotation: '方案报价',
  Negotiation: '商务谈判',
  Won: '赢单',
  Lost: '输单',
  'Pending Payment': '待付款',
  Paid: '已付款',
  Shipped: '已发货',
  Completed: '已完成',
  Cancelled: '已取消',
  Phone: '电话',
  Telephone: '电话',
  WeChat: '微信',
  Email: '邮件',
  Visit: '拜访',
  Meeting: '会议',
  Other: '其他',
  BankTransfer: '银行转账',
  'Bank Transfer': '银行转账',
  Alipay: '支付宝',
  WechatPay: '微信支付',
  'WeChat Pay': '微信支付',
  Cash: '现金',
  Customer: '客户管理',
  Opportunity: '商机管理',
  Order: '订单管理',
  'Follow Record': '跟进记录',
  Role: '角色管理',
  User: '用户管理',
  Create: '新增',
  Update: '修改',
  Delete: '删除',
  Detail: '详情',
  Query: '查询',
  Login: '登录',
  Logout: '退出登录',
  Pay: '付款',
  Ship: '发货',
  Complete: '完成',
  Cancel: '取消'
}

const INDUSTRY_MAP: Record<string, string> = {
  Technology: '科技',
  Internet: '互联网',
  Software: '软件服务',
  Finance: '金融',
  Education: '教育',
  Healthcare: '医疗健康',
  Manufacturing: '制造业',
  Retail: '零售',
  Logistics: '物流',
  Consulting: '咨询服务'
}

const SOURCE_MAP: Record<string, string> = {
  Website: '官网咨询',
  Referral: '客户转介绍',
  Exhibition: '展会线索',
  Ads: '广告投放',
  Search: '搜索引擎',
  Phone: '电话咨询',
  WeChat: '微信咨询',
  Other: '其他'
}

function fromMap(value: unknown, map: Record<string, string>) {
  if (value === null || value === undefined || value === '') return '-'
  const text = String(value)
  return map[text] || text
}

export function displayText(value: unknown) {
  return fromMap(value, TEXT_MAP)
}

export function displayIndustry(value: unknown) {
  return fromMap(value, INDUSTRY_MAP)
}

export function displaySource(value: unknown) {
  return fromMap(value, SOURCE_MAP)
}

export function displayOpportunityStage(stage?: number | string, stageName?: string) {
  const mappedName = stageName ? displayText(stageName) : ''
  if (mappedName && mappedName !== stageName) return mappedName
  if (stage !== undefined && stage !== null && stage !== '') return getLabelByValue(OPPORTUNITY_STAGE_OPTIONS, Number(stage))
  return stageName || '-'
}

export function displayOrderStatus(status?: number | string, statusName?: string) {
  const mappedName = statusName ? displayText(statusName) : ''
  if (mappedName && mappedName !== statusName) return mappedName
  if (status !== undefined && status !== null && status !== '') return getLabelByValue(ORDER_STATUS_OPTIONS, Number(status))
  return statusName || '-'
}

export function displayFollowType(value: unknown) {
  const mapped = displayText(value)
  if (mapped !== value) return mapped
  return value ? getLabelByValue(FOLLOW_TYPE_OPTIONS, String(value)) : '-'
}

export function displayPaymentMethod(value: unknown) {
  return displayText(value)
}

export function displayCustomerLevel(value: number | string | undefined) {
  return value === undefined || value === null || value === '' ? '-' : getLabelByValue(CUSTOMER_LEVEL_OPTIONS, Number(value))
}

export function displayCustomerStatus(value: number | string | undefined) {
  return value === undefined || value === null || value === '' ? '-' : getLabelByValue(CUSTOMER_STATUS_OPTIONS, Number(value))
}

export function displayCustomerIntention(value: number | string | undefined) {
  return value === undefined || value === null || value === '' ? '-' : getLabelByValue(CUSTOMER_INTENTION_OPTIONS, Number(value))
}
