# 前端枚举与字段说明

## 1. 用户状态

| 值 | 含义 | 说明 |
|:---:|------|------|
| 0 | 禁用 | 禁用后无法登录 |
| 1 | 启用 | 默认值 |

## 2. 性别

| 值 | 含义 |
|:---:|------|
| 0 | 未知 |
| 1 | 男 |
| 2 | 女 |

## 3. 角色编码

| 编码 | 名称 | 说明 |
|------|------|------|
| ADMIN | 系统管理员 | 全部权限 |
| SALES | 销售人员 | 业务模块权限 |
| USER | 普通用户 | 基础权限（暂无对应接口） |

## 4. 客户状态

| 值 | 含义 | 说明 |
|:---:|------|------|
| 0 | 无效 | 不再跟进 |
| 1 | 有效 | 正常跟进中 |
| 2 | 公海 | 公有池客户 |

## 5. 客户等级

| 值 | 含义 |
|:---:|------|
| 1 | 普通客户 |
| 2 | 潜力客户 |
| 3 | 重要客户 |
| 4 | 核心客户 |
| 5 | 战略客户 |

## 6. 商机阶段

| 值 | 含义 | 说明 |
|:---:|------|------|
| 1 | 初步接触 | 新建商机默认 |
| 2 | 需求确认 | /stage 推进 |
| 3 | 方案报价 | /stage 推进 |
| 4 | 商务谈判 | /stage 推进 |
| 5 | 赢单 | /win 接口设置 |
| 6 | 输单 | /lose 接口设置 |

**规则**: 新建只能 1-4，/stage 只能 1-4，5/6 必须用 /win /lose

## 7. 跟进客户意向

| 值 | 含义 |
|:---:|------|
| 1 | 低 |
| 2 | 中 |
| 3 | 高 |
| 4 | 强烈 |

## 8. 订单状态

| 值 | 含义 | 说明 |
|:---:|------|------|
| 1 | 待付款 | 新建默认 |
| 2 | 已付款 | /pay 接口 |
| 3 | 已发货 | /ship 接口 |
| 4 | 已完成 | /complete 接口 |
| 5 | 已取消 | /cancel 接口(仅1/2可取消) |

**流转**: 1→2→3→4, 1/2→5  
**终态**: 4/5 不可再变更

## 9. 操作日志状态

| 值 | 含义 |
|:---:|------|
| 0 | 失败 |
| 1 | 成功 |

## 10. 常见字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键，自增 |
| `createdAt` | String(datetime) | 创建时间 `yyyy-MM-dd HH:mm:ss` |
| `updatedAt` | String(datetime) | 更新时间 |
| `deleted` | Integer | 逻辑删除: 0=正常, 1=已删除（API 不返回 deleted=1 的数据） |
| `status` | Integer | 状态字段，含义因模块而异 |
| `remark` | String | 备注 |
| `ownerId` | Long | 负责人用户 ID |
| `ownerName` | String | 负责人姓名（查询时附带） |
| `customerId` | Long | 客户 ID |
| `customerName` | String | 客户名称（查询时附带） |
| `opportunityId` | Long | 商机 ID |
| `opportunityName` | String | 商机名称（查询时附带） |
| `amount` | BigDecimal | 金额，前端按 number 处理注意小数精度 |
| `probability` | Integer | 概率 0-100（%） |
| `score` | Integer | 评分 0-100 |
| `stage` | Integer | 商机阶段 1-6 |
| `stageName` | String | 阶段中文名（查询时附带） |
| `statusName` | String | 状态中文名（查询时附带） |
| `level` | Integer | 等级 1-5 |
| `gender` | Integer | 性别 0/1/2 |
| `sort` | Integer | 排序值 |
| `pageNo` | Long | 分页-当前页（默认1） |
| `pageSize` | Long | 分页-每页条数（默认10，最大100） |
| `total` | Long | 分页-总记录数 |
| `customerIntention` | Integer | 客户意向 1-4 |
| `followType` | String | 跟进方式（电话/微信/邮件/拜访/会议/其他） |
| `followTime` | String(datetime) | 跟进时间 |
| `lastFollowTime` | String(datetime) | 最近跟进时间 |
| `nextFollowTime` | String(datetime) | 下次跟进时间 |
| `paymentMethod` | String | 支付方式 |
| `paymentTime` | String(datetime) | 付款时间 |
| `deliveryTime` | String(datetime) | 发货时间 |
| `finishTime` | String(datetime) | 完成时间 |
| `cancelTime` | String(datetime) | 取消时间 |
| `winTime` | String(datetime) | 赢单时间 |
| `closeTime` | String(datetime) | 关闭时间 |
| `orderNo` | String | 订单编号（自动生成 ORD+时间戳+随机数） |
| `accessToken` | String | JWT Token |
| `expiresIn` | Long | Token 过期秒数（7200） |
| `tokenType` | String | Token 类型（固定 "Bearer"） |
| `roles` | String[] | 角色编码列表 |
| `costTime` | Long | 操作耗时（毫秒） |
| `ipAddress` | String | 客户端 IP |
| `userAgent` | String | 浏览器 UA |
| `errorMessage` | String | 错误信息（操作日志失败时） |
| `moduleName` | String | 操作模块（操作日志） |
| `operationType` | String | 操作类型（操作日志） |
| `operationDesc` | String | 操作描述（操作日志） |
