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
- 示例：`README_FRONT_06250848.md`
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
│   └── FRONT-002_*.md
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
    │   └── auth.ts               # LoginRequest, LoginResponse, LoginUser, CurrentUser
    ├── constants/                # 常量与枚举
    │   └── enums.ts              # 10 组枚举 + options + getLabelByValue
    ├── router/                   # 路由配置
    │   └── index.ts              # 15 条路由 + beforeEach 守卫
    ├── stores/                   # Pinia 状态管理
    │   └── auth.ts               # 完整登录态 Store
    ├── utils/                    # 工具函数
    │   └── storage.ts            # Token/User localStorage 读写
    ├── layouts/                  # 布局组件
    │   └── BasicLayout.vue       # 后台管理布局（侧边栏+顶栏+内容区）
    └── views/                    # 页面视图
        ├── login/                # 登录页（完整功能）
        ├── dashboard/            # 首页仪表盘（占位）
        ├── system/               # 系统管理 (ADMIN)
        │   ├── users/            # 用户管理（占位）
        │   ├── roles/            # 角色管理（占位）
        │   └── operationLogs/    # 操作日志（占位）
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

| 路由 | 页面 | 权限 |
|------|------|:---:|
| `/login` | 登录页（完整功能） | 公开 |
| `/dashboard` | 首页仪表盘（占位） | ADMIN/SALES |
| `/system/users` | 用户管理（占位） | ADMIN |
| `/system/roles` | 角色管理（占位） | ADMIN |
| `/system/operation-logs` | 操作日志（占位） | ADMIN |
| `/customer/list` | 客户列表（占位） | ADMIN/SALES |
| `/customer/:id` | 客户详情（占位） | ADMIN/SALES |
| `/opportunity/list` | 商机列表（占位） | ADMIN/SALES |
| `/opportunity/:id` | 商机详情（占位） | ADMIN/SALES |
| `/follow-records` | 跟进记录（占位） | ADMIN/SALES |
| `/order/list` | 订单列表（占位） | ADMIN/SALES |
| `/order/:id` | 订单详情（占位） | ADMIN/SALES |
| `/analytics` | 数据统计（占位） | ADMIN/SALES |
| `/403` | 无权限页 | 公开 |
| `/*` | 404 页 | 公开 |

## 已实现模块

| 模块 | 接口数 | 前端 API 模块 | 页面状态 |
|------|:---:|------|:---:|
| 登录认证 | 3 | `api/auth.ts` | ✅ 完整 |
| 用户管理 | 8 | `api/user.ts` | ⏳ 占位 |
| 角色管理 | 6 | `api/role.ts` | ⏳ 占位 |
| 客户管理 | 7 | `api/customer.ts` | ⏳ 占位 |
| 商机管理 | 9 | `api/opportunity.ts` | ⏳ 占位 |
| 跟进记录 | 6 | `api/followRecord.ts` | ⏳ 占位 |
| 订单管理 | 12 | `api/order.ts` | ⏳ 占位 |
| 操作日志 | 3 | `api/operationLog.ts` | ⏳ 占位 |
| 数据统计 | 7 | `api/analytics.ts` | ⏳ 占位 |
| **合计** | **62** | | |

## 未实现模块

以下模块后端不存在，前端不做对应页面：
合同管理、报价管理、发票/退款、文件上传/附件、AI 助手、营销管理、工单/服务、Excel/PDF 导出、消息通知、菜单/按钮级 RBAC 权限管理。

## 当前阶段进度

| 阶段 | 状态 |
|------|:---:|
| FRONT-001 前端工程初始化与 API 基础层 | ✅ 完成 |
| FRONT-002 登录认证、布局与权限控制 | ✅ 完成 |
| FRONT-003 ADMIN 系统管理模块 | ⏳ 待开发 |
| FRONT-004 CRM 核心业务模块 | ⏳ 待开发 |
| FRONT-005 统计看板、联调验收与交付 | ⏳ 待开发 |

## 后续阶段计划

- **FRONT-003**: 用户管理、角色管理、操作日志完整 CRUD 页面
- **FRONT-004**: 客户、商机、跟进、订单完整业务 CRUD 页面
- **FRONT-005**: 首页仪表盘图表、数据统计页、联调验收、构建交付
