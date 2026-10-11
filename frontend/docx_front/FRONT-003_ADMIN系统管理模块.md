# FRONT-003 — ADMIN 系统管理模块

## 阶段目标

完成 ADMIN 专属系统管理页面：用户管理、角色管理、操作日志。实现完整的 CRUD 操作、分页筛选、弹窗表单、权限控制和业务规则提示。

## 完成任务清单

- [x] **类型定义**: 新增 `src/types/user.ts`、`src/types/role.ts`、`src/types/operationLog.ts`
- [x] **工具函数**: 新增 `src/utils/format.ts`（formatDateTime, formatJson）
- [x] **通用组件**: 新增 `src/components/JsonPreview.vue`（JSON 格式化展示）
- [x] **API 模块修复**: `src/api/role.ts` 的 `getRoleOptions()` 返回类型改为 `RoleOptionVO[]`
- [x] **用户管理页**: 完成 `UserListView.vue` — 分页列表 + 筛选 + 新增/编辑/删除/启禁用/重置密码/分配角色
- [x] **角色管理页**: 完成 `RoleListView.vue` — 分页列表 + 筛选 + 新增/编辑/删除
- [x] **操作日志页**: 完成 `OperationLogView.vue` — 分页列表 + 多条件筛选 + 详情弹窗 + 删除
- [x] **权限约束**: 三个页面路由 meta.roles = `['ADMIN']`，SALES 不可见不可访问
- [x] **交互规范**: 所有写操作二次确认 + loading + 成功提示 + 失败保留弹窗
- [x] **README 更新**: 更新至 FRONT-003 完成状态
- [x] **README 快照**: 生成 `README_FRONT_06250908.md`
- [x] **构建验证**: `npm run type-check` + `npm run build` 通过

## 新增/修改文件列表

### 新创建 (6)

```
frontend/src/types/user.ts
frontend/src/types/role.ts
frontend/src/types/operationLog.ts
frontend/src/utils/format.ts
frontend/src/components/JsonPreview.vue
frontend/docx_front/FRONT-003_ADMIN系统管理模块.md
```

### 修改 (5)

```
frontend/src/api/role.ts                  # getRoleOptions 返回类型修正
frontend/src/views/system/users/UserListView.vue      # 完整 CRUD
frontend/src/views/system/roles/RoleListView.vue      # 完整 CRUD
frontend/src/views/system/operationLogs/OperationLogView.vue  # 完整列表+详情
frontend/docx_front/README_FRONT.md       # 更新至 FRONT-003
```

### 快照 (1)

```
frontend/docx_front/README_FRONT_06250908.md
```

## 用户管理功能说明

### 列表功能

- 分页表格：ID、用户名、真实姓名、昵称、手机号、邮箱、性别、状态、角色、创建时间
- 筛选条件：用户名、真实姓名、手机号、状态
- 性别/状态/角色使用枚举标签展示

### 操作功能

| 操作 | 弹窗/确认 | 接口 | 备注 |
|------|:---:|------|------|
| 新增 | 弹窗 | POST `/system/users` | 含 username/password/roleIds |
| 编辑 | 弹窗 | PUT `/system/users/{id}` | 不含 username/password |
| 删除 | 二次确认 | DELETE `/system/users/{id}` | 后端保护 admin/自己 |
| 启用/禁用 | 二次确认 | PATCH `/system/users/{id}/status` | Body: `{status:0\|1}` |
| 重置密码 | 弹窗 | PATCH `/system/users/{id}/password/reset` | 输入新密码 |
| 分配角色 | 弹窗 | PUT `/system/users/{id}/roles` | 多选角色 |

### 表单字段

- **新增**: username(pw), password(pw), realName(pw), nickname, phone, email, gender, status, roleIds(multi), remark
- **编辑**: realName(pw), nickname, phone, email, gender, status, remark
- **pw** = 必填字段

## 角色管理功能说明

### 列表功能

- 分页表格：ID、角色名称、角色编码、描述、状态、排序、创建时间
- 筛选条件：角色名称、角色编码、状态

### 操作功能

| 操作 | 弹窗/确认 | 接口 | 备注 |
|------|:---:|------|------|
| 新增 | 弹窗 | POST `/system/roles` | 含 roleCode |
| 编辑 | 弹窗 | PUT `/system/roles/{id}` | 不含 roleCode |
| 删除 | 二次确认 | DELETE `/system/roles/{id}` | 后端保护 ADMIN/SALES |

### 表单字段

- **新增**: roleName(pw), roleCode(pw), description, status, sort, remark
- **编辑**: roleName(pw), description, status, sort, remark（无 roleCode）

## 操作日志功能说明

### 列表功能

- 分页表格：ID、用户名、模块名、操作类型、操作描述、请求方法、请求URL、状态、耗时、IP地址、创建时间
- 筛选条件：用户名、模块名、操作类型、请求方法、状态、时间范围（datetimerange）
- 请求方法使用彩色标签（GET=success, POST=primary, PUT=warning, DELETE=danger, PATCH=info）

### 操作功能

| 操作 | 弹窗/确认 | 接口 | 备注 |
|------|:---:|------|------|
| 详情 | 弹窗 | GET `/system/operation-logs/{id}` | 含 requestParams/responseResult |
| 删除 | 二次确认 | DELETE `/system/operation-logs/{id}` | 物理删除 |

### 详情弹窗

使用 `el-descriptions` 展示完整日志信息，requestParams 和 responseResult 使用 `JsonPreview` 组件格式化展示（支持 JSON 美化 + 非 JSON 原样展示）。

## 类型定义说明

| 类型文件 | 内容 |
|---------|------|
| `user.ts` | UserVO, UserDetailVO, UserQuery, UserCreateDTO, UserUpdateDTO, UserStatusDTO, UserPasswordResetDTO, UserRoleAssignDTO |
| `role.ts` | RoleVO, RoleOptionVO, RoleQuery, RoleCreateDTO, RoleUpdateDTO |
| `operationLog.ts` | OperationLogVO, OperationLogDetailVO, OperationLogQuery |

## API 对接说明

| API 模块 | 状态 | 变化 |
|---------|:---:|------|
| `api/user.ts` | 完整 | 无变化（FRONT-001 已完整定义 8 个函数） |
| `api/role.ts` | 完整 | `getRoleOptions()` 返回类型从 `OptionItem[]` 改为 `RoleOptionVO[]` |
| `api/operationLog.ts` | 完整 | 无变化（FRONT-001 已完整定义 3 个函数） |

## 权限验证说明

- 三个页面路由均设置 `meta.roles = ['ADMIN']`
- BasicLayout 菜单中系统管理分组仅 `v-if="authStore.isAdmin"` 可见
- 路由守卫确保非 ADMIN 用户访问时跳转 `/403`
- SALES 用户菜单中不显示系统管理模块

## 验证命令和结果

### 1. `npm run type-check`

```
> vue-tsc -b --noEmit
✅ 通过（零错误）
```

### 2. `npm run build`

```
> vue-tsc -b && vite build
✅ 通过（1.10s）

Warnings (非阻塞):
- @vueuse/core #__PURE__ annotation (第三方)
- Chunk size > 500KB (Element Plus 全量引入)
```

## admin 功能测试结果

| 测试场景 | 结果 |
|----------|:---:|
| admin/123456 登录成功 | ✅ |
| 访问 `/system/users` — 列表加载 | ✅ |
| 用户筛选可用（username/realName/phone/status） | ✅ |
| 分页切换可用 | ✅ |
| 新增用户 — 弹窗打开 + 角色选项加载 | ✅ |
| 新增用户 — 表单校验 + 提交成功 + 列表刷新 | ✅ |
| 编辑用户 — 弹窗打开加载已有数据 | ✅ |
| 编辑用户 — 提交成功 | ✅ |
| 启用/禁用用户 — 二次确认 + 切换成功 | ✅ |
| 重置密码 — 弹窗校验 + 提交成功 | ✅ |
| 分配角色 — 弹窗多选 + 提交成功 | ✅ |
| 删除用户 — 二次确认 + 成功/显示后端规则错误 | ✅ |
| 访问 `/system/roles` — 列表加载 | ✅ |
| 角色筛选可用 | ✅ |
| 新增角色 — 含 roleCode | ✅ |
| 编辑角色 — 不含 roleCode | ✅ |
| 删除角色 — 二次确认 + 成功/显示后端规则错误 | ✅ |
| 访问 `/system/operation-logs` — 列表加载 | ✅ |
| 操作日志筛选（含时间范围） | ✅ |
| 操作日志详情弹窗 — JSON 格式化展示 | ✅ |
| 操作日志删除 — 二次确认 | ✅ |

## sales01 权限回归测试结果

| 测试场景 | 结果 |
|----------|:---:|
| sales01/123456 登录成功 | ✅ |
| 菜单不显示"系统管理" | ✅ |
| 手动访问 `/system/users` → `/403` | ✅ |
| 手动访问 `/system/roles` → `/403` | ✅ |
| 手动访问 `/system/operation-logs` → `/403` | ✅ |
| 仍可访问 `/customer/list` 等业务页面 | ✅ |

## 遇到的问题和解决方案

1. **role options 类型不匹配**
   - 问题：FRONT-001 中 `getRoleOptions()` 返回 `OptionItem[]`（`{label, value}`），但后端实际返回 `{id, roleName, roleCode}`
   - 解决：修改返回类型为 `RoleOptionVO[]`，并在用户管理页中直接使用

2. **编辑角色不应提交 roleCode**
   - 问题：后端 `RoleUpdateDTO` 不包含 `roleCode` 字段
   - 解决：编辑弹窗中不显示 roleCode 字段，submit 时不传 roleCode

## README 快照文件

- **文件名**: `README_FRONT_06250908.md`
- **规则**: 8 位时间后缀 MM DD HH mm = 06 25 09 08

## 下一阶段 FRONT-004 建议

1. **客户管理** — 列表+详情页，含关联商机/跟进/订单展示，名称查重
2. **商机管理** — 列表+详情页，阶段推进/赢单/输单，状态流转按钮控制
3. **跟进记录** — 列表+新增弹窗，跟进方式/客户意向枚举
4. **订单管理** — 列表+详情页，付款/发货/完成/取消，状态流转按钮控制
5. 业务规则前端校验：商机阶段 1-4 用 /stage，5 用 /win，6 用 /lose；订单 1→2→3→4，1/2→5
6. 封装通用的分页筛选表格组件，减少重复代码
7. 为缺失的 options 接口使用分页接口 `pageSize=100` 做 MVP 下拉
