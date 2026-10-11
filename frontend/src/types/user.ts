/**
 * User Management Types
 */

// ---- List VO ----
export interface UserVO {
  id: number
  username: string
  realName: string
  nickname?: string
  phone?: string
  email?: string
  gender: number
  status: number
  roles: string[]
  createdAt: string
  updatedAt?: string
}

// ---- Detail VO ----
export interface UserDetailVO extends UserVO {
  remark?: string
  roleIds?: number[]
}

// ---- Query ----
export interface UserQuery {
  pageNo?: number
  pageSize?: number
  username?: string
  realName?: string
  phone?: string
  status?: number
}

// ---- Create ----
export interface UserCreateDTO {
  username: string
  password: string
  realName: string
  nickname?: string
  phone?: string
  email?: string
  gender?: number
  status?: number
  roleIds: number[]
  remark?: string
}

// ---- Update ----
export interface UserUpdateDTO {
  realName: string
  nickname?: string
  phone?: string
  email?: string
  gender?: number
  status?: number
  roleIds?: number[]
  remark?: string
}

// ---- Status ----
export interface UserStatusDTO {
  status: 0 | 1
}

// ---- Password Reset ----
export interface UserPasswordResetDTO {
  newPassword: string
}

// ---- Role Assign ----
export interface UserRoleAssignDTO {
  roleIds: number[]
}
