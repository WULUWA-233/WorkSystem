<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="客户ID"><el-input-number v-model="query.customerId" :min="1" placeholder="客户ID" style="width:120px" controls-position="right"/></el-form-item>
        <el-form-item label="商机ID"><el-input-number v-model="query.opportunityId" :min="1" placeholder="商机ID" style="width:120px" controls-position="right"/></el-form-item>
        <el-form-item label="跟进方式">
          <el-select v-model="query.followType" placeholder="全部" clearable style="width:120px"><el-option v-for="o in FOLLOW_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select>
        </el-form-item>
        <el-form-item label="客户意向">
          <el-select v-model="query.customerIntention" placeholder="全部" clearable style="width:110px"><el-option v-for="o in CUSTOMER_INTENTION_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px"/>
        </el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="RefreshRight" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header><div class="card-header"><span>跟进记录</span><el-button v-if="authStore.canWriteBusiness" type="primary" :icon="Plus" @click="openCreate">新增跟进</el-button></div></template>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="55"/>
        <el-table-column prop="customerName" label="客户" width="120"/>
        <el-table-column prop="opportunityName" label="商机" width="120"/>
        <el-table-column label="跟进方式" width="90"><template #default="{row}">{{ displayFollowType(row.followType) }}</template></el-table-column>
        <el-table-column prop="followContent" label="跟进内容" min-width="160" show-overflow-tooltip/>
        <el-table-column prop="followResult" label="跟进结果" width="130" show-overflow-tooltip/>
        <el-table-column label="客户意向" width="80"><template #default="{row}">{{ getLabelByValue(CUSTOMER_INTENTION_OPTIONS,row.customerIntention??0) }}</template></el-table-column>
        <el-table-column prop="followerName" label="跟进人" width="80"/>
        <el-table-column prop="followTime" label="跟进时间" width="110"/>
        <el-table-column prop="nextFollowTime" label="下次跟进" width="110"/>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{row}"><el-button text type="primary" size="small" @click="openDetail(row.id)">详情</el-button><el-button v-if="authStore.canWriteBusiness" text type="danger" size="small" @click="handleDelete(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next,jumper" background style="margin-top:16px;justify-content:flex-end" @current-change="handlePageChange" @size-change="handleSizeChange"/>
    </el-card>

    <!-- Create/Detail Dialogs -->
    <el-dialog v-model="formVisible" title="新增跟进记录" width="560px" :close-on-click-modal="false" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width:100%" @change="handleFormCustomerChange">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="关联商机" prop="opportunityId">
          <el-select v-model="form.opportunityId" filterable placeholder="请选择商机" style="width:100%" @change="handleFormOpportunityChange">
            <el-option v-for="o in availableOpportunities" :key="o.id" :label="o.opportunityName" :value="o.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="跟进方式" prop="followType"><el-select v-model="form.followType" style="width:100%"><el-option v-for="o in FOLLOW_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="跟进内容" prop="followContent"><el-input v-model="form.followContent" type="textarea" :rows="3"/></el-form-item>
        <el-form-item label="跟进结果"><el-input v-model="form.followResult" type="textarea" :rows="2"/></el-form-item>
        <el-form-item label="客户意向"><el-select v-model="form.customerIntention" style="width:100%"><el-option v-for="o in CUSTOMER_INTENTION_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="下次计划"><el-input v-model="form.nextPlan" type="textarea" :rows="2"/></el-form-item>
        <el-form-item label="跟进时间"><el-date-picker v-model="form.followTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%"/></el-form-item>
        <el-form-item label="下次跟进时间"><el-date-picker v-model="form.nextFollowTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%"/></el-form-item>
        <el-form-item label="跟进人ID" prop="followerId"><el-input-number v-model="form.followerId" :min="1" style="width:100%"/></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="formVisible=false">取消</el-button><el-button type="primary" :loading="formLoading" @click="submitForm">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="跟进详情" width="560px"><el-descriptions v-if="detail" :column="2" border size="small">
      <el-descriptions-item label="ID">{{detail.id}}</el-descriptions-item><el-descriptions-item label="客户">{{detail.customerName||'-'}}</el-descriptions-item>
      <el-descriptions-item label="商机">{{detail.opportunityName||'-'}}</el-descriptions-item><el-descriptions-item label="跟进方式">{{displayFollowType(detail.followType)}}</el-descriptions-item>
      <el-descriptions-item label="跟进内容" :span="2">{{detail.followContent||'-'}}</el-descriptions-item>
      <el-descriptions-item label="跟进结果" :span="2">{{detail.followResult||'-'}}</el-descriptions-item>
      <el-descriptions-item label="客户意向">{{getLabelByValue(CUSTOMER_INTENTION_OPTIONS,detail.customerIntention??0)}}</el-descriptions-item>
      <el-descriptions-item label="跟进人">{{detail.followerName||'-'}}</el-descriptions-item>
      <el-descriptions-item label="跟进时间">{{formatDateTime(detail.followTime)}}</el-descriptions-item>
      <el-descriptions-item label="下次跟进">{{formatDateTime(detail.nextFollowTime)}}</el-descriptions-item>
      <el-descriptions-item label="下次计划" :span="2">{{detail.nextPlan||'-'}}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{detail.remark||'-'}}</el-descriptions-item>
    </el-descriptions><template #footer><el-button @click="detailVisible=false">关闭</el-button></template></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { FOLLOW_TYPE_OPTIONS, CUSTOMER_INTENTION_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { displayFollowType } from '@/utils/businessDisplay'
import { useAuthStore } from '@/stores/auth'
import { fetchAllPages } from '@/utils/pagination'
import * as customerApi from '@/api/customer'
import * as oppApi from '@/api/opportunity'
import * as followApi from '@/api/followRecord'
import type { CustomerVO } from '@/types/customer'
import type { OpportunityVO } from '@/types/opportunity'
import type { FollowRecordVO, FollowRecordDetailVO } from '@/types/followRecord'

const authStore=useAuthStore()
const loading=ref(false);const tableData=ref<FollowRecordVO[]>([]);const total=ref(0)
const customerOptions=ref<CustomerVO[]>([])
const opportunityOptions=ref<OpportunityVO[]>([])
const dateRange=ref<[string,string]|null>(null)
const query=reactive({pageNo:1,pageSize:10,customerId:undefined as number|undefined,opportunityId:undefined as number|undefined,followType:'',customerIntention:undefined as number|undefined,followerId:undefined as number|undefined})

let latestFetchId=0
async function fetchData(){
  const requestId=++latestFetchId;loading.value=true;try{const p:Record<string,any>={...query};if(p.followType==='')delete p.followType;if(p.customerIntention===''||p.customerIntention===undefined)delete p.customerIntention;if(p.customerId===undefined)delete p.customerId;if(p.opportunityId===undefined)delete p.opportunityId;if(dateRange.value?.length===2){p.startTime=dateRange.value[0]+' 00:00:00';p.endTime=dateRange.value[1]+' 23:59:59'};const r=await followApi.getFollowRecordPage(p);if(requestId!==latestFetchId)return;tableData.value=r.records;total.value=r.total}catch{}finally{if(requestId===latestFetchId)loading.value=false}}
function handleSearch(){query.pageNo=1;fetchData()}
function handleReset(){query.customerId=undefined;query.opportunityId=undefined;query.followType='';query.customerIntention=undefined;dateRange.value=null;query.pageNo=1;fetchData()}
function handlePageChange(page:number){query.pageNo=page;fetchData()}
function handleSizeChange(size:number){query.pageSize=size;query.pageNo=1;fetchData()}

const formVisible=ref(false),formLoading=ref(false),formRef=ref<FormInstance>()
const form=reactive({customerId:undefined as number|undefined,opportunityId:undefined as number|undefined,followType:'电话',followContent:'',followResult:'',customerIntention:2,nextPlan:'',nextFollowTime:'',followTime:'',followerId:authStore.user?.id as number|undefined,remark:''})
const formRules:FormRules={customerId:[{required:true,message:'请选择客户',trigger:'change'}],opportunityId:[{required:true,message:'请选择商机',trigger:'change'}],followType:[{required:true,message:'请选择跟进方式',trigger:'change'}],followContent:[{required:true,whitespace:true,message:'请输入跟进内容',trigger:'blur'}],followerId:[{required:true,type:'number',min:1,message:'请选择跟进人',trigger:'change'}]}
const availableOpportunities=computed(()=>opportunityOptions.value.filter(o=>!form.customerId||o.customerId===form.customerId))
async function loadFormOptions(){try{const [c,o]=await Promise.all([fetchAllPages(customerApi.getCustomerPage,{status:1}),fetchAllPages(oppApi.getOpportunityPage)]);customerOptions.value=c;opportunityOptions.value=o}catch{customerOptions.value=[];opportunityOptions.value=[]}}
function openCreate(){resetForm();loadFormOptions();formVisible.value=true}
function resetForm(){form.customerId=undefined;form.opportunityId=undefined;form.followType='电话';form.followContent='';form.followResult='';form.customerIntention=2;form.nextPlan='';form.nextFollowTime='';form.followTime='';form.followerId=authStore.user?.id;form.remark='';formRef.value?.clearValidate()}
function handleFormCustomerChange(){if(form.opportunityId&&!availableOpportunities.value.some(o=>o.id===form.opportunityId)){form.opportunityId=undefined}}
function handleFormOpportunityChange(id?:number){const opp=opportunityOptions.value.find(o=>o.id===id);if(opp)form.customerId=opp.customerId}
async function submitForm(){const v=await formRef.value?.validate().catch(()=>false);if(!v)return;formLoading.value=true;try{await followApi.createFollowRecord(form);ElMessage.success('新增成功');formVisible.value=false;fetchData()}catch{}finally{formLoading.value=false}}

const detailVisible=ref(false);const detail=ref<FollowRecordDetailVO|null>(null)
async function openDetail(id:number){try{detail.value=await followApi.getFollowRecordDetail(id);detailVisible.value=true}catch{}}

async function handleDelete(row:FollowRecordVO){try{await ElMessageBox.confirm(`确定删除该跟进记录吗？（ID:${row.id}）`,'删除确认',{type:'warning',confirmButtonText:'确定删除',cancelButtonText:'取消'});await followApi.deleteFollowRecord(row.id);ElMessage.success('删除成功');fetchData()}catch{}}

onMounted(()=>{loadFormOptions();fetchData()})
</script>
<style scoped>.page-container{display:flex;flex-direction:column;gap:16px}.search-card :deep(.el-card__body){padding-bottom:0}.card-header{display:flex;justify-content:space-between;align-items:center}</style>
