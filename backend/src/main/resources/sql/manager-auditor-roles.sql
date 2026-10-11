-- =====================================================
-- Manager and auditor roles
-- Connection:
--   mysql -uroot -p123456 --default-character-set=utf8mb4 < backend/src/main/resources/sql/manager-auditor-roles.sql
-- Password for sample users below: 123456
-- =====================================================

SET NAMES utf8mb4;
USE crm_db;

INSERT INTO sys_role (id, role_name, role_code, description, status, sort, deleted, remark)
VALUES
  (3, '销售经理', 'MANAGER', '负责销售团队过程管理和团队业务数据查看；不进入个人销售业绩榜。', 1, 3, 0, '权限：客户、商机、跟进、订单、统计分析。数据范围：团队数据。业绩口径：不参与个人业绩，团队业绩单独统计。'),
  (4, '只读审计员', 'AUDITOR', '负责业务数据和操作日志只读查看；不允许新增、编辑、删除。', 1, 4, 0, '权限：客户、商机、跟进、订单、统计分析、操作日志只读。数据范围：审计范围内数据。业绩口径：不参与业绩。')
ON DUPLICATE KEY UPDATE
  role_code = VALUES(role_code),
  role_name = VALUES(role_name),
  description = VALUES(description),
  status = 1,
  sort = VALUES(sort),
  deleted = 0,
  remark = VALUES(remark);

INSERT INTO sys_user
  (id, username, password, real_name, nickname, phone, email, gender, status, deleted, remark)
VALUES
  (305, 'xiaoshoujingli', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '销售经理赵磊', '赵磊', '13800003005', 'xiaoshoujingli@example.com', 1, 1, 0, '销售经理验收账号'),
  (306, 'shenjiyuan', '$2a$10$3qDG.kvXnzuScjgns921Duo6VZNjsYqgtfTcwPov27vO75k0Jhr2a', '审计员孙洁', '孙洁', '13800003006', 'shenjiyuan@example.com', 2, 1, 0, '只读审计验收账号')
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
  (305, 3, 0),
  (306, 4, 0)
ON DUPLICATE KEY UPDATE deleted = 0;

SELECT u.username, u.real_name, GROUP_CONCAT(r.role_name ORDER BY r.id SEPARATOR '、') AS roles
FROM sys_user u
JOIN sys_user_role ur ON ur.user_id = u.id AND ur.deleted = 0
JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
WHERE u.id IN (305, 306)
GROUP BY u.id, u.username, u.real_name
ORDER BY u.id;
