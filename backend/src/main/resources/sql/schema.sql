-- =====================================================
-- CRM System - Database Schema
-- Database: MySQL 8.0
-- Charset: utf8mb4 / utf8mb4_general_ci
-- Recommended local connection:
--   username: root
--   password: 123456
-- =====================================================

CREATE DATABASE IF NOT EXISTS crm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE crm_db;

-- =====================================================
-- 1. System Tables (sys_)
-- =====================================================

-- System User
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'User ID',
    `username`      VARCHAR(64)     NOT NULL                 COMMENT 'Login username',
    `password`      VARCHAR(255)    NOT NULL                 COMMENT 'Password (BCrypt)',
    `real_name`     VARCHAR(64)     DEFAULT NULL             COMMENT 'Real Name',
    `nickname`      VARCHAR(64)     DEFAULT NULL             COMMENT 'Nickname',
    `phone`         VARCHAR(20)     DEFAULT NULL             COMMENT 'Phone',
    `email`         VARCHAR(128)    DEFAULT NULL             COMMENT 'Email',
    `avatar`        VARCHAR(255)    DEFAULT NULL             COMMENT 'Avatar URL',
    `gender`        TINYINT         DEFAULT 0                COMMENT 'Gender: 0=Unknown, 1=Male, 2=Female',
    `status`        TINYINT         DEFAULT 1                COMMENT 'Status: 0=Disabled, 1=Enabled',
    `last_login_time`   DATETIME    DEFAULT NULL             COMMENT 'Last Login Time',
    `last_login_ip`     VARCHAR(64) DEFAULT NULL             COMMENT 'Last Login IP',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
    `deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT 'Logic Delete: 0=Normal, 1=Deleted',
    `remark`        VARCHAR(500)    DEFAULT NULL             COMMENT 'Remark',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='System User';

-- System Role
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'Role ID',
    `role_name`     VARCHAR(64)     NOT NULL                 COMMENT 'Role Name',
    `role_code`     VARCHAR(64)     NOT NULL                 COMMENT 'Role Code',
    `description`   VARCHAR(255)    DEFAULT NULL             COMMENT 'Description',
    `status`        TINYINT         DEFAULT 1                COMMENT 'Status: 0=Disabled, 1=Enabled',
    `sort`          INT             DEFAULT 0                COMMENT 'Sort Order',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
    `deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT 'Logic Delete',
    `remark`        VARCHAR(500)    DEFAULT NULL             COMMENT 'Remark',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='System Role';

-- User-Role Association
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT 'User ID',
    `role_id`       BIGINT          NOT NULL                 COMMENT 'Role ID',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    `deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT 'Logic Delete',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User-Role Association';

-- System Permission
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'Permission ID',
    `permission_name`   VARCHAR(64)     NOT NULL                 COMMENT 'Permission Name',
    `permission_code`   VARCHAR(128)    NOT NULL                 COMMENT 'Permission Code',
    `permission_type`   VARCHAR(32)     DEFAULT NULL             COMMENT 'Type: menu, button, api',
    `parent_id`         BIGINT          DEFAULT 0                COMMENT 'Parent Permission ID',
    `path`              VARCHAR(255)    DEFAULT NULL             COMMENT 'Route Path',
    `method`            VARCHAR(10)     DEFAULT NULL             COMMENT 'HTTP Method: GET, POST, PUT, DELETE',
    `sort`              INT             DEFAULT 0                COMMENT 'Sort Order',
    `status`            TINYINT         DEFAULT 1                COMMENT 'Status: 0=Disabled, 1=Enabled',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
    `deleted`           TINYINT         NOT NULL DEFAULT 0       COMMENT 'Logic Delete',
    `remark`            VARCHAR(500)    DEFAULT NULL             COMMENT 'Remark',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='System Permission';

-- Role-Permission Association
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `role_id`       BIGINT          NOT NULL                 COMMENT 'Role ID',
    `permission_id` BIGINT          NOT NULL                 COMMENT 'Permission ID',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    `deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT 'Logic Delete',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Role-Permission Association';

-- Operation Log
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'Log ID',
    `user_id`           BIGINT          DEFAULT NULL             COMMENT 'User ID',
    `username`          VARCHAR(64)     DEFAULT NULL             COMMENT 'Username',
    `module_name`       VARCHAR(64)     DEFAULT NULL             COMMENT 'Module Name',
    `operation_type`    VARCHAR(32)     DEFAULT NULL             COMMENT 'Operation Type',
    `operation_desc`    VARCHAR(255)    DEFAULT NULL             COMMENT 'Operation Description',
    `request_method`    VARCHAR(10)     DEFAULT NULL             COMMENT 'Request Method',
    `request_url`       VARCHAR(255)    DEFAULT NULL             COMMENT 'Request URL',
    `request_params`    TEXT            DEFAULT NULL             COMMENT 'Request Parameters',
    `response_result`   TEXT            DEFAULT NULL             COMMENT 'Response Result',
    `ip_address`        VARCHAR(64)     DEFAULT NULL             COMMENT 'IP Address',
    `user_agent`        VARCHAR(500)    DEFAULT NULL             COMMENT 'User Agent',
    `cost_time`         BIGINT          DEFAULT NULL             COMMENT 'Cost Time (ms)',
    `status`            TINYINT         DEFAULT 1                COMMENT 'Status: 0=Failed, 1=Success',
    `error_message`     TEXT            DEFAULT NULL             COMMENT 'Error Message',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_module_name` (`module_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Operation Log';

-- =====================================================
-- 2. Business Tables (crm_)
-- =====================================================

-- Customer
DROP TABLE IF EXISTS `crm_customer`;
CREATE TABLE `crm_customer` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'Customer ID',
    `customer_name`     VARCHAR(128)    NOT NULL                 COMMENT 'Customer Name',
    `industry`          VARCHAR(64)     DEFAULT NULL             COMMENT 'Industry',
    `source`            VARCHAR(64)     DEFAULT NULL             COMMENT 'Source',
    `level`             VARCHAR(32)     DEFAULT NULL             COMMENT 'Customer Level: 1=Normal, 2=Potential, 3=Important, 4=Core, 5=Strategic',
    `region`            VARCHAR(64)     DEFAULT NULL             COMMENT 'Region',
    `address`           VARCHAR(255)    DEFAULT NULL             COMMENT 'Address',
    `contact_name`      VARCHAR(64)     DEFAULT NULL             COMMENT 'Contact Name',
    `contact_phone`     VARCHAR(20)     DEFAULT NULL             COMMENT 'Contact Phone',
    `contact_email`     VARCHAR(128)    DEFAULT NULL             COMMENT 'Contact Email',
    `owner_id`          BIGINT          DEFAULT NULL             COMMENT 'Owner (Salesperson) ID',
    `status`            TINYINT         DEFAULT 1                COMMENT 'Status: 0=Invalid, 1=Active, 2=Public Pool',
    `score`             INT             DEFAULT 0                COMMENT 'Customer Score',
    `last_follow_time`  DATETIME        DEFAULT NULL             COMMENT 'Last Follow Time',
    `next_follow_time`  DATETIME        DEFAULT NULL             COMMENT 'Next Follow Time',
    `created_by`        BIGINT          DEFAULT NULL             COMMENT 'Created By',
    `updated_by`        BIGINT          DEFAULT NULL             COMMENT 'Updated By',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
    `deleted`           TINYINT         NOT NULL DEFAULT 0       COMMENT 'Logic Delete',
    `remark`            VARCHAR(500)    DEFAULT NULL             COMMENT 'Remark',
    PRIMARY KEY (`id`),
    KEY `idx_customer_name` (`customer_name`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Customer';

-- Opportunity
DROP TABLE IF EXISTS `crm_opportunity`;
CREATE TABLE `crm_opportunity` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'Opportunity ID',
    `customer_id`       BIGINT          NOT NULL                 COMMENT 'Customer ID',
    `opportunity_name`  VARCHAR(128)    NOT NULL                 COMMENT 'Opportunity Name',
    `amount`            DECIMAL(12,2)   DEFAULT 0.00             COMMENT 'Expected Amount',
    `stage`             VARCHAR(32)     DEFAULT NULL             COMMENT 'Stage: 1=Initial, 2=Needs Confirmed, 3=Proposal, 4=Negotiation, 5=Won, 6=Lost',
    `probability`       INT             DEFAULT 0                COMMENT 'Win Probability (%)',
    `expected_date`     DATE            DEFAULT NULL             COMMENT 'Expected Close Date',
    `owner_id`          BIGINT          DEFAULT NULL             COMMENT 'Owner ID',
    `source`            VARCHAR(64)     DEFAULT NULL             COMMENT 'Source',
    `lose_reason`       VARCHAR(500)    DEFAULT NULL             COMMENT 'Loss Reason',
    `win_time`          DATETIME        DEFAULT NULL             COMMENT 'Win Time',
    `close_time`        DATETIME        DEFAULT NULL             COMMENT 'Close Time',
    `last_follow_time`  DATETIME        DEFAULT NULL             COMMENT 'Last Follow Time',
    `next_follow_time`  DATETIME        DEFAULT NULL             COMMENT 'Next Follow Time',
    `created_by`        BIGINT          DEFAULT NULL             COMMENT 'Created By',
    `updated_by`        BIGINT          DEFAULT NULL             COMMENT 'Updated By',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
    `deleted`           TINYINT         NOT NULL DEFAULT 0       COMMENT 'Logic Delete',
    `remark`            VARCHAR(500)    DEFAULT NULL             COMMENT 'Remark',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_stage` (`stage`),
    KEY `idx_expected_date` (`expected_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Opportunity';

-- Follow Record
DROP TABLE IF EXISTS `crm_follow_record`;
CREATE TABLE `crm_follow_record` (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'Follow Record ID',
    `customer_id`           BIGINT          NOT NULL                 COMMENT 'Customer ID',
    `opportunity_id`        BIGINT          DEFAULT NULL             COMMENT 'Opportunity ID',
    `follow_type`           VARCHAR(32)     DEFAULT NULL             COMMENT 'Follow Type: 电话, 微信, 邮件, 拜访, 会议, 其他',
    `follow_content`        TEXT            DEFAULT NULL             COMMENT 'Follow Content',
    `follow_result`         VARCHAR(255)    DEFAULT NULL             COMMENT 'Follow Result',
    `customer_intention`    INT             DEFAULT NULL             COMMENT 'Customer Intention: 1=Low, 2=Medium, 3=High, 4=Strong',
    `next_plan`             VARCHAR(500)    DEFAULT NULL             COMMENT 'Next Plan',
    `next_follow_time`      DATETIME        DEFAULT NULL             COMMENT 'Next Follow Time',
    `follow_time`           DATETIME        DEFAULT NULL             COMMENT 'Actual Follow Time',
    `follower_id`           BIGINT          DEFAULT NULL             COMMENT 'Follower ID',
    `created_by`            BIGINT          DEFAULT NULL             COMMENT 'Created By',
    `updated_by`            BIGINT          DEFAULT NULL             COMMENT 'Updated By',
    `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
    `deleted`               TINYINT         NOT NULL DEFAULT 0       COMMENT 'Logic Delete',
    `remark`                VARCHAR(500)    DEFAULT NULL             COMMENT 'Remark',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_opportunity_id` (`opportunity_id`),
    KEY `idx_follower_id` (`follower_id`),
    KEY `idx_follow_time` (`follow_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Follow Record';

-- Order
DROP TABLE IF EXISTS `crm_order`;
CREATE TABLE `crm_order` (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'Order ID',
    `order_no`              VARCHAR(64)     NOT NULL                 COMMENT 'Order Number',
    `customer_id`           BIGINT          NOT NULL                 COMMENT 'Customer ID',
    `opportunity_id`        BIGINT          DEFAULT NULL             COMMENT 'Opportunity ID',
    `contract_id`           BIGINT          DEFAULT NULL             COMMENT 'Contract ID',
    `amount`                DECIMAL(12,2)   DEFAULT 0.00             COMMENT 'Order Amount',
    `status`                TINYINT         DEFAULT 1                COMMENT 'Status: 1=Pending Payment, 2=Paid, 3=Shipped, 4=Completed, 5=Cancelled',
    `payment_method`        VARCHAR(32)     DEFAULT NULL             COMMENT 'Payment Method',
    `payment_time`          DATETIME        DEFAULT NULL             COMMENT 'Payment Time',
    `delivery_time`         DATETIME        DEFAULT NULL             COMMENT 'Delivery Time',
    `finish_time`           DATETIME        DEFAULT NULL             COMMENT 'Finish Time',
    `cancel_time`           DATETIME        DEFAULT NULL             COMMENT 'Cancel Time',
    `logistics_no`          VARCHAR(64)     DEFAULT NULL             COMMENT 'Logistics Tracking Number',
    `logistics_company`     VARCHAR(64)     DEFAULT NULL             COMMENT 'Logistics Company',
    `owner_id`              BIGINT          DEFAULT NULL             COMMENT 'Owner ID',
    `created_by`            BIGINT          DEFAULT NULL             COMMENT 'Created By',
    `updated_by`            BIGINT          DEFAULT NULL             COMMENT 'Updated By',
    `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created At',
    `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated At',
    `deleted`               TINYINT         NOT NULL DEFAULT 0       COMMENT 'Logic Delete',
    `remark`                VARCHAR(500)    DEFAULT NULL             COMMENT 'Remark',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_opportunity_id` (`opportunity_id`),
    KEY `idx_status` (`status`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Order';
