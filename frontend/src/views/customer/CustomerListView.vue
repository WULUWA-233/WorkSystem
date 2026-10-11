<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="客户名称">
          <el-input v-model="query.customerName" placeholder="客户名称" clearable style="width:160px"/>
        </el-form-item>
        <el-form-item label="行业">
          <el-input v-model="query.industry" placeholder="行业" clearable style="width:140px"/>
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="query.source" placeholder="来源" clearable style="width:120px"/>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="query.level" placeholder="全部" clearable style="width:130px">
            <el-option v-for="o in CUSTOMER_LEVEL_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="地区">
          <el-input v-model="query.region" placeholder="地区" clearable style="width:120px"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:110px">
            <el-option v-for="o in CUSTOMER_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
            start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header"><span>客户列表</span>
          <el-button v-if="authStore.canWriteBusiness" type="primary" :icon="Plus" @click="openCreate">新增客户</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="55"/>
        <el-table-column prop="customerName" label="客户名称" width="150"/>
        <el-table-column label="行业" width="100"><template #default="{row}">{{ displayIndustry(row.industry) }}</template></el-table-column>
        <el-table-column label="来源" width="90"><template #default="{row}">{{ displaySource(row.source) }}</template></el-table-column>
        <el-table-column label="等级" width="100">
          <template #default="{row}">{{ getLabelByValue(CUSTOMER_LEVEL_OPTIONS, row.level) }}</template>
        </el-table-column>
        <el-table-column prop="region" label="地区" width="90"/>
        <el-table-column prop="contactName" label="联系人" width="90"/>
        <el-table-column prop="contactPhone" label="联系电话" width="120"/>
        <el-table-column prop="ownerName" label="负责人" width="100"/>
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':row.status===2?'warning':'info'" size="small">
              {{ getLabelByValue(CUSTOMER_STATUS_OPTIONS, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="60"/>
        <el-table-column prop="lastFollowTime" label="最近跟进" width="110"/>
        <el-table-column prop="createdAt" label="创建时间" width="110">
          <template #default="{row}">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="280">
          <template #default="{row}">
            <el-button text type="primary" size="small" @click="openDetail(row.id)">详情</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="warning" size="small" @click="openStatus(row)">状态</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="success" size="small" @click="goCreateOpp(row)">创建商机</el-button>
            <el-button v-if="authStore.canWriteBusiness" text size="small" @click="goAddFollow(row)">添加跟进</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize"
        :total="total" :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next,jumper"
        background style="margin-top:16px;justify-content:flex-end"
        @current-change="handlePageChange" @size-change="handleSizeChange"/>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="formVisible" :title="isEdit?'编辑客户':'新增客户'" width="600px" :close-on-click-modal="false" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户名称"/>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="行业"><el-input v-model="form.industry" placeholder="行业"/></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源"><el-input v-model="form.source" placeholder="来源"/></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="等级">
            <el-select v-model="form.level" style="width:100%"><el-option v-for="o in CUSTOMER_LEVEL_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="地区"><el-input v-model="form.region" placeholder="地区"/></el-form-item></el-col>
        </el-row>
        <el-form-item label="地址"><el-input v-model="form.address" placeholder="地址"/></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contactName" placeholder="联系人"/></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.contactPhone" placeholder="联系电话"/></el-form-item></el-col>
        </el-row>
        <el-form-item label="联系邮箱"><el-input v-model="form.contactEmail" placeholder="联系邮箱"/></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="负责人ID" prop="ownerId"><el-input-number v-model="form.ownerId" :min="1" style="width:100%" placeholder="负责人用户ID"/></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="form.status" style="width:100%"><el-option v-for="o in CUSTOMER_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="评分"><el-input-number v-model="form.score" :min="0" :max="100" style="width:100%"/></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- Status Dialog -->
    <el-dialog v-model="statusVisible" title="修改状态" width="400px">
      <el-form label-width="80px">
        <el-form-item label="客户">{{ statusTarget?.customerName }}</el-form-item>
        <el-form-item label="状态">
          <el-select v-model="newStatus" style="width:100%">
            <el-option v-for="o in CUSTOMER_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible=false">取消</el-button>
        <el-button type="primary" :loading="statusLoading" @click="confirmStatus">确定</el-button>
      </template>
    </el-dialog>

    <!-- Add Follow Dialog -->
    <el-dialog v-model="followVisible" title="添加跟进记录" width="560px" :close-on-click-modal="false" @closed="resetFollowForm">
      <el-form ref="followFormRef" :model="followForm" :rules="followRules" label-width="100px">
        <el-form-item label="客户">{{ followCustomerName }}</el-form-item>
        <el-form-item label="关联商机" prop="opportunityId">
          <el-select v-model="followForm.opportunityId" filterable placeholder="请选择商机" style="width:100%">
            <el-option v-for="o in followOpportunityOptions" :key="o.id" :label="o.opportunityName" :value="o.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="跟进方式" prop="followType">
          <el-select v-model="followForm.followType" style="width:100%"><el-option v-for="o in FOLLOW_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select>
        </el-form-item>
        <el-form-item label="跟进内容" prop="followContent"><el-input v-model="followForm.followContent" type="textarea" :rows="3"/></el-form-item>
        <el-form-item label="跟进结果"><el-input v-model="followForm.followResult" type="textarea" :rows="2"/></el-form-item>
        <el-form-item label="客户鎰忓悜">
          <el-select v-model="followForm.customerIntention" style="width:100%"><el-option v-for="o in CUSTOMER_INTENTION_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select>
        </el-form-item>
        <el-form-item label="跟进时间"><el-date-picker v-model="followForm.followTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%"/></el-form-item>
        <el-form-item label="下次跟进"><el-date-picker v-model="followForm.nextFollowTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%"/></el-form-item>
        <el-form-item label="备注"><el-input v-model="followForm.remark" type="textarea" :rows="2"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followVisible=false">取消</el-button>
        <el-button type="primary" :loading="followLoading" @click="submitFollow">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { CUSTOMER_STATUS_OPTIONS, CUSTOMER_LEVEL_OPTIONS, CUSTOMER_INTENTION_OPTIONS, FOLLOW_TYPE_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { displayIndustry, displaySource } from '@/utils/businessDisplay'
import { useAuthStore } from '@/stores/auth'
import * as customerApi from '@/api/customer'
import * as followApi from '@/api/followRecord'
import type { CustomerVO, CustomerDetailVO } from '@/types/customer'

const router = useRouter()
const authStore = useAuthStore()

// ---- Table ----
const loading = ref(false)
const tableData = ref<CustomerVO[]>([])
const total = ref(0)
const dateRange = ref<[string,string]|null>(null)
const query = reactive({ pageNo:1, pageSize:10, customerName:'', industry:'', source:'', level:undefined as number|undefined, region:'', status:undefined as number|undefined })

let latestFetchId=0
async function fetchData() {
  const requestId=++latestFetchId;
  loading.value=true
  try {
    const p:Record<string,any>={...query}
    if(p.status===''||p.status===undefined)delete p.status
    if(p.level===''||p.level===undefined)delete p.level
    if(dateRange.value?.length===2){p.startTime=dateRange.value[0]+' 00:00:00';p.endTime=dateRange.value[1]+' 23:59:59'}
    const r=await customerApi.getCustomerPage(p);if(requestId!==latestFetchId)return;tableData.value=r.records;total.value=r.total
  }catch{}finally{if(requestId===latestFetchId)loading.value=false}
}
function handleSearch(){query.pageNo=1;fetchData()}
function handleReset(){query.customerName='';query.industry='';query.source='';query.level=undefined;query.region='';query.status=undefined;dateRange.value=null;query.pageNo=1;fetchData()}
function handlePageChange(page:number){query.pageNo=page;fetchData()}
function handleSizeChange(size:number){query.pageSize=size;query.pageNo=1;fetchData()}

// ---- Create/Edit ----
const formVisible=ref(false),formLoading=ref(false),isEdit=ref(false),editId=ref<number|null>(null),formRef=ref<FormInstance>()
const form=reactive({customerName:'',industry:'',source:'',level:1,region:'',address:'',contactName:'',contactPhone:'',contactEmail:'',ownerId:authStore.user?.id as number|undefined,status:1,score:80,remark:''})
const formRules:FormRules={customerName:[{required:true,whitespace:true,max:128,message:'请输入客户名称（最多128字）',trigger:'blur'}],ownerId:[{required:true,type:'number',min:1,message:'请选择负责人',trigger:'change'}]}
function openCreate(){isEdit.value=false;editId.value=null;resetForm();form.ownerId=authStore.user?.id;formVisible.value=true}
async function openEdit(row:CustomerVO){isEdit.value=true;editId.value=row.id;try{const d:CustomerDetailVO=await customerApi.getCustomerDetail(row.id);Object.assign(form,{customerName:d.customerName,industry:d.industry||'',source:d.source||'',level:d.level,region:d.region||'',address:d.address||'',contactName:d.contactName||'',contactPhone:d.contactPhone||'',contactEmail:d.contactEmail||'',ownerId:d.ownerId,status:d.status,score:d.score??80,remark:d.remark||''});formVisible.value=true}catch{}}
function resetForm(){form.customerName='';form.industry='';form.source='';form.level=1;form.region='';form.address='';form.contactName='';form.contactPhone='';form.contactEmail='';form.ownerId=authStore.user?.id;form.status=1;form.score=80;form.remark='';formRef.value?.clearValidate()}
async function submitForm(){const v=await formRef.value?.validate().catch(()=>false);if(!v)return;formLoading.value=true;try{if(isEdit.value&&editId.value){await customerApi.updateCustomer(editId.value,form)}else{await customerApi.createCustomer(form)};ElMessage.success(isEdit.value?'修改成功':'新增成功');formVisible.value=false;fetchData()}catch{}finally{formLoading.value=false}}

// ---- Status ----
const statusVisible=ref(false),statusLoading=ref(false),statusTarget=ref<CustomerVO|null>(null),newStatus=ref(1)
function openStatus(row:CustomerVO){statusTarget.value=row;newStatus.value=row.status;statusVisible.value=true}
async function confirmStatus(){if(!statusTarget.value)return;statusLoading.value=true;try{await customerApi.changeCustomerStatus(statusTarget.value.id,newStatus.value);ElMessage.success('状态修改成功');statusVisible.value=false;fetchData()}catch{}finally{statusLoading.value=false}}

// ---- Delete ----
async function handleDelete(row:CustomerVO){try{await ElMessageBox.confirm(`确定删除客户"${row.customerName}"吗？此操作不可恢复。`,'删除确认',{type:'warning',confirmButtonText:'确定删除',cancelButtonText:'取消'});await customerApi.deleteCustomer(row.id);ElMessage.success('删除成功');fetchData()}catch{}}

// ---- Navigation ----
function openDetail(id:number){router.push(`/customer/${id}`)}
function goCreateOpp(row:CustomerVO){router.push(`/opportunity/list?create=1&customerId=${row.id}&customerName=${encodeURIComponent(row.customerName)}`)}
async function goAddFollow(row:CustomerVO){followCustomerId=row.id;followCustomerName.value=row.customerName;resetFollowForm();followVisible.value=true;try{const r=await customerApi.getCustomerOpportunities(row.id);followOpportunityOptions.value=r;if(followOpportunityOptions.value.length===1)followForm.opportunityId=followOpportunityOptions.value[0].id}catch{followOpportunityOptions.value=[]}}

// ---- Quick Follow ----
const followVisible=ref(false),followLoading=ref(false),followFormRef=ref<FormInstance>()
let followCustomerId:number|null=null
const followCustomerName=ref('')
const followOpportunityOptions=ref<any[]>([])
const followForm=reactive({opportunityId:undefined as number|undefined,followType:'电话',followContent:'',followResult:'',customerIntention:2,followTime:'',nextFollowTime:'',remark:''})
const followRules:FormRules={opportunityId:[{required:true,message:'请选择商机',trigger:'change'}],followType:[{required:true,message:'请选择跟进方式',trigger:'change'}],followContent:[{required:true,whitespace:true,message:'请输入跟进内容',trigger:'blur'}]}
function resetFollowForm(){followForm.opportunityId=undefined;followForm.followType='电话';followForm.followContent='';followForm.followResult='';followForm.customerIntention=2;followForm.followTime='';followForm.nextFollowTime='';followForm.remark='';followOpportunityOptions.value=[];followFormRef.value?.clearValidate()}
async function submitFollow(){const v=await followFormRef.value?.validate().catch(()=>false);if(!v||!followCustomerId)return;followLoading.value=true;try{await followApi.createFollowRecord({...followForm,customerId:followCustomerId,followerId:authStore.user?.id});ElMessage.success('跟进记录添加成功');followVisible.value=false;fetchData()}catch{}finally{followLoading.value=false}}

onMounted(()=>fetchData())
</script>
<style scoped>.page-container{display:flex;flex-direction:column;gap:16px}.search-card :deep(.el-card__body){padding-bottom:0}.card-header{display:flex;justify-content:space-between;align-items:center}</style>
