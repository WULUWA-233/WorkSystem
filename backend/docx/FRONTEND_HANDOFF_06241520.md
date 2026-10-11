# 前端开发交接文档

## 1. 项目当前阶段

后端已完成 **DEV-001 ~ DEV-012**，涵盖完整的 CRM 核心功能。代码稳定，`mvn clean compile` 通过，接口可正常运行。

## 2. 后端技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 2.7.18 |
| 构建 | Maven | 3.8+ |
| 数据库 | MySQL | 8.0 |
| ORM | MyBatis-Plus | 3.5.5 |
| 认证 | JWT (jjwt) | 0.11.5 |
| 加密 | BCrypt | Spring Security |
| API 文档 | Knife4j (Swagger) | 4.1.0 |
| AOP | Spring AOP | Spring Boot Starter |

## 3. 后端启动方式

```bash
# 1. 进入项目
cd E:\AI\Claude\WorkSystem

# 2. 初始化数据库（手动执行！）
# 使用 MySQL 客户端执行:
#   src/main/resources/sql/schema.sql
#   src/main/resources/sql/data.sql

# 3. 配置数据库连接
# 编辑 src/main/resources/application-dev.yml
# 修改 username / password

# 4. 编译
mvn clean compile

# 5. 启动
mvn spring-boot:run

# 6. 验证
curl http://localhost:8080/api/v1/common/health
# 访问 Knife4j: http://localhost:8080/doc.html
```

## 4. 接口基础信息

- **Base URL**: `http://localhost:8080`
- **API 前缀**: `/api/v1`
- **Content-Type**: `application/json`
- **认证头**: `Authorization: Bearer {token}`
- **Token 获取**: `POST /api/v1/auth/login`
- **Token 有效期**: 7200 秒（2 小时）

## 5. 默认测试账号

| 用户名 | 密码 | 角色 | 权限范围 |
|--------|------|------|----------|
| admin | 123456 | ADMIN | 全部接口 |
| sales01 | 123456 | SALES | 业务模块（客户/商机/跟进/订单/统计） |
| sales02 | 123456 | SALES | 同 sales01 |

## 6. 统一响应格式

所有接口返回结构：

```json
{
  "code": 200,
  "message": "success",
  "data": { },
  "timestamp": 1717000000000
}
```

成功: `code=200`, `data` 有值  
失败: `code!=200`, `message` 含错误说明, `data=null`

## 7. 分页响应格式

分页接口返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "pageNo": 1,
    "pageSize": 10,
    "records": []
  },
  "timestamp": 1717000000000
}
```

Query 参数: `pageNo`(默认1), `pageSize`(默认10, 最大100)

## 8. 通用错误码

| Code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | 校验失败、JSON 格式错误 |
| 401 | 未登录/Token无效 | 无 Token、Token 过期、Token 错误 |
| 403 | 无权限 | 角色不匹配（如 SALES 访问 ADMIN 接口） |
| 404 | 数据不存在 | 查询的 ID 不存在或已删除 |
| 1001 | 业务错误 | 通用业务异常 |
| 1002 | 参数校验错误 | 字段校验失败 |
| 1003 | 数据未找到 | 指定数据不存在 |
| 1004 | 重复数据 | 唯一键冲突 |
| 1005 | 操作失败 | 业务规则阻止（如删除内置角色） |

## 9. 前端登录流程

```
1. 用户输入 username + password
2. POST /api/v1/auth/login  { "username": "admin", "password": "123456" }
3. 成功 → 保存 response.data.accessToken
4. 后续所有请求设置 Header: Authorization: Bearer {accessToken}
5. 获取当前用户: GET /api/v1/auth/me
6. 退出: POST /api/v1/auth/logout（或前端直接清除 Token）
7. Token 过期（401）→ 跳转登录页
8. 403 → 显示"无权限访问"
```

## 10. 权限矩阵

| 路径前缀 | 所需角色 | 说明 |
|----------|----------|------|
| `/api/v1/common/health` | 无 | 健康检查 |
| `/api/v1/auth/login` | 无 | 登录 |
| `/api/v1/auth/me` | 任意已登录 | 当前用户 |
| `/api/v1/auth/logout` | 任意已登录 | 退出 |
| `/api/v1/system/users/**` | ADMIN | 用户管理 |
| `/api/v1/system/roles/**` | ADMIN | 角色管理 |
| `/api/v1/system/operation-logs/**` | ADMIN | 操作日志 |
| `/api/v1/customer/**` | ADMIN, SALES | 客户/跟进 |
| `/api/v1/sales/opportunities/**` | ADMIN, SALES | 商机 |
| `/api/v1/sales/orders/**` | ADMIN, SALES | 订单 |
| `/api/v1/analytics/**` | ADMIN, SALES | 数据统计 |

## 11. 已实现模块清单

| 模块 | 接口数 | 状态 |
|------|:---:|:---:|
| 登录认证 | 3 | ✅ |
| 用户管理 | 8 | ✅ |
| 角色管理 | 6 | ✅ |
| 客户管理 | 7 | ✅ |
| 商机管理 | 9 | ✅ |
| 跟进记录 | 6 | ✅ |
| 订单管理 | 12 | ✅ |
| 操作日志 | 3 | ✅ |
| 数据统计 | 7 | ✅ |
| 健康检查 | 1 | ✅ |
| **合计** | **62** | |

## 12. 未实现模块清单

**以下模块不存在，请勿开发对接页面：**

- ❌ 合同管理
- ❌ 报价管理
- ❌ 发票/退款管理
- ❌ 文件上传/附件
- ❌ AI 智能助手
- ❌ 营销管理
- ❌ 工单/服务管理
- ❌ Excel/PDF 导出
- ❌ 消息通知
- ❌ 权限管理（菜单/按钮级 RBAC 未实现，目前仅角色级控制）
- ❌ 前端页面（当前项目为纯后端，无前端资源）

## 13. 前端建议页面结构

| 页面 | 路由建议 | 权限 |
|------|----------|:---:|
| 登录页 | `/login` | 公开 |
| 首页仪表盘 | `/dashboard` | ADMIN/SALES |
| 用户管理 | `/system/users` | ADMIN |
| 角色管理 | `/system/roles` | ADMIN |
| 客户列表 | `/customer/list` | ADMIN/SALES |
| 客户详情 | `/customer/:id` | ADMIN/SALES |
| 商机列表 | `/opportunity/list` | ADMIN/SALES |
| 商机详情 | `/opportunity/:id` | ADMIN/SALES |
| 跟进记录 | `/follow-records` | ADMIN/SALES |
| 订单列表 | `/order/list` | ADMIN/SALES |
| 订单详情 | `/order/:id` | ADMIN/SALES |
| 操作日志 | `/system/operation-logs` | ADMIN |
| 数据统计 | `/analytics` | ADMIN/SALES |

## 14. 页面与接口对应关系

| 页面 | 主要接口 |
|------|----------|
| 登录页 | POST `/auth/login` |
| 首页仪表盘 | GET `/analytics/overview`, `/analytics/customers/new`, `/analytics/opportunities/stage`, `/analytics/orders/status`, `/analytics/sales/performance` |
| 用户管理 | GET/POST/PUT/DELETE/PATCH `/system/users` |
| 角色管理 | GET/POST/PUT/DELETE `/system/roles`, GET `/system/roles/options` |
| 客户列表 | GET/POST/PUT/DELETE/PATCH `/customer/customers` |
| 客户详情 | GET `/customer/customers/:id`, GET `/customer/customers/:id/opportunities`, GET `/customer/customers/:id/follow-records`, GET `/customer/customers/:id/orders` |
| 商机列表 | GET/POST/PUT/DELETE/PATCH `/sales/opportunities` |
| 商机详情 | GET `/sales/opportunities/:id`, GET `/sales/opportunities/:id/follow-records`, GET `/sales/opportunities/:id/orders` |
| 跟进记录 | GET/POST/DELETE `/customer/follow-records` |
| 订单列表 | GET/POST/PUT/DELETE/PATCH `/sales/orders` |
| 订单详情 | GET `/sales/orders/:id` |
| 操作日志 | GET/DELETE `/system/operation-logs` |
| 数据统计 | GET `/analytics/*` (7 个接口) |

## 15. 主要业务流程

```
登录 → 客户录入 → 创建商机(stage=1) → 添加跟进记录(更新客户/商机 follow_time)
  → 商机阶段推进(1→2→3→4) → 商机赢单(stage=5)
    → 创建订单(status=1) → 付款(2) → 发货(3) → 完成(4)
      → 数据统计查看业绩
```

## 16. 前端开发注意事项

1. **Token 管理**: 所有受保护接口必须带 `Authorization: Bearer {token}`
2. **401 处理**: 收到 401 → 清除本地 Token → 跳转登录页
3. **403 处理**: 收到 403 → 显示"无权限访问"，不跳转
4. **分页参数**: 统一使用 `pageNo` / `pageSize`（不是 page/pageNum）
5. **金额字段**: BigDecimal 类型，JSON 中为数字，前端用 `number` 处理，注意精度
6. **时间格式**: `yyyy-MM-dd HH:mm:ss`，日期字段 `yyyy-MM-dd`
7. **逻辑删除**: 用户/客户/商机/订单删除接口均为逻辑删除（deleted=1），不可逆（目前）
8. **操作日志**: 所有写操作自动被 AOP 记录到 sys_operation_log，只读查询操作不记录
9. **敏感字段**: password 不会出现在任何查询响应中；操作日志中 password/token 已脱敏为 `******`
10. **Knife4j**: 启动后访问 `http://localhost:8080/doc.html` 可直接查看和测试所有接口
