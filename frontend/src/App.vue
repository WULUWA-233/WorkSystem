<template>
  <router-view v-slot="{ Component }">
    <BasicLayout v-if="!isFullScreen">
      <component :is="Component" :key="route.path" />
    </BasicLayout>
    <component :is="Component" :key="route.path" v-else />
  </router-view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'

const route = useRoute()

/**
 * Routes that should render full-screen (no sidebar/header):
 * - /login
 * - /403
 * - 404 (catch-all)
 */
const fullScreenRoutes = ['Login', 'Forbidden', 'NotFound']

const isFullScreen = computed(() => {
  const name = route.name as string | undefined
  return name ? fullScreenRoutes.includes(name) : false
})
</script>
