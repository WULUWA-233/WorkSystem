# DEV-004 登录认证与 Token 拦截开发说明

## 1. 本阶段完成内容

- ✅ 用户账号密码登录（BCrypt 校验）
- ✅ 用户状态校验（禁用/删除检测）
- ✅ JWT Token 生成（7200秒有效期，Bearer 格式）
- ✅ JWT Token 解析与验证
- ✅ 获取当前登录用户信息
- ✅ 退出登录接口
- ✅ 请求认证拦截（OncePerRequestFilter）
- ✅ 未登录访问受保护接口返回 401（统一 JSON 格式）
- ✅ 健康检查、登录、Swagger 文档路径放行
- ✅ 登录成功更新 last_login_time 和 last_login_ip

## 2. 新增文件清单

| 序号 | 文件路径 | 类型 | 说明 |
|------|----------|------|------|
| 1 | `src/main/java/com/crm/mapper/SysUserMapper.java` | Mapper | 用户查询（按用户名） |
| 2 | `src/main/java/com/crm/mapper/SysRoleMapper.java` | Mapper | 角色查询（按用户ID查角色） |
| 3 | `src/main/java/com/crm/mapper/SysUserRoleMapper.java` | Mapper | 用户-角色关联查询 |
| 4 | `src/main/java/com/crm/dto/LoginDTO.java` | DTO | 登录请求参数 |
| 5 | `src/main/java/com/crm/vo/LoginVO.java` | VO | 登录响应（含Token和用户信息） |
| 6 | `src/main/java/com/crm/vo/CurrentUserVO.java` | VO | 当前用户信息响应 |
| 7 | `src/main/java/com/crm/service/AuthService.java` | Service | 认证服务接口 |
| 8 | `src/main/java/com/crm/service/impl/AuthServiceImpl.java` | Service | 认证服务实现 |
| 9 | `src/main/java/com/crm/controller/AuthController.java` | Controller | 认证接口控制器 |
| 10 | `src/main/java/com/crm/config/JwtAuthenticationFilter.java` | Filter | JWT 认证拦截过滤器 |
| 11 | `src/main/java/com/crm/utils/LoginUserContext.java` | Utils | 当前用户上下文（ThreadLocal） |
| 12 | `docx/DEV-004_登录认证与Token拦截_开发说明.md` | 文档 | 本测试说明文档 |

## 3. 修改文件清单

| 序号 | 文件路径 | 修改内容 |
|------|----------|----------|
| 1 | `src/main/java/com/crm/config/SecurityConfig.java` | 注册 JwtAuthenticationFilter，添加 JWT Filter Bean |
| 2 | `src/main/java/com/crm/utils/JwtUtils.java` | 增加 7200s 过期时间常量、简化版 generateToken、validateToken 方法 |
| 3 | `README.md` | 增加登录接口说明、Token 使用方式、受保护接口说明 |

## 4. 核心类说明

### 4.1 AuthController
- **路径前缀**: `/api/v1/auth`
- **职责**: 接收登录/me/logout 请求，参数校验，调用 AuthService
- **TOKEN 获取**: 通过 `LoginUserContext.getCurrentUserId()` 获取当前用户（由 Filter 设置）

### 4.2 AuthServiceImpl
- **登录流程**:
  1. 根据 username 查询用户
  2. 校验用户状态（删除/禁用）
  3. BCrypt 密码校验
  4. 查询用户角色
  5. 生成 JWT Token
  6. 更新 last_login_time 和 last_login_ip
  7. 返回 LoginVO
- **密码校验**: 使用 `PasswordUtils.matches(raw, encoded)`
- **日志安全**: 不在日志中打印明文密码

### 4.3 JwtAuthenticationFilter
- **类型**: `OncePerRequestFilter`（每个请求只执行一次）
- **白名单**: 健康检查、登录、Swagger 文档路径直接放行
- **Token 提取**: 从 `Authorization: Bearer {token}` 提取
- **Token 验证**: 调用 `JwtUtils.validateToken()` 验证有效性
- **上下文设置**: 解析成功后调用 `LoginUserContext.setCurrentUser()` 存入 ThreadLocal
- **401 响应**: Token 缺失/无效/过期时返回统一 JSON 格式
- **清理**: `finally` 块中调用 `LoginUserContext.clear()` 防止内存泄漏

### 4.4 LoginUserContext
- **实现**: 基于 ThreadLocal 存储当前用户 ID 和用户名
- **设置**: `setCurrentUser(Long userId, String username)` — 由 Filter 调用
- **获取**: `getCurrentUserId()` — 由 Controller 调用
- **清理**: `clear()` — 请求结束后清理

### 4.5 SysUserMapper
- 仅提供认证所需查询：`selectByUsername(String username)`
- 继承 BaseMapper，但**不提供**新增/修改/删除/分页方法

### 4.6 SysRoleMapper
- 仅提供认证所需查询：`selectByUserId(Long userId)` — 联表查询用户角色
- 继承 BaseMapper，但**不提供**角色管理 CRUD

### 4.7 JwtUtils
- `generateToken(userId, username)` — 生成 Token，过期时间 7200 秒
- `validateToken(token)` — 验证 Token 是否有效（未过期、可解析）
- `getUserId(token)` — 从 Token 提取用户 ID
- `getUsername(token)` — 从 Token 提取用户名

## 5. 实现接口清单

| 接口编号 | 方法 | 路径 | 认证 | 说明 |
|----------|------|------|------|------|
| AUTH-001 | POST | `/api/v1/auth/login` | 否 | 用户登录 |
| AUTH-002 | GET | `/api/v1/auth/me` | 是 | 获取当前用户信息 |
| AUTH-003 | POST | `/api/v1/auth/logout` | 是 | 退出登录 |
| COMMON-001 | GET | `/api/v1/common/health` | 否 | 健康检查（已有） |

## 6. 数据库涉及表

| 表名 | 用途 | 查询方式 |
|------|------|----------|
| `sys_user` | 用户登录查询、状态更新 | 按 username 查询、按 id 更新 |
| `sys_role` | 用户角色查询 | 联表 sys_user_role 查询 |
| `sys_user_role` | 用户-角色关联 | 按 user_id 查询 |

**注意**: 未创建 SysUserController、SysRoleController，未实现用户/角色管理 CRUD。

## 7. 登录认证流程说明

```
用户请求 POST /api/v1/auth/login
  │
  ├─> AuthController.login()
  │     ├─> 参数校验 (@Valid LoginDTO)
  │     │     ├─ username 非空
  │     │     └─ password 非空
  │     │
  │     └─> AuthService.login(loginDTO, ip)
  │           ├─> 1. sysUserMapper.selectByUsername(username)
  │           │     └─ 用户不存在 → BusinessException("账号或密码错误")
  │           ├─> 2. 检查 deleted 字段
  │           │     └─ deleted=1 → BusinessException("账号或密码错误")
  │           ├─> 3. 检查 status 字段
  │           │     └─ status=0 → BusinessException("用户已被禁用")
  │           ├─> 4. PasswordUtils.matches(password, encoded)
  │           │     └─ 密码错误 → BusinessException("账号或密码错误")
  │           ├─> 5. sysRoleMapper.selectByUserId(userId)
  │           │     └─ 获取角色列表 → ["ADMIN"] / ["SALES"]
  │           ├─> 6. JwtUtils.generateToken(userId, username)
  │           ├─> 7. 更新 last_login_time, last_login_ip
  │           └─> 8. 返回 LoginVO { accessToken, tokenType, expiresIn, user }
  │
  └─> 返回 Result<LoginVO>
```

## 8. Token 拦截流程说明

```
用户请求任意受保护接口 (如 GET /api/v1/auth/me)
  │
  ├─> JwtAuthenticationFilter.doFilterInternal()
  │     │
  │     ├─> 检查是否为白名单路径？
  │     │     ├─ /api/v1/common/health → 放行 ✅
  │     │     ├─ /api/v1/auth/login → 放行 ✅
  │     │     ├─ /doc.html, /swagger-ui/**, ... → 放行 ✅
  │     │     └─ 否 → 继续认证
  │     │
  │     ├─> 提取 Authorization 头
  │     │     ├─ 无头或不以 "Bearer " 开头 → 401 ❌
  │     │     └─ 有 → 提取 Token
  │     │
  │     ├─> JwtUtils.validateToken(token)
  │     │     ├─ 解析失败/过期 → 401 ❌
  │     │     └─ 有效 → 继续
  │     │
  │     ├─> 提取 userId, username
  │     ├─> LoginUserContext.setCurrentUser(userId, username)
  │     ├─> filterChain.doFilter(request, response)
  │     │     └─> Controller 通过 LoginUserContext.getCurrentUserId() 获取用户
  │     └─> finally: LoginUserContext.clear()
  │
  └─> 401 响应格式:
       {
         "code": 401,
         "message": "未登录或Token无效",
         "data": null,
         "timestamp": 1717000000000
       }
```

## 9. 默认测试账号

| 用户名 | 密码 | 角色 | 状态 |
|--------|------|------|------|
| admin | 123456 | ADMIN | 启用 |
| sales01 | 123456 | SALES | 启用 |
| sales02 | 123456 | SALES | 启用 |

**密码存储**: BCrypt 加密，data.sql 中已使用 `PasswordUtils.encode("123456")` 生成密文。

## 10. 测试前准备（重要）

**在运行任何测试之前，必须先手动初始化数据库！**

项目**不会**自动执行 SQL 脚本，需要测试人员手动完成以下步骤：

### Step 1: 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS crm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE crm_db;
```

### Step 2: 执行建表脚本
```sql
SOURCE src/main/resources/sql/schema.sql;
```
或使用 MySQL 客户端（Navicat / DBeaver / MySQL Workbench）打开并执行 `schema.sql`。

### Step 3: 执行初始数据脚本
```sql
SOURCE src/main/resources/sql/data.sql;
```
或使用 MySQL 客户端打开并执行 `data.sql`。

### Step 4: 验证数据
```sql
-- 确认用户已插入
SELECT id, username, real_name, status FROM sys_user;
-- 预期：admin(1, 启用), sales01(1, 启用), sales02(1, 启用)

-- 确认角色已插入
SELECT id, role_name, role_code FROM sys_role;
-- 预期：ADMIN, SALES, USER
```

### Step 5: 确认 application-dev.yml 数据库连接配置正确
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/crm_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```
请根据实际环境修改 `username` 和 `password`。

### Step 6: 启动项目
```bash
cd E:\AI\Claude\WorkSystem
mvn clean compile
mvn spring-boot:run
```

### Step 7: 确认启动成功
```bash
curl http://localhost:8080/api/v1/common/health
# 预期返回: {"code":200,"message":"success","data":"ok","timestamp":...}
```

**⚠️ 注意：如果跳过上述数据库初始化步骤，所有登录相关测试将返回 500 错误（数据库表不存在）。**

## 11. 接口测试步骤

### 测试用例列表

| 序号 | 测试场景 | 预期结果 |
|------|----------|----------|
| 1 | admin 登录成功 | 200, 返回 Token + 用户信息 + 角色 |
| 2 | sales01 登录成功 | 200, 返回 Token + 用户信息 + 角色 |
| 3 | 错误用户名登录 | 401, "账号或密码错误" |
| 4 | 错误密码登录 | 401, "账号或密码错误" |
| 5 | 空用户名登录 | 400, 参数校验错误 |
| 6 | 空密码登录 | 400, 参数校验错误 |
| 7 | 不带 Token 请求 /me | 401, "未登录或Token缺失" |
| 8 | 错误 Token 请求 /me | 401, "未登录或Token无效" |
| 9 | 过期 Token 请求 /me | 401, "未登录或Token无效" |
| 10 | 正确 Token 请求 /me | 200, 返回当前用户信息 |
| 11 | 正确 Token 请求 /logout | 200, 返回 true |
| 12 | 健康检查（无 Token） | 200, 返回 "ok" |

## 12. curl 测试命令

### Windows PowerShell

```powershell
# 1. 健康检查（无需 Token）
curl http://localhost:8080/api/v1/common/health

# 2. admin 登录成功
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"admin\",\"password\":\"123456\"}"

# 3. sales01 登录成功
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"sales01\",\"password\":\"123456\"}"

# 4. 错误用户名登录失败
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"wronguser\",\"password\":\"123456\"}"

# 5. 错误密码登录失败
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"admin\",\"password\":\"wrongpass\"}"

# 6. 空用户名登录失败
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"\",\"password\":\"123456\"}"

# 7. 正确 Token 请求当前用户信息
curl -X GET http://localhost:8080/api/v1/auth/me `
  -H "Authorization: Bearer 替换为实际Token"

# 8. 不带 Token 请求当前用户信息（401）
curl -X GET http://localhost:8080/api/v1/auth/me

# 9. 错误 Token 请求当前用户信息（401）
curl -X GET http://localhost:8080/api/v1/auth/me `
  -H "Authorization: Bearer invalidtoken123456"

# 10. 正确 Token 退出登录
curl -X POST http://localhost:8080/api/v1/auth/logout `
  -H "Authorization: Bearer 替换为实际Token"
```

### Linux / macOS / Git Bash

```bash
# 1. 健康检查
curl http://localhost:8080/api/v1/common/health

# 2. admin 登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 3. sales01 登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"sales01","password":"123456"}'

# 4. 错误用户名
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"wronguser","password":"123456"}'

# 5. 错误密码
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpass"}'

# 6. 空用户名
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"","password":"123456"}'

# 7. 正确 Token 请求 /me
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer <实际Token>"

# 8. 无 Token 请求 /me（401）
curl -X GET http://localhost:8080/api/v1/auth/me

# 9. 错误 Token 请求 /me（401）
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer invalidtoken"

# 10. 正确 Token 退出登录
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <实际Token>"
```

## 13. 常见失败场景测试

### 13.1 账号或密码错误
```json
// Request: {"username":"admin","password":"wrongpass"}
// Response:
{
  "code": 401,
  "message": "账号或密码错误",
  "data": null,
  "timestamp": 1717000000000
}
```

### 13.2 未登录访问受保护接口
```json
// Request: GET /api/v1/auth/me (no Authorization header)
// Response:
{
  "code": 401,
  "message": "未登录或Token缺失",
  "data": null,
  "timestamp": 1717000000000
}
```

### 13.3 Token 无效或过期
```json
// Request: GET /api/v1/auth/me (with invalid/expired token)
// Response:
{
  "code": 401,
  "message": "未登录或Token无效",
  "data": null,
  "timestamp": 1717000000000
}
```

### 13.4 参数校验失败
```json
// Request: {"username":"","password":""}
// Response:
{
  "code": 400,
  "message": "用户名不能为空, 密码不能为空",
  "data": null,
  "timestamp": 1717000000000
}
```

## 14. 本阶段未做内容

- ❌ 用户管理 CRUD（新增/修改/删除/分页查询用户）
- ❌ 角色管理 CRUD（新增/修改/删除/分页查询角色）
- ❌ 权限管理接口
- ❌ RefreshToken 机制
- ❌ Token 黑名单（退出登录不主动失效 Token）
- ❌ 客户管理接口
- ❌ 商机管理接口
- ❌ 跟进记录接口
- ❌ 订单管理接口
- ❌ 操作日志 AOP
- ❌ Redis / RabbitMQ / Elasticsearch / MinIO

## 15. 下一阶段注意事项

1. **Token 黑名单**: 如需实现主动退出使 Token 失效，需要引入 Redis 存储黑名单
2. **权限控制**: 后续可基于 `sys_permission` 表实现 RBAC 权限校验
3. **RefreshToken**: 如需长期登录，建议实现 RefreshToken 机制
4. **操作日志**: 可添加 AOP 切面自动记录 `sys_operation_log`
5. **SysUserMapper**: 后续开发用户管理时可以扩展，但认证查询应保持不变
6. **SysRoleMapper**: 后续开发角色管理时可以扩展
7. **登录失败次数限制**: 建议在生产环境添加登录失败次数限制（如 5 次锁定 30 分钟）
8. **密码加密策略**: 建议在后续阶段实现密码强度校验和定期修改策略
