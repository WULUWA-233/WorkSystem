<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">客户关系管理系统</h1>
      <p class="login-subtitle">请使用账号密码登录</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        autocomplete="off"
        @keyup.enter="handleLogin"
      >
        <input class="autocomplete-decoy" type="text" autocomplete="username" tabindex="-1" />
        <input class="autocomplete-decoy" type="password" autocomplete="current-password" tabindex="-1" />
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入账号"
            :prefix-icon="User"
            size="large"
            clearable
            name="crm-login-user"
            autocomplete="off"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            clearable
            name="crm-login-pass"
            autocomplete="new-password"
          />
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="form.rememberMe">记住账号</el-checkbox>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getRememberedUsername, setRememberedUsername } from '@/utils/storage'

const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  if (authStore.isLoggedIn) {
    router.replace('/dashboard')
    return
  }
  loadRememberedLogin()
})

function loadRememberedLogin() {
  const username = getRememberedUsername()
  if (username === null) return
  form.username = username
  form.rememberMe = true
}

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

watch(() => form.rememberMe, (rememberMe) => {
  if (!rememberMe) {
    setRememberedUsername(null)
  }
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function persistRememberedLogin() {
  setRememberedUsername(form.rememberMe ? form.username : null)
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.loginAction(form.username, form.password)
    persistRememberedLogin()
    ElMessage.success('登录成功')
    const redirect = router.currentRoute.value.query.redirect
    const target = typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//') && redirect.split('?')[0] !== '/login'
      ? redirect : '/dashboard'
    await router.replace(target)
  } catch {
    // Request failures are displayed once by the shared interceptor.
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-title {
  text-align: center;
  font-size: 24px;
  margin: 0 0 8px;
  color: #303133;
}

.login-subtitle {
  text-align: center;
  color: #909399;
  margin: 0 0 32px;
  font-size: 14px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: -4px 0 18px;
  min-height: 24px;
}

.autocomplete-decoy {
  position: fixed;
  top: -1000px;
  left: -1000px;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
}
</style>
