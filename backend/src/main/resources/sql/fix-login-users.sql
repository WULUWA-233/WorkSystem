-- =====================================================
-- Fix CRM login accounts after seed-data username changes
-- MySQL connection:
--   username: root
--   password: 123456
-- =====================================================

SET NAMES utf8mb4;
USE crm_db;

UPDATE `sys_user`
SET `username` = 'admin',
    `real_name` = '系统管理员',
    `nickname` = '管理员',
    `status` = 1,
    `deleted` = 0
WHERE `id` = 1;

UPDATE `sys_user`
SET `username` = 'sales01',
    `real_name` = '张伟',
    `nickname` = '张经理',
    `status` = 1,
    `deleted` = 0
WHERE `id` = 2;

UPDATE `sys_user`
SET `username` = 'sales02',
    `real_name` = '李娜',
    `nickname` = '李顾问',
    `status` = 1,
    `deleted` = 0
WHERE `id` = 3;

SELECT `id`, `username`, `real_name`, `status`, `deleted`
FROM `sys_user`
WHERE `id` IN (1, 2, 3)
ORDER BY `id`;
