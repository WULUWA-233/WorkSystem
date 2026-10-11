-- =====================================================
-- Test login users
-- Connection:
--   mysql -uroot -p123456 --default-character-set=utf8mb4 < backend/src/main/resources/sql/test-login-users.sql
-- Login password for all users below: 123456
-- =====================================================

SET NAMES utf8mb4;
USE crm_db;

INSERT INTO sys_role (id, role_name, role_code, description, status, sort, deleted, remark)
VALUES
  (1, '系统管理员', 'ADMIN', '拥有系统管理权限', 1, 1, 0, '内置角色'),
  (2, '销售顾问', 'SALES', '负责客户、商机、订单和跟进记录', 1, 2, 0, '内置角色')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  description = VALUES(description),
  status = 1,
  deleted = 0;

INSERT INTO sys_user
  (id, username, password, real_name, nickname, phone, email, gender, status, deleted, remark)
VALUES
  (301, 'ceshiadmin', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '测试管理员', '测试管理员', '13800003001', 'ceshiadmin@example.com', 1, 1, 0, '登录验收账号'),
  (302, 'xiaoshouyi', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '销售一部张敏', '张敏', '13800003002', 'xiaoshouyi@example.com', 2, 1, 0, '登录验收账号'),
  (303, 'xiaoshouer', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '销售二部李强', '李强', '13800003003', 'xiaoshouer@example.com', 1, 1, 0, '登录验收账号'),
  (304, 'jinglisan', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '销售经理王芳', '王芳', '13800003004', 'jinglisan@example.com', 2, 1, 0, '登录验收账号')
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  real_name = VALUES(real_name),
  nickname = VALUES(nickname),
  phone = VALUES(phone),
  email = VALUES(email),
  gender = VALUES(gender),
  status = 1,
  deleted = 0,
  remark = VALUES(remark);

INSERT INTO sys_user_role (user_id, role_id, deleted)
VALUES
  (301, 1, 0),
  (301, 2, 0),
  (302, 2, 0),
  (303, 2, 0),
  (304, 2, 0)
ON DUPLICATE KEY UPDATE deleted = 0;

SELECT u.username, u.real_name, GROUP_CONCAT(r.role_name ORDER BY r.id SEPARATOR '、') AS roles, u.status
FROM sys_user u
JOIN sys_user_role ur ON ur.user_id = u.id AND ur.deleted = 0
JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
WHERE u.id BETWEEN 301 AND 304 AND u.deleted = 0
GROUP BY u.id, u.username, u.real_name, u.status
ORDER BY u.id;
