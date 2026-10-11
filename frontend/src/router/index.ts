import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/**
 * Extend vue-router meta type
 */
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    /** Route is public — no auth required */
    public?: boolean
    /** Any of these roles may access the route */
    roles?: string[]
  }
}

// ==============================
// Route definitions
// ==============================
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/DashboardView.vue'),
    meta: { title: '首页仪表盘' }
  },
  {
    path: '/system/users',
    name: 'UserList',
    component: () => import('@/views/system/users/UserListView.vue'),
    meta: { title: '用户管理', roles: ['ADMIN'] }
  },
  {
    path: '/system/roles',
    name: 'RoleList',
    component: () => import('@/views/system/roles/RoleListView.vue'),
    meta: { title: '角色管理', roles: ['ADMIN'] }
  },
  {
    path: '/system/operation-logs',
    name: 'OperationLog',
    component: () => import('@/views/system/operationLogs/OperationLogView.vue'),
    meta: { title: '操作日志', roles: ['ADMIN', 'AUDITOR'] }
  },
  {
    path: '/customer/list',
    name: 'CustomerList',
    component: () => import('@/views/customer/CustomerListView.vue'),
    meta: { title: '客户管理', roles: ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR'] }
  },
  {
    path: '/customer/:id',
    name: 'CustomerDetail',
    component: () => import('@/views/customer/CustomerDetailView.vue'),
    meta: { title: '客户详情', roles: ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR'] }
  },
  {
    path: '/opportunity/list',
    name: 'OpportunityList',
    component: () => import('@/views/opportunity/OpportunityListView.vue'),
    meta: { title: '商机管理', roles: ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR'] }
  },
  {
    path: '/opportunity/:id',
    name: 'OpportunityDetail',
    component: () => import('@/views/opportunity/OpportunityDetailView.vue'),
    meta: { title: '商机详情', roles: ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR'] }
  },
  {
    path: '/follow-records',
    name: 'FollowRecords',
    component: () => import('@/views/followRecord/FollowRecordView.vue'),
    meta: { title: '跟进记录', roles: ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR'] }
  },
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('@/views/order/OrderListView.vue'),
    meta: { title: '订单管理', roles: ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR'] }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/OrderDetailView.vue'),
    meta: { title: '订单详情', roles: ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR'] }
  },
  {
    path: '/analytics',
    name: 'Analytics',
    component: () => import('@/views/analytics/AnalyticsView.vue'),
    meta: { title: '数据统计', roles: ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR'] }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/ForbiddenView.vue'),
    meta: { title: '无权限', public: true }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

// ==============================
// Router instance
// ==============================
const router = createRouter({
  history: createWebHistory(),
  routes
})

// ==============================
// Navigation guard
// ==============================
router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${to.meta.title} - CRM 管理系统` : 'CRM 管理系统'
  const isLoginRoute = to.name === 'Login'
  if (to.meta.public === true && !isLoginRoute) return true

  const authStore = useAuthStore()
  while (authStore.token && !authStore.sessionValidated) {
    const validatingToken = authStore.token
    const validatingVersion = authStore.sessionVersion
    try {
      await authStore.fetchCurrentUser()
    } catch {
      // A failed restore request from an old session cannot clear a later login.
      if (authStore.token !== validatingToken || authStore.sessionVersion !== validatingVersion) continue
      authStore.clearAuth()
      return isLoginRoute ? true : { name: 'Login', query: { redirect: to.fullPath } }
    }
  }

  if (isLoginRoute) return authStore.isLoggedIn ? { name: 'Dashboard' } : true
  if (!authStore.isLoggedIn) return { name: 'Login', query: { redirect: to.fullPath } }
  const requiredRoles = to.meta.roles
  if (requiredRoles?.length && !authStore.hasAnyRole(requiredRoles)) return { name: 'Forbidden' }
  return true
})

export default router
