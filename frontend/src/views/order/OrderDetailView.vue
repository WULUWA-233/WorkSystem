<template>
  <div class="detail-page">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="$router.push('/order/list')">返回列表</el-button>
      <span class="detail-title">订单详情</span>
      <div>
        <el-button v-if="authStore.canWriteBusiness" @click="openEdit" :disabled="detail?.status!==1">编辑</el-button>
        <el-button type="warning" @click="handlePay" v-if="authStore.canWriteBusiness && detail?.status===1">付款</el-button>
        <el-button type="primary" @click="handleShip" v-if="authStore.canWriteBusiness && detail?.status===2">发货</el-button>
        <el-button type="success" @click="handleComplete" v-if="authStore.canWriteBusiness && detail?.status===3">完成</el-button>
        <el-button type="danger" @click="handleCancel" v-if="authStore.canWriteBusiness && (detail?.status===1||detail?.status===2)">取消</el-button>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-descriptions v-if="detail" :column="2" border size="small" title="基本信息">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detail.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商机">{{ detail.opportunityName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ detail.amount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="orderTag(detail.status)" size="small">{{ displayOrderStatus(detail.status, detail.statusName) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ displayPaymentMethod(detail.paymentMethod) }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detail.ownerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="付款时间">{{ formatDateTime(detail.paymentTime) }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ formatDateTime(detail.deliveryTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ formatDateTime(detail.finishTime) }}</el-descriptions-item>
        <el-descriptions-item label="取消时间">{{ formatDateTime(detail.cancelTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.logisticsNo" label="物流单号">{{ detail.logisticsNo }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.logisticsCompany" label="物流公司">{{ detail.logisticsCompany }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(detail.updatedAt) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- Dialogs -->
    <el-dialog v-model="editVisible" title="编辑订单" width="460px" :close-on-click-modal="false"><el-form ref="editRef" :model="eForm" :rules="amountRules" label-width="100px"><el-form-item label="金额" prop="amount"><el-input-number v-model="eForm.amount" :min="0" :precision="2" style="width:100%"/></el-form-item><el-form-item label="支付方式"><el-input v-model="eForm.paymentMethod" maxlength="32"/></el-form-item><el-form-item label="备注"><el-input v-model="eForm.remark" maxlength="500" type="textarea" :rows="3"/></el-form-item></el-form><template #footer><el-button @click="editVisible=false">取消</el-button><el-button type="primary" :loading="eLoading" @click="submitEdit">确定</el-button></template></el-dialog>

    <el-dialog v-model="payVisible" title="付款" width="400px"><el-form label-width="80px"><el-form-item label="订单">{{ detail?.orderNo }}</el-form-item><el-form-item label="支付方式"><el-input v-model="payMethod" maxlength="32"/></el-form-item></el-form><template #footer><el-button @click="payVisible=false">取消</el-button><el-button type="primary" :loading="payLoading" @click="confirmPay">确定付款</el-button></template></el-dialog>

    <el-dialog v-model="shipVisible" title="发货" width="420px"><el-form ref="shipRef" :model="sForm" :rules="{logisticsNo:[{required:true,whitespace:true,message:'请输入物流单号',trigger:'blur'}]}" label-width="80px"><el-form-item label="订单">{{ detail?.orderNo }}</el-form-item><el-form-item label="物流单号" prop="logisticsNo"><el-input v-model="sForm.logisticsNo" maxlength="64"/></el-form-item><el-form-item label="物流公司"><el-input v-model="sForm.logisticsCompany" maxlength="64"/></el-form-item></el-form><template #footer><el-button @click="shipVisible=false">取消</el-button><el-button type="primary" :loading="shipLoading" @click="confirmShip">确定发货</el-button></template></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { ORDER_STATUS_OPTIONS, getLabelByValue } from '@/constants/enums'
import { formatDateTime } from '@/utils/format'
import { displayOrderStatus, displayPaymentMethod } from '@/utils/businessDisplay'
import { useAuthStore } from '@/stores/auth'
import * as orderApi from '@/api/order'
import type { OrderDetailVO } from '@/types/order'

const route=useRoute();const id=Number(route.params.id)
const authStore=useAuthStore()
const loading=ref(false);const detail=ref<OrderDetailVO|null>(null)
function orderTag(s:number){const m:Record<number,string>={1:'warning',2:'primary',3:'',4:'success',5:'danger'};return m[s]||'info'}

async function load(){loading.value=true;try{detail.value=await orderApi.getOrderDetail(id)}catch{}finally{loading.value=false}}

// Edit
const editVisible=ref(false),eLoading=ref(false),editRef=ref<FormInstance>()
const eForm=reactive({amount:undefined as number|undefined,paymentMethod:'',remark:''})
const amountRules={amount:[{required:true,type:'number' as const,min:0,message:'请输入有效订单金额',trigger:'change'}]}
function openEdit(){if(!detail.value)return;eForm.amount=detail.value.amount;eForm.paymentMethod=detail.value.paymentMethod||'';eForm.remark=detail.value.remark||'';editVisible.value=true}
async function submitEdit(){const valid=await editRef.value?.validate().catch(()=>false);if(!valid)return;eLoading.value=true;try{await orderApi.updateOrder(id,eForm);ElMessage.success('修改成功');editVisible.value=false;load()}catch{}finally{eLoading.value=false}}

// Pay
const payVisible=ref(false),payLoading=ref(false),payMethod=ref('')
function handlePay(){payMethod.value=detail.value?.paymentMethod||'银行转账';payVisible.value=true}
async function confirmPay(){if(!payMethod.value.trim()){ElMessage.warning('请输入支付方式');return}payLoading.value=true;try{await orderApi.payOrder(id,{paymentMethod:payMethod.value.trim()});ElMessage.success('付款成功');payVisible.value=false;load()}catch{}finally{payLoading.value=false}}

// Ship
const shipVisible=ref(false),shipLoading=ref(false),shipRef=ref<FormInstance>()
const sForm=reactive({logisticsNo:'',logisticsCompany:''})
function handleShip(){sForm.logisticsNo='';sForm.logisticsCompany='';shipVisible.value=true}
async function confirmShip(){const v=await shipRef.value?.validate().catch(()=>false);if(!v)return;shipLoading.value=true;try{await orderApi.shipOrder(id,{logisticsNo:sForm.logisticsNo,logisticsCompany:sForm.logisticsCompany||undefined});ElMessage.success('发货成功');shipVisible.value=false;load()}catch{}finally{shipLoading.value=false}}

// Complete
async function handleComplete(){try{await ElMessageBox.confirm('确定将此订单标记为已完成吗？','确认完成',{type:'warning'});await orderApi.completeOrder(id);ElMessage.success('订单已完成');load()}catch{}}

// Cancel
async function handleCancel(){try{await ElMessageBox.confirm('确定要取消此订单吗？','确认取消',{type:'warning'});await orderApi.cancelOrder(id);ElMessage.success('订单已取消');load()}catch{}}

onMounted(()=>load())
</script>
<style scoped>.detail-page{display:flex;flex-direction:column;gap:16px}.detail-header{display:flex;justify-content:space-between;align-items:center}.detail-title{font-size:18px;font-weight:600}</style>
