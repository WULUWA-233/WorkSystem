<template>
  <div ref="chartRef" class="base-chart" v-loading="loading"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, shallowRef } from 'vue'
import * as echarts from 'echarts'

const props = withDefaults(defineProps<{
  option: any
  loading?: boolean
}>(), { loading: false })

const chartRef = ref<HTMLElement>()
const chartInstance = shallowRef<echarts.ECharts | null>(null)

function initChart() {
  if (!chartRef.value) return
  chartInstance.value = echarts.init(chartRef.value)
  if (props.option) {
    chartInstance.value.setOption(props.option as any)
  }
  const observer = new ResizeObserver(() => chartInstance.value?.resize())
  observer.observe(chartRef.value)
  ;(chartRef.value as any).__resizeObserver = observer
}

function disposeChart() {
  const el = chartRef.value
  if (el && (el as any).__resizeObserver) (el as any).__resizeObserver.disconnect()
  chartInstance.value?.dispose()
  chartInstance.value = null
}

watch(() => props.option, (opt) => {
  if (chartInstance.value && opt) {
    chartInstance.value.setOption(opt as any, true)
  }
}, { deep: true })

onMounted(() => initChart())
onBeforeUnmount(() => disposeChart())
</script>

<style scoped>
.base-chart { width: 100%; height: 350px; }
</style>
