# FRONT-001 — 前端工程初始化与 API 基础层

## 阶段目标

建立一个可运行、可扩展、可对接后端的前端基础工程。完成项目骨架、请求封装、类型定义、枚举常量、基础路由占位和布局。

## 完成任务清单

- [x] 使用 Vite 脚手架创建 Vue 3 + TypeScript 项目
- [x] 安装所有依赖：Vue 3, Vite, TypeScript, Element Plus, Vue Router, Pinia, Axios, ECharts
- [x] 配置 `package.json` scripts：dev, build, preview, type-check
- [x] 配置 Vite dev proxy（`/api` → `http://localhost:8080`）
- [x] 配置 `@` 路径别名（Vite + TypeScript）
- [x] 创建 `.env.development` / `.env.production`
- [x] 创建 `src/types/api.ts`（ApiResult, PageResult, PageQuery, OptionItem）
- [x] 创建 `src/types/auth.ts`（LoginRequest, LoginResponse, CurrentUser 等）
- [x] 创建 `src/constants/enums.ts`（10 组枚举 + options + getLabelByValue 工具函数）
- [x] 创建 `src/api/request.ts`（Axios 实例、Token 拦截、统一响应解析、401/403 处理）
- [x] 创建 9 个 API 模块文件（auth, user, role, customer, opportunity, followRecord, order, operationLog, analytics）
- [x] 创建 `src/stores/auth.ts` Pinia Store 骨架
- [x] 创建 `src/router/index.ts` 15 条路由占位
- [x] 创建 `src/layouts/BasicLayout.vue` 基础布局
- [x] 创建 13 页面占位 + 403 + 404 页面
- [x] 更新 `src/main.ts` 注册 Element Plus + Pinia + Router
- [x] 创建 `docx_front/` 前端文档目录
- [x] 创建 `README_FRONT.md` 前端项目文档
- [x] 创建 `FRONT-001_前端工程初始化与API基础层.md` 阶段文档

## 新增/修改文件列表

### 新创建文件

```
frontend/
├── .env.development
├── .env.production
├── src/
│   ├── types/api.ts
│   ├── types/auth.ts
│   ├── constants/enums.ts
│   ├── api/request.ts
│   ├── api/auth.ts
│   ├── api/user.ts
│   ├── api/role.ts
│   ├── api/customer.ts
│   ├── api/opportunity.ts
│   ├── api/followRecord.ts
│   ├── api/order.ts
│   ├── api/operationLog.ts
│   ├── api/analytics.ts
│   ├── stores/auth.ts
│   ├── router/index.ts
│   ├── layouts/BasicLayout.vue
│   └── views/
│       ├── login/LoginView.vue
│       ├── dashboard/DashboardView.vue
│       ├── system/users/UserListView.vue
│       ├── system/roles/RoleListView.vue
│       ├── system/operationLogs/OperationLogView.vue
│       ├── customer/CustomerListView.vue
│       ├── customer/CustomerDetailView.vue
│       ├── opportunity/OpportunityListView.vue
│       ├── opportunity/OpportunityDetailView.vue
│       ├── followRecord/FollowRecordView.vue
│       ├── order/OrderListView.vue
│       ├── order/OrderDetailView.vue
│       ├── analytics/AnalyticsView.vue
│       ├── error/ForbiddenView.vue
│       └── error/NotFoundView.vue
docx_front/
├── README_FRONT.md
└── FRONT-001_前端工程初始化与API基础层.md
```

### 修改文件

```
frontend/
├── package.json          # 更新 scripts，添加 type-check
├── vite.config.ts        # 添加 @ 别名 + dev proxy
├── tsconfig.app.json     # 添加 @ 路径映射
├── index.html            # 更新标题为 "CRM 管理系统"
├── src/main.ts           # 注册 Element Plus, Pinia, Router
└── src/App.vue           # 改为 <router-view />
```

### 删除文件

```
frontend/
├── src/style.css          # 不再需要（Element Plus 自带样式）
├── src/assets/            # 清空（暂无静态资源需求）
└── src/components/HelloWorld.vue  # 脚手架模板文件
```

## API 基础层说明

### 请求封装 (`api/request.ts`)

- 创建 Axios 实例，`baseURL: '/api/v1'`，超时 30s
- **请求拦截器**: 自动从 localStorage 读取 `crm_access_token`，注入 `Authorization: Bearer {token}`
- **响应拦截器（成功）**: 解析 `{code, message, data, timestamp}`，`code === 200` 时直接返回 `data`，`code !== 200` 时显示 `message` 并 reject
- **响应拦截器（错误）**:
  - 401 → 清除 Token，提示"登录已过期"
  - 403 → 提示"无权限访问"
  - 404 → 提示"请求的资源不存在"
  - 超时 → 提示"请求超时"
  - 网络异常 → 提示"网络异常"
- Token 存取通过 `getToken()`/`setToken()`/`removeToken()` 工具函数

### API 模块文件

共 9 个 API 模块，覆盖 62 个后端接口：

| 文件 | 函数数 | 对应后端 Controller |
|------|:---:|------|
| `api/auth.ts` | 3 | AuthController |
| `api/user.ts` | 8 | SysUserController |
| `api/role.ts` | 6 | SysRoleController |
| `api/customer.ts` | 7 | CustomerController |
| `api/opportunity.ts` | 9 | OpportunityController |
| `api/followRecord.ts` | 4 | FollowRecordController |
| `api/order.ts` | 10 | OrderController |
| `api/operationLog.ts` | 3 | OperationLogController |
| `api/analytics.ts` | 7 | AnalyticsController |

每个函数签名遵循后端接口路径和参数规范。

### TypeScript 类型

- `ApiResult<T>` → `{ code, message, data: T, timestamp }`
- `PageResult<T>` → `{ total, pageNo, pageSize, records: T[] }`
- `PageQuery` → `{ pageNo?, pageSize? }`
- `OptionItem` → `{ label, value }`
- `LoginRequest`, `LoginResponse`, `LoginUser`, `CurrentUser`

## 枚举说明

共 10 组枚举，每组提供常量对象和 `OptionItem[]` 下拉数组：

1. 用户状态（0禁用/1启用）
2. 性别（0未知/1男/2女）
3. 角色编码（ADMIN/SALES/USER）
4. 客户状态（0无效/1有效/2公海）
5. 客户等级（1-5）
6. 商机阶段（1-6，含赢单/输单）
7. 跟进客户意向（1-4）
8. 跟进方式（电话/微信/邮件/拜访/会议/其他）
9. 订单状态（1-5）
10. 操作日志状态（0失败/1成功）

工具函数：`getLabelByValue(options, value)` 通用标签查找。

## 路由占位说明

共 15 条路由，均为占位页面：

| 路由 | 组件 | 权限 | 状态 |
|------|------|:---:|:---:|
| `/login` | LoginView | 公开 | 占位 |
| `/dashboard` | DashboardView | 需登录 | 占位 |
| `/system/users` | UserListView | ADMIN | 占位 |
| `/system/roles` | RoleListView | ADMIN | 占位 |
| `/system/operation-logs` | OperationLogView | ADMIN | 占位 |
| `/customer/list` | CustomerListView | 需登录 | 占位 |
| `/customer/:id` | CustomerDetailView | 需登录 | 占位 |
| `/opportunity/list` | OpportunityListView | 需登录 | 占位 |
| `/opportunity/:id` | OpportunityDetailView | 需登录 | 占位 |
| `/follow-records` | FollowRecordView | 需登录 | 占位 |
| `/order/list` | OrderListView | 需登录 | 占位 |
| `/order/:id` | OrderDetailView | 需登录 | 占位 |
| `/analytics` | AnalyticsView | 需登录 | 占位 |
| `/403` | ForbiddenView | 公开 | 完成 |
| `/:pathMatch(.*)*` | NotFoundView | 公开 | 完成 |

路由守卫将在 FRONT-002 实现。

## 验证命令与结果

### 1. `npm install`
```
added 152 packages in ~57s
✅ 通过
```

### 2. `npm run type-check`
```
vue-tsc -b --noEmit
✅ 通过（零错误）
```

### 3. `npm run build`
```
vue-tsc -b && vite build
✅ 通过（类型检查 + 构建均成功）

Built in 1.11s
产物: frontend/dist/
```

### 4. `npm run dev`（验证可启动）
项目可在 `http://localhost:5173` 启动，所有占位路由可访问。

## 遇到的问题和解决方案

1. **TS 6.0 废弃 `baseUrl`**: TypeScript 6.0 中 `baseUrl` 已废弃，移除了 `baseUrl` 选项，改用 `paths` 相对路径映射 `"@/*": ["./src/*"]`。

2. **构建警告（非阻塞）**: `@vueuse/core` 的 `#__PURE__` 注释在 Rolldown 中位置不兼容，这是第三方库问题，不影响功能。Element Plus 产物体积较大（~350KB CSS + ~1.1MB JS），后续可优化为按需引入。

## 下一阶段建议

FRONT-002 应完成以下工作：

1. 实现完整登录页面（表单校验、loading、错误提示）
2. 接入 `POST /api/v1/auth/login` 真实接口
3. 完善 Pinia auth store 的 login/logout/fetchCurrentUser 流程
4. 实现路由守卫（beforeEach）：
   - 未登录 → `/login`
   - 已登录访问 `/login` → `/dashboard`
   - 无权限 → `/403`
5. 完善 BasicLayout：
   - 根据角色动态显示/隐藏菜单
   - 顶部栏显示用户名和角色
   - 退出登录按钮
6. 实现 Token 持久化（刷新页面恢复登录态）
7. 403 页面和 404 页面已完成，无需额外开发
