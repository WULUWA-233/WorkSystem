<template>
  <div class="json-preview">
    <pre v-if="formatted"><code>{{ formatted }}</code></pre>
    <span v-else class="json-empty">-</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatJson } from '@/utils/format'

const props = defineProps<{
  data?: string | null
}>()

const formatted = computed(() => {
  if (!props.data) return ''
  return formatJson(props.data)
})
</script>

<style scoped>
.json-preview {
  max-height: 400px;
  overflow-y: auto;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
}
.json-preview pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  line-height: 1.6;
  color: #303133;
}
.json-preview code {
  font-family: 'Courier New', Courier, monospace;
}
.json-empty {
  color: #909399;
}
</style>
