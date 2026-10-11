/**
 * Login request DTO
 */
export interface LoginRequest {
  username: string
  password: string
}

/**
 * Login user info (nested in LoginResponse)
 */
export interface LoginUser {
  id: number
  username: string
  realName: string
  roles: string[]
}

/**
 * Login response data
 */
export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: LoginUser
}

/**
 * Current user info returned by GET /auth/me
 */
export interface CurrentUser {
  id: number
  username: string
  realName: string
  nickname?: string
  phone?: string
  email?: string
  gender?: number
  status?: number
  roles: string[]
  createdAt?: string
  updatedAt?: string
}
