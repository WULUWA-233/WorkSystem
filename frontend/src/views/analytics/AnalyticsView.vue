<template>
  <div class="analytics" v-loading="loading">
    <!-- Filters -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="query" inline>
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
            start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:260px"/>
        </el-form-item>
        <el-form-item label="趋势粒度">
          <el-radio-group v-model="query.groupBy">
            <el-radio-button value="day">按天</el-radio-button>
            <el-radio-button value="month">按月</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Industry & Region Row -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>客户行业分布</span></template>
          <BaseChart :option="industryOption" :loading="chartLoading" v-if="industryOption"/>
          <el-empty v-else description="暂无数据" :image-size="80"/>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>客户地区分布</span></template>
          <BaseChart :option="regionOption" :loading="chartLoading" v-if="regionOption"/>
          <el-empty v-else description="暂无数据" :image-size="80"/>
        </el-card>
      </el-col>
    </el-row>

    <!-- Trend + Stage Row -->
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

    <!-- Order Status + Sales Performance -->
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
          <template #header><span>销售业绩</span></template>
          <BaseChart :option="salesBarOption" :loading="chartLoading" v-if="salesBarOption" style="height:350px"/>
          <el-empty v-else description="暂无数据" :image-size="80"/>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import BaseChart from '@/components/BaseChart.vue'
import * as analyticsApi from '@/api/analytics'
import { OPPORTUNITY_STAGE_OPTIONS, ORDER_STATUS_OPTIONS, getLabelByValue } from '@/constants/enums'
import { displayIndustry, displayOpportunityStage, displayOrderStatus } from '@/utils/businessDisplay'
import type { TrendItemVO, StageStatsVO, OrderStatusStatsVO, SalesPerformanceVO, NameValueVO } from '@/types/analytics'

const loading=ref(false),chartLoading=ref(false)
const dateRange=ref<[string,string]|null>(null)
const query=reactive({groupBy:'day' as 'day'|'month'})

const trendData=ref<TrendItemVO[]>([])
const stageData=ref<StageStatsVO[]>([])
const orderStatusData=ref<OrderStatusStatsVO[]>([])
const salesData=ref<SalesPerformanceVO[]>([])
const industryData=ref<NameValueVO[]>([])
const regionData=ref<NameValueVO[]>([])

function getTimeParams(){
  const p:Record<string,string>={}
  if(dateRange.value?.length===2){
    p.startTime=`${dateRange.value[0]} 00:00:00`
    p.endTime=`${dateRange.value[1]} 23:59:59`
  }
  return p
}
function getDateParams(){
  const p:Record<string,string>={}
  if(dateRange.value?.length===2){
    p.startDate=dateRange.value[0]
    p.endDate=dateRange.value[1]
  }
  return p
}

let latestFetchId=0
async function fetchAll(){
  const requestId=++latestFetchId;
  loading.value=true;chartLoading.value=true
  const tp=getTimeParams();const dp=getDateParams()
  try{
    const [tr,st,os,sp,ind,reg]=await Promise.all([
      analyticsApi.getCustomerTrend({...dp,groupBy:query.groupBy}),
      analyticsApi.getOpportunityStage(tp),
      analyticsApi.getOrderStatus(tp),
      analyticsApi.getSalesPerformance(tp),
      analyticsApi.getCustomerIndustry(tp),
      analyticsApi.getCustomerRegion(tp),
    ])
    if(requestId!==latestFetchId)return;trendData.value=Array.isArray(tr)?tr:tr?.records||[]
    stageData.value=Array.isArray(st)?st:st?.records||[]
    orderStatusData.value=Array.isArray(os)?os:os?.records||[]
    salesData.value=Array.isArray(sp)?sp:sp?.records||[]
    industryData.value=Array.isArray(ind)?ind:ind?.records||[]
    regionData.value=Array.isArray(reg)?reg:reg?.records||[]
  }catch{}finally{if(requestId===latestFetchId){loading.value=false;chartLoading.value=false}}
}

function handleSearch(){fetchAll()}
function handleReset(){dateRange.value=null;query.groupBy='day';fetchAll()}

// Chart options
const trendOption=computed(()=>{
  if(!trendData.value.length)return null
  return {tooltip:{trigger:'axis'},grid:{left:40,right:20,top:10,bottom:30},
    xAxis:{type:'category',data:trendData.value.map(d=>d.date||''),axisLabel:{rotate:30,fontSize:11}},
    yAxis:{type:'value',minInterval:1},
    series:[{data:trendData.value.map(d=>d.count||0),type:'line',smooth:true,areaStyle:{opacity:0.12},itemStyle:{color:'#409eff'}}]}
})

const stageOption=computed(()=>{
  if(!stageData.value.length)return null
  return {tooltip:{trigger:'item',formatter:'{b}: {c} ({d}%)'},legend:{orient:'vertical',right:8,top:'center',textStyle:{fontSize:11}},
    series:[{type:'pie',radius:['40%','70%'],center:['35%','50%'],label:{show:false},
      data:stageData.value.map(d=>({name:displayOpportunityStage(d.stage,d.stageName),value:d.count||0}))}]}
})

const orderStatusOption=computed(()=>{
  if(!orderStatusData.value.length)return null
  return {tooltip:{trigger:'item',formatter:'{b}: {c} ({d}%)'},legend:{orient:'vertical',right:8,top:'center',textStyle:{fontSize:11}},
    series:[{type:'pie',radius:['40%','70%'],center:['35%','50%'],label:{show:false},
      data:orderStatusData.value.map(d=>({name:displayOrderStatus(d.status,d.statusName),value:d.count||0}))}]}
})

const salesBarOption=computed(()=>{
  if(!salesData.value.length)return null
  return {tooltip:{trigger:'axis'},grid:{left:80,right:20,top:10,bottom:30},
    xAxis:{type:'category',data:salesData.value.map(d=>d.ownerName||''),axisLabel:{rotate:30,fontSize:11}},
    yAxis:{type:'value'},
    series:[{name:'订单金额',data:salesData.value.map(d=>d.orderAmount||0),type:'bar',itemStyle:{color:'#67c23a'},barMaxWidth:40}]}
})

const industryOption=computed(()=>{
  if(!industryData.value.length)return null
  return {tooltip:{trigger:'item',formatter:'{b}: {c} ({d}%)'},legend:{orient:'vertical',right:8,top:'center',textStyle:{fontSize:11}},
    series:[{type:'pie',radius:'65%',center:['35%','50%'],label:{show:false},
      data:industryData.value.map(d=>({name:displayIndustry(d.name),value:d.count||d.value||0}))}]}
})

const regionOption=computed(()=>{
  if(!regionData.value.length)return null
  return {tooltip:{trigger:'item',formatter:'{b}: {c} ({d}%)'},legend:{orient:'vertical',right:8,top:'center',textStyle:{fontSize:11}},
    series:[{type:'pie',radius:'65%',center:['35%','50%'],label:{show:false},
      data:regionData.value.map(d=>({name:d.name||'',value:d.count||d.value||0}))}]}
})

onMounted(()=>fetchAll())
</script>
<style scoped>.analytics{display:flex;flex-direction:column}.filter-card{margin-bottom:0}</style>
