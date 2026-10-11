-- =====================================================
-- CRM acceptance sample data
-- Purpose: Chinese demo data for frontend display and manual validation.
-- Safe to run repeatedly. System identifiers such as role_code, status,
-- stage and order status keep their original system values.
-- =====================================================

SET NAMES utf8mb4;
USE crm_db;

-- Ensure built-in roles exist.
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`, `sort`, `remark`) VALUES
(1, '系统管理员', 'ADMIN', '拥有系统全部管理权限', 1, 1, '系统内置角色'),
(2, '销售顾问', 'SALES', '负责客户、商机、跟进和订单管理', 1, 2, '系统内置角色'),
(3, '普通用户', 'USER', '基础访问身份', 1, 3, '系统内置角色')
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `sort` = VALUES(`sort`),
  `remark` = VALUES(`remark`),
  `deleted` = 0;

-- Clear previous sample data by fixed ID ranges.
DELETE FROM `crm_order` WHERE `id` BETWEEN 101 AND 199 OR `order_no` LIKE 'YS2026%';
DELETE FROM `crm_follow_record` WHERE `id` BETWEEN 101 AND 199;
DELETE FROM `crm_opportunity` WHERE `id` BETWEEN 101 AND 199;
DELETE FROM `crm_customer` WHERE `id` BETWEEN 101 AND 199;
DELETE FROM `sys_user_role` WHERE `user_id` BETWEEN 101 AND 199;
DELETE FROM `sys_user` WHERE `id` BETWEEN 101 AND 199 OR `username` IN ('xiaoming', 'wangfang', 'chenhao', 'liuyan');

-- Password for all sample users: 123456
INSERT INTO `sys_user`
(`id`, `username`, `password`, `real_name`, `nickname`, `phone`, `email`, `gender`, `status`, `remark`) VALUES
(101, 'xiaoming', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '赵小明', '赵经理', '13801011001', 'zhaoxiaoming@crm.local', 1, 1, '华北区域销售负责人'),
(102, 'wangfang', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '王芳', '王顾问', '13801011002', 'wangfang@crm.local', 2, 1, '企业客户销售顾问'),
(103, 'chenhao', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '陈浩', '陈主管', '13801011003', 'chenhao@crm.local', 1, 1, '华东区域销售主管'),
(104, 'liuyan', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '刘妍', '刘助理', '13801011004', 'liuyan@crm.local', 2, 1, '运营支持普通用户');

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(101, 2),
(102, 2),
(103, 2),
(104, 3);

INSERT INTO `crm_customer`
(`id`, `customer_name`, `industry`, `source`, `level`, `region`, `address`, `contact_name`, `contact_phone`, `contact_email`, `owner_id`, `status`, `score`, `last_follow_time`, `next_follow_time`, `created_by`, `updated_by`, `created_at`, `updated_at`, `remark`) VALUES
(101, '北京云启科技有限公司', '软件服务', '官网咨询', '5', '北京海淀', '北京市海淀区中关村南大街12号', '宋晨', '13901012001', 'songchen@yunqi.example', 101, 1, 94, '2026-06-24 10:20:00', '2026-06-29 14:00:00', 101, 101, '2026-05-08 09:30:00', '2026-06-24 10:20:00', '重点推进企业级CRM升级'),
(102, '上海澜海制造集团', '智能制造', '客户转介绍', '4', '上海浦东', '上海市浦东新区金科路2889号', '顾婷婷', '13901012002', 'gutingting@lanhai.example', 103, 1, 89, '2026-06-23 15:40:00', '2026-06-28 10:30:00', 103, 103, '2026-05-15 10:10:00', '2026-06-23 15:40:00', '关注销售流程和生产订单协同'),
(103, '广州花城食品有限公司', '食品饮料', '展会线索', '3', '广东广州', '广州市天河区珠江新城华夏路16号', '林嘉怡', '13901012003', 'linjiayi@huacheng.example', 102, 1, 83, '2026-06-22 11:30:00', '2026-06-27 16:00:00', 102, 102, '2026-05-20 13:45:00', '2026-06-22 11:30:00', '计划先做销售团队试点'),
(104, '深圳启航医疗科技有限公司', '医疗健康', '电话咨询', '2', '广东深圳', '深圳市南山区科技园科苑路88号', '何俊', '13901012004', 'hejun@qihang.example', 102, 2, 72, NULL, '2026-07-02 09:30:00', 102, 102, '2026-06-02 16:00:00', '2026-06-02 16:00:00', '暂入公海，等待重新分配'),
(105, '成都锦程教育咨询中心', '教育培训', '广告投放', '3', '四川成都', '成都市高新区天府三街199号', '唐雨', '13901012005', 'tangyu@jincheng.example', 101, 1, 78, '2026-06-21 14:10:00', '2026-06-26 15:30:00', 101, 101, '2026-06-03 11:20:00', '2026-06-21 14:10:00', '预算中等，关注部署周期'),
(106, '杭州星河零售有限公司', '零售', '搜索引擎', '2', '浙江杭州', '杭州市滨江区江南大道3888号', '马欣', '13901012006', 'maxin@xinghe-retail.example', 103, 1, 75, '2026-06-25 09:15:00', '2026-06-30 10:00:00', 103, 103, '2026-06-05 15:30:00', '2026-06-25 09:15:00', '门店会员管理需求明确');

INSERT INTO `crm_opportunity`
(`id`, `customer_id`, `opportunity_name`, `amount`, `stage`, `probability`, `expected_date`, `owner_id`, `source`, `lose_reason`, `win_time`, `close_time`, `last_follow_time`, `next_follow_time`, `created_by`, `updated_by`, `created_at`, `updated_at`, `remark`) VALUES
(101, 101, '云启科技CRM升级项目', 360000.00, '4', 80, '2026-07-18', 101, '官网咨询', NULL, NULL, NULL, '2026-06-24 10:20:00', '2026-06-29 14:00:00', 101, 101, '2026-05-09 10:00:00', '2026-06-24 10:20:00', '已完成二轮方案评审'),
(102, 102, '澜海制造销售流程数字化项目', 520000.00, '3', 65, '2026-08-05', 103, '客户转介绍', NULL, NULL, NULL, '2026-06-23 15:40:00', '2026-06-28 10:30:00', 103, 103, '2026-05-16 09:40:00', '2026-06-23 15:40:00', '需补充ERP对接说明'),
(103, 103, '花城食品客户跟进试点项目', 128000.00, '2', 45, '2026-07-28', 102, '展会线索', NULL, NULL, NULL, '2026-06-22 11:30:00', '2026-06-27 16:00:00', 102, 102, '2026-05-22 13:10:00', '2026-06-22 11:30:00', '客户希望先小范围试点'),
(104, 105, '锦程教育招生线索管理项目', 96000.00, '5', 100, '2026-06-20', 101, '广告投放', NULL, '2026-06-20 11:20:00', '2026-06-20 11:20:00', '2026-06-21 14:10:00', NULL, 101, 101, '2026-06-04 10:30:00', '2026-06-21 14:10:00', '已赢单，进入实施准备'),
(105, 104, '启航医疗售后服务管理项目', 210000.00, '6', 0, '2026-06-24', 102, '电话咨询', '客户今年预算冻结，计划下季度重新评估', NULL, '2026-06-24 17:30:00', NULL, NULL, 102, 102, '2026-06-02 15:00:00', '2026-06-24 17:30:00', '保留下季度跟进机会'),
(106, 106, '星河零售会员运营管理项目', 180000.00, '1', 25, '2026-08-12', 103, '搜索引擎', NULL, NULL, NULL, '2026-06-25 09:15:00', '2026-06-30 10:00:00', 103, 103, '2026-06-05 16:00:00', '2026-06-25 09:15:00', '待安排产品演示'),
(107, 101, '云启科技移动端销售助手项目', 145000.00, '5', 100, '2026-06-25', 101, '官网咨询', NULL, '2026-06-25 16:00:00', '2026-06-25 16:00:00', '2026-06-24 10:20:00', NULL, 101, 101, '2026-06-10 10:20:00', '2026-06-25 16:00:00', '移动端销售助手已确认采购');

INSERT INTO `crm_follow_record`
(`id`, `customer_id`, `opportunity_id`, `follow_type`, `follow_content`, `follow_result`, `customer_intention`, `next_plan`, `next_follow_time`, `follow_time`, `follower_id`, `created_by`, `updated_by`, `created_at`, `updated_at`, `remark`) VALUES
(101, 101, 101, '会议', '与宋晨及采购负责人确认CRM升级范围，重点讨论销售漏斗、订单联动和权限管理。', '客户认可二期实施计划，需要补充最终报价明细。', 4, '发送正式报价单并约采购评审会', '2026-06-29 14:00:00', '2026-06-24 10:20:00', 101, 101, 101, '2026-06-24 10:35:00', '2026-06-24 10:35:00', '重点商机'),
(102, 102, 102, '电话', '回访顾婷婷，沟通制造集团销售流程和订单审批节点。', '客户要求增加与ERP系统的数据同步说明。', 3, '补充ERP对接方案并邮件发送', '2026-06-28 10:30:00', '2026-06-23 15:40:00', 103, 103, 103, '2026-06-23 15:45:00', '2026-06-23 15:45:00', NULL),
(103, 103, 103, '微信', '向林嘉怡发送客户跟进试点功能清单，并确认试点部门人数。', '客户反馈销售一部先试用，预计20人。', 3, '安排线上演示', '2026-06-27 16:00:00', '2026-06-22 11:30:00', 102, 102, 102, '2026-06-22 11:45:00', '2026-06-22 11:45:00', NULL),
(104, 105, 104, '拜访', '现场拜访唐雨，确认招生线索管理、跟进提醒和统计看板需求。', '客户确认标准版可满足当前需求。', 4, '准备合同和实施排期', NULL, '2026-06-21 14:10:00', 101, 101, 101, '2026-06-21 14:20:00', '2026-06-21 14:20:00', '已赢单'),
(105, 104, 105, '电话', '与何俊沟通售后服务管理项目预算和上线窗口。', '客户确认预算冻结，建议下季度再联系。', 1, '下季度重新跟进预算情况', NULL, '2026-06-24 17:10:00', 102, 102, 102, '2026-06-24 17:20:00', '2026-06-24 17:20:00', '输单原因已记录'),
(106, 106, 106, '邮件', '向马欣发送零售会员运营案例和演示预约链接。', '客户已回复，倾向下周安排线上演示。', 2, '确认演示时间', '2026-06-30 10:00:00', '2026-06-25 09:15:00', 103, 103, 103, '2026-06-25 09:25:00', '2026-06-25 09:25:00', NULL),
(107, 101, 107, '会议', '确认移动端销售助手首年账号数量、培训安排和验收节点。', '客户确认采购并要求本周内生成订单。', 4, '创建订单并同步付款信息', NULL, '2026-06-25 15:30:00', 101, 101, 101, '2026-06-25 15:45:00', '2026-06-25 15:45:00', '已赢单'),
(108, 102, NULL, '微信', '跟进澜海制造集团二期预算审批时间。', '客户表示财务审批预计下周完成。', 3, '下周二再次确认审批结果', '2026-06-30 11:00:00', '2026-06-25 11:00:00', 103, 103, 103, '2026-06-25 11:10:00', '2026-06-25 11:10:00', '客户级跟进记录');

INSERT INTO `crm_order`
(`id`, `order_no`, `customer_id`, `opportunity_id`, `contract_id`, `amount`, `status`, `payment_method`, `payment_time`, `delivery_time`, `finish_time`, `cancel_time`, `logistics_no`, `logistics_company`, `owner_id`, `created_by`, `updated_by`, `created_at`, `updated_at`, `remark`) VALUES
(101, 'YS202606200001', 105, 104, NULL, 96000.00, 2, '银行转账', '2026-06-21 10:05:00', NULL, NULL, NULL, NULL, NULL, 101, 101, 101, '2026-06-20 11:30:00', '2026-06-21 10:05:00', '锦程教育标准版首年服务订单'),
(102, 'YS202606250001', 101, 107, NULL, 145000.00, 1, '银行转账', NULL, NULL, NULL, NULL, NULL, NULL, 101, 101, 101, '2026-06-25 16:20:00', '2026-06-25 16:20:00', '云启科技移动端销售助手订单'),
(103, 'YS202606230001', 102, 102, NULL, 98000.00, 3, '银行转账', '2026-06-23 09:30:00', '2026-06-24 15:40:00', NULL, NULL, 'SF202606240888', '顺丰速运', 103, 103, 103, '2026-06-23 09:00:00', '2026-06-24 15:40:00', '澜海制造试点实施服务订单'),
(104, 'YS202606240001', 103, 103, NULL, 30000.00, 4, '支付宝', '2026-06-24 10:00:00', '2026-06-24 16:00:00', '2026-06-25 11:10:00', NULL, 'JD202606240666', '京东物流', 102, 102, 102, '2026-06-24 09:20:00', '2026-06-25 11:10:00', '花城食品试点培训服务已完成'),
(105, 'YS202606220001', 104, 105, NULL, 50000.00, 5, '微信支付', NULL, NULL, NULL, '2026-06-24 18:00:00', NULL, NULL, 102, 102, 102, '2026-06-22 14:00:00', '2026-06-24 18:00:00', '启航医疗预算冻结后取消订单');

SELECT 'sample acceptance data loaded' AS message,
       (SELECT COUNT(*) FROM sys_user WHERE id BETWEEN 101 AND 199 AND deleted = 0) AS sample_users,
       (SELECT COUNT(*) FROM sys_user_role WHERE user_id BETWEEN 101 AND 199 AND deleted = 0) AS sample_user_roles,
       (SELECT COUNT(*) FROM crm_customer WHERE id BETWEEN 101 AND 199 AND deleted = 0) AS sample_customers,
       (SELECT COUNT(*) FROM crm_opportunity WHERE id BETWEEN 101 AND 199 AND deleted = 0) AS sample_opportunities,
       (SELECT COUNT(*) FROM crm_follow_record WHERE id BETWEEN 101 AND 199 AND deleted = 0) AS sample_follow_records,
       (SELECT COUNT(*) FROM crm_order WHERE id BETWEEN 101 AND 199 AND deleted = 0) AS sample_orders;
