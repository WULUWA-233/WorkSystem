# CRM 前端项目 — crm-frontend

## 项目说明

CRM 管理系统前端，基于 Vue 3 + Vite + TypeScript 构建，对接后端 Spring Boot CRM API。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 | ^3.5 |
| 构建工具 | Vite | ^8.1 |
| 语言 | TypeScript | ~6.0 |
| UI 组件库 | Element Plus | ^2.14 |
| 路由 | Vue Router | ^5.1 |
| 状态管理 | Pinia | ^3.0 |
| HTTP 客户端 | Axios | ^1.18 |
| 图表 | ECharts | ^6.1 |
| 图标 | @element-plus/icons-vue | ^2.3 |

## 文档位置

前端所有阶段文档统一存放在：

```
frontend/docx_front/
```

不再使用项目根目录的 `docx_front/`。

## README 快照命名规则

每个阶段完成后生成带 8 位时间后缀的快照：

```
README_FRONT_MMDDHHmm.md
```

- `MM`：月份；`DD`：日期；`HH`：小时（24h）；`mm`：分钟
- 示例：`README_FRONT_06250908.md`
- 快照内容与当时 `README_FRONT.md` 一致，不覆盖旧快照

## 目录结构

```
frontend/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
├── .env.development
├── .env.production
├── docx_front/                  # 前端阶段文档
│   ├── README_FRONT.md           # 最新 README（本文件）
│   ├── README_FRONT_MMDDHHmm.md  # 阶段快照
│   ├── FRONT-001_*.md            # 阶段文档
│   ├── FRONT-002_*.md
│   └── FRONT-003_*.md
└── src/
    ├── main.ts                   # 入口文件
    ├── App.vue                   # 根组件（layout/full-screen 切换）
    ├── api/                      # API 请求模块
    │   ├── request.ts            # Axios 实例 + 拦截器 + 401/403 处理
    │   ├── auth.ts               # 登录认证
    │   ├── user.ts               # 用户管理 (ADMIN)
    │   ├── role.ts               # 角色管理 (ADMIN)
    │   ├── customer.ts           # 客户管理
    │   ├── opportunity.ts        # 商机管理
    │   ├── followRecord.ts       # 跟进记录
    │   ├── order.ts              # 订单管理
    │   ├── operationLog.ts       # 操作日志 (ADMIN)
    │   └── analytics.ts          # 数据统计
    ├── types/                    # TypeScript 类型定义
    │   ├── api.ts                # ApiResult, PageResult, PageQuery, OptionItem
    │   ├── auth.ts               # LoginRequest, LoginResponse, LoginUser, CurrentUser
    │   ├── user.ts               # UserVO, UserDetailVO, UserQuery, UserCreateDTO, etc.
    │   ├── role.ts               # RoleVO, RoleOptionVO, RoleQuery, RoleCreateDTO, etc.
    │   └── operationLog.ts       # OperationLogVO, OperationLogDetailVO, OperationLogQuery
    ├── constants/                # 常量与枚举
    │   └── enums.ts              # 10 组枚举 + options + getLabelByValue
    ├── router/                   # 路由配置
    │   └── index.ts              # 15 条路由 + beforeEach 守卫
    ├── stores/                   # Pinia 状态管理
    │   └── auth.ts               # 完整登录态 Store
    ├── utils/                    # 工具函数
    │   ├── storage.ts            # Token/User localStorage 读写
    │   └── format.ts             # formatDateTime, formatJson
    ├── components/               # 通用组件
    │   └── JsonPreview.vue       # JSON 格式化展示
    ├── layouts/                  # 布局组件
    │   └── BasicLayout.vue       # 后台管理布局（侧边栏+顶栏+内容区）
    └── views/                    # 页面视图
        ├── login/                # 登录页（完整功能）
        ├── dashboard/            # 首页仪表盘（占位）
        ├── system/               # 系统管理 (ADMIN)
        │   ├── users/            # 用户管理（完整 CRUD）
        │   ├── roles/            # 角色管理（完整 CRUD）
        │   └── operationLogs/    # 操作日志（完整列表+详情）
        ├── customer/             # 客户管理（占位）
        ├── opportunity/          # 商机管理（占位）
        ├── followRecord/         # 跟进记录（占位）
        ├── order/                # 订单管理（占位）
        ├── analytics/            # 数据统计（占位）
        └── error/                # 403 / 404
```

## 启动命令

```bash
cd frontend

# 安装依赖
npm install

# 开发模式启动 (默认 http://localhost:5173)
npm run dev

# 类型检查
npm run type-check

# 生产构建
npm run build

# 预览构建产物
npm run preview
```

## 后端接口配置

- **Base URL**: `http://localhost:8080`
- **API 前缀**: `/api/v1`
- **认证头**: `Authorization: Bearer {token}`
- **代理配置**: Vite dev server 将 `/api` 代理到 `http://localhost:8080`

前端通过 Vite proxy 开发，无需配置 CORS。请求路径统一使用 `/api/v1/...`。

## 环境变量

| 变量 | 开发 | 生产 | 说明 |
|------|------|------|------|
| VITE_API_BASE_URL | /api/v1 | /api/v1 | API 基础路径 |
| VITE_BACKEND_TARGET | http://localhost:8080 | http://localhost:8080 | 后端地址 |

## 登录认证说明

### 登录流程

1. 用户输入 username + password
2. 调用 `POST /api/v1/auth/login`
3. 成功后保存 `accessToken` → localStorage (`crm_access_token`)
4. 保存 `user` → localStorage (`crm_user`)
5. 后续所有请求自动携带 `Authorization: Bearer {token}`
6. 刷新页面后从 localStorage 恢复 Token 和用户信息
7. 如果恢复时 Token 有效，自动调用 `GET /api/v1/auth/me` 更新用户信息
8. 退出时调用 `POST /api/v1/auth/logout`，无论成功与否都清除本地状态

### Token 存储

| Key | 内容 | 位置 |
|-----|------|------|
| `crm_access_token` | JWT Token 字符串 | localStorage |
| `crm_user` | 用户对象 JSON | localStorage |

### 路由守卫规则

| 场景 | 行为 |
|------|------|
| 未登录访问受保护页面 | 跳转 `/login?redirect=原路径` |
| 已登录访问 `/login` | 跳转 `/dashboard` |
| Token 存在但 user 缺失 | 调用 `/auth/me` 恢复，失败则清除并跳转 `/login` |
| 已登录但角色不满足 | 跳转 `/403` |
| 401 HTTP 响应 | 清除本地状态，跳转 `/login` |
| 403 HTTP 响应 | 提示"无权限访问"，跳转 `/403` |

## ADMIN/SALES 权限说明

| 菜单/路由 | ADMIN | SALES |
|-----------|:-----:|:-----:|
| 首页仪表盘 `/dashboard` | ✅ | ✅ |
| 客户管理 `/customer/*` | ✅ | ✅ |
| 商机管理 `/opportunity/*` | ✅ | ✅ |
| 跟进记录 `/follow-records` | ✅ | ✅ |
| 订单管理 `/order/*` | ✅ | ✅ |
| 数据统计 `/analytics` | ✅ | ✅ |
| 用户管理 `/system/users` | ✅ | ❌ |
| 角色管理 `/system/roles` | ✅ | ❌ |
| 操作日志 `/system/operation-logs` | ✅ | ❌ |

- SALES 手动访问 `/system/*` → 跳转 `/403`
- SALES 菜单不显示系统管理模块

## 401 / 403 处理说明

- **401**: Axios 响应拦截器检测到 401 → 清除 localStorage Token/User → 动态导入 router 跳转 `/login`
- **403**: Axios 响应拦截器检测到 403 → 提示"无权限访问" → 动态导入 router 跳转 `/403`
- 使用 lazy import (`import('@/router')`) 避免循环依赖

## 默认测试账号

| 用户名 | 密码 | 角色 | 权限范围 |
|--------|------|------|----------|
| admin | 123456 | ADMIN | 全部接口 |
| sales01 | 123456 | SALES | 业务模块 |
| sales02 | 123456 | SALES | 业务模块 |

## 当前可访问路由

| 路由 | 页面 | 权限 | 状态 |
|------|------|:---:|:---:|
| `/login` | 登录页 | 公开 | ✅ 完整 |
| `/dashboard` | 首页仪表盘 | ADMIN/SALES | ✅ 完整 |
| `/system/users` | 用户管理 | ADMIN | ✅ 完整 |
| `/system/roles` | 角色管理 | ADMIN | ✅ 完整 |
| `/system/operation-logs` | 操作日志 | ADMIN | ✅ 完整 |
| `/customer/list` | 客户列表 | ADMIN/SALES | ✅ 完整 |
| `/customer/:id` | 客户详情 | ADMIN/SALES | ✅ 完整 |
| `/opportunity/list` | 商机列表 | ADMIN/SALES | ✅ 完整 |
| `/opportunity/:id` | 商机详情 | ADMIN/SALES | ✅ 完整 |
| `/follow-records` | 跟进记录 | ADMIN/SALES | ✅ 完整 |
| `/order/list` | 订单列表 | ADMIN/SALES | ✅ 完整 |
| `/order/:id` | 订单详情 | ADMIN/SALES | ✅ 完整 |
| `/analytics` | 数据统计 | ADMIN/SALES | ✅ 完整 |
| `/403` | 无权限页 | 公开 | ✅ 完整 |
| `/*` | 404 页 | 公开 | ✅ 完整 |

## 已实现模块

| 模块 | 接口数 | 前端 API 模块 | 页面状态 |
|------|:---:|------|:---:|
| 登录认证 | 3 | `api/auth.ts` | ✅ 完整 |
| 用户管理 | 8 | `api/user.ts` | ✅ 完整 |
| 角色管理 | 6 | `api/role.ts` | ✅ 完整 |
| 客户管理 | 7 | `api/customer.ts` | ✅ 完整 |
| 商机管理 | 9 | `api/opportunity.ts` | ✅ 完整 |
| 跟进记录 | 6 | `api/followRecord.ts` | ✅ 完整 |
| 订单管理 | 12 | `api/order.ts` | ✅ 完整 |
| 操作日志 | 3 | `api/operationLog.ts` | ✅ 完整 |
| 数据统计 | 7 | `api/analytics.ts` | ✅ 完整 |
| **合计** | **62** | | |

## 用户管理页面说明

- 路由: `/system/users`（仅 ADMIN）
- 分页列表 + 筛选（用户名/真实姓名/手机号/状态）
- 新增用户（含密码和角色分配）
- 编辑用户（不含密码和用户名）
- 启用/禁用用户（二次确认）
- 重置密码（弹窗输入新密码）
- 分配角色（从 `/system/roles/options` 获取多选）
- 删除用户（二次确认，后端保护 admin/自己）

## 角色管理页面说明

- 路由: `/system/roles`（仅 ADMIN）
- 分页列表 + 筛选（角色名称/编码/状态）
- 新增角色（含 roleCode）
- 编辑角色（不含 roleCode）
- 删除角色（二次确认，后端保护 ADMIN/SALES 内置角色）

## 操作日志页面说明

- 路由: `/system/operation-logs`（仅 ADMIN）
- 分页列表 + 筛选（用户名/模块名/操作类型/请求方法/状态/时间范围）
- 日志详情弹窗（含 requestParams/responseResult JSON 展示）
- 删除日志（二次确认）

## 客户管理页面说明

- 路由: `/customer/list`、`/customer/:id`（ADMIN/SALES）
- 分页列表 + 筛选（客户名称/行业/来源/等级/地区/状态/时间范围）
- 新增/编辑客户弹窗（含所有字段）
- 修改客户状态
- 删除客户（逻辑删除，二次确认）
- 客户详情页：基础信息 + 关联商机/跟进/订单 Tabs
- 快捷操作：编辑/创建商机/添加跟进

## 商机管理页面说明

- 路由: `/opportunity/list`、`/opportunity/:id`（ADMIN/SALES）
- 分页列表 + 筛选（名称/阶段/金额范围/预计成交日期范围）
- 新增/编辑商机（stage 仅 1-4）
- 阶段推进（弹窗选 1-4，带概率）
- 赢单/输单（输单 loseReason 必填）
- 已关闭商机（stage 5/6）禁用编辑/推进/赢单/输单按钮
- 赢单商机可创建订单
- 商机详情页：基础信息 + 关联跟进/订单 Tabs

## 跟进记录页面说明

- 路由: `/follow-records`（ADMIN/SALES）
- 分页列表 + 筛选（客户ID/商机ID/跟进方式/客户意向/时间范围）
- 新增跟进弹窗（followerId 默认当前用户）
- 跟进详情弹窗
- 删除跟进（二次确认）

## 订单管理页面说明

- 路由: `/order/list`、`/order/:id`（ADMIN/SALES）
- 分页列表 + 筛选（订单编号/客户ID/商机ID/状态/金额范围/时间范围）
- 状态流转按钮按 status 显隐：付款(1)/发货(2)/完成(3)/取消(1,2)
- 终态(4/5)不显示状态变更按钮
- status≥2 禁用删除按钮
- 订单详情页：基本信息 + 按状态显示操作按钮

## 主业务流程

```
登录 → 客户录入 → 创建商机(stage=1) → 添加跟进记录
  → 商机阶段推进(1→2→3→4) → 赢单(stage=5)
    → 创建订单(status=1) → 付款(2) → 发货(3) → 完成(4)
```

## 新增类型文件

| 文件 | 包含类型 |
|------|---------|
| `src/types/user.ts` | UserVO, UserDetailVO, UserQuery, UserCreateDTO, UserUpdateDTO, UserStatusDTO, UserPasswordResetDTO, UserRoleAssignDTO |
| `src/types/role.ts` | RoleVO, RoleOptionVO, RoleQuery, RoleCreateDTO, RoleUpdateDTO |
| `src/types/operationLog.ts` | OperationLogVO, OperationLogDetailVO, OperationLogQuery |
| `src/types/customer.ts` | CustomerVO, CustomerDetailVO, CustomerQuery, CustomerCreateDTO, CustomerUpdateDTO, CustomerStatusDTO, CustomerNameCheckResult |
| `src/types/opportunity.ts` | OpportunityVO, OpportunityDetailVO, OpportunityQuery, OpportunityCreateDTO, OpportunityUpdateDTO, OpportunityStageDTO, OpportunityWinDTO, OpportunityLoseDTO |
| `src/types/followRecord.ts` | FollowRecordVO, FollowRecordDetailVO, FollowRecordQuery, FollowRecordCreateDTO |
| `src/types/order.ts` | OrderVO, OrderDetailVO, OrderQuery, OrderCreateDTO, OrderUpdateDTO, OrderStatusDTO, OrderPayDTO, OrderShipDTO, OrderCancelDTO |

## 新增/修改 API 模块

| 文件 | 变化 |
|------|------|
| `src/api/role.ts` | `getRoleOptions()` 返回类型改为 `RoleOptionVO[]` |
| `src/api/user.ts` | 无变化（FRONT-001 已完整） |
| `src/api/operationLog.ts` | 无变化（FRONT-001 已完整） |

## 未实现模块

以下模块后端不存在，前端不做对应页面：
合同管理、报价管理、发票/退款、文件上传/附件、AI 助手、营销管理、工单/服务、Excel/PDF 导出、消息通知、菜单/按钮级 RBAC 权限管理。

## 当前阶段进度

| 阶段 | 状态 |
|------|:---:|
| FRONT-001 前端工程初始化与 API 基础层 | ✅ 完成 |
| FRONT-002 登录认证、布局与权限控制 | ✅ 完成 |
| FRONT-003 ADMIN 系统管理模块 | ✅ 完成 |
| FRONT-004 CRM 核心业务模块 | ✅ 完成 |
| FRONT-005 统计看板、联调验收与交付 | ✅ 完成 |

## 项目状态

🎉 **前端 MVP 全部完成！** 共 62 个后端接口全对接，13 个页面完整可用，支持 ADMIN/SALES 双角色。

## 后续优化建议

- Element Plus 按需引入减小产物体积
- 客户/商机/用户选择器优化（远程搜索下拉）
- 移动端适配
- CI/CD 自动化构建部署
