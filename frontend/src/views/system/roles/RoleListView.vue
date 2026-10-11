<template>
  <div class="role-list-page">
    <!-- Search Form -->
    <el-card class="search-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="角色名称">
          <el-input v-model="query.roleName" placeholder="角色名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="query.roleCode" placeholder="角色编码" clearable style="width: 180px" />
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
          <span>角色列表</span>
          <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增角色</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="角色名称" width="140">
          <template #default="{ row }">{{ getRoleDisplayName(row.roleCode, row.roleName) }}</template>
        </el-table-column>
        <el-table-column label="描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ getRoleDisplayDescription(row) }}</template>
        </el-table-column>
        <el-table-column label="权限说明" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">{{ getRolePermissionNote(row).permissions }}</template>
        </el-table-column>
        <el-table-column label="数据范围" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ getRolePermissionNote(row).dataScope }}</template>
        </el-table-column>
        <el-table-column label="业绩口径" width="130">
          <template #default="{ row }">
            <el-tag :type="getRolePermissionNote(row).performance === '参与业绩' ? 'success' : 'info'" size="small">
              {{ getRolePermissionNote(row).performance }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成员数" width="90">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openMembersDialog(row)">
              {{ row.userCount ?? 0 }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="系统标识" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.roleCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ getLabelByValue(USER_STATUS_OPTIONS, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="190">
          <template #default="{ row }">
            <el-button text type="success" size="small" @click="openMembersDialog(row)">成员</el-button>
            <el-button text type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-tooltip :content="isBuiltinRole(row) ? '内置角色不可删除' : '删除角色'" placement="top">
              <el-button text type="danger" size="small" :disabled="isBuiltinRole(row)" @click="handleDelete(row)">删除</el-button>
            </el-tooltip>
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
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="520px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode" v-if="!isEdit">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码（如 USER）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择" style="width: 100%" :disabled="isRequiredEnabledEditingRole">
            <el-option v-for="o in USER_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
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

    <el-dialog v-model="membersVisible" title="角色成员" width="620px">
      <el-table :data="members" v-loading="membersLoading" stripe border>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ getLabelByValue(USER_STATUS_OPTIONS, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="membersVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { USER_STATUS_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { getRoleDisplayDescription, getRoleDisplayName, getRolePermissionNote } from '@/utils/roleDisplay'
import * as roleApi from '@/api/role'
import type { RoleVO } from '@/types/role'
import type { UserVO } from '@/types/user'

// ---- Table ----
const loading = ref(false)
const tableData = ref<RoleVO[]>([])
const total = ref(0)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  roleName: '',
  roleCode: '',
  status: undefined as number | undefined
})

let latestFetchId=0
async function fetchData() {
  const requestId=++latestFetchId;
  loading.value = true
  try {
    const params: Record<string, any> = { ...query }
    if (params.status === '' || params.status === undefined) delete params.status
    const res = await roleApi.getRolePage(params)
    if(requestId!==latestFetchId)return;tableData.value= res.records
    total.value = res.total
  } catch { /* ignore */ } finally {
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
  query.roleName = ''
  query.roleCode = ''
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

// ---- Create / Edit ----
const formVisible = ref(false)
const formLoading = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const editingRoleCode = ref('')
const formRef = ref<FormInstance>()

const form = reactive({
  roleName: '',
  roleCode: '',
  description: '',
  status: 1 as number | undefined,
  sort: 0,
  remark: ''
})

const formRules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const isRequiredEnabledEditingRole = computed(() => {
  return isEdit.value && ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR'].includes(editingRoleCode.value)
})

function openCreateDialog() {
  isEdit.value = false
  editId.value = null
  editingRoleCode.value = ''
  resetForm()
  formVisible.value = true
}

async function openEditDialog(row: RoleVO) {
  isEdit.value = true
  editId.value = row.id
  editingRoleCode.value = row.roleCode
  try {
    const detail = await roleApi.getRoleDetail(row.id)
    form.roleName = detail.roleName || ''
    form.description = detail.description || ''
    form.status = detail.status ?? 1
    form.sort = detail.sort ?? 0
    form.remark = detail.remark || ''
    formVisible.value = true
  } catch { /* ignore */ }
}

function resetForm() {
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.status = 1
  form.sort = 0
  form.remark = ''
  editingRoleCode.value = ''
  formRef.value?.clearValidate()
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  formLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      await roleApi.updateRole(editId.value, {
        roleName: form.roleName,
        description: form.description || undefined,
        status: form.status,
        sort: form.sort,
        remark: form.remark || undefined
      })
      ElMessage.success('修改成功')
    } else {
      await roleApi.createRole({
        roleName: form.roleName,
        roleCode: form.roleCode,
        description: form.description || undefined,
        status: form.status ?? 1,
        sort: form.sort,
        remark: form.remark || undefined
      })
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    fetchData()
  } catch { /* ignore */ } finally {
    formLoading.value = false
  }
}

// ---- Built-in role check ----
const isBuiltinRole = (role: RoleVO) => {
  return ['ADMIN', 'SALES', 'MANAGER', 'AUDITOR', 'USER'].includes(role.roleCode)
}

// ---- Members ----
const membersVisible = ref(false)
const membersLoading = ref(false)
const members = ref<UserVO[]>([])

async function openMembersDialog(row: RoleVO) {
  membersVisible.value = true
  membersLoading.value = true
  try {
    members.value = await roleApi.getRoleUsers(row.id)
  } catch {
    members.value = []
  } finally {
    membersLoading.value = false
  }
}

// ---- Delete ----
async function handleDelete(row: RoleVO) {
  if (isBuiltinRole(row)) {
    ElMessage.warning('内置角色不可删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？此操作不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await roleApi.deleteRole(row.id)
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
.role-list-page {
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
