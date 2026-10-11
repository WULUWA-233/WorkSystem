<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="订单编号"><el-input v-model="query.orderNo" placeholder="订单编号" clearable style="width:180px"/></el-form-item>
        <el-form-item label="客户ID"><el-input-number v-model="query.customerId" :min="1" placeholder="客户ID" style="width:120px" controls-position="right"/></el-form-item>
        <el-form-item label="商机ID"><el-input-number v-model="query.opportunityId" :min="1" placeholder="商机ID" style="width:120px" controls-position="right"/></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px"><el-option v-for="o in ORDER_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/></el-select>
        </el-form-item>
        <el-form-item label="金额范围">
          <el-input-number v-model="query.minAmount" :min="0" style="width:110px" controls-position="right" placeholder="最小"/>
          <span style="margin:0 4px">-</span>
          <el-input-number v-model="query.maxAmount" :min="0" style="width:110px" controls-position="right" placeholder="最大"/>
        </el-form-item>
        <el-form-item label="时间"><el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px"/></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button><el-button :icon="RefreshRight" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header><div class="card-header"><span>订单列表</span><el-button v-if="authStore.canWriteBusiness" type="primary" :icon="Plus" @click="openCreate">新增订单</el-button></div></template>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="55"/>
        <el-table-column prop="orderNo" label="订单编号" width="170"/>
        <el-table-column prop="customerName" label="客户" width="120"/>
        <el-table-column prop="opportunityName" label="商机" width="120"/>
        <el-table-column prop="amount" label="金额" width="100"/>
        <el-table-column label="状态" width="90"><template #default="{row}"><el-tag :type="orderTag(row.status)" size="small">{{ displayOrderStatus(row.status, row.statusName) }}</el-tag></template></el-table-column>
        <el-table-column label="支付方式" width="90"><template #default="{row}">{{ displayPaymentMethod(row.paymentMethod) }}</template></el-table-column>
        <el-table-column prop="paymentTime" label="付款时间" width="110"><template #default="{row}">{{ formatDateTime(row.paymentTime) }}</template></el-table-column>
        <el-table-column prop="deliveryTime" label="发货时间" width="110"/>
        <el-table-column prop="finishTime" label="完成时间" width="110"/>
        <el-table-column prop="ownerName" label="负责人" width="80"/>
        <el-table-column label="操作" fixed="right" min-width="380">
          <template #default="{row}">
            <el-button text type="primary" size="small" @click="$router.push(`/order/${row.id}`)">详情</el-button>
            <el-button v-if="authStore.canWriteBusiness" text size="small" @click="openEdit(row)" :disabled="row.status!==1">编辑</el-button>
            <el-button text type="warning" size="small" @click="handlePay(row)" v-if="authStore.canWriteBusiness && row.status===1">付款</el-button>
            <el-button text type="primary" size="small" @click="handleShip(row)" v-if="authStore.canWriteBusiness && row.status===2">发货</el-button>
            <el-button text type="success" size="small" @click="handleComplete(row)" v-if="authStore.canWriteBusiness && row.status===3">完成</el-button>
            <el-button text type="danger" size="small" @click="handleCancel(row)" v-if="authStore.canWriteBusiness && (row.status===1||row.status===2)">取消</el-button>
            <el-button v-if="authStore.canWriteBusiness" text type="danger" size="small" @click="handleDelete(row)" :disabled="row.status>=2">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next,jumper" background style="margin-top:16px;justify-content:flex-end" @current-change="handlePageChange" @size-change="handleSizeChange"/>
    </el-card>

    <!-- Create -->
    <el-dialog v-model="createVisible" title="新增订单" width="480px" :close-on-click-modal="false" @closed="resetCreate">
      <el-form ref="createRef" :model="cForm" :rules="createRules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="cForm.customerId" filterable placeholder="请选择客户" style="width:100%" @change="handleCreateCustomerChange">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="赢单商机" prop="opportunityId">
          <el-select v-model="cForm.opportunityId" filterable placeholder="可选：关联已赢单商机" clearable style="width:100%" @change="handleCreateOpportunityChange">
            <el-option v-for="o in availableWonOpportunities" :key="o.id" :label="o.opportunityName" :value="o.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount"><el-input-number v-model="cForm.amount" :min="0" :precision="2" style="width:100%"/></el-form-item>
        <el-form-item label="支付方式"><el-input v-model="cForm.paymentMethod" maxlength="32" placeholder="如：银行转账"/></el-form-item>
        <el-form-item label="备注"><el-input v-model="cForm.remark" maxlength="500" type="textarea" :rows="3"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="createVisible=false">取消</el-button><el-button type="primary" :loading="cLoading" @click="submitCreate">确定</el-button></template>
    </el-dialog>

    <!-- Edit -->
    <el-dialog v-model="editVisible" title="编辑订单" width="480px" :close-on-click-modal="false">
      <el-form ref="editRef" :model="eForm" :rules="amountRules" label-width="100px">
        <el-form-item label="金额" prop="amount"><el-input-number v-model="eForm.amount" :min="0" :precision="2" style="width:100%"/></el-form-item>
        <el-form-item label="支付方式"><el-input v-model="eForm.paymentMethod" maxlength="32"/></el-form-item>
        <el-form-item label="备注"><el-input v-model="eForm.remark" maxlength="500" type="textarea" :rows="3"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="editVisible=false">取消</el-button><el-button type="primary" :loading="eLoading" @click="submitEdit">确定</el-button></template>
    </el-dialog>

    <!-- Pay -->
    <el-dialog v-model="payVisible" title="付款" width="400px"><el-form label-width="80px"><el-form-item label="订单">{{ payTarget?.orderNo }}</el-form-item><el-form-item label="支付方式"><el-input v-model="payMethod" maxlength="32" placeholder="请选择支付方式"/></el-form-item></el-form><template #footer><el-button @click="payVisible=false">取消</el-button><el-button type="primary" :loading="payLoading" @click="confirmPay">确定付款</el-button></template></el-dialog>

    <!-- Ship -->
    <el-dialog v-model="shipVisible" title="发货" width="420px"><el-form ref="shipRef" :model="sForm" :rules="{logisticsNo:[{required:true,whitespace:true,message:'请输入物流单号',trigger:'blur'}]}" label-width="80px"><el-form-item label="订单">{{ shipTarget?.orderNo }}</el-form-item><el-form-item label="物流单号" prop="logisticsNo"><el-input v-model="sForm.logisticsNo" maxlength="64"/></el-form-item><el-form-item label="物流公司"><el-input v-model="sForm.logisticsCompany" maxlength="64"/></el-form-item></el-form><template #footer><el-button @click="shipVisible=false">取消</el-button><el-button type="primary" :loading="shipLoading" @click="confirmShip">确定发货</el-button></template></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { ORDER_STATUS_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { displayOrderStatus, displayPaymentMethod } from '@/utils/businessDisplay'
import { useAuthStore } from '@/stores/auth'
import { fetchAllPages } from '@/utils/pagination'
import * as orderApi from '@/api/order'
import * as customerApi from '@/api/customer'
import * as oppApi from '@/api/opportunity'
import type { CustomerVO } from '@/types/customer'
import type { OpportunityVO } from '@/types/opportunity'
import type { OrderVO, OrderDetailVO } from '@/types/order'

const route=useRoute();const router=useRouter();const authStore=useAuthStore()
const loading=ref(false);const tableData=ref<OrderVO[]>([]);const total=ref(0)
const customerOptions=ref<CustomerVO[]>([])
const opportunityOptions=ref<OpportunityVO[]>([])
const dateRange=ref<[string,string]|null>(null)
const query=reactive({pageNo:1,pageSize:10,orderNo:'',customerId:undefined as number|undefined,opportunityId:undefined as number|undefined,status:undefined as number|undefined,paymentMethod:'',minAmount:undefined as number|undefined,maxAmount:undefined as number|undefined})
function orderTag(s:number){const m:Record<number,string>={1:'warning',2:'primary',3:'',4:'success',5:'danger'};return m[s]||'info'}

let latestFetchId=0
async function fetchData(){
  const requestId=++latestFetchId;loading.value=true;try{const p:Record<string,any>={...query};['status','customerId','opportunityId','minAmount','maxAmount'].forEach(k=>{if(p[k]===undefined||p[k]==='')delete p[k]});if(dateRange.value?.length===2){p.startTime=dateRange.value[0]+' 00:00:00';p.endTime=dateRange.value[1]+' 23:59:59'};const r=await orderApi.getOrderPage(p);if(requestId!==latestFetchId)return;tableData.value=r.records;total.value=r.total}catch{}finally{if(requestId===latestFetchId)loading.value=false}}
function handleSearch(){query.pageNo=1;fetchData()}
function handleReset(){query.orderNo='';query.customerId=undefined;query.opportunityId=undefined;query.status=undefined;query.paymentMethod='';query.minAmount=undefined;query.maxAmount=undefined;dateRange.value=null;query.pageNo=1;fetchData()}
function handlePageChange(page:number){query.pageNo=page;fetchData()}
function handleSizeChange(size:number){query.pageSize=size;query.pageNo=1;fetchData()}
onMounted(async()=>{const oid=route.query.opportunityId;if(oid){query.opportunityId=Number(oid)};fetchData();if(route.query.create==='1'&&authStore.canWriteBusiness){await openCreate();void router.replace({query:{...route.query,create:undefined}})}})

// Create
const createVisible=ref(false),cLoading=ref(false),createRef=ref<FormInstance>()
const cForm=reactive({customerId:undefined as number|undefined,opportunityId:undefined as number|undefined,amount:undefined as number|undefined,paymentMethod:'',ownerId:authStore.user?.id as number|undefined,remark:''})
const createRules={customerId:[{required:true,message:'请选择客户',trigger:'change'}],amount:[{required:true,type:'number' as const,min:0,message:'请输入有效订单金额',trigger:'change'}]}
const availableWonOpportunities=computed(()=>opportunityOptions.value.filter(o=>o.stage===5&&(!cForm.customerId||o.customerId===cForm.customerId)))
async function loadCreateOptions(){try{const [c,o]=await Promise.all([fetchAllPages(customerApi.getCustomerPage,{status:1}),fetchAllPages(oppApi.getOpportunityPage,{stage:5})]);customerOptions.value=c;opportunityOptions.value=o}catch{customerOptions.value=[];opportunityOptions.value=[]}}
async function openCreate(){resetCreate();await loadCreateOptions();const oid=route.query.opportunityId;if(oid){cForm.opportunityId=Number(oid);handleCreateOpportunityChange(cForm.opportunityId)};createVisible.value=true}
function resetCreate(){cForm.customerId=undefined;cForm.opportunityId=undefined;cForm.amount=undefined;cForm.paymentMethod='';cForm.ownerId=authStore.user?.id;cForm.remark='';createRef.value?.clearValidate()}
function handleCreateCustomerChange(){if(cForm.opportunityId&&!availableWonOpportunities.value.some(o=>o.id===cForm.opportunityId)){cForm.opportunityId=undefined}}
function handleCreateOpportunityChange(id?:number){const opp=opportunityOptions.value.find(o=>o.id===id);if(!opp)return;cForm.customerId=opp.customerId;cForm.amount=opp.amount;cForm.ownerId=opp.ownerId||authStore.user?.id}
async function submitCreate(){const v=await createRef.value?.validate().catch(()=>false);if(!v)return;cLoading.value=true;try{await orderApi.createOrder(cForm);ElMessage.success('订单创建成功');createVisible.value=false;fetchData()}catch{}finally{cLoading.value=false}}

// Edit
const editVisible=ref(false),eLoading=ref(false),editRef=ref<FormInstance>();let editId=0
const eForm=reactive({amount:undefined as number|undefined,paymentMethod:'',remark:''})
const amountRules={amount:[{required:true,type:'number' as const,min:0,message:'请输入有效订单金额',trigger:'change'}]}
async function openEdit(row:OrderVO){editId=row.id;try{const d:OrderDetailVO=await orderApi.getOrderDetail(row.id);eForm.amount=d.amount;eForm.paymentMethod=d.paymentMethod||'';eForm.remark=d.remark||'';editVisible.value=true}catch{}}
async function submitEdit(){const valid=await editRef.value?.validate().catch(()=>false);if(!valid)return;eLoading.value=true;try{await orderApi.updateOrder(editId,eForm);ElMessage.success('修改成功');editVisible.value=false;fetchData()}catch{}finally{eLoading.value=false}}

// Pay
const payVisible=ref(false),payLoading=ref(false),payTarget=ref<OrderVO|null>(null),payMethod=ref('')
function handlePay(row:OrderVO){payTarget.value=row;payMethod.value=row.paymentMethod||'银行转账';payVisible.value=true}
async function confirmPay(){if(!payMethod.value.trim()){ElMessage.warning('请输入支付方式');return}if(!payTarget.value)return;payLoading.value=true;try{await orderApi.payOrder(payTarget.value.id,{paymentMethod:payMethod.value.trim()});ElMessage.success('付款成功');payVisible.value=false;fetchData()}catch{}finally{payLoading.value=false}}

// Ship
const shipVisible=ref(false),shipLoading=ref(false),shipTarget=ref<OrderVO|null>(null),shipRef=ref<FormInstance>()
const sForm=reactive({logisticsNo:'',logisticsCompany:''})
function handleShip(row:OrderVO){shipTarget.value=row;sForm.logisticsNo='';sForm.logisticsCompany='';shipVisible.value=true}
async function confirmShip(){const v=await shipRef.value?.validate().catch(()=>false);if(!v||!shipTarget.value)return;shipLoading.value=true;try{await orderApi.shipOrder(shipTarget.value.id,{logisticsNo:sForm.logisticsNo,logisticsCompany:sForm.logisticsCompany||undefined});ElMessage.success('发货成功');shipVisible.value=false;fetchData()}catch{}finally{shipLoading.value=false}}

// Complete
async function handleComplete(row:OrderVO){try{await ElMessageBox.confirm(`确定将订单 ${row.orderNo} 标记为已完成吗？`,'确认完成',{type:'warning'});await orderApi.completeOrder(row.id);ElMessage.success('订单已完成');fetchData()}catch{}}

// Cancel
async function handleCancel(row:OrderVO){try{await ElMessageBox.confirm(`确定要取消订单 ${row.orderNo} 吗？`,'确认取消',{type:'warning'});await orderApi.cancelOrder(row.id);ElMessage.success('订单已取消');fetchData()}catch{}}

// Delete
async function handleDelete(row:OrderVO){try{await ElMessageBox.confirm(`确定删除订单 ${row.orderNo} 吗？`,'删除确认',{type:'warning',confirmButtonText:'确定删除',cancelButtonText:'取消'});await orderApi.deleteOrder(row.id);ElMessage.success('删除成功');fetchData()}catch{}}
</script>
<style scoped>.page-container{display:flex;flex-direction:column;gap:16px}.search-card :deep(.el-card__body){padding-bottom:0}.card-header{display:flex;justify-content:space-between;align-items:center}</style>
