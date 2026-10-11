<template>
  <div class="basic-layout">
    <!-- Sidebar -->
    <aside class="layout-sidebar">
      <div class="sidebar-header">
        <h2>CRM 系统</h2>
      </div>

      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
        class="sidebar-menu"
      >
        <!-- Business modules -->
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>首页仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/customer/list">
          <el-icon><User /></el-icon>
          <span>客户管理</span>
        </el-menu-item>
        <el-menu-item index="/opportunity/list">
          <el-icon><TrendCharts /></el-icon>
          <span>商机管理</span>
        </el-menu-item>
        <el-menu-item index="/follow-records">
          <el-icon><Notebook /></el-icon>
          <span>跟进记录</span>
        </el-menu-item>
        <el-menu-item index="/order/list">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/analytics">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>

        <!-- System management -->
        <template v-if="authStore.isAdmin || authStore.isAuditor">
          <div class="menu-divider">
            <span>系统管理</span>
          </div>
          <el-menu-item v-if="authStore.isAdmin" index="/system/users">
            <el-icon><Avatar /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.isAdmin" index="/system/roles">
            <el-icon><Key /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/system/operation-logs">
            <el-icon><Files /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>

    <!-- Main area -->
    <div class="layout-right">
      <!-- Top bar -->
      <header class="layout-header">
        <span class="header-title">{{ $route.meta.title || 'CRM 管理系统' }}</span>
        <div class="header-right">
          <el-tag size="small" :type="roleTagType" class="role-tag">
            {{ roleLabel }}
          </el-tag>
          <span class="user-name">{{ authStore.user?.realName || authStore.user?.username || '未登录' }}</span>
          <el-button text type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <!-- Content -->
      <div class="layout-content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Odometer, User, TrendCharts, Notebook, Document,
  DataAnalysis, Avatar, Key, Files
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const roleLabel = computed(() => {
  if (authStore.isAdmin) return '系统管理员'
  if (authStore.isManager) return '销售经理'
  if (authStore.isAuditor) return '只读审计员'
  if (authStore.isSales) return '销售顾问'
  return '普通用户'
})
const roleTagType = computed(() => {
  if (authStore.isAdmin) return 'danger'
  if (authStore.isManager) return 'warning'
  if (authStore.isAuditor) return 'info'
  return 'primary'
})

// Active menu item based on current path
const activeMenu = computed(() => {
  const path = route.path
  // Match parent routes for detail pages
  if (path.startsWith('/customer/')) return '/customer/list'
  if (path.startsWith('/opportunity/')) return '/opportunity/list'
  if (path.startsWith('/order/')) return '/order/list'
  if (path.startsWith('/system/')) {
    if (path.includes('/users')) return '/system/users'
    if (path.includes('/roles')) return '/system/roles'
    if (path.includes('/operation-logs')) return '/system/operation-logs'
  }
  return path
})

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await authStore.logoutAction()
    router.push('/login')
  } catch {
    // User cancelled.
  }
}
</script>

<style scoped>
.basic-layout {
  display: flex;
  height: 100vh;
}
.layout-sidebar {
  width: 220px;
  background: #304156;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  flex-shrink: 0;
}
.sidebar-header {
  padding: 20px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  color: #fff;
  font-weight: 600;
  letter-spacing: 2px;
}
.sidebar-menu {
  border-right: none;
  flex: 1;
}
.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
}
.sidebar-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.06) !important;
}
.menu-divider {
  padding: 16px 20px 8px;
  color: rgba(255, 255, 255, 0.35);
  font-size: 12px;
  letter-spacing: 1px;
  user-select: none;
}
.layout-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
  min-height: 0;
}
.layout-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}
.header-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.role-tag {
  font-size: 12px;
}
.user-name {
  font-size: 14px;
  color: #606266;
}
.layout-content {
  flex: 1;
  min-height: 0;
  padding: 20px;
  overflow-y: auto;
  overflow-x: hidden;
  background: #f0f2f5;
}
</style>
