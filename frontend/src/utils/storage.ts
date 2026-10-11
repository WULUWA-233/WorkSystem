/**
 * Storage utilities — pure functions with no dependencies on router or store.
 *
 * Keys used:
 *   crm_access_token — JWT token string
 *   crm_user         — user object (JSON)
 */

const TOKEN_KEY = 'crm_access_token'
const USER_KEY = 'crm_user'
const REMEMBER_LOGIN_KEY = 'crm_remember_login'

/** Remember only the account name; migrate old caches without retaining passwords. */
export function getRememberedUsername(): string | null {
  try {
    const raw = localStorage.getItem(REMEMBER_LOGIN_KEY)
    if (!raw) return null
    const saved = JSON.parse(raw)
    if (!saved?.rememberMe || typeof saved.username !== 'string') {
      localStorage.removeItem(REMEMBER_LOGIN_KEY)
      return null
    }
    setRememberedUsername(saved.username)
    return saved.username
  } catch {
    localStorage.removeItem(REMEMBER_LOGIN_KEY)
    return null
  }
}

export function setRememberedUsername(username: string | null): void {
  if (username === null) localStorage.removeItem(REMEMBER_LOGIN_KEY)
  else localStorage.setItem(REMEMBER_LOGIN_KEY, JSON.stringify({ username, rememberMe: true }))
}

// ---- Token ----

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// ---- User ----

export function getUser<T = Record<string, unknown>>(): T | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function setUser(user: Record<string, unknown>): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUser(): void {
  localStorage.removeItem(USER_KEY)
}

// ---- Combined ----

/**
 * Clear all auth-related storage.
 */
export function clearAuthStorage(): void {
  removeToken()
  removeUser()
}
