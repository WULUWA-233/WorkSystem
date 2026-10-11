type RoleLike = {
  roleCode?: string
  roleName?: string
  description?: string
}

export type RolePermissionNote = {
  name: string
  description: string
  permissions: string
  dataScope: string
  performance: string
}

const ROLE_DISPLAY: Record<string, RolePermissionNote> = {
  ADMIN: {
    name: '系统管理员',
    description: '负责系统配置、用户角色维护、全量数据查看与操作日志审计',
    permissions: '用户管理、角色管理、操作日志、客户、商机、跟进记录、订单和统计看板',
    dataScope: '全量数据',
    performance: '不参与业绩'
  },
  SALES: {
    name: '销售顾问',
    description: '负责客户经营、商机推进、跟进记录和订单转化',
    permissions: '客户新增编辑、商机创建推进、跟进记录、基于赢单商机创建订单、个人看板',
    dataScope: '本人负责的数据',
    performance: '参与业绩'
  },
  MANAGER: {
    name: '销售经理',
    description: '负责销售团队过程管理和团队业绩查看',
    permissions: '团队客户、团队商机、团队订单、团队跟进、负责人分配和团队看板',
    dataScope: '本人及下属团队数据',
    performance: '按业务配置，可参与个人业绩或仅参与团队统计'
  },
  AUDITOR: {
    name: '只读审计员',
    description: '负责查看业务数据和操作日志，不参与业务操作',
    permissions: '客户、商机、订单、跟进记录、操作日志只读查看',
    dataScope: '按审计范围配置',
    performance: '不参与业绩'
  },
  USER: {
    name: '普通用户',
    description: '基础访问身份，不建议直接用于业务授权',
    permissions: '基础登录和个人信息查看',
    dataScope: '本人基础信息',
    performance: '不参与业绩'
  }
}

export function getRoleDisplayName(roleCode?: string, fallback = '-') {
  if (!roleCode) return fallback || '-'
  return ROLE_DISPLAY[roleCode]?.name || fallback || roleCode
}

export function getRoleDisplayDescription(role?: RoleLike) {
  if (!role) return '-'
  return ROLE_DISPLAY[role.roleCode || '']?.description || role.description || '-'
}

export function getRolePermissionNote(role?: RoleLike): RolePermissionNote {
  const code = role?.roleCode || ''
  return ROLE_DISPLAY[code] || {
    name: role?.roleName || code || '-',
    description: role?.description || '自定义角色，请在角色描述中补充业务用途',
    permissions: role?.description || '未配置权限说明',
    dataScope: '按系统配置',
    performance: code.includes('SALES') ? '参与业绩' : '未配置'
  }
}

export function getRoleOptionLabel(role: RoleLike) {
  const note = getRolePermissionNote(role)
  return `${getRoleDisplayName(role.roleCode, role.roleName)}（${note.performance}）`
}
