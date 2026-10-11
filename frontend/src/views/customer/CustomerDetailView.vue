<template>
  <div class="detail-page">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="$router.push('/customer/list')">返回列表</el-button>
      <span class="detail-title">客户详情</span>
      <div>
        <el-button v-if="authStore.canWriteBusiness" type="primary" @click="openEdit">编辑客户</el-button>
        <el-button v-if="authStore.canWriteBusiness" type="success" @click="goCreateOpp">创建商机</el-button>
        <el-button v-if="authStore.canWriteBusiness" @click="openAddFollow">添加跟进</el-button>
        <el-button type="warning" :icon="MagicStick" @click="openAiAnalysis" :loading="aiLoading">AI分析</el-button>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-descriptions v-if="detail" :column="3" border size="small" title="基本信息">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ detail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="行业">{{ displayIndustry(detail.industry) }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ displaySource(detail.source) }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ getLabelByValue(CUSTOMER_LEVEL_OPTIONS, detail.level) }}</el-descriptions-item>
        <el-descriptions-item label="地区">{{ detail.region || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ detail.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status===1?'success':detail.status===2?'warning':'info'" size="small">{{ getLabelByValue(CUSTOMER_STATUS_OPTIONS, detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ detail.contactEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detail.ownerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ detail.score ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="最近跟进">{{ formatDateTime(detail.lastFollowTime) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" style="margin-top:16px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="关联商机" name="opps">
          <el-table :data="opportunities" stripe border size="small">
            <el-table-column prop="id" label="ID" width="55"/>
            <el-table-column prop="opportunityName" label="商机名称"/>
            <el-table-column label="阶段" width="100"><template #default="{row}">{{ displayOpportunityStage(row.stage, row.stageName) }}</template></el-table-column>
            <el-table-column prop="amount" label="金额" width="100"/>
            <el-table-column prop="probability" label="概率(%)" width="80"/>
            <el-table-column prop="expectedDate" label="预计成交" width="110"/>
            <el-table-column label="操作" width="100"><template #default="{row}"><el-button text type="primary" size="small" @click="$router.push(`/opportunity/${row.id}`)">查看</el-button></template></el-table-column>
          </el-table>
          <el-empty v-if="!opportunities.length" description="暂无关联商机"/>
        </el-tab-pane>
        <el-tab-pane label="跟进记录" name="follows">
          <el-table :data="followRecords" stripe border size="small">
            <el-table-column prop="id" label="ID" width="55"/>
            <el-table-column label="跟进方式" width="90"><template #default="{row}">{{ displayFollowType(row.followType) }}</template></el-table-column>
            <el-table-column prop="followContent" label="跟进内容" show-overflow-tooltip/>
            <el-table-column prop="followResult" label="跟进结果" width="150" show-overflow-tooltip/>
            <el-table-column label="客户意向" width="80"><template #default="{row}">{{ getLabelByValue(CUSTOMER_INTENTION_OPTIONS, row.customerIntention) }}</template></el-table-column>
            <el-table-column prop="followerName" label="跟进人" width="90"/>
            <el-table-column prop="followTime" label="跟进时间" width="110"/>
          </el-table>
          <el-empty v-if="!followRecords.length" description="暂无跟进记录"/>
          <el-pagination v-model:current-page="followQuery.pageNo" :page-size="followQuery.pageSize" :total="followTotal" layout="total,prev,pager,next" style="margin-top:12px" @current-change="loadFollowRecords"/>
        </el-tab-pane>
        <el-tab-pane label="关联订单" name="orders">
          <el-table :data="orders" stripe border size="small">
            <el-table-column prop="id" label="ID" width="55"/>
            <el-table-column prop="orderNo" label="订单编号" width="160"/>
            <el-table-column prop="amount" label="金额" width="100"/>
            <el-table-column label="状态" width="80"><template #default="{row}">{{ displayOrderStatus(row.status, row.statusName) }}</template></el-table-column>
            <el-table-column label="支付方式" width="90"><template #default="{row}">{{ displayPaymentMethod(row.paymentMethod) }}</template></el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="110"/>
            <el-table-column label="操作" width="100"><template #default="{row}"><el-button text type="primary" size="small" @click="$router.push(`/order/${row.id}`)">查看</el-button></template></el-table-column>
          </el-table>
          <el-empty v-if="!orders.length" description="暂无关联订单"/>
          <el-pagination v-model:current-page="orderQuery.pageNo" :page-size="orderQuery.pageSize" :total="orderTotal" layout="total,prev,pager,next" style="margin-top:12px" @current-change="loadOrders"/>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog v-model="editVisible" title="编辑客户" width="560px" :close-on-click-modal="false">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="客户名称" prop="customerName"><el-input v-model="editForm.customerName"/></el-form-item>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="行业"><el-input v-model="editForm.industry"/></el-form-item></el-col><el-col :span="12"><el-form-item label="来源"><el-input v-model="editForm.source"/></el-form-item></el-col></el-row>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="等级"><el-select v-model="editForm.level" style="width:100%"><el-option v-for="o in CUSTOMER_LEVEL_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="状态"><el-select v-model="editForm.status" style="width:100%"><el-option v-for="o in CUSTOMER_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item></el-col></el-row>
        <el-form-item label="地区"><el-input v-model="editForm.region"/></el-form-item>
        <el-form-item label="地址"><el-input v-model="editForm.address"/></el-form-item>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="联系人"><el-input v-model="editForm.contactName"/></el-form-item></el-col><el-col :span="12"><el-form-item label="联系电话"><el-input v-model="editForm.contactPhone"/></el-form-item></el-col></el-row>
        <el-form-item label="联系邮箱"><el-input v-model="editForm.contactEmail"/></el-form-item>
        <el-form-item label="评分"><el-input-number v-model="editForm.score" :min="0" :max="100" style="width:100%"/></el-form-item>
        <el-form-item label="备注"><el-input v-model="editForm.remark" type="textarea" :rows="3"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="editVisible=false">取消</el-button><el-button type="primary" :loading="editLoading" @click="submitEdit">确定</el-button></template>
    </el-dialog>

    <!-- Follow Dialog -->
    <el-dialog v-model="followVisible" title="添加跟进记录" width="500px" :close-on-click-modal="false" @closed="resetFollow">
      <el-form ref="followRef" :model="fForm" :rules="fRules" label-width="100px">
        <el-form-item label="关联商机" prop="opportunityId"><el-select v-model="fForm.opportunityId" filterable placeholder="请选择商机" style="width:100%"><el-option v-for="o in opportunities" :key="o.id" :label="o.opportunityName" :value="o.id"/></el-select></el-form-item>
        <el-form-item label="跟进方式" prop="followType"><el-select v-model="fForm.followType" style="width:100%"><el-option v-for="o in FOLLOW_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="跟进内容" prop="followContent"><el-input v-model="fForm.followContent" type="textarea" :rows="3"/></el-form-item>
        <el-form-item label="跟进结果"><el-input v-model="fForm.followResult" type="textarea" :rows="2"/></el-form-item>
        <el-form-item label="客户意向"><el-select v-model="fForm.customerIntention" style="width:100%"><el-option v-for="o in CUSTOMER_INTENTION_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="跟进时间"><el-date-picker v-model="fForm.followTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%"/></el-form-item>
        <el-form-item label="下次跟进"><el-date-picker v-model="fForm.nextFollowTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="followVisible=false">取消</el-button><el-button type="primary" :loading="fLoading" @click="submitFollow">确定</el-button></template>
    </el-dialog>

    <!-- AI Analysis Dialog -->
    <el-dialog v-model="aiVisible" title="AI 客户分析" width="640px" :close-on-click-modal="false">
      <div v-if="aiResult" class="ai-analysis">
        <el-descriptions :column="2" border size="small" title="分析结论">
          <el-descriptions-item label="客户名称">{{ aiResult.customerName }}</el-descriptions-item>
          <el-descriptions-item label="价值等级">
            <el-tag :type="aiResult.customerLevel==='高价值客户'?'danger':aiResult.customerLevel==='中价值客户'?'warning':'info'">
              {{ aiResult.customerLevel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="aiResult.riskLevel==='高风险'?'danger':aiResult.riskLevel==='中风险'?'warning':'success'">
              {{ aiResult.riskLevel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分析摘要" :span="2">{{ aiResult.summary }}</el-descriptions-item>
        </el-descriptions>

        <el-card shadow="never" style="margin-top:12px" v-if="aiResult.riskTips.length">
          <template #header><span style="font-weight:600">风险提示</span></template>
          <ul class="ai-list">
            <li v-for="(tip,i) in aiResult.riskTips" :key="i">{{ tip }}</li>
          </ul>
        </el-card>

        <el-card shadow="never" style="margin-top:12px" v-if="aiResult.suggestions.length">
          <template #header><span style="font-weight:600">跟进建议</span></template>
          <ul class="ai-list">
            <li v-for="(sug,i) in aiResult.suggestions" :key="i">{{ sug }}</li>
          </ul>
        </el-card>

        <el-card shadow="never" style="margin-top:12px" v-if="aiResult.metrics">
          <template #header><span style="font-weight:600">关键指标</span></template>
          <el-row :gutter="16">
            <el-col :span="8"><div class="ai-metric"><span class="ai-num">{{ aiResult.metrics.opportunityCount }}</span><span class="ai-label">商机数</span></div></el-col>
            <el-col :span="8"><div class="ai-metric"><span class="ai-num">{{ aiResult.metrics.orderCount }}</span><span class="ai-label">订单数</span></div></el-col>
            <el-col :span="8"><div class="ai-metric"><span class="ai-num">{{ aiResult.metrics.followRecordCount }}</span><span class="ai-label">跟进数</span></div></el-col>
            <el-col :span="12"><div class="ai-metric"><span class="ai-num">¥{{ aiResult.metrics.totalOrderAmount.toFixed(0) }}</span><span class="ai-label">订单总额</span></div></el-col>
            <el-col :span="12"><div class="ai-metric"><span class="ai-num">¥{{ aiResult.metrics.paidAmount.toFixed(0) }}</span><span class="ai-label">已付金额</span></div></el-col>
          </el-row>
          <div v-if="aiResult.metrics.lastFollowTime" style="margin-top:8px;font-size:12px;color:#909399">最近跟进：{{ aiResult.metrics.lastFollowTime }}</div>
        </el-card>
      </div>
      <el-empty v-else-if="!aiLoading" description="暂无分析数据"/>
      <template #footer>
        <el-button @click="aiVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, MagicStick } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { CUSTOMER_STATUS_OPTIONS, CUSTOMER_LEVEL_OPTIONS, CUSTOMER_INTENTION_OPTIONS, OPPORTUNITY_STAGE_OPTIONS, ORDER_STATUS_OPTIONS, FOLLOW_TYPE_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { displayFollowType, displayIndustry, displayOpportunityStage, displayOrderStatus, displayPaymentMethod, displaySource } from '@/utils/businessDisplay'
import { useAuthStore } from '@/stores/auth'
import type { FollowRecordVO } from '@/types/followRecord'
import type { OrderVO } from '@/types/order'
import * as customerApi from '@/api/customer'
import * as followApi from '@/api/followRecord'
import * as aiApi from '@/api/ai'
import type { CustomerDetailVO } from '@/types/customer'
import type { OpportunityVO } from '@/types/opportunity'
import type { CustomerAiAnalysisVO } from '@/types/ai'

const route = useRoute();const router = useRouter();const authStore = useAuthStore()
const id = Number(route.params.id)

const loading=ref(false);const detail=ref<CustomerDetailVO|null>(null)
const activeTab=ref('opps')
const opportunities=ref<OpportunityVO[]>([]);const followRecords=ref<FollowRecordVO[]>([]);const orders=ref<OrderVO[]>([])
const followQuery=reactive({pageNo:1,pageSize:10}),orderQuery=reactive({pageNo:1,pageSize:10})
const followTotal=ref(0),orderTotal=ref(0)

async function loadDetail(){loading.value=true;try{detail.value=await customerApi.getCustomerDetail(id);await loadRelated()}catch{}finally{loading.value=false}}
async function loadOpportunities(){try{opportunities.value=await customerApi.getCustomerOpportunities(id)}catch{}}
let latestFollowFetchId=0
async function loadFollowRecords(){
  const requestId=++latestFollowFetchId;try{const result=await customerApi.getCustomerFollowRecords(id,{...followQuery});if(requestId!==latestFollowFetchId)return;followRecords.value=result.records;followTotal.value=result.total}catch{}}
let latestOrderFetchId=0
async function loadOrders(){
  const requestId=++latestOrderFetchId;try{const result=await customerApi.getCustomerOrders(id,{...orderQuery});if(requestId!==latestOrderFetchId)return;orders.value=result.records;orderTotal.value=result.total}catch{}}
async function loadRelated(){await Promise.all([loadOpportunities(),loadFollowRecords(),loadOrders()])}

// Edit
const editVisible=ref(false),editLoading=ref(false),editFormRef=ref<FormInstance>()
const editForm=reactive({customerName:'',industry:'',source:'',level:1,region:'',address:'',contactName:'',contactPhone:'',contactEmail:'',status:1,score:80,remark:''})
const editRules:FormRules={customerName:[{required:true,whitespace:true,max:128,message:'请输入客户名称（最多128字）',trigger:'blur'}]}
function openEdit(){if(!detail.value)return;Object.assign(editForm,{customerName:detail.value.customerName,industry:detail.value.industry||'',source:detail.value.source||'',level:detail.value.level,region:detail.value.region||'',address:detail.value.address||'',contactName:detail.value.contactName||'',contactPhone:detail.value.contactPhone||'',contactEmail:detail.value.contactEmail||'',status:detail.value.status,score:detail.value.score??80,remark:detail.value.remark||''});editVisible.value=true}
async function submitEdit(){const v=await editFormRef.value?.validate().catch(()=>false);if(!v)return;editLoading.value=true;try{await customerApi.updateCustomer(id,editForm);ElMessage.success('修改成功');editVisible.value=false;loadDetail()}catch{}finally{editLoading.value=false}}

// Follow
const followVisible=ref(false),fLoading=ref(false),followRef=ref<FormInstance>()
const fForm=reactive({opportunityId:undefined as number|undefined,followType:'电话',followContent:'',followResult:'',customerIntention:2,followTime:'',nextFollowTime:''})
const fRules:FormRules={opportunityId:[{required:true,message:'请选择商机',trigger:'change'}],followType:[{required:true,message:'请选择跟进方式',trigger:'change'}],followContent:[{required:true,whitespace:true,message:'请输入跟进内容',trigger:'blur'}]}
function openAddFollow(){resetFollow();if(opportunities.value.length===1)fForm.opportunityId=opportunities.value[0].id;followVisible.value=true}
function resetFollow(){fForm.opportunityId=undefined;fForm.followType='电话';fForm.followContent='';fForm.followResult='';fForm.customerIntention=2;fForm.followTime='';fForm.nextFollowTime='';followRef.value?.clearValidate()}
async function submitFollow(){const v=await followRef.value?.validate().catch(()=>false);if(!v)return;fLoading.value=true;try{await followApi.createFollowRecord({...fForm,customerId:id,followerId:authStore.user?.id});ElMessage.success('跟进记录添加成功');followVisible.value=false;loadRelated()}catch{}finally{fLoading.value=false}}

function goCreateOpp(){router.push(`/opportunity/list?create=1&customerId=${id}&customerName=${encodeURIComponent(detail.value?.customerName||'')}`)}

// ---- AI Analysis ----
const aiVisible=ref(false),aiLoading=ref(false)
const aiResult=ref<CustomerAiAnalysisVO|null>(null)
async function openAiAnalysis(){
  aiVisible.value=true;aiLoading.value=true;aiResult.value=null
  try{aiResult.value=await aiApi.getCustomerAnalysis(id)}catch{}finally{aiLoading.value=false}
}

onMounted(()=>loadDetail())
</script>
<style scoped>
.detail-page{display:flex;flex-direction:column;gap:16px}.detail-header{display:flex;justify-content:space-between;align-items:center}.detail-title{font-size:18px;font-weight:600}
.ai-list{margin:0;padding:0 0 0 18px}.ai-list li{margin:6px 0;font-size:14px;line-height:1.6}.ai-metric{text-align:center;padding:12px 0}.ai-num{font-size:22px;font-weight:600;display:block;color:#409eff}.ai-label{font-size:12px;color:#909399;margin-top:4px}
</style>
