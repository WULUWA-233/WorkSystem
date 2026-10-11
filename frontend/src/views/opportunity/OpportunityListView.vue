<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="商机名称"><el-input v-model="query.opportunityName" placeholder="商机名称" clearable style="width:160px"/></el-form-item>
        <el-form-item label="阶段">
          <el-select v-model="query.stage" placeholder="全部" clearable style="width:130px">
            <el-option v-for="o in OPPORTUNITY_STAGE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="金额范围">
          <el-input-number v-model="query.minAmount" :min="0" placeholder="最小" style="width:110px" controls-position="right"/>
          <span style="margin:0 4px">-</span>
          <el-input-number v-model="query.maxAmount" :min="0" placeholder="最大" style="width:110px" controls-position="right"/>
        </el-form-item>
        <el-form-item label="预计成交">
          <el-date-picker v-model="expDateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header><div class="card-header"><span>商机列表</span><el-button v-if="authStore.canWriteBusiness" type="primary" :icon="Plus" @click="openCreate">新增商机</el-button></div></template>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="55"/>
        <el-table-column prop="opportunityName" label="商机名称" width="150"/>
        <el-table-column prop="customerName" label="客户名称" width="140"/>
        <el-table-column label="阶段" width="100">
          <template #default="{row}"><el-tag :type="stageTag(row.stage)" size="small">{{ displayOpportunityStage(row.stage, row.stageName) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="110"/>
        <el-table-column prop="probability" label="概率(%)" width="80"/>
        <el-table-column prop="expectedDate" label="预计成交" width="110"/>
        <el-table-column prop="ownerName" label="负责人" width="90"/>
        <el-table-column prop="createdAt" label="创建时间" width="110"><template #default="{row}">{{ formatDateTime(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" fixed="right" min-width="340">
          <template #default="{row}">
            <el-button text type="primary" size="small" @click="$router.push(`/opportunity/${row.id}`)">详情</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="primary" size="small" @click="openEdit(row)" :disabled="row.stage===5||row.stage===6">编辑</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="warning" size="small" @click="openStage(row)" :disabled="row.stage===5||row.stage===6">推进</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="success" size="small" @click="openWin(row)" :disabled="row.stage===5||row.stage===6">赢单</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="danger" size="small" @click="openLose(row)" :disabled="row.stage===5||row.stage===6">输单</el-button>
            <el-button v-if="authStore.canWriteBusiness && row.stage===5" text type="success" size="small" @click="$router.push(`/order/list?create=1&opportunityId=${row.id}`)">创建订单</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next,jumper" background style="margin-top:16px;justify-content:flex-end" @current-change="handlePageChange" @size-change="handleSizeChange"/>
    </el-card>

    <!-- Create/Edit -->
    <el-dialog v-model="formVisible" :title="isEdit?'编辑商机':'新增商机'" width="520px" :close-on-click-modal="false" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="商机名称" prop="opportunityName"><el-input v-model="form.opportunityName"/></el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width:100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="阶段" v-if="!isEdit">
          <el-select v-model="form.stage" style="width:100%"><el-option v-for="o in OPPORTUNITY_STAGE_OPTIONS.filter(x=>[1,2,3,4].includes(x.value as number))" :key="o.value" :label="o.label" :value="o.value"/></el-select>
        </el-form-item>
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%"/></el-form-item></el-col><el-col :span="12"><el-form-item label="概率(%)"><el-input-number v-model="form.probability" :min="0" :max="100" style="width:100%"/></el-form-item></el-col></el-row>
        <el-form-item label="预计成交"><el-date-picker v-model="form.expectedDate" type="date" value-format="YYYY-MM-DD" style="width:100%"/></el-form-item>
        <el-form-item label="负责人ID" prop="ownerId"><el-input-number v-model="form.ownerId" :min="1" style="width:100%"/></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="formVisible=false">取消</el-button><el-button type="primary" :loading="formLoading" @click="submitForm">确定</el-button></template>
    </el-dialog>

    <!-- Stage Advance -->
    <el-dialog v-model="stageVisible" title="阶段推进" width="400px">
      <el-form label-width="80px">
        <el-form-item label="商机">{{ stageTarget?.opportunityName }}</el-form-item>
        <el-form-item label="当前阶段">{{ displayOpportunityStage(stageTarget?.stage, stageTarget?.stageName) }}</el-form-item>
        <el-form-item label="推进至">
          <el-select v-model="stageForm.stage" style="width:100%"><el-option v-for="s in [1,2,3,4]" :key="s" :label="getLabelByValue(OPPORTUNITY_STAGE_OPTIONS,s)" :value="s"/></el-select>
        </el-form-item>
        <el-form-item label="概率(%)"><el-input-number v-model="stageForm.probability" :min="0" :max="100" style="width:100%"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="stageVisible=false">取消</el-button><el-button type="primary" :loading="stageLoading" @click="confirmStage">确定</el-button></template>
    </el-dialog>

    <!-- Win -->
    <el-dialog v-model="winVisible" title="确认赢单" width="400px">
      <p>确定将商机 <b>{{ winTarget?.opportunityName }}</b> 标记为赢单吗？</p>
      <el-form label-width="60px"><el-form-item label="备注"><el-input v-model="winForm.remark" type="textarea"/></el-form-item></el-form>
      <template #footer><el-button @click="winVisible=false">取消</el-button><el-button type="primary" :loading="winLoading" @click="confirmWin">确定赢单</el-button></template>
    </el-dialog>

    <!-- Lose -->
    <el-dialog v-model="loseVisible" title="确认输单" width="400px">
      <el-form ref="loseFormRef" :model="loseForm" :rules="loseRules" label-width="80px">
        <el-form-item label="商机">{{ loseTarget?.opportunityName }}</el-form-item>
        <el-form-item label="输单原因" prop="loseReason"><el-input v-model="loseForm.loseReason" type="textarea" :rows="3"/></el-form-item>
        <el-form-item label="备注"><el-input v-model="loseForm.remark" type="textarea"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="loseVisible=false">取消</el-button><el-button type="danger" :loading="loseLoading" @click="confirmLose">确定输单</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { OPPORTUNITY_STAGE_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { displayOpportunityStage } from '@/utils/businessDisplay'
import { useAuthStore } from '@/stores/auth'
import { fetchAllPages } from '@/utils/pagination'
import * as customerApi from '@/api/customer'
import * as oppApi from '@/api/opportunity'
import type { CustomerVO } from '@/types/customer'
import type { OpportunityVO, OpportunityDetailVO } from '@/types/opportunity'

const route=useRoute();const router=useRouter();const authStore=useAuthStore()

const loading=ref(false);const tableData=ref<OpportunityVO[]>([]);const total=ref(0)
const customerOptions=ref<CustomerVO[]>([])
const expDateRange=ref<[string,string]|null>(null)
const query=reactive({pageNo:1,pageSize:10,opportunityName:'',stage:undefined as number|undefined,customerId:undefined as number|undefined,minAmount:undefined as number|undefined,maxAmount:undefined as number|undefined})

let latestFetchId=0
async function fetchData(){
  const requestId=++latestFetchId;loading.value=true;try{const p:Record<string,any>={...query};if(p.stage===''||p.stage===undefined)delete p.stage;if(p.customerId===undefined)delete p.customerId;if(expDateRange.value?.length===2){p.expectedStartDate=expDateRange.value[0];p.expectedEndDate=expDateRange.value[1]};const r=await oppApi.getOpportunityPage(p);if(requestId!==latestFetchId)return;tableData.value=r.records;total.value=r.total}catch{}finally{if(requestId===latestFetchId)loading.value=false}}
function handleSearch(){query.pageNo=1;fetchData()}
function handleReset(){query.opportunityName='';query.stage=undefined;query.minAmount=undefined;query.maxAmount=undefined;query.customerId=undefined;expDateRange.value=null;query.pageNo=1;fetchData()}
function handlePageChange(page:number){query.pageNo=page;fetchData()}
function handleSizeChange(size:number){query.pageSize=size;query.pageNo=1;fetchData()}

function stageTag(s:number){return s===5?'success':s===6?'danger':s>=3?'warning':'info'}

async function loadCustomerOptions(){try{customerOptions.value=await fetchAllPages(customerApi.getCustomerPage,{status:1})}catch{customerOptions.value=[]}}

// Query param for customerId from navigation
onMounted(()=>{const cid=route.query.customerId;if(cid)query.customerId=Number(cid);fetchData();if(route.query.create==='1'&&authStore.canWriteBusiness){openCreate();void router.replace({query:{...route.query,create:undefined}})}})

// Create/Edit
const formVisible=ref(false),formLoading=ref(false),isEdit=ref(false),editId=ref<number|null>(null),formRef=ref<FormInstance>()
const form=reactive({opportunityName:'',customerId:undefined as number|undefined,stage:1,amount:undefined as number|undefined,probability:undefined as number|undefined,expectedDate:'',ownerId:authStore.user?.id as number|undefined,remark:''})
const formRules:FormRules={opportunityName:[{required:true,whitespace:true,max:200,message:'请输入商机名称（最多200字）',trigger:'blur'}],customerId:[{required:true,message:'请选择客户',trigger:'change'}],ownerId:[{required:true,type:'number',min:1,message:'请选择负责人',trigger:'change'}]}
function openCreate(){isEdit.value=false;editId.value=null;resetForm();loadCustomerOptions();const cid=route.query.customerId;if(cid)form.customerId=Number(cid);formVisible.value=true}
async function openEdit(row:OpportunityVO){isEdit.value=true;editId.value=row.id;try{const d:OpportunityDetailVO=await oppApi.getOpportunityDetail(row.id);Object.assign(form,{opportunityName:d.opportunityName,customerId:d.customerId,amount:d.amount,probability:d.probability??undefined,expectedDate:d.expectedDate||'',ownerId:d.ownerId,remark:d.remark||''});formVisible.value=true}catch{}}
function resetForm(){form.opportunityName='';form.customerId=undefined;form.stage=1;form.amount=undefined;form.probability=undefined;form.expectedDate='';form.ownerId=authStore.user?.id;form.remark='';formRef.value?.clearValidate()}
async function submitForm(){const v=await formRef.value?.validate().catch(()=>false);if(!v)return;formLoading.value=true;try{if(isEdit.value&&editId.value){await oppApi.updateOpportunity(editId.value,{opportunityName:form.opportunityName,amount:form.amount,probability:form.probability,expectedDate:form.expectedDate||undefined,ownerId:form.ownerId,remark:form.remark||undefined})}else{await oppApi.createOpportunity(form)};ElMessage.success(isEdit.value?'修改成功':'新增成功');formVisible.value=false;fetchData()}catch{}finally{formLoading.value=false}}

// Stage
const stageVisible=ref(false),stageLoading=ref(false),stageTarget=ref<OpportunityVO|null>(null)
const stageForm=reactive({stage:1 as number,probability:undefined as number|undefined})
function openStage(row:OpportunityVO){stageTarget.value=row;stageForm.stage=Math.min(row.stage+1,4);stageForm.probability=row.probability;stageVisible.value=true}
async function confirmStage(){if(!stageTarget.value)return;stageLoading.value=true;try{await oppApi.advanceOpportunityStage(stageTarget.value.id,{stage:stageForm.stage as 1|2|3|4,probability:stageForm.probability});ElMessage.success('阶段推进成功');stageVisible.value=false;fetchData()}catch{}finally{stageLoading.value=false}}

// Win
const winVisible=ref(false),winLoading=ref(false),winTarget=ref<OpportunityVO|null>(null)
const winForm=reactive({remark:''})
function openWin(row:OpportunityVO){winTarget.value=row;winForm.remark='';winVisible.value=true}
async function confirmWin(){if(!winTarget.value)return;winLoading.value=true;try{await oppApi.winOpportunity(winTarget.value.id,{remark:winForm.remark||undefined});ElMessage.success('赢单成功');winVisible.value=false;fetchData()}catch{}finally{winLoading.value=false}}

// Lose
const loseVisible=ref(false),loseLoading=ref(false),loseTarget=ref<OpportunityVO|null>(null),loseFormRef=ref<FormInstance>()
const loseForm=reactive({loseReason:'',remark:''})
const loseRules:FormRules={loseReason:[{required:true,message:'请输入输单原因',trigger:'blur'}]}
function openLose(row:OpportunityVO){loseTarget.value=row;loseForm.loseReason='';loseForm.remark='';loseVisible.value=true}
async function confirmLose(){const v=await loseFormRef.value?.validate().catch(()=>false);if(!v||!loseTarget.value)return;loseLoading.value=true;try{await oppApi.loseOpportunity(loseTarget.value.id,{loseReason:loseForm.loseReason,remark:loseForm.remark||undefined});ElMessage.success('已标记为输单');loseVisible.value=false;fetchData()}catch{}finally{loseLoading.value=false}}

// Delete
async function handleDelete(row:OpportunityVO){try{await ElMessageBox.confirm(`确定删除商机"${row.opportunityName}"吗？`,'删除确认',{type:'warning',confirmButtonText:'确定删除',cancelButtonText:'取消'});await oppApi.deleteOpportunity(row.id);ElMessage.success('删除成功');fetchData()}catch{}}
</script>
<style scoped>.page-container{display:flex;flex-direction:column;gap:16px}.search-card :deep(.el-card__body){padding-bottom:0}.card-header{display:flex;justify-content:space-between;align-items:center}</style>
