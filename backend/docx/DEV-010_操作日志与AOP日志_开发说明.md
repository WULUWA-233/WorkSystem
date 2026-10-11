# DEV-010 操作日志与 AOP 日志开发说明

## 1. 本阶段完成内容

- ✅ `@OperationLog` 注解（module/type/description）
- ✅ `OperationLogAspect` AOP 切面（自动拦截标注方法）
- ✅ 成功日志记录（status=1, cost_time, response）
- ✅ 失败日志记录（status=0, error_message, 重新抛出异常）
- ✅ 敏感信息脱敏（password/newPassword/token/accessToken → ******）
- ✅ 字段长度控制（params 2000/result 4000/error 1000/agent 500）
- ✅ IP 获取（X-Forwarded-For → X-Real-IP → RemoteAddr）
- ✅ 日志记录失败不影响主业务（try-catch + warn log）
- ✅ 操作日志分页查询（8 条件：username/moduleName/operationType/method/status/timeRange）
- ✅ 操作日志详情（含 requestParams/responseResult/userAgent）
- ✅ 删除单条日志（物理删除）
- ✅ 29 个写操作方法添加 `@OperationLog`
- ✅ ADMIN 权限控制（操作日志接口纳入 AdminInterceptor）

## 2. 新增文件清单

| 序号 | 文件 | 类型 |
|------|------|------|
| 1 | `annotation/OperationLog.java` | Annotation |
| 2 | `aspect/OperationLogAspect.java` | AOP Aspect |
| 3 | `dto/OperationLogQueryDTO.java` | DTO |
| 4 | `vo/OperationLogVO.java` | VO |
| 5 | `vo/OperationLogDetailVO.java` | VO |
| 6 | `mapper/SysOperationLogMapper.java` | Mapper |
| 7 | `service/SysOperationLogService.java` | Interface |
| 8 | `service/impl/SysOperationLogServiceImpl.java` | Service |
| 9 | `controller/OperationLogController.java` | Controller |
| 10 | `docx/DEV-010_操作日志与AOP日志_开发说明.md` | 文档 |

## 3. 修改文件清单

| 文件 | 修改内容 |
|------|----------|
| `pom.xml` | 新增 `spring-boot-starter-aop` 依赖 |
| `config/WebMvcConfig.java` | AdminInterceptor 增加 `/api/v1/system/operation-logs/**` |
| `controller/AuthController.java` | login/logout 添加 @OperationLog |
| `controller/SysUserController.java` | 6 个写方法添加 @OperationLog |
| `controller/SysRoleController.java` | 3 个写方法添加 @OperationLog |
| `controller/CustomerController.java` | 4 个写方法添加 @OperationLog |
| `controller/OpportunityController.java` | 6 个写方法添加 @OperationLog |
| `controller/FollowRecordController.java` | 2 个写方法添加 @OperationLog |
| `controller/OrderController.java` | 8 个写方法添加 @OperationLog |
| `README.md` | API 表 + Project Structure |

## 4. 核心类说明

### OperationLogAspect
- `@Around("@annotation(com.crm.annotation.OperationLog)")` 拦截所有标注方法
- 记录开始时间 → 执行方法 → 捕获异常/成功 → 计算耗时 → 保存日志
- `RequestContextHolder` 获取 HttpServletRequest
- `LoginUserContext` 获取当前用户
- 敏感字段正则替换：`"password":"***"` → `"password":"******"`
- try-catch 包裹日志写入，失败仅 warn

### OperationLogController
- 3 个端点：分页/详情/删除
- 路径：`/api/v1/system/operation-logs`
- AdminInterceptor 保护（仅 ADMIN）

## 5. 实现接口清单

| 编号 | 方法 | 路径 | 权限 |
|------|------|------|:--:|
| LOG-001 | GET | `/api/v1/system/operation-logs` | ADMIN |
| LOG-002 | GET | `/api/v1/system/operation-logs/{id}` | ADMIN |
| LOG-003 | DELETE | `/api/v1/system/operation-logs/{id}` | ADMIN |

## 6. 数据库涉及表

| 表 | 操作 |
|------|------|
| `sys_operation_log` | 插入（AOP 自动）、查询、物理删除 |

## 7. AOP 日志记录规则

- 标注 `@OperationLog` 的方法自动记录
- 写操作必须记录，查询操作不记录
- 健康检查、Swagger 路径不记录
- 操作日志查询接口自身不记录
- 成功: status=1, error_message=null
- 失败: status=0, error_message=异常message, 异常继续抛出

## 8. 脱敏规则

| 敏感字段 | 处理 |
|----------|------|
| password, oldPassword, newPassword, confirmPassword | "******" |
| token, accessToken, refreshToken | "******" |
| authorization | "******" |

## 9. 权限控制

```
/api/v1/system/operation-logs/** → AdminInterceptor → ADMIN only
```

## 10-12. 测试

| 账号 | 角色 | 日志访问 |
|------|------|:---:|
| admin | ADMIN | ✅ |
| sales01 | SALES | ❌ 403 |

```bash
TOKEN="<admin-token>"
# 分页
curl "http://localhost:8080/api/v1/system/operation-logs?pageNo=1&pageSize=10" -H "Authorization: Bearer $TOKEN"
# 详情
curl http://localhost:8080/api/v1/system/operation-logs/1 -H "Authorization: Bearer $TOKEN"
# 删除
curl -X DELETE http://localhost:8080/api/v1/system/operation-logs/1 -H "Authorization: Bearer $TOKEN"
# AOP 验证：执行任意写操作后检查 sys_operation_log 表
```

## 13. 本阶段未做内容

- ❌ 批量删除 / 清空日志
- ❌ Excel 导出
- ❌ 数据统计
- ❌ 异步日志（当前同步写入）

## 14. README 快照

`docx/README_06241125.md`

## 15. 下一阶段注意事项

1. 可引入异步队列避免日志写入阻塞业务
2. 可增加日志归档/定期清理
3. 统计报表模块可基于已有数据开发
