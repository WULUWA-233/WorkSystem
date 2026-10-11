# 前端页面与接口映射表

> 所有接口 Base URL: `http://localhost:8080`  
> 认证头: `Authorization: Bearer {token}`  
> Content-Type: `application/json`

## 1. 登录页

| 接口 | 方法 | 路径 | 请求参数 | 返回 |
|------|------|------|----------|------|
| 登录 | POST | `/api/v1/auth/login` | `{username, password}` | `{accessToken, tokenType:"Bearer", expiresIn:7200, user:{id,username,realName,roles}}` |

**跳转**: 成功 → 首页 `/dashboard`，失败 → 显示 `message`

## 2. 首页仪表盘

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 概览 | GET | `/api/v1/analytics/overview` | 客户/商机/订单/跟进总数+金额 |
| 客户趋势 | GET | `/api/v1/analytics/customers/new?groupBy=day` | 新增客户趋势 |
| 商机阶段 | GET | `/api/v1/analytics/opportunities/stage` | 6 阶段 count+amount |
| 订单状态 | GET | `/api/v1/analytics/orders/status` | 5 状态 count+amount |
| 销售业绩 | GET | `/api/v1/analytics/sales/performance` | 按销售人员统计 |
| 行业分布 | GET | `/api/v1/analytics/customers/industry` | 饼图数据 |
| 地区分布 | GET | `/api/v1/analytics/customers/region` | 饼图数据 |

**权限**: ADMIN / SALES

## 3. 用户管理页

**权限**: **仅 ADMIN**

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/api/v1/system/users?pageNo=&pageSize=&username=&realName=&phone=&status=` | 查询参数可选 |
| 用户详情 | GET | `/api/v1/system/users/{id}` | 含角色列表 |
| 新增用户 | POST | `/api/v1/system/users` | Body: `{username,password,realName,nickname,phone,email,gender,status,roleIds,remark}` |
| 修改用户 | PUT | `/api/v1/system/users/{id}` | Body: `{realName,nickname,phone,email,gender,status,remark}` |
| 删除用户 | DELETE | `/api/v1/system/users/{id}` | 逻辑删除，不可删 admin/自己 |
| 启用/禁用 | PATCH | `/api/v1/system/users/{id}/status` | Body: `{status:0|1}` |
| 重置密码 | PATCH | `/api/v1/system/users/{id}/password/reset` | Body: `{newPassword}` |
| 分配角色 | PUT | `/api/v1/system/users/{id}/roles` | Body: `{roleIds:[1,2]}` |

## 4. 角色管理页

**权限**: **仅 ADMIN**

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/api/v1/system/roles?pageNo=&pageSize=&roleName=&roleCode=&status=` | |
| 下拉选项 | GET | `/api/v1/system/roles/options` | 返回 `[{id,roleName,roleCode}]` |
| 角色详情 | GET | `/api/v1/system/roles/{id}` | |
| 新增角色 | POST | `/api/v1/system/roles` | Body: `{roleName,roleCode,description,status,sort,remark}` |
| 修改角色 | PUT | `/api/v1/system/roles/{id}` | Body: `{roleName,description,status,sort,remark}` |
| 删除角色 | DELETE | `/api/v1/system/roles/{id}` | 不可删 ADMIN(1)/SALES(2)，有用户绑定时不可删 |

## 5. 客户管理页

**权限**: ADMIN / SALES

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/api/v1/customer/customers?pageNo=&pageSize=&customerName=&industry=&source=&level=&region=&status=&ownerId=&startTime=&endTime=` | |
| 客户详情 | GET | `/api/v1/customer/customers/{id}` | 含 ownerName |
| 新增客户 | POST | `/api/v1/customer/customers` | Body: `{customerName,industry,source,level,region,address,contactName,contactPhone,contactEmail,ownerId,status,score,remark}` |
| 修改客户 | PUT | `/api/v1/customer/customers/{id}` | |
| 删除客户 | DELETE | `/api/v1/customer/customers/{id}` | 逻辑删除 |
| 修改状态 | PATCH | `/api/v1/customer/customers/{id}/status` | Body: `{status:0|1|2}` |
| 名称查重 | GET | `/api/v1/customer/customers/check-name?customerName=&excludeId=` | 返回 `{exists:true|false}` |

**客户详情页额外关联查询**:
| 客户商机 | GET | `/api/v1/customer/customers/{id}/opportunities` | |
| 客户跟进 | GET | `/api/v1/customer/customers/{id}/follow-records` | |
| 客户订单 | GET | `/api/v1/customer/customers/{id}/orders` | |

## 6. 商机管理页

**权限**: ADMIN / SALES

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/api/v1/sales/opportunities?pageNo=&pageSize=&customerId=&opportunityName=&stage=&ownerId=&minAmount=&maxAmount=&expectedStartDate=&expectedEndDate=` | |
| 商机详情 | GET | `/api/v1/sales/opportunities/{id}` | |
| 新增商机 | POST | `/api/v1/sales/opportunities` | stage 仅 1-4 |
| 修改商机 | PUT | `/api/v1/sales/opportunities/{id}` | 已关闭不可修改 |
| 删除商机 | DELETE | `/api/v1/sales/opportunities/{id}` | 逻辑删除 |
| 阶段推进 | PATCH | `/api/v1/sales/opportunities/{id}/stage` | Body: `{stage:1-4,probability}` |
| 赢单 | PATCH | `/api/v1/sales/opportunities/{id}/win` | Body: `{remark}` |
| 输单 | PATCH | `/api/v1/sales/opportunities/{id}/lose` | Body: `{loseReason,remark}` loseReason 必填 |
| 客户商机 | GET | `/api/v1/customer/customers/{id}/opportunities` | |

## 7. 跟进记录页

**权限**: ADMIN / SALES

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/api/v1/customer/follow-records?pageNo=&pageSize=&customerId=&opportunityId=&followerId=&followType=&customerIntention=&startTime=&endTime=` | |
| 新增跟进 | POST | `/api/v1/customer/follow-records` | Body: `{customerId,opportunityId?,followType,followContent,followResult,customerIntention,nextPlan,nextFollowTime,followTime,followerId,remark}` |
| 跟进详情 | GET | `/api/v1/customer/follow-records/{id}` | |
| 删除跟进 | DELETE | `/api/v1/customer/follow-records/{id}` | 逻辑删除 |
| 客户跟进 | GET | `/api/v1/customer/customers/{id}/follow-records` | |
| 商机跟进 | GET | `/api/v1/sales/opportunities/{id}/follow-records` | |

## 8. 订单管理页

**权限**: ADMIN / SALES

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/api/v1/sales/orders?pageNo=&pageSize=&orderNo=&customerId=&opportunityId=&status=&paymentMethod=&ownerId=&minAmount=&maxAmount=&startTime=&endTime=` | |
| 订单详情 | GET | `/api/v1/sales/orders/{id}` | |
| 新增订单 | POST | `/api/v1/sales/orders` | 仅赢单商机可创建，orderNo 自动生成 |
| 修改订单 | PUT | `/api/v1/sales/orders/{id}` | 已关闭不可修改 |
| 删除订单 | DELETE | `/api/v1/sales/orders/{id}` | status≥2 不可删 |
| 状态修改 | PATCH | `/api/v1/sales/orders/{id}/status` | Body: `{status:1-5}` |
| 付款 | PATCH | `/api/v1/sales/orders/{id}/pay` | 1→2, Body: `{paymentMethod}` |
| 发货 | PATCH | `/api/v1/sales/orders/{id}/ship` | 2→3, Body: `{logisticsNo,logisticsCompany}` |
| 完成 | PATCH | `/api/v1/sales/orders/{id}/complete` | 3→4 |
| 取消 | PATCH | `/api/v1/sales/orders/{id}/cancel` | 1/2→5 |
| 客户订单 | GET | `/api/v1/customer/customers/{id}/orders` | |
| 商机订单 | GET | `/api/v1/sales/opportunities/{id}/orders` | |

## 9. 操作日志页

**权限**: **仅 ADMIN**

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/api/v1/system/operation-logs?pageNo=&pageSize=&username=&moduleName=&operationType=&requestMethod=&status=&startTime=&endTime=` | 无 requestParams/responseResult（列表精简） |
| 日志详情 | GET | `/api/v1/system/operation-logs/{id}` | 含 requestParams/responseResult/userAgent |
| 删除日志 | DELETE | `/api/v1/system/operation-logs/{id}` | 物理删除 |

## 10. 数据统计页

**权限**: ADMIN / SALES

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 概览 | GET | `/api/v1/analytics/overview?startTime=&endTime=` | |
| 客户趋势 | GET | `/api/v1/analytics/customers/new?startDate=&endDate=&groupBy=day|month` | |
| 商机阶段 | GET | `/api/v1/analytics/opportunities/stage?startTime=&endTime=` | |
| 订单状态 | GET | `/api/v1/analytics/orders/status?startTime=&endTime=` | |
| 销售业绩 | GET | `/api/v1/analytics/sales/performance?ownerId=` | |
| 行业分布 | GET | `/api/v1/analytics/customers/industry?startTime=&endTime=` | |
| 地区分布 | GET | `/api/v1/analytics/customers/region?startTime=&endTime=` | |
