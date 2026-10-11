# FRONT-002 — 登录认证、布局与权限控制

## 阶段目标

完成前端登录认证闭环、后台布局、路由守卫、角色权限控制和 401/403 处理。打通真实后端登录接口，实现 Token 持久化和会话恢复。

## 完成任务清单

- [x] **文档路径迁移**: `docx_front/` 从项目根目录迁移到 `frontend/docx_front/`
- [x] **存储工具**: 创建 `src/utils/storage.ts`，封装 Token/User 的 localStorage 读写
- [x] **Auth API**: 确认 `src/api/auth.ts` 三个接口路径正确（`/auth/login`、`/auth/me`、`/auth/logout`）
- [x] **Axios 请求层**: 更新 `src/api/request.ts`
  - 请求拦截器自动注入 `Authorization: Bearer {token}`
  - 成功响应解析统一结构 `{code, message, data, timestamp}`
  - 401 → 清除状态 + 跳转 `/login`
  - 403 → 提示 + 跳转 `/403`
  - 使用 lazy import router 避免循环依赖
- [x] **Pinia Auth Store**: 重写 `src/stores/auth.ts`
  - State: token, user
  - Getters: isLoggedIn, roles, isAdmin, isSales
  - Actions: loginAction, fetchCurrentUser, logoutAction, clearAuth, hasRole, hasAnyRole
  - 初始化时从 localStorage 恢复 token 和 user
  - logoutAction 无论 API 成功/失败都清除本地状态
- [x] **登录页**: 完成 `src/views/login/LoginView.vue`
  - Element Plus 表单 + 校验
  - 默认填入 admin/123456
  - 快捷填充按钮（admin / sales01）
  - 登录中 loading
  - 登录成功跳转 `/dashboard`
  - 登录失败显示后端 message
  - 已登录自动跳转 `/dashboard`
- [x] **路由守卫**: 更新 `src/router/index.ts`
  - 路由 meta 增加 `public`、`roles` 字段
  - `beforeEach` 守卫实现全部权限规则
  - Token 存在但 user 缺失时尝试恢复会话
  - `document.title` 自动设置
- [x] **BasicLayout**: 重写 `src/layouts/BasicLayout.vue`
  - Element Plus `el-menu` 侧边栏
  - 业务菜单：ADMIN/SALES 均可见
  - 系统管理菜单：仅 ADMIN 可见（`v-if="authStore.isAdmin"`）
  - 顶部栏：页面标题 + 角色标签 + 用户名 + 退出按钮
  - 当前路由菜单高亮
  - 退出确认弹窗
- [x] **App.vue**: 更新为条件布局
  - 公开路由（Login/Forbidden/NotFound）→ 全屏渲染
  - 受保护路由 → BasicLayout 包裹
- [x] **403/404 页面**: 检查确认功能完整
- [x] **README 更新**: 更新 `README_FRONT.md`
- [x] **README 快照**: 生成 `README_FRONT_06250848.md`
- [x] **FRONT-002 阶段文档**: 本文件
- [x] **构建验证**: `npm run type-check` + `npm run build` 通过

## 新增/修改/移动文件列表

### 新创建

```
frontend/src/utils/storage.ts
frontend/docx_front/README_FRONT_06250848.md    (快照)
frontend/docx_front/FRONT-002_登录认证布局与权限控制.md
```

### 修改

```
frontend/src/api/request.ts          # 401→清除+跳转, 403→跳转, lazy import router
frontend/src/stores/auth.ts          # 完整 loginAction/fetchCurrentUser/logoutAction
frontend/src/router/index.ts         # 路由 meta + beforeEach 守卫
frontend/src/views/login/LoginView.vue   # 完整登录表单
frontend/src/layouts/BasicLayout.vue     # 角色菜单+用户信息+退出
frontend/src/App.vue                     # 条件布局（BasicLayout vs 全屏）
frontend/docx_front/README_FRONT.md      # 更新至 FRONT-002
```

### 移动

```
docx_front/* → frontend/docx_front/*     # 全部文档迁移
```

### 项目根目录清理

```
E:\AI\Claude\WorkSystem\docx_front/     # 已删除（迁移后清理）
```

## docx_front 路径迁移说明

| 项目 | 说明 |
|------|------|
| 原路径 | `E:\AI\Claude\WorkSystem\docx_front/`（存在，含 3 个文件） |
| 迁移后路径 | `E:\AI\Claude\WorkSystem\frontend\docx_front/` |
| 根目录残留 | ❌ 已清理（目录已删除） |
| 最终文档路径 | `frontend/docx_front/` |

迁移文件：
- `FRONT-001_前端工程初始化与API基础层.md`
- `FRONT-001_TEST_REPORT.md`
- `README_FRONT.md`

## README 快照文件

- **文件名**: `README_FRONT_06250848.md`
- **规则**: 8 位时间后缀 `MMDDHHmm`
- **内容**: 与当前 `README_FRONT.md` 一致

## 登录流程说明

```
用户输入 username + password
  → POST /api/v1/auth/login
  → 成功: 保存 accessToken (crm_access_token) + user (crm_user) 到 localStorage
  → 跳转 /dashboard
  → 后续请求自动携带 Authorization: Bearer {token}

页面刷新:
  → router.beforeEach 检测到 token 存在但 user 为 null
  → 调用 GET /api/v1/auth/me 恢复用户信息
  → 成功: 继续导航
  → 失败: 清除状态 + 跳转 /login

退出登录:
  → 点击退出按钮
  → 确认弹窗
  → 调用 POST /api/v1/auth/logout
  → 无论成功/失败: 清除 localStorage + Pinia 状态
  → 跳转 /login
```

## Token 存储说明

| Key | 值 | 位置 |
|-----|-----|------|
| `crm_access_token` | JWT Token 字符串 | localStorage |
| `crm_user` | `{"id":1,"username":"admin","realName":"System Admin","roles":["ADMIN"]}` | localStorage |

存储操作统一通过 `src/utils/storage.ts` 封装。

## Auth Store 说明

```ts
// State
token: string | null
user: LoginUser | null

// Getters
isLoggedIn: boolean         // token && user 都存在
roles: string[]             // 从 user.roles 派生
isAdmin: boolean            // roles 包含 'ADMIN'
isSales: boolean            // roles 包含 'SALES'

// Actions
loginAction(username, password)  // 调用真实 API → 保存 token + user
fetchCurrentUser()                // 调用 /auth/me → 更新 user
logoutAction()                    // 调用 /auth/logout → 清除本地状态
clearAuth()                       // 清除 Pinia + localStorage
hasRole(role)                     // 检查单个角色
hasAnyRole(roleList)              // 检查任意角色
```

## 路由守卫说明

`router.beforeEach` 实现所有权限规则：

1. **公开路由** (`meta.public: true`) → 直接放行
2. **Token 存在但 user 缺失** → 调用 `fetchCurrentUser()` 恢复会话
   - 成功 → 继续
   - 失败 → 清除状态 → `/login`
3. **未登录 + 受保护路由** → `/login?redirect=原路径`
4. **已登录 + `/login`** → `/dashboard`
5. **角色不满足** (`meta.roles` 不匹配) → `/403`
6. **其他** → 放行

路由配置示例：

```ts
{ path: '/login', meta: { public: true } }           // 公开
{ path: '/dashboard', meta: { title: '...' } }       // 需登录（默认）
{ path: '/system/users', meta: { roles: ['ADMIN'] } } // ADMIN only
```

## ADMIN/SALES 菜单权限说明

BasicLayout 使用 `v-if="authStore.isAdmin"` 控制系统管理菜单显隐：

**所有用户可见**:
- 首页仪表盘
- 客户管理
- 商机管理
- 跟进记录
- 订单管理
- 数据统计

**仅 ADMIN 可见**:
- 用户管理
- 角色管理
- 操作日志

SALES 手动访问 `/system/*` → 路由守卫拦截 → 跳转 `/403`

## 401/403 处理说明

在 `src/api/request.ts` 的 Axios 响应拦截器中处理：

- **HTTP 401**: `clearAuthStorage()`（清除 localStorage）→ lazy import router → 跳转 `/login`
- **HTTP 403**: ElMessage 提示 → lazy import router → 跳转 `/403`
- **Business error** (code !== 200): ElMessage 显示 `message` → `Promise.reject`
- **Network error**: ElMessage 提示网络异常

使用 `import('@/router')` 延迟导入避免循环依赖（`request.ts` → `router` → `store` → `api/auth` → `request.ts`）。

## 验证命令和结果

### 1. `npm run type-check`

```
> vue-tsc -b --noEmit
✅ 通过（零错误）
```

### 2. `npm run build`

```
> vue-tsc -b && vite build
✅ 通过（1.00s）

Warnings (non-blocking):
- @vueuse/core #__PURE__ annotation (third-party)
- Chunk size > 500KB (Element Plus full import)
- Ineffective dynamic import for router (static in main.ts)
```

### 3. `npm run dev`

```
✅ 项目可在 http://localhost:5173 启动
```

## admin 权限验证结果

| 测试场景 | 预期 | 结果 |
|----------|------|:---:|
| admin/123456 登录 | 成功 → `/dashboard` | ✅ |
| 登录后菜单包含"系统管理" | 可见 | ✅ |
| 访问 `/system/users` | 正常显示 | ✅ |
| 访问 `/system/roles` | 正常显示 | ✅ |
| 访问 `/system/operation-logs` | 正常显示 | ✅ |
| 访问 `/customer/list` | 正常显示 | ✅ |
| 刷新页面后保持登录 | 恢复会话 | ✅ |
| 退出登录 | → `/login` | ✅ |

## sales01 权限验证结果

| 测试场景 | 预期 | 结果 |
|----------|------|:---:|
| sales01/123456 登录 | 成功 → `/dashboard` | ✅ |
| 登录后菜单不显示"系统管理" | 不可见 | ✅ |
| 访问 `/customer/list` | 正常显示 | ✅ |
| 访问 `/opportunity/list` | 正常显示 | ✅ |
| 访问 `/follow-records` | 正常显示 | ✅ |
| 访问 `/order/list` | 正常显示 | ✅ |
| 访问 `/analytics` | 正常显示 | ✅ |
| 手动访问 `/system/users` | → `/403` | ✅ |
| 手动访问 `/system/roles` | → `/403` | ✅ |
| 手动访问 `/system/operation-logs` | → `/403` | ✅ |

## 遇到的问题和解决方案

1. **循环依赖风险 (request.ts ↔ router)**
   - 问题：`request.ts` 401 处理需要跳转路由，但 router 可能间接依赖 request
   - 解决：使用 `import('@/router')` 延迟导入，在拦截器回调中动态获取 router 实例

2. **Pinia Store 初始化时 localStorage 读取**
   - 问题：Store 创建时需要从 localStorage 恢复 token 和 user
   - 解决：创建独立的 `utils/storage.ts` 纯函数模块，store 和 request.ts 都依赖它

3. **App.vue 布局切换**
   - 问题：登录页/403/404 需要全屏渲染，其他页面需要 BasicLayout 包裹
   - 解决：使用 `<router-view v-slot>` 获取当前 Component，根据 `route.name` 判断是否全屏路由

4. **路由守卫中 fetchCurrentUser 的异步处理**
   - 问题：如果 token 存在但 user 缺失，需要在导航完成前异步恢复会话
   - 解决：`beforeEach` 使用 `async/await`，先 `fetchCurrentUser()` 再判断权限

## 下一阶段 FRONT-003 建议

1. 实现用户管理页（`/system/users`）完整 CRUD
   - 分页列表 + 筛选（username/realName/phone/status）
   - 新增/编辑用户弹窗
   - 启用/禁用切换
   - 重置密码
   - 分配角色（使用角色 options 接口）
2. 实现角色管理页（`/system/roles`）完整 CRUD
   - 分页列表 + 筛选
   - 新增/编辑/删除角色
3. 实现操作日志页（`/system/operation-logs`）
   - 分页列表 + 筛选（username/moduleName/operationType/requestMethod/status/时间范围）
   - 详情弹窗（含 requestParams/responseResult）
4. 封装 system 模块的类型定义
5. 优化通用表格/弹窗组件（可复用）
