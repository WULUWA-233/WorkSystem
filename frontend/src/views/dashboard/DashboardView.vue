<template>
  <div class="dashboard" v-loading="loading">
    <!-- Stats Cards -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{color:card.color}">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row 1 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span>新增客户趋势</span></template>
          <BaseChart :option="trendOption" :loading="chartLoading" v-if="trendOption"/>
          <el-empty v-else description="暂无数据" :image-size="80"/>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span>商机阶段分布</span></template>
          <BaseChart :option="stageOption" :loading="chartLoading" v-if="stageOption"/>
          <el-empty v-else description="暂无数据" :image-size="80"/>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row 2 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span>订单状态分布</span></template>
          <BaseChart :option="orderStatusOption" :loading="chartLoading" v-if="orderStatusOption"/>
          <el-empty v-else description="暂无数据" :image-size="80"/>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span>销售业绩排行</span></template>
          <el-table :data="salesData" stripe border size="small" v-if="salesData.length" max-height="310">
            <el-table-column prop="ownerName" label="销售人员" width="100"/>
            <el-table-column prop="customerCount" label="客户数" width="80"/>
            <el-table-column prop="opportunityCount" label="商机数" width="80"/>
            <el-table-column prop="orderCount" label="订单数" width="80"/>
            <el-table-column label="订单金额" min-width="120">
              <template #default="{row}">{{ (row.orderAmount??0).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无销售数据" :image-size="80"/>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import BaseChart from '@/components/BaseChart.vue'
import * as analyticsApi from '@/api/analytics'
import { OPPORTUNITY_STAGE_OPTIONS, ORDER_STATUS_OPTIONS, getLabelByValue } from '@/constants/enums'
import { displayOpportunityStage, displayOrderStatus } from '@/utils/businessDisplay'
import type { OverviewVO, TrendItemVO, StageStatsVO, OrderStatusStatsVO, SalesPerformanceVO } from '@/types/analytics'

const loading=ref(false),chartLoading=ref(false)
const overview=ref<OverviewVO>({})
const trendData=ref<TrendItemVO[]>([])
const stageData=ref<StageStatsVO[]>([])
const orderStatusData=ref<OrderStatusStatsVO[]>([])
const salesData=ref<SalesPerformanceVO[]>([])

function toNumber(value: unknown) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : undefined
}

function pickNumber(...values: unknown[]) {
  for (const value of values) {
    const numberValue = toNumber(value)
    if (numberValue !== undefined) return numberValue
  }
  return undefined
}

function normalizeOverview(data: OverviewVO | null | undefined): OverviewVO {
  const source = data || {}
  return {
    ...source,
    customerCount: pickNumber(source.customerCount, source.customerTotal),
    opportunityCount: pickNumber(source.opportunityCount, source.opportunityTotal),
    orderCount: pickNumber(source.orderCount, source.orderTotal),
    orderAmount: pickNumber(source.orderAmount),
    paidAmount: pickNumber(source.paidAmount, source.paidOrderAmount),
    opportunityAmount: pickNumber(source.opportunityAmount),
    followCount: pickNumber(source.followCount, source.followRecordTotal),
    todayFollowCount: pickNumber(source.todayFollowCount)
  }
}

// Stat cards
const statCards=computed(()=>{
  const o=overview.value
  return [
    {label:'客户总数',value:o.customerCount??'-',color:'#409eff'},
    {label:'商机总数',value:o.opportunityCount??'-',color:'#67c23a'},
    {label:'订单总额',value:o.orderAmount!=null?`¥${(o.orderAmount).toFixed(0)}`:'-',color:'#e6a23c'},
    {label:'已付金额',value:o.paidAmount!=null?`¥${(o.paidAmount).toFixed(0)}`:'-',color:'#f56c6c'},
    {label:'商机金额',value:o.opportunityAmount!=null?`¥${(o.opportunityAmount).toFixed(0)}`:'-',color:'#909399'},
    {label:'订单总数',value:o.orderCount??'-',color:'#e6a23c'},
    {label:'跟进总数',value:o.followCount??'-',color:'#67c23a'},
    {label:'今日跟进',value:o.todayFollowCount??'-',color:'#f56c6c'},
  ]
})

// Trend chart option
const trendOption=computed(()=>{
  if(!trendData.value.length)return null
  return {
    tooltip:{trigger:'axis'},
    grid:{left:40,right:20,top:20,bottom:30},
    xAxis:{type:'category',data:trendData.value.map(d=>d.date||''),axisLabel:{rotate:30,fontSize:11}},
    yAxis:{type:'value',minInterval:1},
    series:[{data:trendData.value.map(d=>d.count||0),type:'line',smooth:true,areaStyle:{opacity:0.15},itemStyle:{color:'#409eff'}}]
  }
})

// Stage pie
const stageOption=computed(()=>{
  if(!stageData.value.length)return null
  return {
    tooltip:{trigger:'item',formatter:'{b}: {c} ({d}%)'},
    legend:{orient:'vertical',right:10,top:'center',textStyle:{fontSize:11}},
    series:[{type:'pie',radius:['40%','70%'],center:['35%','50%'],avoidLabelOverlap:false,
      label:{show:false},emphasis:{label:{show:true,fontSize:14}},
      data:stageData.value.map(d=>({name:displayOpportunityStage(d.stage,d.stageName),value:d.count||0}))
    }]
  }
})

// Order status pie
const orderStatusOption=computed(()=>{
  if(!orderStatusData.value.length)return null
  return {
    tooltip:{trigger:'item',formatter:'{b}: {c} ({d}%)'},
    legend:{orient:'vertical',right:10,top:'center',textStyle:{fontSize:11}},
    series:[{type:'pie',radius:['40%','70%'],center:['35%','50%'],avoidLabelOverlap:false,
      label:{show:false},emphasis:{label:{show:true,fontSize:14}},
      data:orderStatusData.value.map(d=>({name:displayOrderStatus(d.status,d.statusName),value:d.count||0}))
    }]
  }
})

async function loadAll(){
  loading.value=true;chartLoading.value=true
  try{
    const [ov,tr,st,os,sp]=await Promise.all([
      analyticsApi.getOverview(),
      analyticsApi.getCustomerTrend({groupBy:'day'}),
      analyticsApi.getOpportunityStage(),
      analyticsApi.getOrderStatus(),
      analyticsApi.getSalesPerformance()
    ])
    overview.value=normalizeOverview(ov)
    trendData.value=Array.isArray(tr)?tr:tr?.records||[]
    stageData.value=Array.isArray(st)?st:st?.records||[]
    orderStatusData.value=Array.isArray(os)?os:os?.records||[]
    salesData.value=Array.isArray(sp)?sp:sp?.records||[]
  }catch{}finally{loading.value=false;chartLoading.value=false}
}

onMounted(()=>loadAll())
</script>

<style scoped>
.dashboard{min-height:300px}
.stats-row{margin-bottom:0}
.stat-card{text-align:center;cursor:default}
.stat-label{font-size:13px;color:#909399;margin-bottom:8px}
.stat-value{font-size:28px;font-weight:600}
</style>
