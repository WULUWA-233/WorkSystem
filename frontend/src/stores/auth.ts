import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginUser } from '@/types/auth'
import { ROLE_CODE } from '@/constants/enums'
import { getToken, setToken, getUser, setUser, clearAuthStorage } from '@/utils/storage'
import * as authApi from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<LoginUser | null>(getUser<LoginUser>())
  // A cached user must be checked with the server before granting access.
  const sessionValidated = ref(false)
  const sessionVersion = ref(0)
  let currentUserRequest: Promise<void> | null = null

  const isLoggedIn = computed(() => !!token.value && !!user.value && sessionValidated.value)
  const roles = computed<string[]>(() => user.value?.roles ?? [])
  const isAdmin = computed(() => roles.value.includes(ROLE_CODE.ADMIN))
  const isSales = computed(() => roles.value.includes(ROLE_CODE.SALES))
  const isManager = computed(() => roles.value.includes(ROLE_CODE.MANAGER))
  const isAuditor = computed(() => roles.value.includes(ROLE_CODE.AUDITOR))
  const canWriteBusiness = computed(() => isLoggedIn.value && !isAuditor.value &&
    (isAdmin.value || isSales.value || isManager.value))

  function setTokenAction(newToken: string) {
    sessionVersion.value++
    sessionValidated.value = false
    currentUserRequest = null
    token.value = newToken
    setToken(newToken)
  }

  function setUserAction(newUser: LoginUser) {
    user.value = newUser
    setUser(newUser as unknown as Record<string, unknown>)
  }

  async function loginAction(username: string, password: string): Promise<void> {
    const data = await authApi.login({ username, password })
    setTokenAction(data.accessToken)
    setUserAction(data.user)
    sessionValidated.value = true
  }

  function fetchCurrentUser(): Promise<void> {
    if (currentUserRequest) return currentUserRequest
    const revision = sessionVersion.value
    const request = (async () => {
      const data = await authApi.getCurrentUser()
      // An expired request must not restore a cleared or newly signed-in session.
      if (revision !== sessionVersion.value || !token.value) return
      setUserAction({ id: data.id, username: data.username, realName: data.realName, roles: data.roles })
      sessionValidated.value = true
    })()
    currentUserRequest = request
    const clearRequest = () => { if (currentUserRequest === request) currentUserRequest = null }
    request.then(clearRequest, clearRequest)
    return request
  }

  async function logoutAction(): Promise<void> {
    try {
      await authApi.logout()
    } catch {
      // Local logout must succeed even when the server is unavailable.
    } finally {
      clearAuth()
    }
  }

  function clearAuth() {
    sessionVersion.value++
    currentUserRequest = null
    sessionValidated.value = false
    token.value = null
    user.value = null
    clearAuthStorage()
  }

  function hasRole(role: string): boolean { return roles.value.includes(role) }
  function hasAnyRole(roleList: string[]): boolean { return roleList.some(hasRole) }

  return {
    token, user, sessionValidated, sessionVersion, isLoggedIn, roles, isAdmin, isSales, isManager, isAuditor,
    canWriteBusiness, setToken: setTokenAction, setUser: setUserAction, loginAction,
    fetchCurrentUser, logoutAction, clearAuth, hasRole, hasAnyRole
  }
})
