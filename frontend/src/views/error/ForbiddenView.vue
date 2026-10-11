<template>
  <div class="error-page">
    <h1>403</h1>
    <p>无权限访问此页面</p>
    <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
// ForbiddenView — 403 page
</script>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  color: #666;
}
.error-page h1 {
  font-size: 72px;
  margin: 0;
  color: #e6a23c;
}
</style>
