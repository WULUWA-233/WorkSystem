# WorkSystem — CRM 实训项目

基于 Spring Boot 和 Vue 的客户关系管理系统，包含客户、商机、跟进记录、订单、数据统计，以及用户、角色和操作日志管理。

项目仍处于实训完善阶段。目前已修复一批接口、权限、业务状态和前端交互问题，并完成本地自动化验证；尚未完成服务器与真实 MySQL 数据库的完整联调。

## 技术与目录

| 目录 | 内容 |
| --- | --- |
| `backend/` | Java 17、Spring Boot 2.7.18、MyBatis-Plus、Spring Security、JWT、MySQL |
| `frontend/` | Vue 3、TypeScript、Vite、Pinia、Element Plus、ECharts |
| `backend/src/main/resources/sql/` | 数据表与实训演示数据脚本 |
| `backend/docx/`、`frontend/docx_front/` | 开发过程中保留的阶段文档 |

本地验证使用 JDK 21、Maven 3.8.8 和 Node.js 24；后端编译目标为 Java 17。前端安装版本以 `package-lock.json` 为准。

## 本地启动

准备 MySQL 8 数据库。先阅读 `backend/src/main/resources/sql/schema.sql` 与 `data.sql`，再在独立的实训数据库中初始化；`schema.sql` 包含删除并重建表的语句。项目默认不自动执行初始化脚本。

后端默认使用 `dev` 配置。通过环境变量覆盖本地连接信息，例如 PowerShell：

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://localhost:3306/crm_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8'
$env:SPRING_DATASOURCE_USERNAME = '你的数据库用户名'
$env:SPRING_DATASOURCE_PASSWORD = '你的数据库密码'
cd backend
mvn spring-boot:run
```

前端在另一个终端运行：

```powershell
cd frontend
npm ci
npm run dev
```

前端默认地址为 `http://localhost:5173`，后端为 `http://localhost:8080`。开发服务器将 `/api` 请求代理给后端；需要调整目标时，可在 `frontend/.env.development.local` 设置 `VITE_BACKEND_TARGET`。

## 生产配置

启用 `prod` 配置时，必须设置 `DB_PASSWORD` 和 `JWT_SECRET`，不提供生产密码或签名密钥的默认值。`JWT_SECRET` 应为随机生成、至少 32 字节的秘密值。数据库账号可通过 `DB_USERNAME` 设置；连接地址可通过 `SPRING_DATASOURCE_URL` 覆盖。

```powershell
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:DB_USERNAME = '你的数据库用户名'
$env:DB_PASSWORD = '你的数据库密码'
$env:JWT_SECRET = '替换为你自己的随机密钥，至少32字节'
```

`npm run build` 生成前端 `dist/`。部署时需配置 Web 服务器，将 `/api` 转发到后端；Vite 的开发代理不会随静态文件部署。

## 自动化验证

```powershell
cd backend
mvn test
```

```powershell
cd frontend
npm ci
npm test
npm run type-check
npm run build
```

当前修复版本已通过后端 84 项测试、前端 29 项测试、TypeScript 类型检查和前端生产构建。后端测试使用模拟依赖，不代表真实数据库或服务器联调已经通过。

## 本次修复

- 校正订单与商机状态流转、无请求体接口、日期校验、分页和统计范围。
- 加强并发更新与关联删除检查，修复跟进时间回写和日志序列化。
- 修复账号停用、密码变更后的令牌失效处理，以及前端登录状态和请求竞态。
- 移除明文密码记忆，补充覆盖业务边界和权限行为的测试。

令牌校验规则已有调整，旧版本登录会话需要重新登录。阶段文档可能描述较早版本，当前行为应结合源码与测试确认。

GitHub 版本使用清理后的源码建立提交历史，保留了开发与演示配置，未包含本地生产凭据、旧敏感提交、依赖目录、构建产物和个人压缩包。
