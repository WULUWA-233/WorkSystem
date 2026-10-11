import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/list-pages.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { getRememberedUsername } from './utils/storage'

// Remove plaintext passwords from legacy remembered-login caches on every startup.
getRememberedUsername()
const app = createApp(App)

// Element Plus with Chinese locale
app.use(ElementPlus, { locale: zhCn })

// Register all Element Plus icons globally
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Pinia
app.use(createPinia())

// Vue Router
app.use(router)

app.mount('#app')
