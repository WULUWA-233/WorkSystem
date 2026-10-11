-- =====================================================
-- Clean non-Chinese business demo data and add 10 linked samples
-- Connection:
--   mysql -uroot -p123456 --default-character-set=utf8mb4 < backend/src/main/resources/sql/clean-chinese-business-data.sql
-- =====================================================

SET NAMES utf8mb4;
USE crm_db;

-- Ensure Chinese sample owners exist and have valid sales roles.
INSERT INTO sys_role (id, role_name, role_code, description, status, sort, deleted, remark)
VALUES
  (1, '系统管理员', 'ADMIN', '拥有系统管理权限', 1, 1, 0, '内置角色'),
  (2, '销售顾问', 'SALES', '负责客户、商机、订单和跟进记录', 1, 2, 0, '内置角色')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  description = VALUES(description),
  status = VALUES(status),
  deleted = 0,
  remark = VALUES(remark);

INSERT INTO sys_user (id, username, password, real_name, nickname, phone, email, gender, status, deleted, remark)
VALUES
  (101, 'zhangwei', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '张伟', '张伟', '13800001001', 'zhangwei@example.com', 1, 1, 0, '中文样例销售'),
  (102, 'liuna', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '刘娜', '刘娜', '13800001002', 'liuna@example.com', 2, 1, 0, '中文样例销售'),
  (103, 'wangqiang', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '王强', '王强', '13800001003', 'wangqiang@example.com', 1, 1, 0, '中文样例销售')
ON DUPLICATE KEY UPDATE
  real_name = VALUES(real_name),
  nickname = VALUES(nickname),
  phone = VALUES(phone),
  email = VALUES(email),
  status = 1,
  deleted = 0,
  remark = VALUES(remark);

INSERT INTO sys_user_role (user_id, role_id, deleted)
VALUES (101, 2, 0), (102, 2, 0), (103, 2, 0)
ON DUPLICATE KEY UPDATE deleted = 0;

-- Remove active bad data from the business views:
-- 1. orders without valid customers/opportunities;
-- 2. follow records without valid opportunities;
-- 3. non-Chinese or code-like customer/opportunity names.
UPDATE crm_order o
LEFT JOIN crm_customer c ON c.id = o.customer_id AND c.deleted = 0
LEFT JOIN crm_opportunity p ON p.id = o.opportunity_id AND p.deleted = 0
SET o.deleted = 1, o.updated_at = NOW()
WHERE o.deleted = 0
  AND (
    c.id IS NULL
    OR p.id IS NULL
    OR c.customer_name REGEXP '[A-Za-z0-9_]'
    OR p.opportunity_name REGEXP '[A-Za-z0-9_]'
  );

UPDATE crm_follow_record f
LEFT JOIN crm_customer c ON c.id = f.customer_id AND c.deleted = 0
LEFT JOIN crm_opportunity p ON p.id = f.opportunity_id AND p.deleted = 0
SET f.deleted = 1, f.updated_at = NOW()
WHERE f.deleted = 0
  AND (
    f.opportunity_id IS NULL
    OR c.id IS NULL
    OR p.id IS NULL
    OR c.customer_name REGEXP '[A-Za-z0-9_]'
    OR p.opportunity_name REGEXP '[A-Za-z0-9_]'
  );

UPDATE crm_opportunity
SET deleted = 1, updated_at = NOW()
WHERE deleted = 0
  AND opportunity_name REGEXP '[A-Za-z0-9_]';

UPDATE crm_customer
SET deleted = 1, updated_at = NOW()
WHERE deleted = 0
  AND customer_name REGEXP '[A-Za-z0-9_]';

-- Make the sample range re-runnable.
DELETE FROM crm_order WHERE id BETWEEN 201 AND 210 OR order_no LIKE 'CN20260626%';
DELETE FROM crm_follow_record WHERE id BETWEEN 201 AND 210;
DELETE FROM crm_opportunity WHERE id BETWEEN 201 AND 210;
DELETE FROM crm_customer WHERE id BETWEEN 201 AND 210;

INSERT INTO crm_customer
  (id, customer_name, industry, source, level, region, address, contact_name, contact_phone, contact_email, owner_id, status, score, last_follow_time, next_follow_time, created_by, updated_by, deleted, remark)
VALUES
  (201, '北京星云科技有限公司', '软件服务', '官网咨询', '4', '北京', '北京市海淀区中关村南路八号', '赵明', '13910002001', 'zhaoming@example.com', 101, 1, 92, '2026-06-25 09:20:00', '2026-07-02 10:00:00', 101, 101, 0, '关注客户关系管理升级'),
  (202, '上海远航制造集团', '装备制造', '展会获客', '5', '上海', '上海市浦东新区张江路一百号', '陈晨', '13910002002', 'chenchen@example.com', 102, 1, 95, '2026-06-25 10:10:00', '2026-07-03 14:30:00', 102, 102, 0, '集团销售流程统一管理'),
  (203, '广州南粤食品有限公司', '食品饮料', '老客户推荐', '3', '广州', '广州市天河区体育西路六十六号', '黄丽', '13910002003', 'huangli@example.com', 103, 1, 86, '2026-06-25 11:00:00', '2026-07-04 09:30:00', 103, 103, 0, '需要门店客户跟进方案'),
  (204, '深圳康宁医疗科技有限公司', '医疗健康', '电话咨询', '4', '深圳', '深圳市南山区科技园北区十二号', '林涛', '13910002004', 'lintao@example.com', 101, 1, 90, '2026-06-25 13:40:00', '2026-07-05 15:00:00', 101, 101, 0, '售后服务管理诉求明确'),
  (205, '成都锦程教育咨询中心', '教育培训', '线上活动', '3', '成都', '成都市高新区天府大道中段九十九号', '周敏', '13910002005', 'zhoumin@example.com', 102, 1, 84, '2026-06-25 14:15:00', '2026-07-06 11:00:00', 102, 102, 0, '招生线索管理需求稳定'),
  (206, '杭州清源零售有限公司', '连锁零售', '伙伴推荐', '4', '杭州', '杭州市滨江区江南大道十八号', '吴倩', '13910002006', 'wuqian@example.com', 103, 1, 88, '2026-06-25 15:00:00', '2026-07-07 10:30:00', 103, 103, 0, '会员运营管理场景清晰'),
  (207, '南京瑞禾物流有限公司', '物流运输', '官网咨询', '3', '南京', '南京市建邺区江东中路三十号', '郑磊', '13910002007', 'zhenglei@example.com', 101, 1, 82, '2026-06-25 15:45:00', '2026-07-08 09:00:00', 101, 101, 0, '关注运输调度协同'),
  (208, '苏州启新装备有限公司', '工业设备', '展会获客', '4', '苏州', '苏州市工业园区星湖街二十八号', '孙悦', '13910002008', 'sunyue@example.com', 102, 1, 89, '2026-06-25 16:20:00', '2026-07-09 14:00:00', 102, 102, 0, '渠道客户管理需求较强'),
  (209, '武汉智谷咨询有限公司', '企业咨询', '老客户推荐', '3', '武汉', '武汉市武昌区中北路一百二十号', '何帆', '13910002009', 'hefan@example.com', 103, 1, 83, '2026-06-25 17:00:00', '2026-07-10 10:00:00', 103, 103, 0, '需要项目交付看板'),
  (210, '西安华辰能源有限公司', '新能源', '电话咨询', '5', '西安', '西安市高新区锦业路五十号', '马宁', '13910002010', 'maning@example.com', 101, 1, 94, '2026-06-25 17:35:00', '2026-07-11 15:30:00', 101, 101, 0, '大客户服务管理项目');

INSERT INTO crm_opportunity
  (id, customer_id, opportunity_name, amount, stage, probability, expected_date, owner_id, source, win_time, close_time, last_follow_time, next_follow_time, created_by, updated_by, deleted, remark)
VALUES
  (201, 201, '星云科技客户关系管理升级项目', 186000.00, '5', 100, '2026-07-15', 101, '官网咨询', '2026-06-25 09:30:00', '2026-06-25 09:30:00', '2026-06-25 09:20:00', '2026-07-02 10:00:00', 101, 101, 0, '已确认实施范围'),
  (202, 202, '远航制造销售流程数字化项目', 328000.00, '5', 100, '2026-07-20', 102, '展会获客', '2026-06-25 10:20:00', '2026-06-25 10:20:00', '2026-06-25 10:10:00', '2026-07-03 14:30:00', 102, 102, 0, '集团统一采购'),
  (203, 203, '南粤食品客户跟进试点项目', 98000.00, '5', 100, '2026-07-12', 103, '老客户推荐', '2026-06-25 11:10:00', '2026-06-25 11:10:00', '2026-06-25 11:00:00', '2026-07-04 09:30:00', 103, 103, 0, '先做重点门店试点'),
  (204, 204, '康宁医疗售后服务管理项目', 216000.00, '5', 100, '2026-07-18', 101, '电话咨询', '2026-06-25 13:50:00', '2026-06-25 13:50:00', '2026-06-25 13:40:00', '2026-07-05 15:00:00', 101, 101, 0, '售后流程先上线'),
  (205, 205, '锦程教育招生线索管理项目', 76000.00, '5', 100, '2026-07-10', 102, '线上活动', '2026-06-25 14:25:00', '2026-06-25 14:25:00', '2026-06-25 14:15:00', '2026-07-06 11:00:00', 102, 102, 0, '暑期招生前交付'),
  (206, 206, '清源零售会员运营管理项目', 145000.00, '5', 100, '2026-07-22', 103, '伙伴推荐', '2026-06-25 15:10:00', '2026-06-25 15:10:00', '2026-06-25 15:00:00', '2026-07-07 10:30:00', 103, 103, 0, '先接入核心门店'),
  (207, 207, '瑞禾物流运输调度协同项目', 132000.00, '5', 100, '2026-07-24', 101, '官网咨询', '2026-06-25 15:55:00', '2026-06-25 15:55:00', '2026-06-25 15:45:00', '2026-07-08 09:00:00', 101, 101, 0, '运输调度协同为重点'),
  (208, 208, '启新装备渠道客户管理项目', 168000.00, '5', 100, '2026-07-26', 102, '展会获客', '2026-06-25 16:30:00', '2026-06-25 16:30:00', '2026-06-25 16:20:00', '2026-07-09 14:00:00', 102, 102, 0, '渠道客户分级管理'),
  (209, 209, '智谷咨询项目交付看板项目', 89000.00, '5', 100, '2026-07-14', 103, '老客户推荐', '2026-06-25 17:10:00', '2026-06-25 17:10:00', '2026-06-25 17:00:00', '2026-07-10 10:00:00', 103, 103, 0, '项目进度可视化'),
  (210, 210, '华辰能源大客户服务项目', 296000.00, '5', 100, '2026-07-28', 101, '电话咨询', '2026-06-25 17:45:00', '2026-06-25 17:45:00', '2026-06-25 17:35:00', '2026-07-11 15:30:00', 101, 101, 0, '大客户服务闭环');

INSERT INTO crm_follow_record
  (id, customer_id, opportunity_id, follow_type, follow_content, follow_result, customer_intention, next_plan, next_follow_time, follow_time, follower_id, created_by, updated_by, deleted, remark)
VALUES
  (201, 201, 201, '电话', '确认客户关系管理升级范围和上线时间', '客户认可方案，进入合同流程', 4, '安排实施启动会', '2026-07-02 10:00:00', '2026-06-25 09:20:00', 101, 101, 101, 0, '商机已赢单'),
  (202, 202, 202, '会议', '沟通集团销售流程统一管理方案', '采购负责人确认预算', 4, '提交实施排期', '2026-07-03 14:30:00', '2026-06-25 10:10:00', 102, 102, 102, 0, '需同步集团信息部'),
  (203, 203, 203, '微信', '跟进门店客户管理试点需求', '客户同意先从重点门店开始', 3, '发送试点清单', '2026-07-04 09:30:00', '2026-06-25 11:00:00', 103, 103, 103, 0, '试点范围已确认'),
  (204, 204, 204, '拜访', '现场梳理售后服务管理流程', '售后主管确认流程节点', 4, '准备上线计划', '2026-07-05 15:00:00', '2026-06-25 13:40:00', 101, 101, 101, 0, '重点关注服务响应'),
  (205, 205, 205, '电话', '确认招生线索来源和分配规则', '客户希望尽快上线', 3, '提供账号清单', '2026-07-06 11:00:00', '2026-06-25 14:15:00', 102, 102, 102, 0, '暑期招生压力较大'),
  (206, 206, 206, '会议', '讨论会员运营和门店跟进闭环', '客户确认首批门店名单', 4, '安排数据导入', '2026-07-07 10:30:00', '2026-06-25 15:00:00', 103, 103, 103, 0, '先接入核心门店'),
  (207, 207, 207, '电话', '确认运输调度协同的关键字段', '客户接受标准版本方案', 3, '收集调度人员名单', '2026-07-08 09:00:00', '2026-06-25 15:45:00', 101, 101, 101, 0, '后续考虑移动端'),
  (208, 208, 208, '拜访', '沟通渠道客户分级和跟进规则', '客户确认按区域分批上线', 4, '整理渠道客户模板', '2026-07-09 14:00:00', '2026-06-25 16:20:00', 102, 102, 102, 0, '渠道数据量较大'),
  (209, 209, 209, '微信', '确认项目交付看板字段和权限', '客户认可看板样式', 3, '输出权限配置表', '2026-07-10 10:00:00', '2026-06-25 17:00:00', 103, 103, 103, 0, '需兼顾管理层视图'),
  (210, 210, 210, '会议', '梳理大客户服务流程和跟进节点', '客户确认服务闭环方案', 4, '安排合同盖章', '2026-07-11 15:30:00', '2026-06-25 17:35:00', 101, 101, 101, 0, '重点客户优先交付');

INSERT INTO crm_order
  (id, order_no, customer_id, opportunity_id, amount, status, payment_method, payment_time, delivery_time, finish_time, logistics_no, logistics_company, owner_id, created_by, updated_by, deleted, remark)
VALUES
  (201, 'CN202606260001', 201, 201, 186000.00, 2, '银行转账', '2026-06-26 09:30:00', NULL, NULL, NULL, NULL, 101, 101, 101, 0, '星云科技首付款已确认'),
  (202, 'CN202606260002', 202, 202, 328000.00, 1, '银行转账', NULL, NULL, NULL, NULL, NULL, 102, 102, 102, 0, '远航制造待付款'),
  (203, 'CN202606260003', 203, 203, 98000.00, 4, '支付宝', '2026-06-26 10:20:00', '2026-06-26 11:00:00', '2026-06-26 16:30:00', 'YT2606260003', '圆通速递', 103, 103, 103, 0, '南粤食品试点订单完成'),
  (204, 'CN202606260004', 204, 204, 216000.00, 2, '银行转账', '2026-06-26 11:10:00', NULL, NULL, NULL, NULL, 101, 101, 101, 0, '康宁医疗已付款'),
  (205, 'CN202606260005', 205, 205, 76000.00, 3, '微信支付', '2026-06-26 11:40:00', '2026-06-26 15:00:00', NULL, 'SF2606260005', '顺丰速运', 102, 102, 102, 0, '锦程教育交付中'),
  (206, 'CN202606260006', 206, 206, 145000.00, 2, '银行转账', '2026-06-26 13:20:00', NULL, NULL, NULL, NULL, 103, 103, 103, 0, '清源零售首批门店款项已到账'),
  (207, 'CN202606260007', 207, 207, 132000.00, 1, '银行转账', NULL, NULL, NULL, NULL, NULL, 101, 101, 101, 0, '瑞禾物流待财务付款'),
  (208, 'CN202606260008', 208, 208, 168000.00, 2, '银行转账', '2026-06-26 14:15:00', NULL, NULL, NULL, NULL, 102, 102, 102, 0, '启新装备已付款'),
  (209, 'CN202606260009', 209, 209, 89000.00, 4, '支付宝', '2026-06-26 14:40:00', '2026-06-26 15:20:00', '2026-06-26 17:10:00', 'JD2606260009', '京东物流', 103, 103, 103, 0, '智谷咨询订单完成'),
  (210, 'CN202606260010', 210, 210, 296000.00, 2, '银行转账', '2026-06-26 15:30:00', NULL, NULL, NULL, NULL, 101, 101, 101, 0, '华辰能源服务项目已付款');

-- Verification summary.
SELECT COUNT(*) AS bad_active_opportunities
FROM crm_opportunity
WHERE deleted = 0 AND opportunity_name REGEXP '[A-Za-z0-9_]';

SELECT COUNT(*) AS follow_records_without_valid_opportunity
FROM crm_follow_record f
LEFT JOIN crm_opportunity p ON p.id = f.opportunity_id AND p.deleted = 0
WHERE f.deleted = 0 AND (f.opportunity_id IS NULL OR p.id IS NULL);

SELECT COUNT(*) AS orders_without_valid_customer
FROM crm_order o
LEFT JOIN crm_customer c ON c.id = o.customer_id AND c.deleted = 0
WHERE o.deleted = 0 AND c.id IS NULL;

SELECT COUNT(*) AS added_chinese_customers
FROM crm_customer
WHERE deleted = 0 AND id BETWEEN 201 AND 210;

SELECT COUNT(*) AS added_chinese_opportunities
FROM crm_opportunity
WHERE deleted = 0 AND id BETWEEN 201 AND 210;

SELECT COUNT(*) AS added_linked_follow_records
FROM crm_follow_record
WHERE deleted = 0 AND id BETWEEN 201 AND 210 AND opportunity_id IS NOT NULL;

SELECT COUNT(*) AS added_linked_orders
FROM crm_order
WHERE deleted = 0 AND id BETWEEN 201 AND 210 AND customer_id BETWEEN 201 AND 210 AND opportunity_id BETWEEN 201 AND 210;
