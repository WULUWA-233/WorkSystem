<template>
  <div class="detail-page">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="$router.push('/opportunity/list')">返回列表</el-button>
      <span class="detail-title">商机详情</span>
      <div>
        <el-button v-if="authStore.canWriteBusiness" type="primary" @click="openEdit" :disabled="detail?.stage===5||detail?.stage===6">编辑</el-button>
        <el-button type="warning" v-if="authStore.canWriteBusiness" @click="openStage" :disabled="detail?.stage===5||detail?.stage===6">阶段推进</el-button>
        <el-button type="success" v-if="authStore.canWriteBusiness" @click="openWin" :disabled="detail?.stage===5||detail?.stage===6">赢单</el-button>
        <el-button type="danger" v-if="authStore.canWriteBusiness" @click="openLose" :disabled="detail?.stage===5||detail?.stage===6">输单</el-button>
        <el-button type="success" @click="$router.push(`/order/list?create=1&opportunityId=${id}`)" v-if="authStore.canWriteBusiness && detail?.stage===5">创建订单</el-button>
        <el-button v-if="authStore.canWriteBusiness" @click="openAddFollow">添加跟进</el-button>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-descriptions v-if="detail" :column="3" border size="small" title="基本信息">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="商机名称">{{ detail.opportunityName }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detail.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="阶段"><el-tag :type="stageTag(detail.stage)" size="small">{{ displayOpportunityStage(detail.stage, detail.stageName) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="金额">{{ detail.amount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="概率">{{ detail.probability ?? '-' }}%</el-descriptions-item>
        <el-descriptions-item label="预计成交">{{ detail.expectedDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detail.ownerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.winTime" label="赢单时间">{{ formatDateTime(detail.winTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.closeTime" label="关闭时间">{{ formatDateTime(detail.closeTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.loseReason" label="输单原因" :span="3">{{ detail.loseReason }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" style="margin-top:16px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="跟进记录" name="follows">
          <el-table :data="followRecords" stripe border size="small">
            <el-table-column prop="id" label="ID" width="55"/><el-table-column label="方式" width="80"><template #default="{row}">{{ displayFollowType(row.followType) }}</template></el-table-column>
            <el-table-column prop="followContent" label="跟进内容" show-overflow-tooltip/><el-table-column prop="followerName" label="跟进人" width="80"/>
            <el-table-column prop="followTime" label="跟进时间" width="110"/>
          </el-table>
          <el-empty v-if="!followRecords.length" description="暂无跟进"/>
          <el-pagination v-model:current-page="followQuery.pageNo" :page-size="followQuery.pageSize" :total="followTotal" layout="total,prev,pager,next" style="margin-top:12px" @current-change="loadFollowRecords"/>
        </el-tab-pane>
        <el-tab-pane label="关联订单" name="orders">
          <el-table :data="orders" stripe border size="small">
            <el-table-column prop="id" label="ID" width="55"/><el-table-column prop="orderNo" label="订单编号" width="160"/>
            <el-table-column prop="amount" label="金额" width="100"/><el-table-column label="状态" width="80"><template #default="{row}">{{ displayOrderStatus(row.status, row.statusName) }}</template></el-table-column>
            <el-table-column label="操作" width="100"><template #default="{row}"><el-button text type="primary" size="small" @click="$router.push(`/order/${row.id}`)">查看</el-button></template></el-table-column>
          </el-table>
          <el-empty v-if="!orders.length" description="暂无订单"/>
          <el-pagination v-model:current-page="orderQuery.pageNo" :page-size="orderQuery.pageSize" :total="orderTotal" layout="total,prev,pager,next" style="margin-top:12px" @current-change="loadOrders"/>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Dialogs same as list page -->
    <el-dialog v-model="editVisible" title="编辑商机" width="500px" :close-on-click-modal="false"><el-form ref="editRef" :model="eForm" :rules="{opportunityName:[{required:true,whitespace:true,max:200,message:'请输入商机名称（最多200字）',trigger:'blur'}]}" label-width="100px"><el-form-item label="商机名称" prop="opportunityName"><el-input v-model="eForm.opportunityName"/></el-form-item><el-row :gutter="16"><el-col :span="12"><el-form-item label="金额"><el-input-number v-model="eForm.amount" :min="0" :precision="2" style="width:100%"/></el-form-item></el-col><el-col :span="12"><el-form-item label="概率(%)"><el-input-number v-model="eForm.probability" :min="0" :max="100" style="width:100%"/></el-form-item></el-col></el-row><el-form-item label="备注"><el-input v-model="eForm.remark" type="textarea" :rows="3"/></el-form-item></el-form><template #footer><el-button @click="editVisible=false">取消</el-button><el-button type="primary" :loading="eLoading" @click="submitEdit">确定</el-button></template></el-dialog>

    <el-dialog v-model="stageVisible" title="阶段推进" width="400px"><el-form label-width="80px"><el-form-item label="推进至"><el-select v-model="sForm.stage" style="width:100%"><el-option v-for="s in [1,2,3,4]" :key="s" :label="getLabelByValue(OPPORTUNITY_STAGE_OPTIONS,s)" :value="s"/></el-select></el-form-item><el-form-item label="概率"><el-input-number v-model="sForm.probability" :min="0" :max="100" style="width:100%"/></el-form-item></el-form><template #footer><el-button @click="stageVisible=false">取消</el-button><el-button type="primary" :loading="sLoading" @click="confirmStage">确定</el-button></template></el-dialog>

    <el-dialog v-model="winVisible" title="确认赢单" width="400px"><p>确定将此商机标记为赢单吗？</p><el-form label-width="60px"><el-form-item label="备注"><el-input v-model="wForm.remark" type="textarea"/></el-form-item></el-form><template #footer><el-button @click="winVisible=false">取消</el-button><el-button type="primary" :loading="wLoading" @click="confirmWin">确定赢单</el-button></template></el-dialog>

    <el-dialog v-model="loseVisible" title="确认输单" width="400px"><el-form ref="loseRef" :model="lForm" :rules="{loseReason:[{required:true,message:'请输入输单原因',trigger:'blur'}]}" label-width="80px"><el-form-item label="商机">{{ detail?.opportunityName }}</el-form-item><el-form-item label="输单原因" prop="loseReason"><el-input v-model="lForm.loseReason" type="textarea" :rows="3"/></el-form-item><el-form-item label="备注"><el-input v-model="lForm.remark" type="textarea"/></el-form-item></el-form><template #footer><el-button @click="loseVisible=false">取消</el-button><el-button type="danger" :loading="lLoading" @click="confirmLose">确定输单</el-button></template></el-dialog>

    <!-- Follow Dialog (simplified) -->
    <el-dialog v-model="followVisible" title="添加跟进" width="460px" :close-on-click-modal="false"><el-form ref="fRef" :model="fForm" :rules="{followType:[{required:true,message:'请选择跟进方式',trigger:'change'}],followContent:[{required:true,whitespace:true,message:'请输入跟进内容',trigger:'blur'}]}" label-width="80px"><el-form-item label="跟进方式" prop="followType"><el-select v-model="fForm.followType" style="width:100%"><el-option v-for="o in FOLLOW_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item><el-form-item label="跟进内容" prop="followContent"><el-input v-model="fForm.followContent" type="textarea" :rows="3"/></el-form-item><el-form-item label="客户意向"><el-select v-model="fForm.customerIntention" style="width:100%"><el-option v-for="o in CUSTOMER_INTENTION_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item></el-form><template #footer><el-button @click="followVisible=false">取消</el-button><el-button type="primary" :loading="fLoading" @click="submitFollow">确定</el-button></template></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { OPPORTUNITY_STAGE_OPTIONS, ORDER_STATUS_OPTIONS, FOLLOW_TYPE_OPTIONS, CUSTOMER_INTENTION_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { displayFollowType, displayOpportunityStage, displayOrderStatus } from '@/utils/businessDisplay'
import { useAuthStore } from '@/stores/auth'
import type { FollowRecordVO } from '@/types/followRecord'
import type { OrderVO } from '@/types/order'
import * as oppApi from '@/api/opportunity'
import * as followApi from '@/api/followRecord'
import type { OpportunityDetailVO } from '@/types/opportunity'

const route=useRoute();const authStore=useAuthStore();const id=Number(route.params.id)
const loading=ref(false);const detail=ref<OpportunityDetailVO|null>(null)
const activeTab=ref('follows');const followRecords=ref<FollowRecordVO[]>([]);const orders=ref<OrderVO[]>([])
const followQuery=reactive({pageNo:1,pageSize:10}),orderQuery=reactive({pageNo:1,pageSize:10})
const followTotal=ref(0),orderTotal=ref(0)
function stageTag(s:number){return s===5?'success':s===6?'danger':s>=3?'warning':'info'}

let latestFollowFetchId=0
async function loadFollowRecords(){
  const requestId=++latestFollowFetchId;try{const result=await oppApi.getOpportunityFollowRecords(id,{...followQuery});if(requestId!==latestFollowFetchId)return;followRecords.value=result.records;followTotal.value=result.total}catch{}}
let latestOrderFetchId=0
async function loadOrders(){
  const requestId=++latestOrderFetchId;try{const result=await oppApi.getOpportunityOrders(id,{...orderQuery});if(requestId!==latestOrderFetchId)return;orders.value=result.records;orderTotal.value=result.total}catch{}}
async function load(){loading.value=true;try{detail.value=await oppApi.getOpportunityDetail(id);await Promise.all([loadFollowRecords(),loadOrders()])}catch{}finally{loading.value=false}}

// Edit
const editVisible=ref(false),eLoading=ref(false),editRef=ref<FormInstance>()
const eForm=reactive({opportunityName:'',amount:undefined as number|undefined,probability:undefined as number|undefined,remark:''})
function openEdit(){if(!detail.value)return;eForm.opportunityName=detail.value.opportunityName;eForm.amount=detail.value.amount;eForm.probability=detail.value.probability;eForm.remark=detail.value.remark||'';editVisible.value=true}
async function submitEdit(){const valid=await editRef.value?.validate().catch(()=>false);if(!valid)return;eLoading.value=true;try{await oppApi.updateOpportunity(id,{opportunityName:eForm.opportunityName,amount:eForm.amount,probability:eForm.probability,remark:eForm.remark||undefined});ElMessage.success('修改成功');editVisible.value=false;load()}catch{}finally{eLoading.value=false}}

// Stage
const stageVisible=ref(false),sLoading=ref(false)
const sForm=reactive({stage:1 as number,probability:undefined as number|undefined})
function openStage(){if(!detail.value)return;sForm.stage=Math.min(detail.value.stage+1,4);sForm.probability=detail.value.probability;stageVisible.value=true}
async function confirmStage(){sLoading.value=true;try{await oppApi.advanceOpportunityStage(id,{stage:sForm.stage as 1|2|3|4,probability:sForm.probability});ElMessage.success('阶段推进成功');stageVisible.value=false;load()}catch{}finally{sLoading.value=false}}

// Win
const winVisible=ref(false),wLoading=ref(false);const wForm=reactive({remark:''})
function openWin(){wForm.remark='';winVisible.value=true}
async function confirmWin(){wLoading.value=true;try{await oppApi.winOpportunity(id,{remark:wForm.remark||undefined});ElMessage.success('赢单成功');winVisible.value=false;load()}catch{}finally{wLoading.value=false}}

// Lose
const loseVisible=ref(false),lLoading=ref(false),loseRef=ref<FormInstance>()
const lForm=reactive({loseReason:'',remark:''})
function openLose(){lForm.loseReason='';lForm.remark='';loseVisible.value=true}
async function confirmLose(){const v=await loseRef.value?.validate().catch(()=>false);if(!v)return;lLoading.value=true;try{await oppApi.loseOpportunity(id,{loseReason:lForm.loseReason,remark:lForm.remark||undefined});ElMessage.success('已标记为输单');loseVisible.value=false;load()}catch{}finally{lLoading.value=false}}

// Follow
const followVisible=ref(false),fLoading=ref(false),fRef=ref<FormInstance>()
const fForm=reactive({followType:'电话',followContent:'',customerIntention:2})
function openAddFollow(){fForm.followType='电话';fForm.followContent='';fForm.customerIntention=2;followVisible.value=true}
async function submitFollow(){const v=await fRef.value?.validate().catch(()=>false);if(!v)return;fLoading.value=true;try{await followApi.createFollowRecord({...fForm,customerId:detail.value?.customerId,opportunityId:id,followerId:authStore.user?.id});ElMessage.success('跟进添加成功');followVisible.value=false;load()}catch{}finally{fLoading.value=false}}

onMounted(()=>load())
</script>
<style scoped>.detail-page{display:flex;flex-direction:column;gap:16px}.detail-header{display:flex;justify-content:space-between;align-items:center}.detail-title{font-size:18px;font-weight:600}</style>
