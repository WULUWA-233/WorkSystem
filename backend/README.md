# CRM System

Customer Relationship Management System based on Spring Boot.

## Tech Stack

| Category       | Technology                     | Version      |
|---------------|--------------------------------|--------------|
| Language      | Java                           | 17           |
| Framework     | Spring Boot                    | 2.7.18       |
| Build Tool    | Maven                          | 3.8+         |
| ORM           | MyBatis-Plus                   | 3.5.5        |
| Database      | MySQL                          | 8.0          |
| API Docs      | Knife4j (Swagger)             | 4.1.0        |
| Auth          | JWT + BCrypt                   | jjwt 0.11.5  |
| Utils         | Lombok                         | 1.18.30      |

## Project Structure

```
src/main/java/com/crm/
├── CrmApplication.java          # Main entry point
├── annotation/                  # Custom annotations
│   └── OperationLog.java        # AOP operation log annotation
├── aspect/                      # AOP aspects
│   └── OperationLogAspect.java  # Auto-log write operations
├── common/                      # Common base classes
│   ├── Result.java              # Unified API response
│   ├── ResultCode.java          # Response status codes
│   ├── PageQuery.java           # Pagination query params
│   ├── PageResult.java          # Pagination response
│   ├── BusinessException.java   # Business exception
│   └── GlobalExceptionHandler.java  # Global exception handler
├── config/                      # Configuration classes
│   ├── SecurityConfig.java      # Spring Security + JWT filter config
│   ├── JwtAuthenticationFilter.java  # JWT token authentication filter
│   ├── MyBatisPlusConfig.java   # MyBatis-Plus config
│   ├── MetaObjectHandlerConfig.java  # Auto-fill timestamps
│   ├── WebMvcConfig.java        # Interceptor registration
│   └── Knife4jConfig.java        # Swagger/Knife4j config
├── controller/                  # REST controllers
│   ├── AuthController.java      # Login / me / logout
│   ├── HealthController.java    # Health check endpoint
│   ├── SysUserController.java   # User management (ADMIN)
│   ├── SysRoleController.java   # Role management (ADMIN)
│   ├── CustomerController.java  # Customer management (ADMIN/SALES)
│   ├── OpportunityController.java # Opportunity management (ADMIN/SALES)
│   ├── FollowRecordController.java # Follow record management (ADMIN/SALES)
│   ├── OrderController.java      # Order management (ADMIN/SALES)
│   ├── OperationLogController.java # Operation log (ADMIN)
│   └── AnalyticsController.java    # Analytics (ADMIN/SALES)
├── entity/                      # Database entities (10 tables)
│   ├── SysUser.java             # sys_user
│   ├── SysRole.java             # sys_role
│   ├── SysUserRole.java         # sys_user_role
│   ├── SysPermission.java       # sys_permission
│   ├── SysRolePermission.java   # sys_role_permission
│   ├── SysOperationLog.java     # sys_operation_log
│   ├── CrmCustomer.java         # crm_customer
│   ├── CrmOpportunity.java      # crm_opportunity
│   ├── CrmFollowRecord.java     # crm_follow_record
│   └── CrmOrder.java            # crm_order
├── dto/                         # Data Transfer Objects
│   ├── LoginDTO.java            # Login request
│   ├── UserCreateDTO.java       # Create user request
│   ├── UserUpdateDTO.java       # Update user request
│   ├── UserQueryDTO.java        # User query params
│   ├── UserStatusDTO.java       # User status toggle
│   ├── UserPasswordResetDTO.java # Password reset request
│   ├── UserRoleAssignDTO.java   # Role assignment request
│   ├── RoleCreateDTO.java       # Create role request
│   ├── RoleUpdateDTO.java       # Update role request
│   ├── RoleQueryDTO.java        # Role query params
│   ├── CustomerCreateDTO.java   # Create customer request
│   ├── CustomerUpdateDTO.java   # Update customer request
│   ├── CustomerQueryDTO.java    # Customer query params
│   ├── CustomerStatusDTO.java   # Customer status change
│   ├── OpportunityCreateDTO.java # Create opportunity request
│   ├── OpportunityUpdateDTO.java # Update opportunity request
│   ├── OpportunityQueryDTO.java  # Opportunity query params
│   ├── OpportunityStageDTO.java  # Stage advance request
│   ├── OpportunityLoseDTO.java   # Win/Lose request
│   ├── FollowRecordCreateDTO.java # Create follow request
│   ├── FollowRecordQueryDTO.java  # Follow query params
│   ├── OrderCreateDTO.java        # Create order request
│   ├── OrderUpdateDTO.java        # Update order request
│   ├── OrderQueryDTO.java         # Order query params
│   ├── OrderStatusDTO.java        # Status change request
│   ├── OrderPayDTO.java           # Pay request
│   ├── OrderShipDTO.java          # Ship request
│   ├── OrderCancelDTO.java        # Cancel request
│   ├── OperationLogQueryDTO.java   # Log query params
│   └── AnalyticsQueryDTO.java      # Analytics query params
├── vo/                          # View Objects
│   ├── LoginVO.java             # Login response
│   ├── CurrentUserVO.java       # Current user response
│   ├── UserVO.java              # User list item
│   ├── UserDetailVO.java        # User detail
│   ├── RoleVO.java              # Role item
│   ├── RoleOptionVO.java        # Role dropdown option
│   ├── CustomerVO.java          # Customer list item
│   ├── CustomerDetailVO.java    # Customer detail
│   ├── OpportunityVO.java        # Opportunity list item
│   ├── OpportunityDetailVO.java  # Opportunity detail
│   ├── FollowRecordVO.java       # Follow record list item
│   ├── FollowRecordDetailVO.java # Follow record detail
│   ├── OrderVO.java               # Order list item
│   ├── OrderDetailVO.java         # Order detail
│   ├── OperationLogVO.java        # Log list item
│   ├── OperationLogDetailVO.java  # Log detail
│   ├── OverviewVO.java            # Dashboard overview
│   ├── TrendItemVO.java           # Trend data point
│   ├── StageStatsVO.java          # Stage distribution
│   ├── OrderStatusStatsVO.java    # Order status distribution
│   ├── SalesPerformanceVO.java    # Sales performance
│   └── NameValueVO.java           # Industry/Region stat
├── mapper/                      # MyBatis Mappers
│   ├── SysUserMapper.java       # User queries
│   ├── SysRoleMapper.java       # Role queries
│   ├── SysUserRoleMapper.java   # User-Role queries
│   ├── CrmCustomerMapper.java   # Customer queries
│   ├── CrmOpportunityMapper.java # Opportunity queries
│   ├── CrmFollowRecordMapper.java # Follow record queries
│   ├── CrmOrderMapper.java        # Order queries
│   ├── SysOperationLogMapper.java  # Operation log queries
│   └── AnalyticsMapper.java        # Analytics aggregation queries
├── service/                     # Service interfaces
│   ├── AuthService.java         # Authentication
│   ├── SysUserService.java      # User management
│   ├── SysRoleService.java      # Role management
│   ├── CrmCustomerService.java  # Customer management
│   ├── CrmOpportunityService.java # Opportunity management
│   ├── CrmFollowRecordService.java # Follow record management
│   ├── CrmOrderService.java      # Order management
│   ├── SysOperationLogService.java # Operation log management
│   ├── AnalyticsService.java     # Analytics queries
│   └── impl/                    # Service implementations
│       ├── AuthServiceImpl.java
│       ├── SysUserServiceImpl.java
│       ├── SysRoleServiceImpl.java
│       ├── CrmCustomerServiceImpl.java
│       ├── CrmOpportunityServiceImpl.java
│       ├── CrmFollowRecordServiceImpl.java
│       ├── CrmOrderServiceImpl.java
│       ├── SysOperationLogServiceImpl.java
│       └── AnalyticsServiceImpl.java
├── interceptor/                 # Interceptors
│   ├── AdminInterceptor.java    # ADMIN-only (system routes)
│   └── CustomerPermissionInterceptor.java  # ADMIN/SALES (customer+opportunity)
└── utils/                       # Utility classes
    ├── PasswordUtils.java       # BCrypt encoder/matcher
    ├── JwtUtils.java            # JWT token generator/parser
    └── LoginUserContext.java    # ThreadLocal current user
docx/                             # Documentation
├── API接口文档_DEV-012.md        # Complete API reference
├── FRONTEND_HANDOFF_06241520.md  # Frontend handoff guide
├── FRONTEND_PAGE_API_MAP_06241520.md  # Page-API mapping
├── FRONTEND_ENUMS_AND_FIELDS_06241520.md # Enums & fields
├── DEV-001~012_*.md              # Phase development docs
└── README_*.md                   # README snapshots
```

## Prerequisites

- **JDK 17** or later
- **Maven 3.8+**
- **MySQL 8.0+**

## Quick Start

### 1. Clone / Enter Project Directory

```bash
cd E:\AI\Claude\WorkSystem\backend
```

### 2. Initialize Database ⚠️ MANDATORY

**The database must be manually initialized before starting the application.** The project does NOT auto-execute SQL scripts.

Execute the SQL scripts in MySQL:

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS crm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE crm_db;

-- Run schema (creates all tables)
SOURCE src/main/resources/sql/schema.sql;

-- Run initial data (inserts users, roles, assignments)
SOURCE src/main/resources/sql/data.sql;
```

Or import the SQL files using any MySQL client (Navicat, DBeaver, MySQL Workbench, etc.).

Verify the data was inserted:
```sql
SELECT id, username, real_name FROM sys_user;
-- Expected: 管理员, 张伟, 李娜
SELECT id, role_name, role_code FROM sys_role;
-- Expected: ADMIN, SALES, USER
```

### 3. Configure Database Connection

Edit `src/main/resources/application-dev.yml` and update the database connection:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/crm_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true
    username: root      # Change to your MySQL username
    password: 123456    # Change to your MySQL password
```

### 4. Build & Run

```bash
# Compile
mvn clean compile

# Run (development mode)
mvn spring-boot:run

# Or package and run
mvn clean package -DskipTests
java -jar target/crm-system-1.0.0-SNAPSHOT.jar
```

### 5. Verify

Access the health check endpoint:

```bash
curl http://localhost:8080/api/v1/common/health
```

Expected response:

```json
{
  "code": 200,
  "message": "success",
  "data": "ok",
  "timestamp": 1717000000000
}
```

### 6. Login & Authentication

Login to get a JWT token:

```bash
# Windows PowerShell
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"管理员\",\"password\":\"123456\"}"

# Linux / macOS / Git Bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"管理员","password":"123456"}'
```

Response:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "管理员",
      "realName": "系统管理员",
      "roles": ["ADMIN"]
    }
  },
  "timestamp": 1717000000000
}
```

### 7. Access Protected Endpoints

All endpoints except health check, login, and doc paths require a Bearer token:

```bash
# Get current user info (replace <token> with actual token from login)
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer <token>"

# Logout
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <token>"
```

Without a valid token, protected endpoints return:
```json
{
  "code": 401,
  "message": "Not logged in or token invalid",
  "data": null,
  "timestamp": 1717000000000
}
```

### 8. API Documentation

Once the application is running, access Knife4j Swagger UI at:

```
http://localhost:8080/doc.html
```

## Default Accounts

All passwords are BCrypt-encrypted in the database. Default plain-text password for all users is: **123456**

| Username | Real Name    | Role          |
|----------|-------------|---------------|
| 管理员 | 系统管理员 | ADMIN |
| 张伟 | 张伟 | SALES |
| 李娜 | 李娜 | SALES |

### Regenerate BCrypt Hashes

If you need to regenerate the BCrypt password hashes, run:

```bash
# In IDE: Run com.crm.utils.PasswordUtils.main()

# Or via command line:
cd E:\AI\Claude\WorkSystem\backend
mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt -q
java -cp "target\classes;<paste-classpath-from-classpath.txt>" com.crm.utils.PasswordUtils
```

Then copy the generated hash to `data.sql`.

## Database Tables

### System Tables (sys_)

| Table              | Description              |
|-------------------|--------------------------|
| sys_user          | System users             |
| sys_role          | System roles             |
| sys_user_role     | User-role assignments    |
| sys_permission    | Permissions (menus/APIs) |
| sys_role_permission | Role-permission assignments |
| sys_operation_log | Operation audit log      |

### Business Tables (crm_)

| Table              | Description     |
|-------------------|-----------------|
| crm_customer      | Customers       |
| crm_opportunity   | Opportunities   |
| crm_follow_record | Follow records  |
| crm_order         | Orders          |

## Configuration Files

| File                  | Purpose                     |
|----------------------|-----------------------------|
| application.yml      | Common configuration        |
| application-dev.yml  | Development environment     |
| application-prod.yml | Production environment      |

Default active profile: **dev**

## API Endpoints Summary

### Auth & Common

| Method | Path | Auth | Role | Description |
|--------|------|:---:|------|-------------|
| GET | `/api/v1/common/health` | No | - | Health check |
| POST | `/api/v1/auth/login` | No | - | User login |
| GET | `/api/v1/auth/me` | Yes | Any | Current user info |
| POST | `/api/v1/auth/logout` | Yes | Any | User logout |

### User Management (ADMIN only)

| Method | Path | Auth | Role | Description |
|--------|------|:---:|------|-------------|
| GET | `/api/v1/system/users` | Yes | ADMIN | User pagination |
| GET | `/api/v1/system/users/{id}` | Yes | ADMIN | User detail |
| POST | `/api/v1/system/users` | Yes | ADMIN | Create user |
| PUT | `/api/v1/system/users/{id}` | Yes | ADMIN | Update user |
| DELETE | `/api/v1/system/users/{id}` | Yes | ADMIN | Delete user (logic) |
| PATCH | `/api/v1/system/users/{id}/status` | Yes | ADMIN | Enable/disable |
| PATCH | `/api/v1/system/users/{id}/password/reset` | Yes | ADMIN | Reset password |
| PUT | `/api/v1/system/users/{id}/roles` | Yes | ADMIN | Assign roles |

### Role Management (ADMIN only)

| Method | Path | Auth | Role | Description |
|--------|------|:---:|------|-------------|
| GET | `/api/v1/system/roles` | Yes | ADMIN | Role pagination |
| GET | `/api/v1/system/roles/options` | Yes | ADMIN | Role dropdown |
| GET | `/api/v1/system/roles/{id}` | Yes | ADMIN | Role detail |
| POST | `/api/v1/system/roles` | Yes | ADMIN | Create role |
| PUT | `/api/v1/system/roles/{id}` | Yes | ADMIN | Update role |
| DELETE | `/api/v1/system/roles/{id}` | Yes | ADMIN | Delete role |

### Customer Management (ADMIN / SALES)

| Method | Path | Auth | Role | Description |
|--------|------|:---:|------|-------------|
| GET | `/api/v1/customer/customers` | Yes | ADMIN/SALES | Customer pagination |
| GET | `/api/v1/customer/customers/{id}` | Yes | ADMIN/SALES | Customer detail |
| POST | `/api/v1/customer/customers` | Yes | ADMIN/SALES | Create customer |
| PUT | `/api/v1/customer/customers/{id}` | Yes | ADMIN/SALES | Update customer |
| DELETE | `/api/v1/customer/customers/{id}` | Yes | ADMIN/SALES | Delete customer (logic) |
| PATCH | `/api/v1/customer/customers/{id}/status` | Yes | ADMIN/SALES | Status change (0/1/2) |
| GET | `/api/v1/customer/customers/check-name` | Yes | ADMIN/SALES | Check name duplicate |

### Opportunity Management (ADMIN / SALES)

| Method | Path | Auth | Role | Description |
|--------|------|:---:|------|-------------|
| GET | `/api/v1/sales/opportunities` | Yes | ADMIN/SALES | Opportunity pagination |
| GET | `/api/v1/sales/opportunities/{id}` | Yes | ADMIN/SALES | Opportunity detail |
| POST | `/api/v1/sales/opportunities` | Yes | ADMIN/SALES | Create opportunity |
| PUT | `/api/v1/sales/opportunities/{id}` | Yes | ADMIN/SALES | Update opportunity |
| DELETE | `/api/v1/sales/opportunities/{id}` | Yes | ADMIN/SALES | Delete opportunity |
| PATCH | `/api/v1/sales/opportunities/{id}/stage` | Yes | ADMIN/SALES | Advance stage (1-4) |
| PATCH | `/api/v1/sales/opportunities/{id}/win` | Yes | ADMIN/SALES | Mark as won |
| PATCH | `/api/v1/sales/opportunities/{id}/lose` | Yes | ADMIN/SALES | Mark as lost |
| GET | `/api/v1/customer/customers/{cid}/opportunities` | Yes | ADMIN/SALES | List by customer |

### Follow Record Management (ADMIN / SALES)

| Method | Path | Auth | Role | Description |
|--------|------|:---:|------|-------------|
| GET | `/api/v1/customer/follow-records` | Yes | ADMIN/SALES | Follow pagination |
| POST | `/api/v1/customer/follow-records` | Yes | ADMIN/SALES | Create follow record |
| GET | `/api/v1/customer/follow-records/{id}` | Yes | ADMIN/SALES | Follow detail |
| DELETE | `/api/v1/customer/follow-records/{id}` | Yes | ADMIN/SALES | Delete follow record |
| GET | `/api/v1/customer/customers/{cid}/follow-records` | Yes | ADMIN/SALES | Follows by customer |
| GET | `/api/v1/sales/opportunities/{oid}/follow-records` | Yes | ADMIN/SALES | Follows by opportunity |

### Order Management (ADMIN / SALES)

| Method | Path | Auth | Role | Description |
|--------|------|:---:|------|-------------|
| GET | `/api/v1/sales/orders` | Yes | ADMIN/SALES | Order pagination |
| GET | `/api/v1/sales/orders/{id}` | Yes | ADMIN/SALES | Order detail |
| POST | `/api/v1/sales/orders` | Yes | ADMIN/SALES | Create order |
| PUT | `/api/v1/sales/orders/{id}` | Yes | ADMIN/SALES | Update order |
| DELETE | `/api/v1/sales/orders/{id}` | Yes | ADMIN/SALES | Delete order |
| PATCH | `/api/v1/sales/orders/{id}/status` | Yes | ADMIN/SALES | Update status |
| PATCH | `/api/v1/sales/orders/{id}/pay` | Yes | ADMIN/SALES | Pay order |
| PATCH | `/api/v1/sales/orders/{id}/ship` | Yes | ADMIN/SALES | Ship order |
| PATCH | `/api/v1/sales/orders/{id}/complete` | Yes | ADMIN/SALES | Complete order |
| PATCH | `/api/v1/sales/orders/{id}/cancel` | Yes | ADMIN/SALES | Cancel order |
| GET | `/api/v1/customer/customers/{cid}/orders` | Yes | ADMIN/SALES | Orders by customer |
| GET | `/api/v1/sales/opportunities/{oid}/orders` | Yes | ADMIN/SALES | Orders by opportunity |

### Operation Log (ADMIN only)

| Method | Path | Auth | Role | Description |
|--------|------|:---:|------|-------------|
| GET | `/api/v1/system/operation-logs` | Yes | ADMIN | Log pagination |
| GET | `/api/v1/system/operation-logs/{id}` | Yes | ADMIN | Log detail |
| DELETE | `/api/v1/system/operation-logs/{id}` | Yes | ADMIN | Delete log |

### Analytics (ADMIN / SALES)

| Method | Path | Auth | Role | Description |
|--------|------|:---:|------|-------------|
| GET | `/api/v1/analytics/overview` | Yes | ADMIN/SALES | Overview dashboard |
| GET | `/api/v1/analytics/customers/new` | Yes | ADMIN/SALES | Customer trend |
| GET | `/api/v1/analytics/opportunities/stage` | Yes | ADMIN/SALES | Stage distribution |
| GET | `/api/v1/analytics/orders/status` | Yes | ADMIN/SALES | Order status distribution |
| GET | `/api/v1/analytics/sales/performance` | Yes | ADMIN/SALES | Sales performance |
| GET | `/api/v1/analytics/customers/industry` | Yes | ADMIN/SALES | Industry distribution |
| GET | `/api/v1/analytics/customers/region` | Yes | ADMIN/SALES | Region distribution |

### User Management Quick Test

```bash
# Login as 管理员 first, then use the token
curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"管理员","password":"123456"}'

# Create user
curl -X POST http://localhost:8080/api/v1/system/users -H "Content-Type: application/json" -H "Authorization: Bearer <adminToken>" -d '{"username":"王强","password":"123456","realName":"王强","roleIds":[2]}'

# List users
curl "http://localhost:8080/api/v1/system/users?pageNo=1&pageSize=10" -H "Authorization: Bearer <adminToken>"

# Sales user cannot access system management → 403
curl "http://localhost:8080/api/v1/system/users?pageNo=1&pageSize=10" -H "Authorization: Bearer <salesToken>"
```

## Frontend Handoff / 前端交接说明

前端开发请按以下顺序阅读交接文档：

| 序号 | 文档 | 路径 |
|:---:|------|------|
| 1 | 项目 README | `README.md` (本文件) |
| 2 | 前端交接总文档 | `docx/FRONTEND_HANDOFF_06241520.md` |
| 3 | 页面-接口映射表 | `docx/FRONTEND_PAGE_API_MAP_06241520.md` |
| 4 | 枚举与字段说明 | `docx/FRONTEND_ENUMS_AND_FIELDS_06241520.md` |
| 5 | 完整 API 文档 | `docx/API接口文档_DEV-012.md` |
| 6 | Swagger UI | `http://localhost:8080/doc.html` |

**快速链接**: 启动项目后访问 `http://localhost:8080/doc.html` 可直接查看和测试所有接口。

## Notes

- Current phase: **DEV-001 ~ DEV-012** — complete CRM system with API documentation (Swagger/Knife4j)
- Contract and invoice modules are **NOT** implemented yet
- **API Docs:** http://localhost:8080/doc.html (Knife4j/Swagger UI)
- JWT Token expires in **7200 seconds (2 hours)**
- Protected endpoints require `Authorization: Bearer {token}` header
- System management endpoints (`/api/v1/system/**`) require **ADMIN** role
- Non-ADMIN users receive **403 Forbidden** on system management endpoints
- Logout does not invalidate the token server-side (MVP — no token blacklist)
- Redis, RabbitMQ, Elasticsearch, MinIO are **NOT** included yet
