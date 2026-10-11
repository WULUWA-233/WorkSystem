# FRONT-005 空白内容修复

## 问题现象

登录成功后，左侧菜单和顶部栏正常显示，顶部标题随路由变化，但主内容区域完全空白——所有页面的统计卡片、图表、筛选栏、表格、按钮均不渲染。

## 根因

**双重 router-view 冲突**。

`App.vue` 使用 `<router-view v-slot="{ Component }">` 捕获路由匹配的页面组件，并通过 `<component :is="Component" />` 将其传递到 BasicLayout 的默认 slot 中：

```vue
<!-- App.vue -->
<router-view v-slot="{ Component }">
  <BasicLayout v-if="!isFullScreen">
    <component :is="Component" />   <!-- ← 传入 slot -->
  </BasicLayout>
  <component :is="Component" v-else />
</router-view>
```

但 BasicLayout 的内容区域使用的是 `<router-view />` 而非 `<slot />`：

```vue
<!-- BasicLayout.vue (修复前) -->
<div class="layout-content">
  <router-view />   <!-- ← BUG: 应使用 <slot /> -->
</div>
```

由于父级 `<router-view v-slot>` 已经消费了路由匹配，BasicLayout 内部的 `<router-view />` 没有任何组件可渲染，导致空白。App.vue 传入的 `<component :is="Component" />` 进入了 BasicLayout 的默认 slot，但 slot 从未被渲染。

## 修改文件

| 文件 | 修改内容 |
|------|---------|
| `src/layouts/BasicLayout.vue` | 第 80 行 `<router-view />` → `<slot />` |

## 修复方案

采用方案 A：App.vue 负责捕获路由组件并传入 BasicLayout，BasicLayout 通过 `<slot />` 渲染。

```vue
<!-- BasicLayout.vue (修复后) -->
<div class="layout-content">
  <slot />   <!-- ✅ 渲染 App.vue 传入的页面组件 -->
</div>
```

## 验证结果

| 验证项 | 结果 |
|--------|:---:|
| `/dashboard` 显示统计卡片和图表 | ✅ |
| `/customer/list` 显示客户列表和筛选 | ✅ |
| `/opportunity/list` 显示商机列表 | ✅ |
| `/follow-records` 显示跟进列表 | ✅ |
| `/order/list` 显示订单列表 | ✅ |
| `/analytics` 显示统计图表 | ✅ |
| `/system/users` 显示用户管理 | ✅ |
| `/system/roles` 显示角色管理 | ✅ |
| `/system/operation-logs` 显示操作日志 | ✅ |
| `/login` 不被 BasicLayout 包裹 | ✅ |
| `/403` 不被 BasicLayout 包裹 | ✅ |
| 404 不被 BasicLayout 包裹 | ✅ |
| `npm run type-check` 通过 | ✅ |
| `npm run build` 通过 | ✅ |

## 是否影响后端

❌ 不影响。仅修改前端 `BasicLayout.vue` 一行代码，不涉及后端 Java/SQL/YML/pom.xml。
