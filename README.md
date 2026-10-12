# WorkSystem · 客户关系管理系统

WorkSystem 是一个基于 Spring Boot 和 Vue 3 的前后端分离 CRM 系统，围绕客户信息、销售商机、跟进记录和订单组织业务流程，并提供数据统计、角色权限和操作日志功能。

## 功能模块

| 模块 | 功能 |
| --- | --- |
| 客户管理 | 客户信息维护、条件查询、状态管理及关联业务查看 |
| 商机管理 | 商机创建、阶段推进、赢单与丢单记录 |
| 跟进管理 | 客户与商机的跟进记录维护和查询 |
| 订单管理 | 订单创建、付款登记、发货与物流信息、完成和取消 |
| 数据统计 | 客户、商机、订单与销售业绩的统计展示 |
| 用户与权限 | 用户管理、角色分配和访问控制 |
| 操作日志 | 业务操作记录查询 |

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 2.7.18、MyBatis-Plus、Spring Security、JWT |
| 前端 | Vue 3、TypeScript、Vite、Pinia、Element Plus |
| 数据存储 | MySQL 8 |
| 图表展示 | ECharts |
| 接口文档 | Knife4j / Swagger |
| 构建工具 | Maven、npm |

## 项目结构

```text
WorkSystem/
├── backend/
│   ├── src/main/java/com/crm/    # 接口、业务逻辑、权限与数据访问
│   ├── src/main/resources/      # 应用配置、映射文件与 SQL 脚本
│   ├── src/test/                # 后端测试
│   └── pom.xml
├── frontend/
│   ├── src/                     # 页面、组件、路由、状态与接口封装
│   ├── tests/                   # 前端测试
│   └── package.json
└── README.md
```

## 本地运行

环境：Java 17 或以上、Maven 3.8 或以上、Node.js 24、MySQL 8。

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

## 部署配置

后端通过环境变量设置数据库连接和 JWT 签名密钥。启用 `prod` 配置时，设置 `DB_PASSWORD` 和 `JWT_SECRET`；`JWT_SECRET` 使用随机生成、至少 32 字节的秘密值。数据库账号通过 `DB_USERNAME` 设置，连接地址通过 `SPRING_DATASOURCE_URL` 设置。

```powershell
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:DB_USERNAME = '你的数据库用户名'
$env:DB_PASSWORD = '你的数据库密码'
$env:JWT_SECRET = '替换为你自己的随机密钥，至少32字节'
```

前端执行 `npm run build` 生成 `dist/`，由 Web 服务器提供静态文件，并将 `/api` 请求转发到后端。
