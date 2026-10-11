/**
 * Role Management Types
 */

// ---- List VO ----
export interface RoleVO {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status: number
  sort: number
  userCount?: number
  remark?: string
  createdAt: string
  updatedAt?: string
}

// ---- Option VO (dropdown) ----
export interface RoleOptionVO {
  id: number
  roleName: string
  roleCode: string
}

// ---- Query ----
export interface RoleQuery {
  pageNo?: number
  pageSize?: number
  roleName?: string
  roleCode?: string
  status?: number
}

// ---- Create ----
export interface RoleCreateDTO {
  roleName: string
  roleCode: string
  description?: string
  status?: number
  sort?: number
  remark?: string
}

// ---- Update (no roleCode) ----
export interface RoleUpdateDTO {
  roleName: string
  description?: string
  status?: number
  sort?: number
  remark?: string
}
