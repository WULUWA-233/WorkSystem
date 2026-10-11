<template>
  <div class="log-list-page">
    <!-- Search Form -->
    <el-card class="search-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="用户名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="模块名">
          <el-input v-model="query.moduleName" placeholder="模块名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="query.operationType" placeholder="操作类型" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="请求方法">
          <el-select v-model="query.requestMethod" placeholder="全部" clearable style="width: 100px">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 100px">
            <el-option v-for="o in OPERATION_LOG_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
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
        <span>操作日志列表</span>
      </template>

      <div class="table-wrapper">
        <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="用户名" width="100" />
          <el-table-column label="模块名" width="100"><template #default="{ row }">{{ displayText(row.moduleName) }}</template></el-table-column>
          <el-table-column label="操作类型" width="100"><template #default="{ row }">{{ displayText(row.operationType) }}</template></el-table-column>
          <el-table-column label="操作描述" min-width="160" show-overflow-tooltip><template #default="{ row }">{{ displayText(row.operationDesc) }}</template></el-table-column>
          <el-table-column prop="requestMethod" label="请求方法" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="methodTagType(row.requestMethod)">{{ row.requestMethod }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="70">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ getLabelByValue(OPERATION_LOG_STATUS_OPTIONS, row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="耗时" width="80">
            <template #default="{ row }">{{ row.costTime }}ms</template>
          </el-table-column>
          <el-table-column prop="ipAddress" label="IP地址" width="140" />
          <el-table-column prop="createdAt" label="创建时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="140">
            <template #default="{ row }">
              <el-button text type="primary" size="small" @click="openDetail(row.id)">详情</el-button>
              <el-button v-if="authStore.canWriteBusiness" text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="640px" :close-on-click-modal="false">
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="模块名">{{ displayText(detail.moduleName) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ displayText(detail.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ displayText(detail.operationDesc) }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ detail.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 1 ? 'success' : 'danger'" size="small">
            {{ getLabelByValue(OPERATION_LOG_STATUS_OPTIONS, detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detail.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMessage" label="错误信息" :span="2">
          <span style="color: #f56c6c">{{ detail.errorMessage }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <JsonPreview :data="detail.requestParams" />
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <JsonPreview :data="detail.responseResult" />
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.userAgent" label="UserAgent" :span="2">
          <span style="font-size: 12px; word-break: break-all">{{ detail.userAgent }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { OPERATION_LOG_STATUS_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { displayText } from '@/utils/businessDisplay'
import { useAuthStore } from '@/stores/auth'
import JsonPreview from '@/components/JsonPreview.vue'
import * as logApi from '@/api/operationLog'
import type { OperationLogVO, OperationLogDetailVO } from '@/types/operationLog'

// ---- Table ----
const authStore = useAuthStore()
const loading = ref(false)
const tableData = ref<OperationLogVO[]>([])
const total = ref(0)

const dateRange = ref<[string, string] | null>(null)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  username: '',
  moduleName: '',
  operationType: '',
  requestMethod: '',
  status: undefined as number | undefined
})

let latestFetchId=0
async function fetchData(next?: { pageNo?: number; pageSize?: number }) {
  const requestId = ++latestFetchId
  const targetPageNo = next?.pageNo ?? query.pageNo
  const targetPageSize = next?.pageSize ?? query.pageSize

  loading.value = true
  try {
    const params: Record<string, any> = {
      ...query,
      pageNo: targetPageNo,
      pageSize: targetPageSize
    }
    if (params.status === '' || params.status === undefined) delete params.status
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    } else {
      delete params.startTime
      delete params.endTime
    }
    const res = await logApi.getOperationLogPage(params)
    if(requestId!==latestFetchId)return;tableData.value= Array.isArray(res.records) ? res.records : []
    total.value = Number(res.total || 0)
    query.pageNo = Number(res.pageNo || targetPageNo)
    query.pageSize = Number(res.pageSize || targetPageSize)
  } catch (error) {
    console.error('[OperationLog] fetchData failed:', error)
  } finally {
    if(requestId===latestFetchId)loading.value=false
  }
}

// ---- Pagination event handlers ----
function handlePageChange(page: number) {
  fetchData({ pageNo: page, pageSize: query.pageSize })
}

function handleSizeChange(size: number) {
  fetchData({ pageNo: 1, pageSize: size })
}

function handleSearch() {
  fetchData({ pageNo: 1, pageSize: query.pageSize })
}

function handleReset() {
  query.username = ''
  query.moduleName = ''
  query.operationType = ''
  query.requestMethod = ''
  query.status = undefined
  dateRange.value = null
  fetchData({ pageNo: 1, pageSize: 10 })
}

// ---- Detail ----
const detailVisible = ref(false)
const detail = ref<OperationLogDetailVO | null>(null)

async function openDetail(id: number) {
  try {
    detail.value = await logApi.getOperationLogDetail(id)
    detailVisible.value = true
  } catch { /* ignore */ }
}

// ---- Delete ----
async function handleDelete(row: OperationLogVO) {
  try {
    await ElMessageBox.confirm(`确定要删除该操作日志吗？（ID: ${row.id}）`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await logApi.deleteOperationLog(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled or error */ }
}

// ---- Helpers ----
function methodTagType(method: string): 'success' | 'primary' | 'warning' | 'danger' | 'info' {
  const map: Record<string, any> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'info'
  }
  return map[method] || 'info'
}

// ---- Init ----
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.log-list-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
  padding-bottom: 24px;
}

.table-card {
  flex-shrink: 0;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}
.table-wrapper {
  width: 100%;
  overflow-x: auto;
}
.pagination-wrapper {
  position: relative;
  z-index: 1;
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  padding-bottom: 8px;
}
</style>
