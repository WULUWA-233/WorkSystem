<template>
  <div class="user-list-page">
    <!-- Search Form -->
    <el-card class="search-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="用户名" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="query.realName" placeholder="真实姓名" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="o in USER_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增用户</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">
            {{ getLabelByValue(GENDER_OPTIONS, row.gender) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ getLabelByValue(USER_STATUS_OPTIONS, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="140">
          <template #default="{ row }">
            <el-tag v-for="r in row.roles" :key="r" size="small" style="margin-right: 4px">
              {{ getRoleDisplayName(r, r) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="320">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button text type="warning" size="small" @click="openStatusDialog(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button text type="info" size="small" @click="openPasswordDialog(row)">重置密码</el-button>
            <el-button v-if="authStore.isAdmin" text type="success" size="small" @click="openRoleDialog(row)">更改角色</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </el-card>

    <!-- Create / Edit Dialog -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="用户名" prop="username" v-if="!isEdit">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
            <el-option v-for="o in GENDER_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择" style="width: 100%">
            <el-option v-for="o in USER_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds" v-if="!isEdit">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roleOptions" :key="r.id" :label="getRoleOptionLabel(r)" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- Status Toggle Dialog -->
    <el-dialog v-model="statusVisible" title="确认操作" width="400px">
      <p>确定要{{ statusTarget?.status === 1 ? '禁用' : '启用' }}用户 <b>{{ statusTarget?.username }}</b> 吗？</p>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusLoading" @click="confirmStatus">确定</el-button>
      </template>
    </el-dialog>

    <!-- Password Reset Dialog -->
    <el-dialog v-model="passwordVisible" title="重置密码" width="440px" :close-on-click-modal="false" @closed="resetPasswordForm">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="用户名">
          <span>{{ passwordTarget?.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="confirmPassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- Role Assign Dialog -->
    <el-dialog v-model="roleVisible" title="更改角色" width="460px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <span>{{ roleTarget?.username }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="selectedRoleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roleOptions" :key="r.id" :label="getRoleOptionLabel(r)" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleLoading" @click="confirmRoles">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { USER_STATUS_OPTIONS, GENDER_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { getRoleDisplayName, getRoleOptionLabel } from '@/utils/roleDisplay'
import { useAuthStore } from '@/stores/auth'
import * as userApi from '@/api/user'
import * as roleApi from '@/api/role'
import type { UserVO, UserDetailVO } from '@/types/user'
import type { RoleOptionVO } from '@/types/role'

// ---- Table ----
const authStore = useAuthStore()
const loading = ref(false)
const tableData = ref<UserVO[]>([])
const total = ref(0)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  username: '',
  realName: '',
  phone: '',
  status: undefined as number | undefined
})

let latestFetchId=0
async function fetchData() {
  const requestId=++latestFetchId;
  loading.value = true
  try {
    const params: Record<string, any> = { ...query }
    if (params.status === '' || params.status === undefined) delete params.status
    const res = await userApi.getUserPage(params)
    if(requestId!==latestFetchId)return;tableData.value= res.records
    total.value = res.total
  } catch {
    // error handled by interceptor
  } finally {
    if(requestId===latestFetchId)loading.value=false
  }
}

function handleSearch() {
  query.pageNo = 1
  fetchData()
}

function handlePageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function handleSizeChange(size: number) {
  query.pageSize = size
  query.pageNo = 1
  fetchData()
}

function handleReset() {
  query.username = ''
  query.realName = ''
  query.phone = ''
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

// ---- Create / Edit ----
const formVisible = ref(false)
const formLoading = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const roleOptions = ref<RoleOptionVO[]>([])

const form = reactive({
  username: '',
  password: '',
  realName: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 0 as number | undefined,
  status: 1 as number | undefined,
  roleIds: [] as number[],
  remark: ''
})

const formRules: FormRules = {
  username: [{ required: true, whitespace: true, min: 2, max: 64, message: '用户名须为 2 至 64 字符', trigger: 'blur' }],
  password: [{ required: true, min: 6, max: 64, message: '密码须为 6 至 64 字符', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  roleIds: [{ required: true, type: 'array', min: 1, message: '请选择角色', trigger: 'change' }]
}

async function loadRoleOptions() {
  try {
    roleOptions.value = await roleApi.getRoleOptions()
  } catch { /* ignore */ }
}

function openCreateDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  formVisible.value = true
  loadRoleOptions()
}

async function openEditDialog(row: UserVO) {
  isEdit.value = true
  editId.value = row.id
  try {
    const detail: UserDetailVO = await userApi.getUserDetail(row.id)
    form.realName = detail.realName || ''
    form.nickname = detail.nickname || ''
    form.phone = detail.phone || ''
    form.email = detail.email || ''
    form.gender = detail.gender ?? 0
    form.status = detail.status ?? 1
    form.remark = detail.remark || ''
    formVisible.value = true
  } catch { /* ignore */ }
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.nickname = ''
  form.phone = ''
  form.email = ''
  form.gender = 0
  form.status = 1
  form.roleIds = []
  form.remark = ''
  formRef.value?.clearValidate()
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!isEdit.value && !form.roleIds.length) {
    ElMessage.warning('请选择角色')
    return
  }

  formLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      await userApi.updateUser(editId.value, {
        realName: form.realName,
        nickname: form.nickname || undefined,
        phone: form.phone || undefined,
        email: form.email || undefined,
        gender: form.gender,
        status: form.status,
        remark: form.remark || undefined
      })
      ElMessage.success('修改成功')
    } else {
      await userApi.createUser({
        username: form.username,
        password: form.password,
        realName: form.realName,
        nickname: form.nickname || undefined,
        phone: form.phone || undefined,
        email: form.email || undefined,
        gender: form.gender,
        status: form.status ?? 1,
        roleIds: form.roleIds,
        remark: form.remark || undefined
      })
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    fetchData()
  } catch { /* error handled by interceptor */ } finally {
    formLoading.value = false
  }
}

// ---- Status Toggle ----
const statusVisible = ref(false)
const statusLoading = ref(false)
const statusTarget = ref<UserVO | null>(null)

function openStatusDialog(row: UserVO) {
  statusTarget.value = row
  statusVisible.value = true
}

async function confirmStatus() {
  if (!statusTarget.value) return
  statusLoading.value = true
  try {
    const newStatus = statusTarget.value.status === 1 ? 0 : 1
    await userApi.toggleUserStatus(statusTarget.value.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    statusVisible.value = false
    fetchData()
  } catch { /* ignore */ } finally {
    statusLoading.value = false
  }
}

// ---- Password Reset ----
const passwordVisible = ref(false)
const passwordLoading = ref(false)
const passwordTarget = ref<UserVO | null>(null)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({ newPassword: '' })
const passwordRules: FormRules = {
  newPassword: [{ required: true, min: 6, max: 64, message: '密码须为 6 至 64 字符', trigger: 'blur' }]
}

function openPasswordDialog(row: UserVO) {
  passwordTarget.value = row
  passwordVisible.value = true
}

function resetPasswordForm() {
  passwordForm.newPassword = ''
  passwordFormRef.value?.clearValidate()
}

async function confirmPassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!passwordTarget.value) return
  passwordLoading.value = true
  try {
    await userApi.resetUserPassword(passwordTarget.value.id, passwordForm.newPassword)
    ElMessage.success('密码重置成功')
    passwordVisible.value = false
  } catch { /* ignore */ } finally {
    passwordLoading.value = false
  }
}

// ---- Role Assign ----
const roleVisible = ref(false)
const roleLoading = ref(false)
const roleTarget = ref<UserVO | null>(null)
const selectedRoleIds = ref<number[]>([])

async function openRoleDialog(row: UserVO) {
  if (!authStore.isAdmin) {
    ElMessage.warning('只有系统管理员可以更改用户角色')
    return
  }
  roleTarget.value = row
  await loadRoleOptions()
  try {
    const detail: UserDetailVO = await userApi.getUserDetail(row.id)
    selectedRoleIds.value = detail.roleIds || []
  } catch {
    selectedRoleIds.value = []
  }
  roleVisible.value = true
}

async function confirmRoles() {
  if (!roleTarget.value) return
  if (!authStore.isAdmin) {
    ElMessage.warning('只有系统管理员可以更改用户角色')
    return
  }
  if (!selectedRoleIds.value.length) {
    ElMessage.warning('不允许用户无角色')
    return
  }
  roleLoading.value = true
  try {
    await userApi.assignUserRoles(roleTarget.value.id, selectedRoleIds.value)
    ElMessage.success('角色更改成功')
    roleVisible.value = false
    fetchData()
  } catch { /* ignore */ } finally {
    roleLoading.value = false
  }
}

// ---- Delete ----
async function handleDelete(row: UserVO) {
  if (row.id === authStore.user?.id) {
    ElMessage.warning('不能删除自己')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？此操作不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await userApi.deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled or error */ }
}

// ---- Init ----
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-list-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
