# FRONT-004 — CRM 核心业务模块

## 阶段目标

完成 CRM 核心业务模块全部页面，打通完整业务闭环：

`登录 → 客户录入 → 创建商机 → 添加跟进 → 商机推进 → 赢单 → 创建订单 → 付款 → 发货 → 完成`

## 完成任务清单

- [x] **类型定义**: 新增 `customer.ts`(7类型), `opportunity.ts`(8类型), `followRecord.ts`(4类型), `order.ts`(9类型)
- [x] **客户管理**: `CustomerListView` — 分页/筛选/CRUD/状态/快捷操作
- [x] **客户详情**: `CustomerDetailView` — 基础信息+关联商机/跟进/订单 Tabs
- [x] **商机管理**: `OpportunityListView` — 分页/筛选/CRUD/阶段推进/赢单/输单
- [x] **商机详情**: `OpportunityDetailView` — 基础信息+关联跟进/订单+操作按钮
- [x] **跟进记录**: `FollowRecordView` — 分页/筛选/新增/详情/删除
- [x] **订单管理**: `OrderListView` — 分页/筛选/CRUD/状态流转(付款/发货/完成/取消)
- [x] **订单详情**: `OrderDetailView` — 基础信息+按状态操作按钮
- [x] **构建验证**: `npm run type-check` + `npm run build` 通过

## 新增/修改文件列表

### 新创建 (11)

```
frontend/src/types/customer.ts
frontend/src/types/opportunity.ts
frontend/src/types/followRecord.ts
frontend/src/types/order.ts
frontend/docx_front/README_FRONT_06250932.md
frontend/docx_front/FRONT-004_CRM核心业务模块.md
```

### 修改 (9)

```
frontend/src/views/customer/CustomerListView.vue       # 完整 CRUD
frontend/src/views/customer/CustomerDetailView.vue     # 详情+关联 Tabs
frontend/src/views/opportunity/OpportunityListView.vue # 完整 CRUD+流转
frontend/src/views/opportunity/OpportunityDetailView.vue # 详情+操作
frontend/src/views/followRecord/FollowRecordView.vue   # 完整 CRUD
frontend/src/views/order/OrderListView.vue             # 完整 CRUD+流转
frontend/src/views/order/OrderDetailView.vue           # 详情+操作
frontend/docx_front/README_FRONT.md                    # 更新至 FRONT-004
```

## 客户管理功能说明

### 列表页 (`/customer/list`)
- 筛选: 客户名称/行业/来源/等级/地区/状态/时间范围
- 表格: ID/名称/行业/来源/等级/地区/联系人/电话/负责人/状态/评分/最近跟进/创建时间
- 操作: 详情/编辑/状态/创建商机/添加跟进/删除
- 新增/编辑弹窗: 完整字段（新增含 ownerId 默认当前用户）

### 详情页 (`/customer/:id`)
- el-descriptions 展示基础信息
- el-tabs 展示关联商机/跟进/订单
- 快捷操作: 编辑客户/创建商机/添加跟进

## 商机管理功能说明

### 列表页 (`/opportunity/list`)
- 筛选: 名称/阶段/金额范围/预计成交日期范围
- 表格: ID/名称/客户/阶段(彩色标签)/金额/概率/预计成交/负责人/创建时间
- 操作: 详情/编辑/推进/赢单/输单/创建订单(仅win)/删除
- 已关闭商机(stage 5/6): 编辑/推进/赢单/输单按钮 disabled

### 详情页 (`/opportunity/:id`)
- 基础信息 + 关联跟进/订单 Tabs
- 状态操作按钮根据 stage 显隐

## 跟进记录功能说明

- 列表页 (`/follow-records`)
- 筛选: 客户ID/商机ID/跟进方式/客户意向/时间范围
- 新增弹窗: followerId 默认当前用户，opportunityId 可选
- 详情弹窗: 完整信息展示
- 删除二次确认

## 订单管理功能说明

### 列表页 (`/order/list`)
- 筛选: 订单编号/客户ID/商机ID/状态/金额范围/时间范围
- 表格: ID/编号/客户/商机/金额/状态(彩色标签)/支付方式/时间/负责人
- 按钮按 status 显隐控制:
  - status=1: 付款+取消+编辑+删除
  - status=2: 发货+取消(删除disabled)
  - status=3: 完成
  - status=4/5: 终态无操作(编辑disabled)

### 详情页 (`/order/:id`)
- 基础信息展示（含物流单号/公司）
- 操作按钮按 status 显隐

## 类型定义说明

| 文件 | 类型数量 | 关键类型 |
|------|:---:|------|
| `customer.ts` | 7 | CustomerVO, CustomerDetailVO, CustomerQuery, CustomerCreateDTO, CustomerUpdateDTO, CustomerStatusDTO, CustomerNameCheckResult |
| `opportunity.ts` | 8 | OpportunityVO, OpportunityDetailVO, OpportunityQuery, OpportunityCreateDTO, OpportunityUpdateDTO, OpportunityStageDTO, OpportunityWinDTO, OpportunityLoseDTO |
| `followRecord.ts` | 4 | FollowRecordVO, FollowRecordDetailVO, FollowRecordQuery, FollowRecordCreateDTO |
| `order.ts` | 9 | OrderVO, OrderDetailVO, OrderQuery, OrderCreateDTO, OrderUpdateDTO, OrderStatusDTO, OrderPayDTO, OrderShipDTO, OrderCancelDTO |

## API 对接说明

API 模块 FRONT-001 已完整定义，FRONT-004 未修改 API 文件，直接使用现有函数。

| API 模块 | 函数数 | 状态 |
|---------|:---:|:---:|
| `api/customer.ts` | 10 | 完整（无变化） |
| `api/opportunity.ts` | 10 | 完整（无变化） |
| `api/followRecord.ts` | 4 | 完整（无变化） |
| `api/order.ts` | 10 | 完整（无变化） |

## 下拉选择策略

- ownerId/followerId: 默认当前登录用户 `authStore.user?.id`
- customerId: 使用 `el-input-number` 手动输入
- opportunityId: 使用 `el-input-number` 手动输入
- 不使用 `/system/users` 接口（避免 SALES 用户 403）

## 权限验证说明

- 所有业务页面 `meta.roles` 未设置（FRONT-001 默认），ADMIN/SALES 均可访问
- SALES 用户仍然不能访问 `/system/*` 路由
- 业务表单中 ownerId/followerId 默认当前用户，不调用 ADMIN-only 接口

## 验证命令和结果

### 1. `npm run type-check`
```
✅ 通过（零错误）
```

### 2. `npm run build`
```
✅ 通过（1.18s）
Warnings: @vueuse/core #__PURE__ (third-party), chunk size (Element Plus 全量)
```

## admin 功能测试结果

| 模块 | 测试场景 | 结果 |
|------|----------|:---:|
| 客户 | 列表加载/筛选/新增/编辑/状态/删除/详情 | ✅ |
| 客户 | 详情页关联商机/跟进/订单展示 | ✅ |
| 商机 | 列表加载/筛选/新增/编辑/删除 | ✅ |
| 商机 | 阶段推进/赢单/输单(loseReason必填) | ✅ |
| 商机 | 已关闭商机按钮禁用 | ✅ |
| 商机 | 详情页关联跟进/订单展示 | ✅ |
| 跟进 | 列表加载/筛选/新增/详情/删除 | ✅ |
| 订单 | 列表加载/筛选/新增/编辑/删除 | ✅ |
| 订单 | 付款/发货/完成/取消 | ✅ |
| 订单 | status≥2 删除disabled / 终态按钮隐藏 | ✅ |
| 订单 | 详情页基础信息展示 | ✅ |

## sales01 功能测试结果

| 测试场景 | 结果 |
|----------|:---:|
| sales01 登录 → 可访问所有业务页面 | ✅ |
| 客户/商机/跟进/订单 CRUD 正常 | ✅ |
| 系统管理菜单不可见 | ✅ |
| 直访 `/system/*` → `/403` | ✅ |
| 退出后访问业务页面 → `/login` | ✅ |

## 主业务流程验证

完整闭环可走通：
1. admin 登录 ✅
2. 客户管理 → 新增客户 ✅
3. 商机管理 → 创建商机(stage=1) ✅
4. 跟进记录 → 添加跟进 ✅
5. 商机推进(stage=2→3→4) ✅
6. 商机赢单(stage=5) ✅
7. 订单管理 → 创建订单(status=1) ✅
8. 订单付款(status=2) ✅
9. 订单发货(status=3) ✅
10. 订单完成(status=4) ✅

## 遇到的问题和解决方案

1. **TypeScript 可选类型传参错误**
   - 问题: `getLabelByValue` 接受 `string|number` 但 VO 中某些字段为 `number|undefined`
   - 解决: 使用 `?? 0` 提供默认值

2. **ownerId/followerId 下拉缺失**
   - 问题: 后端无独立的用户 options 接口，且 SALES 不能调用 `/system/users`
   - 解决: 使用 `el-input-number` 手动输入，默认值设为当前登录用户 id

3. **订单状态流转按钮控制**
   - 问题: 需要根据 status 字段精确控制按钮显隐
   - 解决: 使用 `v-if` 按 status 值精确控制每个按钮

## README 快照文件

**文件名**: `README_FRONT_06250932.md`（2026-06-25 09:32）

## 文档验证

- ✅ `README_FRONT.md` 已更新（路由/模块状态/业务页面说明/主流程）
- ✅ `README_FRONT_06250932.md` 快照已生成
- ✅ `FRONT-004_CRM核心业务模块.md` 已创建
- ✅ 根目录 `docx_front/` 未重新出现

## 下一阶段 FRONT-005 建议

1. **首页仪表盘**: 概览卡片(客户/商机/订单/跟进总数+金额) + 趋势图 + 饼图
2. **数据统计页**: 7 个 analytics 接口图表展示，时间筛选、day/month 切换
3. **ECharts 图表**: 折线图/饼图/柱状图，处理空数据
4. **全局体验优化**: 表格空状态、loading 统一、金额格式化、时间格式化
5. **全流程联调**: admin/sales01/sales02 三账号完整测试
6. **异常流程验证**: 401/403/错误Token/删除保护等
7. **前端 README 完善**: 最终交付文档
