# CRM Backend Optimization Report - 2026-06-25 16:20

## Scope

本次优化聚焦后端代码的安全边界、配置可维护性、列表查询性能和参数错误可见性。前端未做代码修改。

## Changed Files

| File | Change |
|---|---|
| `src/main/java/com/crm/config/SecurityConfig.java` | 将原来的 `anyRequest().permitAll()` 改为 Spring Security 路径级授权 |
| `src/main/java/com/crm/config/JwtAuthenticationFilter.java` | Token 校验后加载用户角色，写入 `LoginUserContext` 和 Spring Security 上下文 |
| `src/main/java/com/crm/interceptor/AdminInterceptor.java` | 优先复用登录上下文中的角色，避免重复查询 |
| `src/main/java/com/crm/utils/JwtUtils.java` | JWT 密钥与过期时间改为从配置读取 |
| `src/main/resources/application.yml` | 新增 `app.jwt.secret` 和 `app.jwt.expiration-seconds` 配置 |
| `src/main/java/com/crm/utils/DateParseUtils.java` | 新增统一日期解析工具，非法日期返回参数错误 |
| `src/main/java/com/crm/service/impl/CrmCustomerServiceImpl.java` | 优化客户列表 owner 名称查询；日期参数不再静默吞错 |
| `src/main/java/com/crm/service/impl/CrmOrderServiceImpl.java` | 优化订单列表客户、商机、负责人名称查询；日期参数不再静默吞错 |
| `src/main/java/com/crm/service/impl/CrmFollowRecordServiceImpl.java` | 跟进记录日期参数不再静默吞错 |
| `src/main/java/com/crm/service/impl/CrmOpportunityServiceImpl.java` | 商机日期参数不再静默吞错 |

## Security Improvements

1. Spring Security 现在负责基础接口授权：
   - `/api/v1/common/health`、`/api/v1/auth/login`、Swagger 相关路径放行。
   - `/api/v1/system/**` 需要 `ADMIN`。
   - `/api/v1/customer/**`、`/api/v1/sales/**`、`/api/v1/analytics/**` 需要 `ADMIN` 或 `SALES`。
   - 其他接口要求已认证。

   Spring Security 的 401/403 也已改为项目统一 JSON 响应，避免默认错误响应格式和前端拦截器不一致。

2. JWT Filter 在认证成功后会：
   - 从 token 解析 `userId` 和 `username`。
   - 查询当前用户角色。
   - 写入 `LoginUserContext`。
   - 写入 `SecurityContextHolder`，让 Spring Security 能基于角色判断权限。
   - 请求结束后清理 ThreadLocal 和 SecurityContext。

3. JWT 配置支持环境变量：
   - `JWT_SECRET`
   - `JWT_EXPIRATION_SECONDS`

开发环境仍保留默认值，避免本地启动中断；生产环境建议必须显式设置 `JWT_SECRET`。

## Performance Improvements

1. 客户列表原先每条记录调用一次 `sysUserMapper.selectById(ownerId)` 获取负责人名称。本次改为批量查询负责人并用 Map 组装。

2. 订单列表原先每条记录分别查询客户、商机、负责人名称。本次改为批量查询：
   - `customerMapper.selectBatchIds(...)`
   - `opportunityMapper.selectBatchIds(...)`
   - `sysUserMapper.selectBatchIds(...)`

这能显著减少分页列表接口的 SQL 次数，尤其是 pageSize 接近 100 时。

## Validation Improvements

原代码中多处日期解析失败后直接忽略，例如筛选条件传错时接口仍返回成功，但查询条件失效。现在统一通过 `DateParseUtils` 处理：

| Type | Required Format |
|---|---|
| Date | `yyyy-MM-dd` |
| DateTime | `yyyy-MM-dd HH:mm:ss` |

非法格式会抛出 `BusinessException(ResultCode.PARAM_ERROR)`，由全局异常处理器返回统一错误响应。

## Verification

已执行：

```bash
mvn test
```

结果：

```text
BUILD SUCCESS
No tests to run.
```

说明：当前项目没有测试用例，验证结果只能证明编译通过，不能证明业务行为已被测试覆盖。

## Remaining Recommendations

1. 为认证、权限、订单状态流转、商机赢单/输单、日期参数错误补集成测试。
2. 继续优化商机列表、跟进列表等其他列表接口的 N+1 查询。
3. 将后端迁移到独立 `backend/` 目录，形成 `backend/` + `frontend/` 的清晰 monorepo 结构。
4. 将 DTO 的范围校验前移到 Bean Validation，例如 `@Min`、`@Max`、`@DecimalMin`、`@Email`、`@Size`。
5. 将订单状态、商机阶段、客户状态抽成后端 enum，并让前端枚举从统一契约维护。
6. 修复 README 与阶段文档的编码乱码问题，统一为 UTF-8。
